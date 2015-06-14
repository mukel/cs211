package cs211.imageprocessing;

import processing.core.PVector;

public class RotationSmoother implements RotationProvider {	
	private RotationProvider source;
	
	
	private PVector rot;
	
	public RotationSmoother(RotationProvider source) {
		this.source = source;
	}

	@Override
	public PVector getRotation() {
		PVector oldRot = rot;
		rot = source.getRotation();
		
		if (oldRot == null)
			return rot;
		
		rot = new PVector((oldRot.x * 3 + rot.x) / 4, (oldRot.y * 3 + rot.y) / 4, (oldRot.z * 3 + rot.z) / 4);
		return rot;
	}
}