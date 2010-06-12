package dk.impact.sheeplifter.spatials.actors.ship;

import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;

/**
 * Input Handler for the Flag Rush game. This controls a supplied spatial
 * allowing us to move it forward, backward and rotate it left and right.
 *
 * @author Mark Powell
 *
 */
public class SpaceshipHandler extends InputHandler{
    //the vehicle we are going to control
    private Spaceship spaceship;
    //the default action
    private DriftAction drift;

    public void update(float time) {
        if ( isEnabled() ) {
            super.update(time);
        }

        //we always want to allow friction to control the drift
        drift.performAction(event);
        spaceship.update(time);
    }

    /**
     * Supply the node to control and the api that will handle input creation.
     * @param spaceship the node we wish to move
     * @param api the library that will handle creation of the input.
     */
    public SpaceshipHandler(Spaceship spaceship, String api) {
        this.spaceship = spaceship;
        setKeyBindings(api);
        setActions(spaceship);

    }

    /**
     * creates the keyboard object, allowing us to obtain the values of a keyboard as keys are
     * pressed. It then sets the actions to be triggered based on if certain keys are pressed (WSAD).
     * @param api the library that will handle creation of the input.
     */
    private void setKeyBindings(String api) {
        KeyBindingManager keyboard = KeyBindingManager.getKeyBindingManager();

        keyboard.set("forward", KeyInput.KEY_W);
        keyboard.set("backward", KeyInput.KEY_S);
        keyboard.set("turnRight", KeyInput.KEY_D);
        keyboard.set("turnLeft", KeyInput.KEY_A);
    }

    /**
     * assigns action classes to triggers. These actions handle moving the node forward, backward and
     * rotating it. It also creates an action for drifting that is not assigned to key trigger, this
     * action will occur each frame.
     * @param node the node to control.
     */
    private void setActions(Spaceship node) {
        ForwardAndBackwardAction forward = new ForwardAndBackwardAction(node, ForwardAndBackwardAction.FORWARD);
        addAction(forward, "forward", true);
        ForwardAndBackwardAction backward = new ForwardAndBackwardAction(node, ForwardAndBackwardAction.BACKWARD);
        addAction(backward, "backward", true);
        ShipRotationAction rotateLeft = new ShipRotationAction(node, ShipRotationAction.LEFT);
        addAction(rotateLeft, "turnLeft", true);
        ShipRotationAction rotateRight = new ShipRotationAction(node, ShipRotationAction.RIGHT);
        addAction(rotateRight, "turnRight", true);

        BeamAction beamAction = new BeamAction(spaceship); // Note, that we use this alternative syntax in order to get only key press and key release events.
        addAction(beamAction, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_SPACE, InputHandler.AXIS_NONE, false);        

        //not triggered by keyboard
        drift = new DriftAction(node);
    }
}
