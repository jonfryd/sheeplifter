package dk.impact.sheeplifter.spatials.actors.sheep;

import com.jme.animation.SpatialTransformer;
import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.util.CloneImportExport;
import com.jme.util.Timer;
import com.jmex.effects.water.ImprovedNoise;

import dk.impact.sheeplifter.audio.GameAudioSystem;
import dk.impact.sheeplifter.spatials.CollisionEventInterface;
import dk.impact.sheeplifter.spatials.World;
import dk.impact.sheeplifter.util.ModelLoader;

public class SheepBody extends Node implements CollisionEventInterface {
	private static final long serialVersionUID = 1L;
    
	private static final float EPSILON = 0.1f;
	
	private static final float TIME_CAN_FLOAT = 30;
		
    private static final float MIN_LEGGED_SPEED = 17;
    private static final float MAX_LEGGED_SPEED = 25;
    
	public enum Action {
		Undefined,
		Grassing,
		Walking,
		Running,
		Swimming,
		Drowning
	}
	
	public enum Presence {
		Undefined,
		OnLand,
		InAir,
		InWater
	}
	
	public enum Turn {
		No,
		Right,
		Left
	}
	
	private enum Animation {
		Undefined,
		NoLegMovement,
		WalkJump
	}
	
    private Matrix3f 			incr = new Matrix3f();
    private Vector3f 			tempVa = new Vector3f();
    private Matrix3f 			tempMa = new Matrix3f();
    private Matrix3f 			tempMb = new Matrix3f();
    
	private Spatial 			model;

	private Presence			currentPresence;
	private Action				currentAction;
	private Presence			previousPresence;
	private Action				previousAction;
	
	private boolean				hadGroundContact;
	private boolean				hadGroundContactPreviously;
	private float				timeHadGroundContact;
	private float				timeLostGroundContact;
	private float				timeStartedFloating;
	
	private float				ratioBelowWater;
	
	private Turn				turning;
	private boolean				motion;
	
	private float				turnAmount;
	private float				movementAcceleration;

	private GameAudioSystem		gameAudioSystem;
	
	private World				world;
    private boolean             isBeingAbducted;
    private boolean				hasDrowned;
	private boolean 			wasDrowningPreviously;
    
    private Vector3f 			velocity = new Vector3f();
    private Vector3f 			interpolatedVelocity = new Vector3f();	

    private float 				gravityStrength = 40f;
    private Vector3f 			interpolatedGravityForce = new Vector3f();
    
    private SpatialTransformer 	spatialTransformer;
    private Animation			currentAnimation;
    
    private float				nextBaaSound;
    
    private float				bestLeggedSpeed;
    
    private Node				cameraLookAtNode;

    
    private static CloneImportExport	cloneImportExport;
    
	public SheepBody(String name, GameAudioSystem gameAudioSystem, World world) {
    	super(name);
    	this.gameAudioSystem = gameAudioSystem;
    	this.world = world;
        this.ratioBelowWater = 0;
        currentAnimation = Animation.Undefined;
    	hadGroundContact = true;
    	motion = false;
    	bestLeggedSpeed = (float) Math.random() * (MAX_LEGGED_SPEED - MIN_LEGGED_SPEED) + MIN_LEGGED_SPEED;
    	
    	setPreviousPresence(Presence.Undefined);
    	setCurrentPresence(Presence.Undefined);

    	setPreviousAction(Action.Undefined);
    	setCurrentAction(Action.Undefined);
    	
    	build();
    	
    	setCurrentAction(Action.Walking);
    	//turnLeft((float) Math.random());
    	stopTurning();
    	//goStraight();
    	setMovementAcceleration(30);
    }
    
    protected void build() {
    	loadModel();
    	attachChild(model);
    	
    	// add bounding box
        setModelBound(new BoundingBox());
        updateModelBound();
        
        updateGeometricState(0, true);
        
        cameraLookAtNode = new Node();
        cameraLookAtNode.setLocalTranslation(0, ((BoundingBox) getWorldBound()).yExtent, ((BoundingBox) getWorldBound()).zExtent);
        attachChild(cameraLookAtNode);
    }
    
	protected void loadModel() {
		if (cloneImportExport == null) {
			cloneImportExport = new CloneImportExport();
			cloneImportExport.saveClone(ModelLoader.loadJMEModel("res/geometry/new-sheep.jme"));
		}
		
		model = (Spatial) cloneImportExport.loadClone();
		
		//scale it to be MUCH smaller than it is originally
        model.setLocalScale(0.15f);
        
        //rotate
        Quaternion q = new Quaternion();
        q.fromAngles(-FastMath.PI * 0.5f, FastMath.PI * 0.5f, 0);
        model.setLocalRotation(q);    
        
        //reset animation speed and enable cycling
        spatialTransformer = (SpatialTransformer) model.getController(0);
        spatialTransformer.setRepeatType(Controller.RT_CYCLE);
        spatialTransformer.setSpeed(1000f / (30.0f * (float) (Math.random() * 0.06f + 0.97f))); // add slight variantion in animation speed to avoid synchronous jumping

        //default walk
        playWalkJumpAnimation();
	}
	
    public void update(float timeDelta){
    	float	currentTime = Timer.getTimer().getTimeInSeconds();
    	
    	hadGroundContactPreviously = hadGroundContact;
    	wasDrowningPreviously = isDrowning();
    	
    	// update forces and influences

    	// Add gravity force.        
        interpolatedGravityForce.set(0, -1, 0).multLocal(gravityStrength * timeDelta);
        velocity.addLocal(interpolatedGravityForce);    	

        // update presence (in air, in water or on land?)
    	updatePresence();
    	
        // Turn sheep by adjusting rotation
    	switch(getTurning()) {
	    	case Left:
	    	case Right:
	            getLocalRotation().getRotationColumn(1, tempVa);
	            tempVa.normalizeLocal();
	            incr.fromAngleNormalAxis((getTurning() == Turn.Left ? 1 : -1) * turnAmount * timeDelta * (1 - ratioBelowWater), tempVa);
	            getLocalRotation().fromRotationMatrix(incr.mult(getLocalRotation().toRotationMatrix(tempMa), tempMb));
	            getLocalRotation().normalize();
	    		break;
    	}
    	
        // Turn and move if running, swimming or walking
    	switch(getCurrentAction()) {
	    	case Running:
	    	case Swimming:
	    	case Walking:
	        	// apply acceleration in the direction we are heading
	            getLocalRotation().getRotationColumn(0, tempVa);
	            tempVa.normalizeLocal();
	            
	            Vector3f temp = tempVa.mult(movementAcceleration * timeDelta  * (1 - ratioBelowWater));
	        	velocity.addLocal(temp);
	        	
	        	Vector3f horizontalVelocity = velocity.mult(new Vector3f(1, 0, 1));
	        	float horizontalSpeed = horizontalVelocity.length();

	        	if (horizontalSpeed > (bestLeggedSpeed * (1 - ratioBelowWater))) {
	        		horizontalVelocity.multLocal((bestLeggedSpeed * (1 - ratioBelowWater)) / horizontalSpeed);
	        	}
	        	
	        	if (horizontalVelocity.dot(tempVa) > 0 || motion) {
		        	velocity.setX(horizontalVelocity.getX());
		        	velocity.setZ(horizontalVelocity.getZ());	        		
	        	}
	        	else {
		        	velocity.setX(0);
		        	velocity.setZ(0);	        			        		
	        	}
	        	
	    		break;
    	}
    	
    	// add velocity
    	interpolatedVelocity.set(velocity).multLocal(timeDelta);
        getLocalTranslation().addLocal(interpolatedVelocity);

        updateGeometricState(0, true);

        // Retrieve surface point at current sheep (x, z) position
        World.SurfacePoint surfacePoint = world.getSurfacePointAt(getWorldTranslation());

    	hadGroundContact = false;
    	
        if (surfacePoint.getType() == World.SurfaceType.IslandTerrain) {
            if ((surfacePoint.getHeight() > getWorldTranslation().getY())) {
            	hadGroundContact = true;
            	
            	getLocalTranslation().setY(surfacePoint.getHeight());
            }        	
        }
        else {
        	// Ocean
        	float	islandHeight = world.getIslandHeight(getWorldTranslation());
        	
        	if (World.isHeightValid(islandHeight)) {
                if ((islandHeight > getWorldTranslation().getY())) {
                	hadGroundContact = true;

                	getLocalTranslation().setY(islandHeight);
                }        	        		
        	}
            
        	// Respect water surface when we're not drowning
            if ((getCurrentPresence() == Presence.InWater) && (getCurrentAction() != Action.Drowning)) {
            	if (ratioBelowWater > 0.5f) {
        			float sheepYExtent = ((BoundingBox) getWorldBound()).yExtent;
        			
            		getLocalTranslation().setY(surfacePoint.getHeight() - sheepYExtent);

            		velocity.setY(0);
            	}
            }
        }        
        
        if (hadGroundContact) {
    		velocity.setY(0);
        }

        //simulate breathing by scale a tiny bit
        setLocalScale(1f + (float) FastMath.sin(currentTime * 4f) * 0.05f);
        
        // adjust orientation according to surface
        if (getCurrentPresence() != Presence.InAir) {
            //get the normal of the terrain at our current location. We then apply it to the up vector
            //of the sheep.
            Vector3f normal = hadGroundContact ? world.getIslandSurfaceNormal(getWorldTranslation().getX(), getWorldTranslation().getZ(), null) : surfacePoint.getNormal();

            //simulate breathing by subtitle tilting
            normal.setX(normal.getX() + (float) ImprovedNoise.noise(0.5f * currentTime, 0, 0) * 0.1f);
            normal.setZ(normal.getZ() + (float) ImprovedNoise.noise(0, 0, 0.5f * currentTime) * 0.1f);
            normal.normalizeLocal();
            
            if(normal != null) {
            	// possibly interpolate to avoid jerkyness
            	Quaternion q = getLocalRotation().clone();
            	rotateUpTo(normal);

            	float q0Amount = FastMath.clamp(1f - timeDelta * 1.0f, 0.9f, 1);
            	
            	getLocalRotation().slerp(q, q0Amount);
            	getLocalRotation().normalize();
            	updateGeometricState(0, true);
            }
        }
        
        if (!hadGroundContactPreviously && hadGroundContact) {
        	timeHadGroundContact = currentTime;  
        }
        if (hadGroundContactPreviously && !hadGroundContact) {
        	timeLostGroundContact = currentTime;      
        }
        
        if (isDrowning()) {
        	playNoLegMovementAnimation();       	
        }
        else {
        	playWalkJumpAnimation();
        }

        if (!wasDrowningPreviously && isDrowning()) {
        	timeStartedFloating = currentTime;
        }
        
        if (isDrowning() && (getTimeUntilDrowning() <= 0)) {
        	setCurrentAction(Action.Drowning);

        	if (!hasDrowned) {
            	hasDrowned = isDrowning() && (ratioBelowWater > 0.99);        	
            }
        }
        
        controlSound(currentTime);
    }
    
    private void controlSound(float currentTime) {
        if (nextBaaSound == 0) {
        	nextBaaSound = currentTime + (float) Math.random() * 4f + 3f;
        	return;
        }

        if (currentTime > nextBaaSound) {
        	if (getCurrentPresence() != Presence.InWater) {
            	nextBaaSound = currentTime + (float) Math.random() * 10f + 5f;        		        		
        	}
        	else {
            	nextBaaSound = currentTime + (float) Math.random() * 3f + 3f;        		
        	}

        	int sound = (int) (Math.random() * 13 + 1);
        	
            gameAudioSystem.addPositionalTrack("res/sound/maeh" + sound + ".wav", this, 0.5f);
        }
    }
    
    protected void playNoLegMovementAnimation() {
    	if (currentAnimation != Animation.NoLegMovement) {
    		spatialTransformer.setNewAnimationTimes(0, 200); // zero leg movement + no jump
            currentAnimation = Animation.NoLegMovement;
    	}
    }
    
    protected void playWalkJumpAnimation() {
    	if (currentAnimation != Animation.WalkJump) {
            spatialTransformer.setNewAnimationTimes(200, 400); // walk + jump anim
            currentAnimation = Animation.WalkJump;
    	}
    }
    
    private void updatePresence() {
    	Vector3f 			position = getWorldTranslation();
        World.SurfacePoint 	surfacePoint = world.getSurfacePointAt(position);
    	float				sheepFeetHeight = position.getY();
		float 				sheepYExtent = ((BoundingBox) getWorldBound()).yExtent;

    	ratioBelowWater = 0;
    	
    	// Are we above ground?
    	if ((surfacePoint.getHeight() +  EPSILON) < sheepFeetHeight) {
    		setCurrentPresence(Presence.InAir);
    		return;
    	}
 
    	// In water?
    	if (((surfacePoint.getHeight() - EPSILON) > sheepFeetHeight) || (surfacePoint.getType() == World.SurfaceType.Ocean)) {
    		setCurrentPresence(Presence.InWater);
    		
    		// floating? If, not, how close are we to floating?
    		ratioBelowWater = (surfacePoint.getHeight() - sheepFeetHeight) / (2 * sheepYExtent);
    		ratioBelowWater = FastMath.clamp(ratioBelowWater, 0, 1f);
    		
    		return;
    	}
    	
    	// OK, must we on land then
    	setCurrentPresence(Presence.OnLand);
    	return;
    }

	public Spatial getModel() {
		return model;
	}

	public Presence getCurrentPresence() {
		return currentPresence;
	}

	protected void setCurrentPresence(Presence presence) {
		this.currentPresence = presence;
	}
	
    public Action getCurrentAction() {
		return currentAction;
	}

	protected void setCurrentAction(Action currentAction) {
		this.currentAction = currentAction;
	}
	
	public Presence getPreviousPresence() {
		return previousPresence;
	}

	protected void setPreviousPresence(Presence previousPresence) {
		this.previousPresence = previousPresence;
	}

	public Action getPreviousAction() {
		return previousAction;
	}

	protected void setPreviousAction(Action previousAction) {
		this.previousAction = previousAction;
	}

	public Turn getTurning() {
		return turning;
	}

	public void setTurning(Turn turning) {
		this.turning = turning;
	}

	public float getTurnAmount() {
		return turnAmount;
	}

	public void setTurnAmount(float turnAmount) {
		this.turnAmount = turnAmount;
	}

	public float getMovementAcceleration() {
		return movementAcceleration;
	}

	public void setMovementAcceleration(float movementAcceleration) {
		this.movementAcceleration = movementAcceleration;
	}
	
	public void turnRight(float amount) {
		setTurning(Turn.Right);
		turnAmount = amount;
	}

	public void turnLeft(float amount) {
		setTurning(Turn.Left);
		turnAmount = amount;
	}
	
	public void stopTurning() {
		setTurning(Turn.No);
		turnAmount = 0;		
	}

	public void goForward() {
		setMovementAcceleration(3);
		motion = true;
	}

	public void stopMotion() {
		setMovementAcceleration(-3);
		motion = false;
	}	
	
	public float getHeading() {
		float angles[] = getLocalRotation().toAngles(null);
		
		return angles[1];
	}

    public boolean spatialsCollided(Spatial source, Spatial target){
        if((this == source) && (target instanceof SheepBody)){

            float sheepXExtent = ((BoundingBox) getWorldBound()).xExtent;
            float sheepZExtent = ((BoundingBox) getWorldBound()).zExtent;

            float sheepMaxExtent = (float) Math.sqrt(sheepXExtent * sheepXExtent + sheepZExtent * sheepZExtent);

            Vector3f center1 = getWorldBound().getCenter();
            Vector3f center2 = target.getWorldBound().getCenter();
            Vector3f direction = center1.subtract(center2);

            while(direction.length() < 0.1){
                direction.set((float) Math.random(), 0, (float) Math.random());
            }

            direction.addLocal((float) (Math.random() * 0.05f - 0.025f), 0f, (float) (Math.random() * 0.05f - 0.025f));

            float directionLength = Math.abs(sheepMaxExtent - direction.length() * 0.5f) * 0.04f;

            Vector3f displace1 = direction.mult(new Vector3f(directionLength * (float) (Math.random() * 0.5f + 0.5f), directionLength * (float) (Math.random() * 0.5f + 0.5f), directionLength * (float) (Math.random() * 0.5f + 0.5f)));
            Vector3f displace2 = direction.mult(new Vector3f(directionLength * (float) (Math.random() * 0.5f + 0.5f), directionLength * (float) (Math.random() * 0.5f + 0.5f), directionLength * (float) (Math.random() * 0.5f + 0.5f)));

            getLocalTranslation().addLocal(displace1);
            target.getLocalTranslation().subtractLocal(displace2);

            update(0);
            ((SheepBody) target).update(0);

            return true;
        }

        return false;
	}
	
	public String toString() {
		return getName() + ": " + currentPresence.toString() + " " + ratioBelowWater;
	}

    public void setBeingAbducted(boolean beingAbducted){
        isBeingAbducted = beingAbducted;
    }

    public boolean isBeingAbducted(){
        return isBeingAbducted;
    }
    
    public boolean isDrowning() {
    	return !hadGroundContact && getCurrentPresence() == Presence.InWater;
    }
    
    public float getTimeUntilDrowning() {
    	return FastMath.clamp(TIME_CAN_FLOAT - (Timer.getTimer().getTimeInSeconds() - timeStartedFloating), 0, TIME_CAN_FLOAT);
    }
    
    public boolean hasDrowned() {
    	return hasDrowned;
    }

	public Node getCameraLookAtNode() {
		return cameraLookAtNode;
	}
	
	protected void moveToTopSurface() {
        World.SurfacePoint surfacePoint = world.getSurfacePointAt(getWorldTranslation());
    	getLocalTranslation().setY(surfacePoint.getHeight());    
    	
        Vector3f normal = surfacePoint.getNormal();
    	rotateUpTo(normal);
	}
}
