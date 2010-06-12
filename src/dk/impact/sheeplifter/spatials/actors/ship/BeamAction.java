package dk.impact.sheeplifter.spatials.actors.ship;

import com.jme.input.action.KeyInputAction;
import com.jme.input.action.InputActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Jeppe Schmidt
 * Date: 18-01-2009
 * Time: 12:19:13
 * To change this template use File | Settings | File Templates.
 */
public class BeamAction extends KeyInputAction{

    private Spaceship spaceship;

    public BeamAction(Spaceship spaceship){
        this.spaceship = spaceship;
    }

    public void performAction(InputActionEvent evt){
        if(evt.getTriggerPressed()){
            spaceship.startAbductorBeam();
        } else{
            spaceship.stopAbductorBeam();
        }
    }
}
