package dk.impact.sheeplifter.spatials.actors;

import com.jme.math.Vector3f;
import com.jme.scene.Spatial;

/**
 *
 */
public interface Abductable{

    public boolean mayAbduct();

    public void abductionCommenced();

    public void abductionCompleted();

    public void abductableStacked();

    public Spatial getAbductionSpatial();

    public float getWorldBoundsXExtend();

    public float getWorldBoundsYExtend();

    public float getWorldBoundsZExtend();

}
