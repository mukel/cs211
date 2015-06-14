package cs211.imageprocessing;

import java.util.Arrays;

import processing.core.PApplet;
import processing.core.PImage;

public class RawImage implements Cloneable {
	public final int width;	
	public final int height;
	public final int[] pixels;	
	public RawImage(int width, int height) {
		this.width = width;
		this.height = height;
		pixels = new int[width * height];
	}	
	
	public RawImage(int width, int height, int[] pixels) {
		this.width = width;
		this.height = height;
		this.pixels = pixels;
	}
	
	public RawImage(PImage src) {
		this.width = src.width;
		this.height = src.height;		
		pixels = src.pixels.clone();
	}
	
	public Object clone() {
		return new RawImage(width, height, pixels.clone());
	}
	
	public PImage toPImage(PApplet parent) {		
		PImage result = parent.createImage(width,  height, PApplet.RGB);
		for (int i = 0; i < width * height; ++i)
			result.pixels[i] = pixels[i];
		result.updatePixels();
		return result;
	}	
}
