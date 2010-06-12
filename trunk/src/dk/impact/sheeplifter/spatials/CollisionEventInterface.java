package dk.impact.sheeplifter.spatials;

import com.jme.scene.Spatial;

public interface CollisionEventInterface {
	boolean spatialsCollided(Spatial source, Spatial target);
}
