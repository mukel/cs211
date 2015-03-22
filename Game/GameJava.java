/** 
* @author Montero Aimee
* @author Peterssen Alfonso
* @author Mbanga Ndjock Pierre Armel 229047
*/
import java.awt.Shape;
import java.math.MathContext;
import java.text.Format;
import java.util.ArrayList;
import java.util.Vector;
import processing.event.MouseEvent;

import javax.swing.Box;


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
	float boxWidth = 500;
	float boxDepth = 500;
	
	float sphereRadius = 30;
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
	
	ArrayList<Cylinder> cyliderPos;
	Mover mover;
	
	float cylinderBaseSize = 30;
	float cylinderHeight = 30;
	int cylinderResolution = 20;
	
	PShape cylinder;
	
	PFont f;
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
		mover = new Mover();
		/*
		location = new PVector(0, -sphereRadius - boxHeight/2f, 0);
		velocity = new PVector(0, 0, 0);
		acceleration = new PVector(0, 0, 0);
		*/
		gravity = new PVector(0.0f, 0.0f, 0.0f);
		cyliderPos = new ArrayList<>();
		f = createFont("Arial",16,true); 
		
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
			(x >= - boxWidth/2 + cylinderBaseSize && x <= + boxWidth/2 - cylinderBaseSize)&&
			 y >= - boxDepth/2 + cylinderBaseSize && y <= + boxDepth/2 -cylinderBaseSize)
		{
			tooCloseOfEdge = false;
			if(mover.checkBallPos(x, y))
			{			
				cyliderPos.add(new Cylinder(new PVector(x, - boxHeight/2f, y )) );
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
		if(modeStandar)
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
		camera(width/2, height/2, depth, width/2, height/2, 0, 0, 1, 0);
		
		directionalLight(50, 100, 200, 0, 0,-1);
		ambientLight(102, 102, 102);
		
		background(200);
		//pushMatrix();
		
			
		translate(width/2, height/2, 0);
		Cylinder c;
		if(modeStandar)
		{
			tooCloseOfBall = false;
			tooCloseOfEdge = false;
			
			rotateY(rotY);
			rotateZ(rotZ);
			rotateX(rotX);
		
			stroke(20);
			noFill();
			box(boxWidth, boxHeight, boxDepth);
		
			pushMatrix();
		
			mover.update();
			mover.checkEdge();
			mover.checkCylinder();
			
			//mover.display();
		
			popMatrix();
		}
		
		else
		{
			stroke(20);
			noFill();
			box(boxWidth, boxDepth, boxHeight);
						
		}
		pushMatrix();
		
		for(int i = 0; i < cyliderPos.size(); i++)
		{
			pushMatrix();
			
			cyliderPos.get(i).display();
			popMatrix();
		}
		
		pushMatrix();
		
		mover.display();
		popMatrix();
		
		  	
		popMatrix();
		
		
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
	class Mover
	{
		
		PVector location;
		PVector velocity;
		PVector acceleration;
		PVector oldLocation;
		
		/** 
		 * Initialises instance variables such as the sphere location
		 * to get started with the physical simulation of the game.
		 */
		Mover()
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
			
			
			location.add(PVector.mult(velocity.get(), 0.1f));
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
				velocity.x = velocity.x *-0.5f;
				
			}
			if(location.z >= boxDepth/2 - sphereRadius/2 || location.z <= -boxDepth/2 + sphereRadius/2)
			{
				velocity.z = velocity.z *-0.5f;
			}
			
			
			location.x = Math.max(location.x, -boxWidth/2 + sphereRadius/2);
			location.x = Math.min(location.x, +boxWidth/2 - sphereRadius/2);
			
			location.z = Math.max(location.z, -boxDepth/2 + sphereRadius/2);
			location.z = Math.min(location.z, +boxDepth/2 - sphereRadius/2);
			
			
		
		}
		boolean checkBallPos(float x, float z)
		{
			
			float dist = dist(location.x, location.z,x, z);//n.mag();
			println("ball posx: "+ location.x + " ball pos y : " + location.z);
			println("X : " + x + "  Y : " + z);
			println("dist " + dist + " radio S + radio C : " + (sphereRadius + cylinderBaseSize) );
			
			return (dist > sphereRadius + cylinderBaseSize);
		}
		
		void checkCylinder()
		{
			for(int i = 0; i < cyliderPos.size(); i++)
			{
				PVector cPos = cyliderPos.get(i).cylinderlocation.get();
				//cPos.sub(new PVector(width/2, 0, height/2));
				
				PVector n = new PVector(location.x - cPos.x, 0, location.z - cPos.z);
				float dist = n.mag();
				
				if(dist <= sphereRadius + cylinderBaseSize)
				{
					
					PVector normalizedN = n.get();
					normalizedN.normalize();
					normalizedN.mult(cylinderBaseSize + sphereRadius);
					location = new PVector(cPos.x + normalizedN.x, location.y, cPos.z + normalizedN.z );
					
					n.normalize();
					float dot = n.dot(velocity);
					n.mult(2*dot);
					velocity.sub(n);
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
	class Cylinder
	{
		/** 
		 * @instanceVariable cylinderlocation  The current location of the cylinder
		 * @instanceVariable cylinder		   The cylinder shape in 3D mode
		 * @instanceVariable platCylinder	   The cylinder shape in 2D mode
		 * 
		 */
		PVector cylinderlocation;
		
		PShape cylinder;
		PShape platCylinder;
		
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
				x[i] = sin(angle)*cylinderBaseSize;
				y[i] = cos(angle)*cylinderBaseSize;
			}
			
			
			PShape openCylinder;
			PShape topCylinder;
			PShape BottomCylinder;
			
			openCylinder = createShape();
			openCylinder.beginShape(QUAD_STRIP);
			
			for(int i = 0; i < x.length; i++)
			{
				if(mode)
				{
					openCylinder.vertex(x[i], 0, y[i]);
					openCylinder.vertex(x[i], cylinderHeight, y[i]);
				}
				else
				{
					openCylinder.vertex(x[i], y[i], 0);
					openCylinder.vertex(x[i], y[i], cylinderHeight);
				}
			
			}
			openCylinder.endShape();
			
			topCylinder = createShape();
			topCylinder.beginShape(TRIANGLE_FAN);
			if(mode)
				topCylinder.vertex(0,cylinderHeight, 0);
			else
				topCylinder.vertex(0, 0, cylinderHeight);
			for(int i = 0; i < cylinderResolution; i++)
			{
				if(mode)
				{
					topCylinder.vertex(x[i], cylinderHeight, y[i]);
					topCylinder.vertex(x[i+1], cylinderHeight, y[i+1]);
				}
				else
				{
					topCylinder.vertex(x[i], y[i], cylinderHeight);
					topCylinder.vertex(x[i+1], y[i+1], cylinderHeight);
				}	
			}
			topCylinder.endShape();
			
			BottomCylinder = createShape();
			BottomCylinder.beginShape(TRIANGLE_FAN);
			BottomCylinder.vertex(0, 0, 0);		
			for(int i = 0; i < cylinderResolution; i++)
			{	
				if(mode)
				{
					BottomCylinder.vertex(x[i], 0, y[i]);
					BottomCylinder.vertex(x[i+1], 0, y[i+1]);
				}
				else
				{
					BottomCylinder.vertex(x[i], y[i], 0);
					BottomCylinder.vertex(x[i+1], y[i+1], 0);
					
				}
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
		Cylinder(PVector location)
		{
			cylinderlocation = location;
			
			cylinder = CreateCylinder(true);
			platCylinder = CreateCylinder(false);
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
				//translate( -width/2,0,  -height/2);
				translate(cylinderlocation.x, cylinderlocation.y - cylinderHeight, cylinderlocation.z );
				tint(200);
				noStroke();
				fill(200, 100, 100);
				shape(cylinder);
			}
			else
			{
				//translate(- width/2, - height/2, 0);
				translate(cylinderlocation.x ,cylinderlocation.z);
				tint(200);

				noStroke();
				fill(200, 100, 100);
				shape(platCylinder);
		
			}
		
			popMatrix();
			
		}
	}
	
	public static void main(String[] args) {
		PApplet.main(GameJava.class.getName());
	}

}