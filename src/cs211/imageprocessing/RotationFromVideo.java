package cs211.imageprocessing;

import java.util.Arrays;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Movie;

public class RotationFromVideo implements RotationProvider {

	PApplet parent;
	Movie video;
	volatile boolean loop;
	volatile PVector rotation;
	BoardDetector detector;
	
	Thread detectorThread;

	public RotationFromVideo(PApplet parent, String fileName) {		
		video = new Movie(parent, fileName);
		this.parent = new PApplet();		
		detector = new BoardDetector(parent);
		//this.parent = parent;
	}	
	
	public void loop() {
		loop = true;
		video.loop();
		detectorThread = new Thread(new Detector());
		detectorThread.start();
	}
	
	public void stop() {
		loop = false;
		try {
			detectorThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	public PVector getRotation() {
		return rotation;
	}

	class Detector implements Runnable {
		@Override
		public void run() {
			while (loop) {
				PImage img = null;
				if (video.available()) {
					video.read();
					img = video.get();
				}				
				if (img == null)
					continue ;
				PVector[] corners = detector.getCorners(img);
				if (corners == null)
					continue ;
				TwoDThreeD t = new TwoDThreeD(parent.width, parent.height);
				rotation = t.get3DRotations(Arrays.asList(corners));
			}
		}
	}
}

