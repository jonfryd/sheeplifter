package dk.impact.sheeplifter.spatials.actors.ship;

import com.jme.input.action.KeyInputAction;
import com.jme.input.action.InputActionEvent;
import jmetest.flagrushtut.lesson8.Vehicle;

/**
 * AccelerateAction defines the action that occurs when the key is pressed to
 * speed the Vehicle up. It obtains the velocity of the vehicle and
 * translates the vehicle by this value.
 * @author Mark Powell
 *
 */
public class ForwardAndBackwardAction extends KeyInputAction{
    public static final int FORWARD = 0;
    public static final int BACKWARD = 1;
    //the node to manipulate
    private Spaceship node;
    private int direction;

    /**
     * The vehicle to accelerate is supplied during construction.
     * @param node the vehicle to speed up.
     * @param direction Constant either FORWARD or BACKWARD
     */
    public ForwardAndBackwardAction(Spaceship node, int direction) {
        this.node = node;
        this.direction = direction;
    }

    /**
     * the action calls the vehicle's accelerate or brake command which adjusts its velocity.
     */
    public void performAction(InputActionEvent evt) {
        if(direction == FORWARD) {
            node.accelerate(evt.getTime());
        } else if(direction == BACKWARD){
            node.brake(evt.getTime());
        }
    }
}