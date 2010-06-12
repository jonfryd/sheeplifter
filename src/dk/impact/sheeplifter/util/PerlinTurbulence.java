package dk.impact.sheeplifter.util;

import com.jme.math.Vector3f;
import com.jmex.effects.water.ImprovedNoise;

public class PerlinTurbulence {
	public static float turbulence(Vector3f point, float loFreq, float hiFreq) {
		Vector3f	newPoint = new Vector3f(point);
		float 		t;
		
		newPoint.setX(newPoint.getX() + 123.456f);

		t = 0;
		for (float freq = loFreq; freq < hiFreq; freq *= 2f) {
			t += Math.abs(ImprovedNoise.noise(newPoint.getX(), newPoint.getY(), newPoint.getZ())) / freq;
			
			newPoint.multLocal(2f);
		}
		
		return t - 0.3f;
	}
}
