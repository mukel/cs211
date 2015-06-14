package cs211.imageprocessing;

import java.util.Arrays;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Movie;

public class RotationFromVideo {

	PApplet parent;
	Movie video;	
	BoardDetector detector;
	volatile boolean loop;
	volatile List<PVector[]> corners;
	Thread detectorThread;

	public RotationFromVideo(PApplet parent, String fileName) {		
		video = new Movie(parent, fileName);
		this.parent = parent;		
		detector = new BoardDetector(parent);
	}
	
	public List<PVector[]> getCandidates() {
		return corners;
	}
	
	public PImage getFrame() {
		PImage img = null;
		if (video.available()) {
			video.read();
			img = video.get();
		}				
		return img;			
	}
	
	public void loop() {
		detectorThread = new Thread(new Detector());
		loop = true;
		video.loop();
		detectorThread.start();
	}
	
	public void stop() {
		loop = false;
		try {
			detectorThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class Detector implements Runnable {
		@Override
		public void run() {
			while (loop) {
				PImage img = getFrame();
				if (img == null)
					continue ;
				List<PVector[]> newCorners = detector.getCorners(img);
				if (newCorners == null)
					continue ;
				corners = newCorners;
			}			
		}
	}
}

