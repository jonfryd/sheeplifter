package dk.impact.sheeplifter.spatials.actors.ship;

import com.jme.input.action.KeyInputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.math.FastMath;
import jmetest.flagrushtut.lesson8.Vehicle;

/**
 * VehicleRotateLeftAction turns the vehicle to the left (while
 * traveling forward).
 * @author Mark Powell
 *
 */
public class ShipRotationAction extends KeyInputAction{
    public static final int RIGHT = 0;
    public static final int LEFT = 1;
    //temporary variables to handle rotation
    private static final Matrix3f incr = new Matrix3f();
    private static final Matrix3f tempMa = new Matrix3f();
    private static final Matrix3f tempMb = new Matrix3f();

    //we are using +Y as our up
    private Vector3f upAxis = new Vector3f(0,1,0);
    //the node to manipulate
    private Spaceship spaceship;
    private int direction;
    private int modifier = 1;

    /**
     * create a new action with the vehicle to turn.
     * @param spaceship the vehicle to turn
     */
    public ShipRotationAction(Spaceship spaceship, int direction) {
        this.spaceship = spaceship;
        this.direction = direction;
    }

    /**
     * turn the vehicle by its turning speed. If the vehicle is traveling
     * backwards, swap direction.
     */
    public void performAction(InputActionEvent evt) {

        //affect the direction
        if(direction == LEFT) {
            modifier = 1;
        } else if(direction == RIGHT) {
            modifier = -1;
        }

        //we want to turn differently depending on which direction we are traveling in.
        if(spaceship.getVelocity() < 0) {
            incr.fromAngleNormalAxis(-modifier * spaceship.getTurnSpeed() * evt.getTime(), upAxis);
        } else {
            incr.fromAngleNormalAxis(modifier * spaceship.getTurnSpeed() * evt.getTime(), upAxis);
        }

        spaceship.getLocalRotation().fromRotationMatrix(
                        incr.mult(spaceship.getLocalRotation().toRotationMatrix(tempMa),
                        tempMb));
        spaceship.getLocalRotation().normalize();


        if(spaceship.getVelocity() < -FastMath.FLT_EPSILON || spaceship.getVelocity() > FastMath.FLT_EPSILON) {
            // Tilt the ship, if it is going fast enough.
            spaceship.setRotateOn(modifier);
        }


    }
}