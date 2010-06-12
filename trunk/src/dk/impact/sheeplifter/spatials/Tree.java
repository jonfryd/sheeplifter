package dk.impact.sheeplifter.spatials;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.system.DisplaySystem;

public class Tree extends Plant {
	private static final long serialVersionUID = 1L;
	
    public Tree(String name, String filename, DisplaySystem display) {
    	super(name, filename, display);
    }
    
	protected void loadModel() {
		super.loadModel();

		getModel().setLocalScale(2f);

		//rotate
        Quaternion q = new Quaternion();
        q.fromAngles(-FastMath.PI * 0.5f, 0, 0);
        getModel().setLocalRotation(q);
        
        updateGeometricState(0, true);
	}
}
