package cs211.tangiblegame;
/** 
* @author Montero Aimee 221053
* @author Peterssen Alfonso 221982
* @author Mbanga Ndjock Pierre Armel 229047
*/

import java.util.*;

import processing.event.MouseEvent;



import processing.core.*;

public class GameJava extends PApplet{
	
	float wheelFactor = 0.5f;
	
	@Override
	public void mouseWheel(MouseEvent me) {
		float f = me.getCount();
		wheelFactor += f * 0.1f;
		// clamp wheelFactor to [0,1, 2]
		wheelFactor = Math.min(wheelFactor, Math.min(0.1f, wheelFactor));
	}

	float MAXVITESSE = 100;

	float depth = 800;
		
	float boxHeight = 10;
	float boxWidth = 350;
	float boxDepth = 350;
	
	float sphereRadius = 15;
	//float density = 0.002f;
	//float masa = density * (4/3.0f * PI * (sphereRadio*sphereRadio*sphereRadio));
	

	float rotZ = 0;
	float rotY = 0;
	float rotX = 0;

	/*
	PVector location;
	PVector velocity;
	PVector acceleration;
	*/
	float gravityConst = 2.8f;
	float mu = 0.1f;

	float normal = 1;
	float frictionMagnitud = normal*mu;
	
	boolean modeStandar = true;
	
	PVector gravity;
	PVector friction;
	
	ArrayList<Shape> cyliderPos;
	Ball ball;
	
	PShape lightHouse;
	
	float shapeWidth = 20;
	float shapeHeight = 20;
	int cylinderResolution = 20;
	
	
	
	

	float velocity;
	float TotalScore;
	float lastScore;
	
	PFont f;
	int mySurfaceHeight = 175;
	PGraphics mySurface;
	PGraphics topViw;
	PGraphics Score;
	PGraphics barChart;
	HScrollbar hsb;
	
	int counter;
	
	HScrollbar scrollH;
	HScrollbar scrollV;

	float scoreBoxPos;
	float scoreBoxWidth;
	float scoreBoxHeight;
	float scoreLimit;
	List<Float> bars;

	
	/** 
	 * This function is called once when the program starts. It
	 * defines initial environment properties such as screen 
	 * size, background color and initial variable values.
	 */
	public void setup()
	{
		size(displayWidth, displayHeight, P3D);
		noStroke();
		
		background(250);
		ball = new Ball();
		

		lightHouse = loadShape("models/Lighthouse_7.obj");
		lightHouse.rotateX(PI);
		
		gravity = new PVector(0.0f, 0.0f, 0.0f);
		cyliderPos = new ArrayList<Shape>();
		f = createFont("Arial",16,true); 
		
		
		
		mySurface = createGraphics(width, mySurfaceHeight, P2D);
		topViw = createGraphics(mySurfaceHeight - 10, mySurfaceHeight - 10, P2D);
		
		Score = createGraphics(3* mySurfaceHeight / 4, mySurfaceHeight - 10);
		
		barChart = createGraphics(width - topViw.width - Score.width - 45 , 3*mySurfaceHeight/4 + 5);
		
		scoreBoxPos = 0;
		scoreBoxHeight = 5f;
		scoreBoxWidth = 10f;
		scoreLimit = 2500;
		counter = 0;
		
		//image(barChart, 2*mySurfaceHeight + 70,height - mySurfaceHeight- 25 );
		
		scrollH = new HScrollbar(false, topViw.width + Score.width + 35, height - 130 , barChart.width/2, 20);
		scrollV = new HScrollbar(true,  topViw.width + Score.width + 10, height - mySurfaceHeight - 95, 20, barChart.height);
		
		
		bars = new ArrayList<>();
		
	}
	/** 
	 * Checks whether the user press a key button. if a key button
	 * has indeed been pressed, consequent modification such as
	 * changing the image viewed by the user are undertaken. The
	 * key that was pressed is stored in the key variable.
	 * 
	 */
	public void keyPressed() 
	{
		 
		if (key == CODED && modeStandar)
		{
		    if (keyCode == LEFT)
		    {
		      rotY -= PI/50;
		    }
		    else if (keyCode == RIGHT)
		    {
		      rotY += PI/50 ;
		    }
		    else if(keyCode == SHIFT)
		    {
		    	modeStandar = false;
		    }
		  }		
	}
	/** 
	 * Checks whether the user releases the SHIFT button supposed
	 * previously pressed. If so it changes the current mode from 
	 * the non-standard 2D mode to the standard 3D mode. The key
	 * that was released is stored in the key variable.
	 */
	public void keyReleased()
	{
		if(key == CODED)
		{
			if(keyCode == SHIFT)
			{
				modeStandar = true;
			}
			
		}
	}
	/** 
	 * Detects whether the user clicks the mouse. If so it adds to the 
	 * cylinder buffer a new cylinder to be drawn at that particular
	 * location.
	 * 
	 */
	boolean tooCloseOfEdge = false;
	boolean tooCloseOfBall = false;
	public void mouseClicked()
	{
		int x = mouseX - width/2;
		int y = mouseY - height/2;
		
		println("mouse clicked");
		//Changer ici les coordonees du cilindre
		if(!modeStandar &&
			(x >= - boxWidth/2 + shapeWidth && x <= + boxWidth/2 - shapeWidth)&&
			 y >= - boxDepth/2 + shapeWidth && y <= + boxDepth/2 -shapeWidth)
		{
			tooCloseOfEdge = false;
			if(ball.checkPos(x, y))
			{			
				cyliderPos.add(new Shape(new PVector(x, - boxHeight/2f, y )) );
				tooCloseOfBall = false;
			}
			else
			{
				tooCloseOfBall = true;
			}
		}
		else 
		{
			tooCloseOfEdge = true;
		}

	}
	
	

	
	/** 
	 * Keeps track ofthe mouse dragging trajectory on the screen 
	 * to accordingly modify rotational angles. This helps to change the image
	 * from the user point of view as well as the direction of the gravity
	 * vector.
	 */
	@Override
	public void mouseDragged() 
	{
		if(modeStandar && !scrollH.locked && !scrollV.locked)
		{	
			rotZ += (mouseX - pmouseX) / (wheelFactor*200);
			rotX += -(mouseY - pmouseY) / (wheelFactor*200);
			  
			  if(rotZ < -PI/3)
			  {
				 rotZ = -PI/3;
			  }
			  else if(rotZ > PI/3)
			  {
				  rotZ = PI/3;
				  
			  }
			  if(rotX < -PI/3)
			  {
				  rotX = -PI/3;
			  }
			  else if(rotX > PI/3)
			  {
				  rotX = PI/3;
			  }

			  gravity.x = gravityConst*sin(rotZ);
			  gravity.z = -gravityConst*sin(rotX);

		}
	}
	
	/** 
	 * Draws all forms and shapes visible on the screen.
	 * It does so for both the 3D and 2D coordinate system. By using
	 * pushMatrix() and popMatrix methods it ensures that each shape 
	 * is drawn in its corresponding coordinate system. 
	**/
	public void draw()
	{
		//camera(-width/2, -5*height/2, 0, boxWidth/2, boxHeight/2, boxDepth/2, 0, 1, 0);
		background(200);
		//pushMatrix();
	
		drawSurface();
		image(mySurface, 0, height - mySurfaceHeight);
		
		drawTopView();
		image(topViw, 5, height - mySurfaceHeight + 5);
		
		drawScore();
		image(Score, topViw.width + 5, height - mySurfaceHeight + 5);
		
		
		scrollH.update();
		scrollH.display();
		
		scrollV.update();
		scrollV.display();
		
		
		scoreBoxWidth = 10*(scrollH.getPos() + 0.2f);
		
		scoreBoxHeight = 10*(scrollV.getPos() + 0.2f);
		scoreLimit = 2500/(scrollV.getPos()+0.2f);
		
		drawBarChart();
		image(barChart, topViw.width + Score.width + 35 ,height - mySurfaceHeight + 5);
		
		/*
		camera(width/2, height/2, depth, width/2, height/2, 0, 0, 1, 0);
			
		directionalLight(50, 100, 200, 0, 0,-1);
		ambientLight(102, 102, 102);
		*/
	
		pushMatrix();
		
		translate(width/2, height/2, 0);
		if(modeStandar)
		{
			tooCloseOfBall = false;
			tooCloseOfEdge = false;
			
			rotateY(rotY);
			rotateZ(rotZ);
			rotateX(rotX);
		
			stroke(20);
			fill(255);
			box(boxWidth, boxHeight, boxDepth);
		
			
			ball.update();
			ball.checkEdge();
			ball.checkCylinders();
			
			//mover.display();
		
		}
		
		else
		{
			stroke(10);
			fill(255);
			box(boxWidth, boxDepth, boxHeight);
						
		}
		pushMatrix();
		
		for(int i = 0; i < cyliderPos.size(); i++)
		{
		
			cyliderPos.get(i).display();
		}
		
		
		ball.display();
		popMatrix();
		
		  	
		popMatrix();
		
		
	}
	public void drawSurface()
	{
		
		mySurface.beginDraw();
		mySurface.background(238, 223, 204);
		mySurface.endDraw();
		
	}
	public void drawTopView()
	{
		topViw.beginDraw();
		topViw.background(0, 51, 102);
		PVector location = ball.get2DLocation();
		
		PVector newLocation = topViwLocation(location.x, location.y);
		
		topViw.fill(152,0,0);
		float newBallRadius = map(sphereRadius, 0, boxWidth*boxDepth, 0, mySurfaceHeight*mySurfaceHeight);
		topViw.ellipse(newLocation.x, newLocation.y, 4*newBallRadius, 4*newBallRadius);
		
		float newCylinderRadius = map(shapeWidth, 0, boxWidth*boxDepth, 0, mySurfaceHeight*mySurfaceHeight);
		for(int i = 0; i < cyliderPos.size(); i++)
		{
			PVector cylinder = cyliderPos.get(i).shapelocation;
			newLocation = topViwLocation(cylinder.x, cylinder.z);
			
			
			
			topViw.fill(240);
			topViw.ellipse(newLocation.x, newLocation.y, 4*newCylinderRadius, 4*newCylinderRadius);
		}
	
		topViw.endDraw();
	}
	
	
	PVector topViwLocation(float x , float y)
	{
		float newX = map(x, -boxWidth/2, boxWidth/2, 0, mySurfaceHeight);
		float newY = map(y, -boxDepth/2, boxDepth/2, 0, mySurfaceHeight);
		
		return new PVector(newX, newY);
		
	}
	
	void drawScore()
	{
		Score.beginDraw();
		Score.background(238, 223, 204);
		
		Score.stroke(255);
		Score.strokeWeight(3);
		Score.line(5, 5, Score.width - 5, 5);
		Score.line(5, 5, 5, Score.height - 5);
		Score.line(5, Score.height - 5, Score.width - 5, Score.height - 5);
		Score.line(Score.width - 5, Score.height - 5, Score.width - 5, 5);
		
		Score.textSize(13);
		Score.fill(0);
		Score.text("Total Score:", 15, 30);
		Score.text(TotalScore, 20, 45);
		
		
		Score.text("Velocity:", 15, 80);
		Score.text(ball.getVelocityMag(), 20, 95);
		
		
		Score.text("Last Score:", 15, 130);
		Score.text(lastScore, 20, 145);
		
		
		Score.endDraw();
	}
	
	void drawBarChart()
	{
		
		barChart.beginDraw();
		barChart.background(240, 230, 210);
		barChart.line(0, barChart.height / 2, barChart.width, barChart.height/2);
		
		if(modeStandar && ++counter > 20)
		{
			counter = 0;
			if(bars.size() == 0 || TotalScore != bars.get(bars.size() - 1))
				bars.add(TotalScore);
		}
		
		float value;
		int sing = 0;
		int upperHalf = 0;
		int boxNumber = 0;
		float heigth = 0;
		for(int i = 0; i < bars.size(); i++ )
		{
			value = bars.get(i); 
			sing = (value > 0)?  1 : -1;
			upperHalf = (value > 0)?  1 : 0;
			
			
			heigth = map(Math.abs(value), 0, scoreLimit, 0, barChart.height/2);
			
			
			boxNumber = Math.round(heigth / scoreBoxHeight);
											
			for(int j = 0; j < boxNumber; j++)
			{
				barChart.fill(255*(1-upperHalf), 255*upperHalf,0);
				barChart.rect(i*scoreBoxWidth, barChart.height/2 - sing*(j + upperHalf)*scoreBoxHeight , scoreBoxWidth, scoreBoxHeight);
			}
		
		
		}
		barChart.endDraw();
	}
	
	
	
	/** 
	 * Provides methods to control physical and 
	 * logical behaviours of the game. 
	 * 
	 * @see #Mover()
	 * @see #update()
	 * @see #display()
	 * @see #checkEdge()
	 * @see #checkCylinder(int)
	 */
	class Ball
	{
		PVector location;
		PVector velocity;
		PVector acceleration;
		PVector oldLocation;
		
		/** 
		 * Initialises instance variables such as the sphere location
		 * to get started with the physical simulation of the game.
		 */
		Ball()
		{
			location = new PVector(0, -sphereRadius - boxHeight/2f, 0);
			velocity = new PVector(0, 0, 0);
			acceleration = new PVector(0, 0, 0);

			gravity = new PVector(0.0f, 0.0f, 0.0f);
		}
		/** 
		 * Updates instance variables such as friction or location
		 * to simulate the physical behaviour of the sphere. 
		 * 
		 */
		void update()
		{
			friction = velocity.get();
			
			
			friction.mult(-1);
			friction.normalize();
			friction.mult(frictionMagnitud);
			
			PVector totalForce = friction.get();
			totalForce.add(gravity);
			
			acceleration = totalForce.get();
		
			velocity.add(acceleration);
			velocity.limit(MAXVITESSE);
			
			//correctVelocity();
			location.add(PVector.mult(velocity.get(), 0.1f));
		}
		void updateTotalScore(float toAdd)
		{
			if(toAdd > 2 || toAdd < -2) 
				TotalScore += toAdd;
		}
		
		
		
		/** 
		 * Aims to correctly set the sphere location on the 
		 * plate. Depending on the current mode, it could be necessary 
		 * or not to change the sphere location coordinates so that it
		 * fits with the mode the user is observing. By default locations
		 * are given in the standard coordinate system. That's the 3D
		 * coordinate system. 
		 * 
		 * This method essentially performs when necessary a projection
		 * of the sphere coordinate from 3D to 2D.
		 *  
		 */
		void display()
		{	
			if(modeStandar)
				translate(location.x, location.y, location.z);
			else
			{
				PVector newlocation = new PVector(location.x, location.z, 0);
				translate(newlocation.x, newlocation.y, newlocation.z);
			}
			noStroke();
			fill(0, 200, 0);
			sphere(sphereRadius);
		}
		
		PVector get2DLocation()
		{
			return new PVector(location.x, location.z, 0);
		}
		float getVelocityMag()
		{
			float mag = velocity.mag();
			
			return (mag < 2)? 0: mag;
		}
		
		/** 
		 * Checks whether the sphere hits an obstacle and apply
		 * consequent changes in the sphere displacement. That's its loca
		 * tion and its velocity. An abstacle could be either a plate border
		 * or a cylinder. This is done by comparing the sphere current loca
		 * tion with border locations and each cylinder location on the plate.
		 * 
		 */
		void checkEdge()
		{
			if(location.x >= boxWidth/2 - sphereRadius/2 || location.x <= -boxWidth/2 + sphereRadius/2)
			{
				lastScore = velocity.mag();
				
				velocity.x = velocity.x *-0.5f;
				
				
				updateTotalScore(-velocity.mag());
				
				
			}
			if(location.z >= boxDepth/2 - sphereRadius/2 || location.z <= -boxDepth/2 + sphereRadius/2)
			{
				lastScore = velocity.mag();
				
				velocity.z = velocity.z *-0.5f;
				
				updateTotalScore(-velocity.mag());
			}
			
			
			location.x = Math.max(location.x, -boxWidth/2 + sphereRadius/2);
			location.x = Math.min(location.x, +boxWidth/2 - sphereRadius/2);
			
			location.z = Math.max(location.z, -boxDepth/2 + sphereRadius/2);
			location.z = Math.min(location.z, +boxDepth/2 - sphereRadius/2);
			
			
		
		}
	
		
		
		boolean checkPos(float x, float z)
		{
			
			float dist = dist(location.x, location.z,x, z);//n.mag();
			//println("ball posx: "+ location.x + " ball pos y : " + location.z);
			//println("X : " + x + "  Y : " + z);
			//println("dist " + dist + " radio S + radio C : " + (sphereRadius + cylinderBaseSize) );
			
			return (dist > sphereRadius + shapeWidth);
		}
		
		void checkCylinders()
		{
			for(int i = 0; i < cyliderPos.size(); i++)
			{
				PVector cPos = cyliderPos.get(i).shapelocation.get();
				//cPos.sub(new PVector(width/2, 0, height/2));
				
				PVector n = new PVector(location.x - cPos.x, 0, location.z - cPos.z);
				float dist = n.mag();
				
				if(dist <= sphereRadius + shapeWidth)
				{
					lastScore = velocity.mag();
					
					
					PVector normalizedN = n.get();
					normalizedN.normalize();
					normalizedN.mult(shapeWidth + sphereRadius);
					location = new PVector(cPos.x + normalizedN.x, location.y, cPos.z + normalizedN.z );
					
					n.normalize();
					float dot = n.dot(velocity);
					n.mult(2*dot);
					velocity.sub(n);
					velocity.mult(0.9f);
					
					updateTotalScore(velocity.mag());
				}
				
			}
		}
		
	}
	
	
	
	/** 
	 * Models cylinders by constructing at initialisation two of them for both
	 * the 3D and 2D coordinate system. Among the functionalities it gives
	 * us it can display the cylinder on the screen.
	 * 
	 * @see #CreateCylinder(boolean)
	 * @see #display()
	 * @see #Cylinder(PVector)
	 */
	class Shape
	{
		/** 
		 * @instanceVariable cylinderlocation  The current location of the cylinder
		 * @instanceVariable cylinder		   The cylinder shape in 3D mode
		 * @instanceVariable platCylinder	   The cylinder shape in 2D mode
		 * 
		 */
		PVector shapelocation;
		
		PShape cylinder;
		
		/** 
		 * Creates a cylinder in a given mode.
		 * 
		 * @param mode   The mode used to construct the cylinder
		 * 				 if mode = true the cylinder will be cons
		 * 				 tructed in 3D mode else it would be cons
		 * 				 tructed in 2D mode.  
		 * 
		 * @return The cylinder constructed. It's composed of the 
		 * 		   cylinder shape in 3D and 2D mode along with its
		 * 		   initial position.
		 */
		private PShape CreateCylinder(boolean mode)
		{
			PShape cylinder;
			float angle;
			float[] x = new float[cylinderResolution + 1];
			float[] y = new float[cylinderResolution + 1];
			

			for(int i = 0; i < x.length; i++)
			{
				angle = (TWO_PI / cylinderResolution)*i;
				x[i] = sin(angle)*shapeWidth;
				y[i] = cos(angle)*shapeWidth;
			}
			
			
			PShape openCylinder;
			PShape topCylinder;
			PShape BottomCylinder;
			
			openCylinder = createShape();
			openCylinder.beginShape(QUAD_STRIP);
			
			for(int i = 0; i < x.length; i++)
			{
				openCylinder.vertex(x[i], 0, y[i]);
				openCylinder.vertex(x[i], shapeHeight, y[i]);
				
			}
			openCylinder.endShape();
			
			topCylinder = createShape();
			topCylinder.beginShape(TRIANGLE_FAN);
			
			topCylinder.vertex(0,shapeHeight, 0);
			
			for(int i = 0; i < cylinderResolution; i++)
			{
				topCylinder.vertex(x[i], shapeHeight, y[i]);
				topCylinder.vertex(x[i+1], shapeHeight, y[i+1]);
				
			}
			topCylinder.endShape();
			
			BottomCylinder = createShape();
			BottomCylinder.beginShape(TRIANGLE_FAN);
			BottomCylinder.vertex(0, 0, 0);		
			for(int i = 0; i < cylinderResolution; i++)
			{	
				BottomCylinder.vertex(x[i], 0, y[i]);
				BottomCylinder.vertex(x[i+1], 0, y[i+1]);
				
			}
			
			BottomCylinder.endShape();		
			
			cylinder = createShape(GROUP);
			cylinder.addChild(topCylinder);
			cylinder.addChild(openCylinder);
			cylinder.addChild(BottomCylinder);
			
			return cylinder;
		}
		/** 
		 * Constructs a cylinder at a given location.
		 * @param location the location at which to construct the cylinder
		 */
		Shape(PVector location)
		{
			shapelocation = location;
			cylinder = CreateCylinder(true);
			
			
		
		}
	
		/** 
		 * Displays the cylinder on the screen given the mode
		 * currently used by the user.
		 */
		void display()
		{
			pushMatrix();
			
			
			if(modeStandar)
			{
				translate(shapelocation.x, shapelocation.y , shapelocation.z );
				
				noStroke();
				//rotateX(PI);
				
				shape(lightHouse);
			}
			else
			{
				translate(shapelocation.x ,shapelocation.z, boxHeight);
				
				noStroke();
				//fill(200, 100, 100);
				rotateX(-PI/2);
				shape(lightHouse);
				
			}
		
			popMatrix();
			
		}
	}
	
	public static void main(String[] args) {
		PApplet.main(GameJava.class.getName());
	}
	class HScrollbar {
		  float barWidth;  //Bar's width in pixels
		  float barHeight; //Bar's height in pixels
		  float xPosition;  //Bar's x position in pixels
		  float yPosition;  //Bar's y position in pixels
		  
		  float sliderPosition, newSliderPosition;    //Position of slider
		  float sliderPositionMin, sliderPositionMax; //Max and min values of slider
		  
		  boolean vertical;
		  
		  boolean mouseOver;  //Is the mouse over the slider?
		  boolean locked;     //Is the mouse clicking and dragging the slider now?

		  /**
		   * @brief Creates a new horizontal scrollbar
		   * 
		   * @param x The x position of the top left corner of the bar in pixels
		   * @param y The y position of the top left corner of the bar in pixels
		   * @param w The width of the bar in pixels
		   * @param h The height of the bar in pixels
		   * @param v Indicate if the scroll is vertical, false if is horizontal
		   */
		  HScrollbar (boolean v, float x, float y, float w, float h) {
		    barWidth = w;
		    barHeight = h;
		    
		    xPosition = x;
		    yPosition = y;
		    
		    vertical = v;
		    
		    sliderPosition = (!v)? xPosition + barWidth/2 - barHeight/2 : yPosition + barHeight / 2 - barWidth/2;
		    newSliderPosition = sliderPosition;
		    
		    sliderPositionMin = (!v) ? xPosition : yPosition;
		    sliderPositionMax = (!v) ? xPosition + barWidth - barHeight : yPosition + barHeight - barWidth ;
		  }

		  /**
		   * @brief Updates the state of the scrollbar according to the mouse movement
		   */
		  void update() {
			  mouseOver = isMouseOver();
		    if (mousePressed && mouseOver) {
		      locked = true;
		    }
		    if (!mousePressed) {
		      locked = false;
		    }
		    if (locked) {
		      newSliderPosition = (!vertical) ? constrain(mouseX - barHeight/2, sliderPositionMin, sliderPositionMax) :
		    	  								constrain(mouseY - barWidth/2, sliderPositionMin, sliderPositionMax);
		    }
		    if (abs(newSliderPosition - sliderPosition) > 1) {
		      sliderPosition = sliderPosition + (newSliderPosition - sliderPosition);
		    }
		  }

		  /**
		   * @brief Clamps the value into the interval
		   * 
		   * @param val The value to be clamped
		   * @param minVal Smallest value possible
		   * @param maxVal Largest value possible
		   * 
		   * @return val clamped into the interval [minVal, maxVal]
		   */
		  float constrain(float val, float minVal, float maxVal) {
		    return min(max(val, minVal), maxVal);
		  }

		  /**
		   * @brief Gets whether the mouse is hovering the scrollbar
		   *
		   * @return Whether the mouse is hovering the scrollbar
		   */
		  boolean isMouseOver() {
		    if (mouseX  > xPosition && mouseX < xPosition + barWidth &&
		      mouseY  > yPosition && mouseY  < yPosition+barHeight) {
		      return true;
		    }
		    else {
		      return false;
		    }
		  }

		  /**
		   * @brief Draws the scrollbar in its current state
		   */ 
		  void display() {
		    noStroke();
		    fill(204);
		    rect(xPosition, yPosition, barWidth, barHeight);
		    if (mouseOver || locked) {
		      fill(0, 0, 0);
		    }
		    else {
		      fill(102, 102, 102);
		    }
		    if(!vertical)
		    	rect(sliderPosition, yPosition, barHeight, barHeight);
		    else
		    	rect(xPosition, sliderPosition, barWidth, barWidth);
		  }

		  /**
		   * @brief Gets the slider position
		   * 
		   * @return The slider position in the interval [0,1] corresponding to [leftmost position, rightmost position]
		   */
		  float getPos() {
		    if(vertical)
		    	return (sliderPosition - yPosition) / (barHeight - barWidth);
		    
		    return (sliderPosition - xPosition)/(barWidth - barHeight);
		  }
		}
	

}
