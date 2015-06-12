import processing.core.*;

public class InteractiveProjectionJava extends PApplet {

	public static void main(String[] args) {
		PApplet.main(InteractiveProjectionJava.class.getName());
	}
	
	float rotX;
	float rotY;
	float scaleFactor = 1;

	public void setup() {
		size(640, 480, P2D);
	}

	public void draw() {
		background(255, 255, 255);
		My3DPoint eye = new My3DPoint(0, 0, -5000);
		My3DPoint origin = new My3DPoint(0, 0, 0);

		float dimX = 100, dimY = 150, dimZ = 300;

		My3DBox input3DBox = new My3DBox(origin, dimX, dimY, dimZ);
		
		// put the cuboid center at the origin
		float[][] transform0 = translationMatrix(-dimX/2, -dimY/2, -dimZ/2);
		input3DBox = transformBox(input3DBox, transform0);
		
		float[][] transform4 = scaleMatrix(scaleFactor, scaleFactor, scaleFactor);
		input3DBox = transformBox(input3DBox, transform4);

		// rotated around x
		float[][] transform1 = rotateXMatrix(rotX);
		input3DBox = transformBox(input3DBox, transform1);
		//projectBox(eye, input3DBox).render();

		// rotated around y
		float[][] transform2 = rotateYMatrix(rotY);
		input3DBox = transformBox(input3DBox, transform2);
		//projectBox(eye, input3DBox).render();
		
		// translate to screen center
		float[][] transform3 = translationMatrix(width / 2, height / 2, 0);
		input3DBox = transformBox(input3DBox, transform3);
		projectBox(eye, input3DBox).render();
	}
	
	@Override
	public void keyPressed() {
		final float delta = PI/20;
		if (key == CODED) {
			if (keyCode == UP) rotX += delta;
			if (keyCode == DOWN) rotX -= delta;
			if (keyCode == LEFT) rotY -= delta;
			if (keyCode == RIGHT) rotY += delta;
		}
	}

	public void mouseDragged() {
	  scaleFactor += (pmouseY - mouseY) / 500.0f;
	  scaleFactor = Math.max(0.2f, Math.min(scaleFactor, 3));
	}

	public static class My2DPoint {
		float x;
		float y;

		My2DPoint(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	public class My2DBox {
		My2DPoint[] s;

		My2DBox(My2DPoint[] s) {
			this.s = s;
		}

		public void render() {
			int[] id = new int[] { 2, 0, 1, 3, 6, 4, 5, 7 };
			for (int i = 7; i >= 0; --i)
				for (int j = i + 1; j < 8; ++j)
					if (Integer.bitCount(id[i] ^ id[j]) == 1)
						line(s[i].x, s[i].y, s[j].x, s[j].y);
		}
	}

	public static class My3DBox {
		My3DPoint[] p;

		public My3DBox(My3DPoint origin, float dimX, float dimY, float dimZ) {
			float x = origin.x;
			float y = origin.y;
			float z = origin.z;
			this.p = new My3DPoint[] { new My3DPoint(x, y + dimY, z + dimZ),
					new My3DPoint(x, y, z + dimZ),
					new My3DPoint(x + dimX, y, z + dimZ),
					new My3DPoint(x + dimX, y + dimY, z + dimZ),
					new My3DPoint(x, y + dimY, z), origin,
					new My3DPoint(x + dimX, y, z),
					new My3DPoint(x + dimX, y + dimY, z) };
		}

		My3DBox(My3DPoint[] p) {
			this.p = p;
		}
	}

	public static class My3DPoint {
		float x;
		float y;
		float z;

		public My3DPoint(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	public My2DPoint projectPoint(My3DPoint eye, My3DPoint p) {
		return new My2DPoint(
				(p.x - eye.x) * -eye.z / (p.z - eye.z),
				(p.y - eye.y) * -eye.z / (p.z - eye.z));
	}

	public My2DBox projectBox(My3DPoint eye, My3DBox box) {
		My2DPoint[] points = new My2DPoint[box.p.length];
		for (int i = 0; i < points.length; ++i)
			points[i] = projectPoint(eye, box.p[i]);
		return new My2DBox(points);
	}

	public float[][] rotateXMatrix(float angle) {
		return (new float[][] { { 1, 0, 0, 0 },
				{ 0, cos(angle), sin(angle), 0 },
				{ 0, -sin(angle), cos(angle), 0 }, { 0, 0, 0, 1 } });
	}

	public float[][] rotateYMatrix(float angle) {
		return (new float[][] { { cos(angle), 0, -sin(angle), 0 },
				{ 0, 1, 0, 0 }, { sin(angle), 0, cos(angle), 0 },
				{ 0, 0, 0, 1 } });
	}

	public float[][] rotateZMatrix(float angle) {
		return (new float[][] { { cos(angle), sin(angle), 0, 0 },
				{ -sin(angle), cos(angle), 0, 0 }, { 0, 0, 1, 0 },
				{ 0, 0, 0, 1 } });
	}

	public float[][] scaleMatrix(float x, float y, float z) {
		return (new float[][] { { x, 0, 0, 0 }, { 0, y, 0, 0 }, { 0, 0, z, 0 },
				{ 0, 0, 0, 1 } });
	}

	public float[][] translationMatrix(float x, float y, float z) {
		return (new float[][] { { 1, 0, 0, x }, { 0, 1, 0, y }, { 0, 0, 1, z },
				{ 0, 0, 0, 1 } });
	}

	public float[] matrixProduct(float[][] m, float[] vector4) {
		float[] res = new float[vector4.length];
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[i].length; j++)
				res[i] += m[i][j] * vector4[j];
		return res;
	}

	public My3DPoint euclidean3DPoint(float[] a) {
		My3DPoint result = new My3DPoint(a[0] / a[3], a[1] / a[3], a[2] / a[3]);
		return result;
	}

	public My3DBox transformBox(My3DBox box, float[][] transformationMatrix) {
		My3DPoint[] p = new My3DPoint[box.p.length];
		for (int i = 0; i < box.p.length; i++) {
			p[i] = euclidean3DPoint(matrixProduct(transformationMatrix,
					homogeneous3DPoint(box.p[i])));
		}
		return new My3DBox(p);
	}

	public float[] homogeneous3DPoint(My3DPoint p) {
		float[] result = { p.x, p.y, p.z, 1 };
		return result;
	}
}
