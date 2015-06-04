package cs211.imageprocessing;

import processing.core.*;

public class Main extends PApplet {
	PImage img;

	HScrollbar thresholdBar1;
	HScrollbar thresholdBar2;

	public void setup() {
		size(800, 600, P3D);
		img = loadImage("board4.jpg");
		thresholdBar1 = new HScrollbar(this, 0, 580, 800, 20);
		thresholdBar2 = new HScrollbar(this, 0, 550, 800, 20);
		thresholdBar1.setPos(0.27f);
		thresholdBar2.setPos(0.53f);
		thresholdBar1.update();
		thresholdBar2.update();
	}

	PImage convolute(PImage image, float[][] kernel, float weight) {
		PImage result = createImage(image.width, image.height, ALPHA);
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

	PImage sobel(PImage img) {

		float[][] hsobel = { { 0, 1, 0 }, { 0, 0, 0 }, { 0, -1, 0 } };
		float[][] vsobel = { { 0, 0, 0 }, { 1, 0, -1 }, { 0, 0, 0 } };

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
						if (nx < 0 || nx >= img.width || ny < 0
								|| ny >= img.height)
							continue;
						
						hsum += brightness(img.pixels[ny * img.width + nx]) / 255
								* hsobel[dy + 1][dx + 1];

						vsum += brightness(img.pixels[ny * img.width + nx]) / 255
								* vsobel[dy + 1][dx + 1];
					}

				float t = 255 * (float) Math.sqrt(Math.pow(hsum, 2)
						+ Math.pow(vsum, 2));

				upper = Math.max(upper, t);
				buffer[y * img.width + x] = t;
			}

		for (int i = 0; i < img.width * img.height; ++i)
			result.pixels[i] = (buffer[i] > (int) (upper * 0.3f)) ? color(255)
					: color(0);

		return result;
	}
	
	PImage thresholdFilter(PImage img, float h1, float h2, float s1, float s2, float b1, float b2) {
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

	public void draw() {
		/*
		 * for (int i = 0; i < img.width * img.height; ++i) { result.pixels[i] =
		 * brightness(img.pixels[i]) < (thresholdBar.getPos()*255) ? color(0) :
		 * color(255); }
		 */

		float[][] identity = { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0 } };
		float[][] kernel1 = { { 0, 0, 0 }, { 0, 2, 0 }, { 0, 0, 0 } };
		float[][] kernel2 = { { 0, 1, 0 }, { 1, 0, 1 }, { 0, 1, 0 } };

		//float gaussianWeight = 99f;
		float[][] gaussian = { { 9, 12, 9 }, { 12, 15, 12 }, { 9, 12, 9 } };

		PImage filtered = thresholdFilter(img,
				thresholdBar1.getPos() * 255, thresholdBar2.getPos() * 255,
				100, 255,
				0, 255
				);
		PImage blurred = convolute(convolute(filtered, gaussian, 99), gaussian, 99);
		image(sobel(blurred), 0, 0);

		// image(convolute(img, identity, identityWeight), 0, 0);
		// image(convolute(img, gaussian, gaussianWeight), 0, 0);
		// image(convolute(img, sobel, sobelWeight), 0, 0);

		thresholdBar1.display();
		thresholdBar1.update();
		thresholdBar2.display();
		thresholdBar2.update();
		
		System.out.println(thresholdBar1.getPos());
		System.out.println(thresholdBar2.getPos());
	}

	public static void main(String[] args) {
		PApplet.main(Main.class.getName());
	}
}
