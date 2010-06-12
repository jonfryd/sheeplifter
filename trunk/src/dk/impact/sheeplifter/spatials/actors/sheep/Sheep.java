package dk.impact.sheeplifter.spatials.actors.sheep;

import com.jme.animation.SpatialTransformer;
import com.jme.bounding.BoundingBox;
import com.jme.scene.Controller;
import com.jme.scene.Spatial;
import dk.impact.sheeplifter.audio.GameAudioSystem;
import dk.impact.sheeplifter.spatials.CollisionManager;
import dk.impact.sheeplifter.spatials.World;
import dk.impact.sheeplifter.spatials.actors.Abductable;

public class Sheep implements Abductable{
	private SheepBody	        body;
	private SheepMind	        mind;
	
	private boolean				stonedMode;

	private boolean             abductionCommenced;
    private boolean             abductionCompleted;

    private WoolParticleNode    woolParticleNode;

    private CollisionManager	collisionManager;

    public Sheep(String name, GameAudioSystem gameAudioSystem, World world) {
		body = new SheepBody(name, gameAudioSystem, world);
		mind = new SheepMind(body, world);
    }

	public Sheep(String name, GameAudioSystem gameAudioSystem, World world, Spatial spaceShipNode, CollisionManager collisionManager) {
		this(name, gameAudioSystem, world);
		this.collisionManager = collisionManager;
        woolParticleNode = new WoolParticleNode(spaceShipNode, -4);
    }

    public void update(float timeDelta){
        if(!abductionCommenced && !abductionCompleted){
        	if (stonedMode) {
                body.moveToTopSurface();        		
        	}
        	else {
                mind.update();
                body.update(timeDelta);        		
        	}
        }
    }

	public SheepBody getBody() {
		return body;
	}

	public SheepMind getMind() {
		return mind;
	}

	public String toString() {
		return body.toString() + " " + mind.toString();
	}

    public boolean mayAbduct(){
        return !body.hasDrowned();
    }

    public void abductionCommenced(){
        abductionCommenced = true;

        if (collisionManager != null) {
            collisionManager.removeChecksInvolving(body);
        }

        body.setBeingAbducted(true);

        if (woolParticleNode != null) {
            body.attachChild(woolParticleNode);
            woolParticleNode.setCullHint(Spatial.CullHint.Never);
        }

        // Shift to the abduction animation sequence.
        SpatialTransformer st = (SpatialTransformer) body.getModel().getController(0);
        st.setNewAnimationTimes(400, 600);
        st.setRepeatType(Controller.RT_CYCLE);
        st.setSpeed(1000f / 30.0f);
    }

    public void abductionCompleted(){
        abductionCompleted = true;

        if (woolParticleNode != null) {
            woolParticleNode.setCullHint(Spatial.CullHint.Always);
        }
    }

    public void abductableStacked(){
        // Shift back to first animation sequence.
        SpatialTransformer st = (SpatialTransformer) body.getModel().getController(0);
        st.setNewAnimationTimes(0, 199);
        st.setRepeatType(Controller.RT_CYCLE);
        st.setSpeed(1000f / 30.0f);

        SheepOnShipController controller = new SheepOnShipController(body);
        body.addController(controller);

    }

    public Spatial getAbductionSpatial(){
        return body;
    }

    public float getWorldBoundsXExtend(){
        return ((BoundingBox) body.getWorldBound()).xExtent;
    }

    public float getWorldBoundsYExtend(){
        return ((BoundingBox) body.getWorldBound()).yExtent;
    }

    public float getWorldBoundsZExtend(){
        return ((BoundingBox) body.getWorldBound()).zExtent;
    }

    public boolean hasAbductionCommenced() {
    	return abductionCommenced;
    }

    public boolean hasAbductionCompleted() {
    	return abductionCompleted;
    }

    /**
     * Mode used during intro sequence
     */
    public void enableStonedMode() {
    	stonedMode = true; 
    	
        body.playNoLegMovementAnimation();
    }
    
    public void disableStonedMode() {
    	stonedMode = false;
    }
}
    
    
