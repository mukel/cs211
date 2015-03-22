import java.util.ArrayList;
import java.util.List;

import processing.core.*;
import processing.event.MouseEvent;

public class GameJava extends PApplet {
	
	float cylinderRadius = 50;
	float cylinderHeight = 50;
	
	int cylinderResolution = 40;
	
	PShape closedCylinder;

	int zzfactor;
	float rotX;
	float rotY;
	float rotZ;
	float wheelFactor;
	
	PVector box;
	PVector ball;
	
	float ballRotX;
	float ballRotZ;
	
	float sphereRadius;
	int sphereResolution;
	
	Mover mover;

	List<PVector> cylinderLocations = new ArrayList<PVector>();

	class Mover {
		
		PVector location;
		PVector velocity;
		PVector acceleration;
		PVector friction;
		float mu;
		float maxVelocity;
		float rotateFactor;
		float velocityFactor;
		float bounceFactor;

		public void update(float ax, float az) {
			acceleration.x = sin(ax) * rotateFactor;
			acceleration.y = sin(az) * rotateFactor;
			acceleration.z = 0;

			friction = velocity.get();
			friction.normalize();
			friction.mult(-mu);
			acceleration.add(friction);
			velocity.add(acceleration);
			velocity.limit(maxVelocity);
			location.add(PVector.mult(velocity, velocityFactor));

			// Faux
			
			//ballRotZ += velocityFactor * velocity.x /sphereRadius;
			//ballRotX += velocityFactor * velocity.y /sphereRadius;
		}
		
		public void checkEdges() {
			if (location.y >= box.z / 2) {
				location.y = box.z / 2;
				velocity.y *= -bounceFactor;
			} else if (location.y <= -box.z / 2) {
				location.y = -box.z / 2;
				velocity.y *= -bounceFactor;
			}
			if (location.x >= box.x / 2) {
				location.x = box.x / 2;
				velocity.x *= -bounceFactor;
			} else if (location.x <= -box.x / 2) {
				location.x = -box.x / 2;
				velocity.x *= -bounceFactor;
			}

			for(PVector cylinderPos : cylinderLocations) {
				PVector normal = PVector.sub(location, cylinderPos);
				float distance = normal.mag();

				float radii = sphereRadius + cylinderRadius;
				
				if(distance <= radii) {
					normal.normalize();
					
					PVector newLocation = PVector.add(cylinderPos, PVector.mult(normal, radii));
					location = newLocation;
					
					float dot = normal.dot(velocity);
					normal.mult(2*dot);
					velocity.sub(normal);
				}
			}
		}

		Mover() {
			mu = 0.1F;
			maxVelocity = 100f;
			rotateFactor = 1.0f;
			velocityFactor = 0.05f;
			bounceFactor = 0.5f;
			location = new PVector(0, 0, -sphereRadius - box.y / 2);
			velocity = new PVector(0, 0, 0);
			friction = new PVector(0, 0, 0);
			acceleration = new PVector(0, 0, 0);
		}
	}
	
	@Override
	public void mouseClicked() {
		float x = map(mouseX, 0, width, -box.x/2, box.x/2);
		float y = map(mouseY, 0, height, -box.z/2, box.z/2);
		cylinderLocations.add(new PVector(x, +box.y/2, y));
	}
	public void createCylinder() {
		float[] x = new float[cylinderResolution + 1];
		float[] y = new float[cylinderResolution + 1];
		// get the x and y position on a circle for all the sides
		for (int i = 0; i < x.length; i++) {
			float angle = (TWO_PI / cylinderResolution) * i;
			x[i] = sin(angle) * cylinderRadius;
			y[i] = cos(angle) * cylinderRadius;
		}

		PShape openCylinder = createShape();
		openCylinder.beginShape(QUAD_STRIP);
		// draw the border of the cylinder
		for (int i = 0; i < x.length; i++) {
			openCylinder.vertex(x[i], y[i], 0);
			openCylinder.vertex(x[i], y[i], cylinderHeight);
		}
		openCylinder.endShape();

		PShape top = createShape();
		top.beginShape(TRIANGLE_FAN);
		// draw the top of the cylinder
		top.vertex(0, 0, cylinderHeight);
		for (int i = 0; i < x.length; i++) {
			top.vertex(x[i], y[i], cylinderHeight);
			top.vertex(x[(i + 1) % x.length], y[(i + 1) % x.length],
					cylinderHeight);
		}
		top.endShape();

		PShape bottom = createShape();
		bottom.beginShape(TRIANGLE_FAN);
		// draw the bottom of the cylinder
		bottom.vertex(0, 0, 0);
		for (int i = 0; i < x.length; i++) {
			bottom.vertex(x[i], y[i], 0);
			bottom.vertex(x[(i + 1) % x.length], y[(i + 1) % x.length], 0);
		}
		bottom.endShape();

		closedCylinder = createShape(GROUP);
		closedCylinder.addChild(top);
		closedCylinder.addChild(bottom);
		closedCylinder.addChild(openCylinder);
	}

	public void setup() {
		size(displayWidth, displayHeight, P3D);
		createCylinder();
		wheelFactor = 1;
		box = new PVector(500, 20, 500);
		sphereRadius = 20F;
		sphereResolution = 10;
		mover = new Mover();
	}
	
	void drawPlayScene() {
		spotLight(51F, 102F, 126F, 1000F, 0, 0, 0, 1.0F, 0, PI / 2, 2);
		ambientLight(128F, 128F, 128F);
		background(200);
		rotY = ((float) zzfactor * PI) / 36F;
		pushMatrix();
		translate(width / 2, height / 2, 0);
		lights();
		stroke(255);
		rotateX(rotX);
		rotateZ(rotZ);
		rotateY(rotY);
		fill(150);
		box(box.x, box.y, box.z);
		for (PVector center : cylinderLocations) {
			pushMatrix();
			rotateX(PI/2);
			translate(center.x, box.y, center.y);
			
			fill(200);
			noStroke();
			shape(closedCylinder);
			popMatrix();
		}
		
		mover.checkEdges();
		mover.update(rotZ, rotX);
		translate(mover.location.x, mover.location.z, -mover.location.y);
		
		// noStroke();
		// fill(50);
		// shininess(10F);
		{
			pushMatrix();
			rotateX(ballRotX);
			rotateZ(ballRotZ);
			//sphereDetail(sphereResolution);
			sphere(sphereRadius);
			popMatrix();
		}

		popMatrix();
	}

	void drawEditMode() {
		stroke(20);
		noFill();
		box(box.x, box.z, box.y);

		for (PVector center : cylinderLocations) {
			pushMatrix();	
			translate(-width/2, -height/2, 0);
			translate(center.x, center.z, center.y);
			shape(closedCylinder);
			popMatrix();
		}
	}
	
	void drawPlayMode() {
		drawPlayScene();
	}

	public void draw() {
		if (interfaceMode == EDIT) drawEditMode();
		else if (interfaceMode == PLAY) drawPlayMode();
	}

	public void mouseDragged() {
		rotX += map(mouseY, 0, height, 5.235988F * wheelFactor, -5.235988F
				* wheelFactor)
				- map(pmouseY, 0, height, 5.235988F * wheelFactor, -5.235988F
						* wheelFactor);
		rotZ += map(mouseX, 0, width, -5.235988F * wheelFactor,
				5.235988F * wheelFactor)
				- map(pmouseX, 0, width, -5.235988F * wheelFactor,
						5.235988F * wheelFactor);
		if (rotX > PI / 3)
			rotX = PI / 3;
		if (rotX < -PI / 3)
			rotX = -PI / 3;
		if (rotZ > PI / 3)
			rotZ = PI / 3;
		if (rotZ < -PI / 3)
			rotZ = -PI / 3;
	}
	
	final int EDIT = 0;
	final int PLAY = 1;
	int interfaceMode = PLAY;
	
	@Override
	public void keyPressed() {	 
		if (key == CODED && keyCode == SHIFT)
			   interfaceMode = EDIT;
	} 
	
	@Override
	public void keyReleased() {
		if(key == CODED && keyCode == SHIFT)
			interfaceMode = PLAY;
	}

	public void mouseWheel(MouseEvent mouseevent) {
		float f = mouseevent.getCount();
		wheelFactor += f * 0.1F;
		if (wheelFactor > 1.5F)
			wheelFactor = 1.5F;
		if (wheelFactor < 0.2F)
			wheelFactor = 0.2F;
	}

	public static void main(String[] args) {
		PApplet.main(GameJava.class.getName());
	}
}
