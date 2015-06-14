package cs211.imageprocessing;

import processing.core.PVector;

public class RotationFix implements RotationProvider {	
	private RotationProvider source;	
	private PVector rot = null;
	
	public RotationFix(RotationProvider source) {
		this.source = source;
	}
	
	static private float PI = (float)Math.PI;
	
	private float fix(float oldValue, float newValue) {
		oldValue += 2 * PI;
		newValue += 2 * PI;		
		while (newValue - oldValue >= PI/2) newValue -= PI/2;
		while (oldValue - newValue >= PI/2) newValue += PI/2;
		return newValue - 2*PI;
	}

	@Override
	public PVector getRotation() {
		PVector oldRot = rot;
		rot = source.getRotation();		
		if (oldRot == null)
			return rot;		
		rot = new PVector(fix(oldRot.x, rot.x), fix(oldRot.y, rot.y), fix(oldRot.z, rot.z));		
		return rot;
	}
}