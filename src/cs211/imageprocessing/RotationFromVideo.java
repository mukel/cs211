package cs211.imageprocessing;

import java.util.Arrays;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Movie;

public class RotationFromVideo implements RotationProvider {

	PApplet parent;
	Movie video;	
	BoardDetector detector;
	
	Thread detectorThread;

	public RotationFromVideo(PApplet parent, String fileName) {		
		video = new Movie(parent, fileName);
		this.parent = parent;		
		detector = new BoardDetector(parent);
	}
	
	volatile PVector[] corners;
	
	public PVector[] getCorners() {
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
		video.loop();
	}
	
	public void stop() {
		video.stop();
	}
	
	PVector rotation;

	@Override
	public PVector getRotation() {
		PImage img = getFrame();
		if (img == null)
			return rotation;
		PVector[] newCorners = detector.getCorners(img);
		if (newCorners == null)
			return rotation;
		corners = newCorners;
		TwoDThreeD t = new TwoDThreeD(img.width, img.height);
		rotation = t.get3DRotations(Arrays.asList(corners));
		return rotation;
	}
}

