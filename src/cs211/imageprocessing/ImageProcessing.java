/*
Aimee Montero 221053
Alfonso2 Peterssen 228219
Pierre Mbanga 229047
 */
package cs211.imageprocessing;
import processing.core.PApplet;
import processing.core.PVector;
import processing.video.Capture;
import static processing.core.PApplet.*;
import processing.core.PImage;
import processing.video.Movie;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ImageProcessing {
	
	public static float[] toHsb(int rgb) {
		float[] hsb = Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff,
				rgb & 0xff, null);
		return hsb;
	}
	
	public static float hue(int rgb) {
		return toHsb(rgb)[0] * 255;
	}	
	public static float saturation(int rgb) {
		return toHsb(rgb)[1] * 255;
	}
	public static float brightness(int rgb) {
		return toHsb(rgb)[2] * 255;
	}
	
	public static int color(float gray) {
		int t = (int)(gray * 255);
		return (t << 16) | (t << 8) << t;
	}
	
	public static int WHITE = (1 << 24) - 1;
	public static int BLACK = 0;

	public static RawImage convoluteOf(RawImage image, float[][] kernel, float weight) {
		RawImage result = (RawImage)image.clone();
		for (int y = 0; y < image.height; ++y)
			for (int x = 0; x < image.width; ++x) {
				float sum = 0;
				for (int dx = -1; dx <= 1; ++dx)
					for (int dy = -1; dy <= 1; ++dy) {
						int nx = x + dx;
						int ny = y + dy;
						if (nx < 0 || nx >= image.width || ny < 0
								|| ny >= image.height)
							continue;
						sum += brightness(image.pixels[ny * image.width + nx])
								* kernel[dy + 1][dx + 1];
					}
				result.pixels[y * image.width + x] = color(sum / weight);
			}
		return result;
	}


	public static RawImage gaussianBlur(RawImage image) {
		final float[][] kernel = {{9, 12, 9}, {12, 15, 12}, {9, 12, 9}};
		final float weight = 99;
		RawImage result = new RawImage(image.width, image.height);
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

	public static RawImage sobel(RawImage img) {
		final float[][] hSobel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		final float[][] vSobel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };
		float[] buffer = new float[img.width * img.height];
		RawImage result = new RawImage(img.width, img.height);
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
				float t = sqrt(hsum*hsum + vsum*vsum);
				buffer[y * img.width + x] = t;
				upper = max(upper, t);
			}
		for (int i = 0; i < img.width * img.height; ++i)
			result.pixels[i] = (buffer[i] > (int) (upper * 0.3f)) ? WHITE : BLACK;
		return result;
	}

	static float discretizationStepsPhi = 0.01f;
	static float discretizationStepsR = 1.5f;

	public static int[] getAccumulator(RawImage edgeImg, int phiDim, int rDim) {
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
		return accumulator;
	}

	public static List<PVector> hough(RawImage edgeImg, int maxLines) {
		// dimensions of the accumulator
		int	phiDim	= (int)(Math.PI/discretizationStepsPhi);
		int	rDim =	(int)(((edgeImg.width +	edgeImg.height)*2 + 1)/	discretizationStepsR);
		int[] accumulator = getAccumulator(edgeImg, phiDim, rDim);

		List<Integer> bestCandidates = new ArrayList<>();

		int neighbourhood = 10;
		int minVotes = 20;

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

		List<PVector> lines = new ArrayList<PVector>();
		for	( int i = 0; i < Math.min(maxLines, bestCandidates.size()); i++) {
			int idx = bestCandidates.get(i);
			// first, compute back the (r, phi) polar coordinates:
			int accPhi	=	idx/(rDim + 2) -1;
			int	accR	=	idx	- (	accPhi + 1 )*(rDim + 2  ) - 1;
			float r = (	accR - (rDim -	1)*0.5f)*discretizationStepsR;
			float phi = accPhi*	discretizationStepsPhi;
			lines.add(new PVector(r, phi));
		}

		return lines;
	}

	public static RawImage colorFilterInPlace(RawImage img, float h1, float h2, float s1, float s2, float b1, float b2) {
		for (int i = 0; i < img.width * img.height; ++i) {
			int rgb = img.pixels[i];
			float[] hsb = toHsb(rgb);
			float h = hsb[0] * 255;
			float s = hsb[1] * 255;
			float b = hsb[2] * 255;
			img.pixels[i] = (h1 <= h && h <= h2 && s1 <= s && s <= s2 && b1 <= b && b <= b2) ? rgb : BLACK;
		}
		return img;
	}
	
	public static RawImage thresholdFilterInPlace(RawImage img, float h1, float h2, float s1, float s2, float b1, float b2) {		
		for (int i = 0; i < img.width * img.height; ++i) {
			int rgb = img.pixels[i];
			float[] hsb = toHsb(rgb);
			float h = hsb[0] * 255;
			float s = hsb[1] * 255;
			float b = hsb[2] * 255;
			img.pixels[i] = (h1 <= h && h <= h2 && s1 <= s && s <= s2 && b1 <= b && b <= b2) ? WHITE : BLACK;
		}
		return img;
	}
}
