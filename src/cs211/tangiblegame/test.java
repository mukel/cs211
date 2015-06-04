package cs211.tangiblegame;


import java.awt.Shape;
import java.math.MathContext;
import java.text.Format;
import java.util.ArrayList;
import java.util.Vector;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.event.MouseEvent;

public class test extends PApplet{

	PGraphics mySurface;
	public void setup()
	{
		size(400, 400, P2D);
		mySurface = createGraphics(200,200, P2D);
	}
	
	public void draw()
	{
		background(200, 0,0);
		drawSurface();
		image(mySurface, 10, 190);
	}
	public void drawSurface()
	{
		mySurface.beginDraw();
		mySurface.background(255);
		mySurface.ellipse(50,50,25,25);
		mySurface.endDraw();
	}
}
