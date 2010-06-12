package dk.impact.sheeplifter.spatials.actors.sheep;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import com.jme.util.Timer;
import com.jmex.effects.water.ImprovedNoise;

/**
 * @author Jeppe Schmidt
 * @version $Revision$
 */
public class SheepOnShipController extends Controller{

    public static final float SIN_RANGE_START = 0;

    public static final float SIN_RANGE_END = FastMath.PI / 2 + FastMath.PI / 5;

    private States state;

    private float popAngle;

    private Quaternion rollQuaternion = new Quaternion();

    private Quaternion origQuaternion;
    
    private float originalEulerAngles[];
    private float newEulerAngles[];

    private Vector3f rollVecDirection = new Vector3f();

    private Quaternion tempResult = new Quaternion();

    private enum States{
        pop,
        rolling
    }

    private Spatial spatial;

    public SheepOnShipController(Spatial spatial){
        this.spatial = spatial;
        state = States.pop;
        origQuaternion = spatial.getLocalRotation();
        originalEulerAngles = origQuaternion.toAngles(null);
        newEulerAngles = new float[3];
    }

    public void update(float time){
        switch(state){
            case pop:
                popAngle += 4.30 * time;
                if(popAngle >= SIN_RANGE_END){
                    popAngle = SIN_RANGE_END;
                    state = States.rolling;
                }
                float scale = FastMath.sin(popAngle);
                spatial.setLocalScale(scale * 1.3f);
                break;

            case rolling:
                float currentTime = Timer.getTimer().getTimeInSeconds();

                newEulerAngles[0] = (float) (originalEulerAngles[0] + ImprovedNoise.noise(1 * currentTime, 0, 0) * 0.07f);
                newEulerAngles[1] = (float) (originalEulerAngles[1] + ImprovedNoise.noise(0, 1 * currentTime, 0) * 0.07f);
                newEulerAngles[2] = (float) (originalEulerAngles[2] + ImprovedNoise.noise(0, 0, 1 * currentTime) * 0.07f);
                
                tempResult.fromAngles(newEulerAngles);
                
                spatial.setLocalRotation(tempResult);
                break;
        }
    }
}
