/*
Aimee Montero 221053
Alfonso2 Peterssen 228219
Pierre Mbanga 229047

This is code is tuned to work with a specific set of images,
it should work on other inputs with some further tweaking.

This is a preliminar/condensed (just working) version, final submission will be properly formatted and commented.
 */
package cs211.imageprocessing;
import processing.core.*;
import java.util.*;

public class ImageProcessing extends PApplet {

	// generated in hough() function
	static PImage houghImg;

	public void setup() {
		PImage img = loadImage("./images/board4.jpg");
		img.resize(400, 300);

		size(1200, 300);
		noLoop();

		background(0);

		PImage filtered = greenFilter(img,
			0.34f * 255, 0.54f * 255,
			100, 255,
			0, 255
		);

		// Double gaussian blur to get rid of the isolated dots (noise)
		// this also reduces precision, but improves line detection accuracy
		PImage blurred = gaussianBlur(gaussianBlur(filtered));
		PImage noiseless = intensityFilter(blurred, 200, 255);
		PImage source = sobel(noiseless);

		List<PVector> lines = hough(source, 6);
		QuadGraph qg = new QuadGraph();
		qg.build(lines, width, height);
		List<int[]> quads = qg.findCycles();

		// Filter malformed quads
		List<int[]> bestQuads = new ArrayList<>();
		for (int[] quad : quads) {
			PVector line1 = lines.get(quad[0]);
			PVector line2 = lines.get(quad[1]);
			PVector line3 = lines.get(quad[2]);
			PVector line4 = lines.get(quad[3]);

			PVector c1 = intersection(line1, line2);
			PVector c2 = intersection(line2, line3);
			PVector c3 = intersection(line3, line4);
			PVector c4 = intersection(line4, line1);

			if (QuadGraph.isConvex(c1, c2, c3, c4) &&
					QuadGraph.nonFlatQuad(c1, c2, c3, c4) &&
					QuadGraph.validArea(c1, c2, c3, c4, width * height, width * height / 100)) {
				bestQuads.add(quad);
			}
		}

		// Original image
		image(img, 0, 0);

		if (bestQuads.size() > 0) {
			ArrayList<PVector> bestLines = new ArrayList<>();
			for (int index : bestQuads.get(0))
				bestLines.add(lines.get(index));
			drawLines(bestLines);
			drawIntersections(bestLines);
			drawQuads(Collections.singletonList(bestQuads.get(0)), lines);
		}

		// Hough accumulator
		houghImg.resize(400, 300);
		if (houghImg != null)
			image(houghImg, 400, 0);

		// Preprocessed image
		image(source, 800, 0);
	}

	PImage gaussianBlur(PImage image) {
		final float[][] kernel = {{9, 12, 9}, {12, 15, 12}, {9, 12, 9}};
		final float weight = 99;
		PImage result = createImage(image.width, image.height, ALPHA);
		for (int y = 1; y + 1 < image.height; ++y)
			for (int x = 1; x + 1 < image.width; ++x) {
				float sum = 0;
				for (int dx = -1; dx <= 1; ++dx)
					for (int dy = -1; dy <= 1; ++dy) {
						int nx = x + dx;
						int ny = y + dy;
						if (nx < 0 || nx >= image.width || ny < 0 || ny >= image.height)
							continue;
						sum += brightness(image.pixels[ny * image.width + nx]) * kernel[dy + 1][dx + 1];
					}
				result.pixels[y * image.width + x] = color(sum / weight);
			}
		return result;
	}

	PImage sobel(PImage img) {
		final float[][] hSobel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		final float[][] vSobel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };
		float[] buffer = new float[img.width * img.height];
		PImage result = createImage(img.width, img.height, ALPHA);
		float upper = 0;
		for (int y = 1; y + 1 < img.height; ++y)
			for (int x = 1; x + 1 < img.width; ++x) {
				float hsum = 0;
				float vsum = 0;
				for (int dx = -1; dx <= 1; ++dx)
					for (int dy = -1; dy <= 1; ++dy) {
						int nx = x + dx;
						int ny = y + dy;
						if (nx < 0 || nx >= img.width || ny < 0 || ny >= img.height)
							continue;
						hsum += brightness(img.pixels[ny * img.width + nx])
								* hSobel[dy + 1][dx + 1];
						vsum += brightness(img.pixels[ny * img.width + nx])
								* vSobel[dy + 1][dx + 1];
					}
				float t = (float)Math.sqrt(hsum*hsum + vsum*vsum);

				upper = Math.max(upper, t);
				buffer[y * img.width + x] = t;
			}
		for (int i = 0; i < img.width * img.height; ++i)
			result.pixels[i] = (buffer[i] > (int) (upper * 0.3f)) ? color(255) : color(0);
		return result;
	}
	
	PImage greenFilter(PImage img, float h1, float h2, float s1, float s2, float b1, float b2) {
		PImage result = createImage(img.width, img.height, ALPHA);
		for (int i = 0; i < img.width * img.height; ++i) {
			int c = img.pixels[i];
			float h = hue(c);
			float s = saturation(c);
			float b = brightness(c);
			result.pixels[i] = (h1 <= h && h <= h2 && s1 <= s && s <= s2 && b1 <= b && b <= b2) ? color(255) : color(0);
		}
		return result;
	}

	PImage intensityFilter(PImage img, float b1, float b2) {
		PImage result = createImage(img.width, img.height, ALPHA);
		for (int i = 0; i < img.width * img.height; ++i) {
			int c = img.pixels[i];
			float b = brightness(c);
			result.pixels[i] = (b1 <= b && b <= b2) ? color(255) : color(0);
		}
		return result;
	}

	float discretizationStepsPhi = 0.02f;
	float discretizationStepsR = 2.0f;

	int[] getAccumulator(PImage edgeImg, int phiDim, int rDim) {
		// our accumulator (with a 1 pix margin around)
		int[] accumulator = new	int[(phiDim + 2 )*(rDim + 2)];

		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for(int	y =	0; y <	edgeImg.height; y++) {
			for(int	x =	0; x < edgeImg.width; x++) {
				// Are we on an edge?
				if(brightness(edgeImg.pixels[y*edgeImg.width + x]) != 0 ) {
					for(int i = 0; i < phiDim; i++) {
						float phi = map(i,  0,  phiDim, 0, PI);
						double rd = (( x*cos(phi) + y*sin(phi) ) / discretizationStepsR);
						int r = (int)Math.round(rd + (rDim - 1)/2);
						accumulator[(i+1)*(rDim + 2) + r + 1]++;
					}
				}
			}
		}

		houghImg = createImage( rDim	+ 2, phiDim + 2, ALPHA );
		for(int i = 0; i < accumulator.length; i++)
			houghImg.pixels[i] = color(min(255, accumulator[i]));
		houghImg.updatePixels();

		return accumulator;
	}

	public ArrayList<PVector> hough(PImage edgeImg, int nLines) {
		// dimensions of the accumulator
		int	phiDim	= (int)(Math.PI/discretizationStepsPhi);
		int	rDim =	(int)(((edgeImg.width +	edgeImg.height)*2 + 1)/	discretizationStepsR);
		int[] accumulator = getAccumulator(edgeImg, phiDim, rDim);

		ArrayList<Integer> bestCandidates = new ArrayList<>();

		int neighbourhood = 5;
		int minVotes = 120;

		for(int accR = 0; accR < rDim; accR++) {
			for(int accPhi = 0; accPhi < phiDim; accPhi++) {
				int idx = (accPhi + 1)*(rDim + 2) + accR + 1;
				if(accumulator[idx] > minVotes) {
					boolean bestCand = true;
					for(int dPhi = -neighbourhood/2; dPhi < neighbourhood/2+1; dPhi++) {
						if(accPhi + dPhi < 0 || accPhi + dPhi >= phiDim)
							continue;
						for(int dR = -neighbourhood/2; dR < neighbourhood/2 + 1; dR++) {
							if(accR + dR < 0 || accR+dR >= rDim)
								continue;
							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2) + accR + dR + 1;
							if(accumulator[idx] < accumulator[neighbourIdx]) {
								bestCand = false;
								break;
							}
						}
						if(!bestCand)
							break;
					}
					if(bestCand)
						bestCandidates.add(idx);
				}
			}
		}

		HoughComparator cmp = new HoughComparator(accumulator);

		Collections.sort(bestCandidates, cmp);

		ArrayList<PVector> lines = new ArrayList<PVector>();
		for	( int i = 0; i < Math.min(nLines, bestCandidates.size()); i++)
		{
			int idx = bestCandidates.get(i);
			// first, compute back the (r, phi) polar coordinates:
			int accPhi	=	(int)(idx/(rDim + 2))-1;
			int	accR	=	idx	- (	accPhi + 1 )*(rDim + 2  ) - 1;
			float r = (	accR - (rDim -	1)*0.5f)*discretizationStepsR;
			float phi = accPhi*	discretizationStepsPhi;

			lines.add(new PVector(r, phi));


		}
		return lines;

	}

	void drawLines(List<PVector> lines) {

		for (PVector p : lines) {
			float r = p.x;
			float phi = p.y;

			// Cartesian equation of a line: y = ax + b
			// in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
			// => y = 0 : x = r / cos(phi)
			// => x = 0 : y = r / sin(phi)
			// compute the intersection of this line with the 4 borders of
			// the image
			int x0 = 0;
			int y0 = (int) (r / sin(phi));
			int x1 = (int) (r / cos(phi));
			int y1 = 0;
			int x2 = width;
			int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
			int y3 = width;
			int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));
			// Finally, plot the lines
			stroke(204, 102, 0);
			if (y0 > 0) {
				if (x1 > 0) {
					line(x0, y0, x1, y1);
				} else if (y2 > 0) line(x0, y0, x2, y2);
				else line(x0, y0, x3, y3);
			} else {
				if (x1 > 0) {
					if (y2 > 0) line(x1, y1, x2, y2);
					else line(x1, y1, x3, y3);
				} else
					line(x2, y2, x3, y3);
			}
		}
	}

	public static PVector intersection(PVector line1, PVector line2) {
		float d = cos(line2.y)*sin(line1.y) - cos(line1.y)*sin(line2.y);
		if (abs(d) < 1e-4)
			return new PVector(-1, -1);
		float x = (line2.x * sin(line1.y) - line1.x * sin(line2.y)) / d;
		float y = (-line2.x * cos(line1.y) + line1.x * cos(line2.y)) / d;
		return new PVector(x, y);
	}

	public void drawIntersections(List<PVector> lines) {
		for(int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);
			for(int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);
				PVector p = intersection(line1, line2);
				fill(255, 128, 0);
				ellipse(p.x, p.y, 10, 10);

			}
		}
	}

	void drawQuads(List<int[]> quads, List<PVector> lines) {
		for (int[] quad : quads) {
			PVector l1 = lines.get(quad[0]);
			PVector l2 = lines.get(quad[1]);
			PVector l3 = lines.get(quad[2]);
			PVector l4 = lines.get(quad[3]);
			// (intersection() is a simplified version of the
			// intersections() method you wrote last week, that simply
			// return the coordinates of the intersection between 2 lines)
			PVector c12 = intersection(l1, l2);
			PVector c23 = intersection(l2, l3);
			PVector c34 = intersection(l3, l4);
			PVector c41 = intersection(l4, l1);
			// Choose a random, semi-transparent colour
			Random random = new Random();
			fill(color(min(255, random.nextInt(300)),
					min(255, random.nextInt(300)),
					min(255, random.nextInt(300)), 50));
			quad(c12.x,c12.y,c23.x,c23.y,c34.x,c34.y,c41.x,c41.y);
		}
	}

	public static void main(String[] args) {
		PApplet.main(ImageProcessing.class.getName());
	}
}
