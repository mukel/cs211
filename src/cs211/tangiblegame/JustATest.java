package cs211.tangiblegame;

import cs211.HScrollbar;
import cs211.imageprocessing.BoardDetector;
import cs211.imageprocessing.ImageProcessing;
import processing.core.*;
import processing.video.Movie;

import java.util.Collections;
import java.util.List;

public class JustATest extends PApplet {
	
	Movie video;

	public void setup() {
		size(800, 600);
		video = new Movie(this, "C:\\Users\\mukel\\Desktop\\cs211\\resources\\videos\\testvideo.mp4");
		video.loop();
	}


	public void draw() {
		
		PImage img = null;
		if (video.available()) {
			video.read();
			img = (PImage)video.get();
		}

//		PImage img = loadImage("C:\\Users\\mukel\\Desktop\\cs211\\resources\\images\\board4.jpg");
		if (img == null)
			return ;


		BoardDetector detector = new BoardDetector(this);
		
		//image(detector.preprocessImage(img), 0, 0);
		//ImageProcessing ip = new ImageProcessing(this);
		image(img, 0, 0);
		PVector[] quad = detector.getCorners(img);
		if (quad == null) {
			System.out.println("No quads");
			return ;
		}
		//background(0);
		
		int r = 0;
		int g = 255;
		for (PVector p : quad) {
			fill(r, g, 0);
			r += 50;
			g -= 50;
			ellipse(p.x, p.y, 10, 10);
		}
	}
	public static void main(String[] args) {
		PApplet.main(JustATest.class.getName());
	}
}
