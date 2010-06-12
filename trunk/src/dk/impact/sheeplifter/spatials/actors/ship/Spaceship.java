package dk.impact.sheeplifter.spatials.actors.ship;

import com.jme.animation.SpatialTransformer;
import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.system.GameSettings;
import com.jmex.audio.RangedAudioTracker;

import dk.impact.sheeplifter.audio.GameAudioSystem;
import dk.impact.sheeplifter.spatials.World;
import dk.impact.sheeplifter.spatials.actors.Abductable;
import dk.impact.sheeplifter.util.ModelLoader;

import java.util.ArrayList;
import java.util.logging.Logger;

public class Spaceship extends Node{

    private static final int MAX_STACK_COUNT = 5;

    private static final long serialVersionUID = 1L;

    public static final int TIME_IN_BEAM_BEFORE_ABDUCTION_STARTS = 500;

    private static final float TRACTOR_PULL_POWER = 16;

    private static final float ABDUCTABLE_BEAM_AREA_GROUND_RADIUS = 22;

    public static final float DISTANCE_TO_GROUND = 90;

    private static final Logger logger = Logger.getLogger(Spaceship.class.getName());

    private static final float LEAN_BUFFER = 0.2f;

    private Vector3f[] sheepStackLocations = new Vector3f[MAX_STACK_COUNT];
    private Quaternion[] sheepStackRotations = new Quaternion[MAX_STACK_COUNT];
    private int sheepStackCount = 0;
    private ArrayList<Abductable> stackedSheep;

    private float weight;
    private float velocity;
    private float acceleration;
    private float braking;
    private float turnSpeed;

    private float maxSpeed = 30;
    private float minSpeed = 10;

//  temporary vector for the rotation
    private static final Vector3f tempVa = new Vector3f();
    private int lean;
    private float leanAngle;
    private Vector3f leanAxis = new Vector3f(0,0,1);
    private Quaternion q = new Quaternion();

    private Spatial model;

    private Node modelNode;

    private Thruster rearThrusterRight;
    private Thruster rearThrusterLeft;

    private Vector3f abductableVelocity = new Vector3f();
    private Vector3f interpolatedAbductableVelocity = new Vector3f();
    private Vector2f tempDST = new Vector2f();

    private boolean stopBeamOnAbductionComplete;

    private AbductorBeam abductorBeam;

    private Node shipNode;

    private Vector3f tempWd = new Vector3f();
    private Vector2f tempSL = new Vector2f();
    private Vector2f tempAL = new Vector2f();    

    private Abductable currentAbductableInBeam;
    private long timeStampAbductableIntersBeam;
    private boolean abductionCommenced;
    private ArrayList<Abductable> abductables = new ArrayList<Abductable>();
    private Vector3f interpolatedUPAcceleration = new Vector3f();

    private GameSettings gameSettings;
    private GameAudioSystem gameAudioSystem;

    private World world;
    private float previousShipY;

    private SpaceshipHandler spaceshipHandler;

    private boolean accellerating;

    private float randomHoverPulseAngle = 0;
    private float randomHoverPulseDist = 0;

	private RangedAudioTracker thrusterSound;
	private RangedAudioTracker abductorBeamSound;
	
    public Spaceship() {
        super("Spaceship");
        build();
    }

    public Spaceship(GameSettings gameSettings, GameAudioSystem gameAudioSystem, World world) {
        super("Spaceship");
        this.gameSettings = gameSettings;
        this.gameAudioSystem = gameAudioSystem;
        this.world = world;
        build();
        generateSheepStackLocations(-37, 4.5f, -7);
    }

    protected void generateSheepStackLocations(float zDistFromShipCenter, float xDistance, float yDistance){
        float startY = 8.7f;
        float sheepWidth = 7;
        float sheepHeight = 10;

        // First deck translation.
        sheepStackLocations[0] = new Vector3f(0, startY + 13, zDistFromShipCenter);
        sheepStackLocations[1] = new Vector3f(-sheepWidth -xDistance - 10.5f, startY + 2, zDistFromShipCenter);
        sheepStackLocations[2] = new Vector3f(sheepWidth + xDistance + 10.5f, startY + 2, zDistFromShipCenter);

        // Second Deck translation.
        sheepStackLocations[3] = new Vector3f(-sheepWidth, startY + sheepHeight + yDistance, zDistFromShipCenter);
        sheepStackLocations[4] = new Vector3f(sheepWidth, startY + sheepHeight + yDistance, zDistFromShipCenter);

        // First deck rotations.
        float rndAngle = (FastMath.rand.nextFloat() * (FastMath.PI / 9)) - (FastMath.PI / 9) / 2;
        sheepStackRotations[0] = new Quaternion();
        sheepStackRotations[0].fromAngles(0f, -FastMath.PI / 2f + rndAngle, FastMath.PI);

        rndAngle = FastMath.rand.nextFloat() * (FastMath.PI / 8) ;
        sheepStackRotations[1] = new Quaternion();
        sheepStackRotations[1].fromAngles(-FastMath.PI / 2 + rndAngle, -FastMath.PI / 2f, FastMath.PI);

        rndAngle = FastMath.rand.nextFloat() * (FastMath.PI / 9) ;
        sheepStackRotations[2] = new Quaternion();        
        sheepStackRotations[2].fromAngles(FastMath.PI / 2 - rndAngle, -FastMath.PI / 2f, FastMath.PI);

        // Second deck rotations.
        sheepStackRotations[3] = new Quaternion();
        rndAngle = (FastMath.rand.nextFloat() * (FastMath.PI / 4)) - (FastMath.PI / 4) / 2;
        sheepStackRotations[3].fromAngles(0, FastMath.PI + rndAngle, 0);

        sheepStackRotations[4] = new Quaternion();
        rndAngle = (FastMath.rand.nextFloat() * (FastMath.PI / 4)) - (FastMath.PI / 4) / 2;
        sheepStackRotations[4].fromAngles(0f, -FastMath.PI / 2 + rndAngle, 0);

        stackedSheep = new ArrayList<Abductable>();
    }

    protected void build() {
    	float factor = 30;
        acceleration = 15 * factor;
        braking = 15 * factor;
        turnSpeed = 2.5f;
        weight = 35 * factor;
        maxSpeed = 35 * factor;
        minSpeed = 15 * factor;

        loadModel();
        
        // Create ship group which is independent from updrift particle system
        shipNode = new Node("ship group");
        shipNode.attachChild(modelNode);

        // Create rear thrusters.
        Quaternion q = new Quaternion();
        q.fromAngles(FastMath.PI, 0, 0);
        rearThrusterRight = new Thruster(this, 0.035f * 6, new Vector3f(0, 0, -1), new Vector3f(17.8f, 0.5f, -45));
        modelNode.attachChild(rearThrusterRight.getParticleMesh());
        rearThrusterLeft = new Thruster(this, 0.035f * 6, new Vector3f(0, 0, -1), new Vector3f(-17.8f, 0.5f, -45));
        modelNode.attachChild(rearThrusterLeft.getParticleMesh());
        
        // Create the abductor beam.
        abductorBeam = new AbductorBeam();
        abductorBeam.setLocalTranslation(0, -1f, 0);
        attachChild(abductorBeam);
        attachChild(shipNode);
        
        updateRenderState();

        // Input
        spaceshipHandler = new SpaceshipHandler(this, gameSettings.getRenderer());        
    }
    
	protected void loadModel() {
		modelNode = new Node("modelNode");

        model = ModelLoader.loadJMEModel("res/geometry/vehicle_wolf.jme");
        model.setModelBound(new BoundingBox());
        model.updateModelBound();

        SpatialTransformer st = (SpatialTransformer) model.getController(0);
        st.setSpeed(1000f / 60f);
        st.setRepeatType(Controller.RT_CYCLE);

        // scale it to be MUCH smaller than it is originally
        model.setLocalScale(.5f);
        
        // Fix initial orientation.
        Quaternion q = new Quaternion();
        q.fromAngles(-FastMath.PI * 0.5f, 0, 0);
        model.setLocalRotation(q);

        modelNode.attachChild(model);
    }

    public void update(float timeDelta){        
        this.localTranslation.addLocal(this.localRotation.getRotationColumn(2, tempVa)
                .multLocal(velocity * timeDelta));
        processLean(timeDelta);

        // Update thrusters
        // ====================================
        if(accellerating){
            if(!rearThrusterLeft.isBurning()){
                rearThrusterLeft.setBurning(true);
                rearThrusterRight.setBurning(true);
                
                gameAudioSystem.addPositionalTrack("res/sound/engine_thrust_01a.wav", shipNode, 1.0f);
                thrusterSound = gameAudioSystem.addPositionalLoopedTrack("res/sound/engineloop3.wav", shipNode, 1f);
            }
        } else {
            if(rearThrusterLeft.isBurning()){
                rearThrusterLeft.setBurning(false);
                rearThrusterRight.setBurning(false);
                
                gameAudioSystem.removePositionalLoopedTrack(thrusterSound);
            }
        }
        accellerating = false;
        rearThrusterRight.update(timeDelta);
        rearThrusterLeft.update(timeDelta);
        // --------------------------------------


        if(abductorBeam.isActive()){
            abductorBeam.update(timeDelta);            
        }
        updateRenderState();

        // Test if our beam is containing an abductable spatial (only one abductable can be beamed at a given time).
        if(abductorBeam.isActive() && !abductionCommenced){
            for(Abductable abductable : abductables){
                if(abductable.mayAbduct()){
                    if(isAbductableInsideBeam(abductable)){
                        // --> It's in the beam!
                        if(abductable != currentAbductableInBeam){
                            timeStampAbductableIntersBeam = System.currentTimeMillis();
                            currentAbductableInBeam = abductable;
                        }
                        if(System.currentTimeMillis() > timeStampAbductableIntersBeam + TIME_IN_BEAM_BEFORE_ABDUCTION_STARTS){
                            abductable.abductionCommenced();
                            abductionCommenced = true;

                            // Attach the abductable to the spaceships' node and translates it's location and rotation into local space of the ship.
                            Spatial spatial = abductable.getAbductionSpatial();
                            spatial.getWorldTranslation().subtract(this.getWorldTranslation(), tempWd);
                            Vector3f toAngleVec = new Vector3f();
                            float angle = this.getLocalRotation().toAngleAxis(toAngleVec);
                            Quaternion negRotQ = new Quaternion();
                            negRotQ.fromAngleAxis(-angle, toAngleVec);
                            negRotQ.multLocal(tempWd);
                            spatial.getLocalRotation().multLocal(negRotQ);
                            spatial.getParent().detachChild(spatial);
                            attachChild(spatial);
                            spatial.getLocalTranslation().set(tempWd);
                        }
                    } else {
                        // --> It's not in the beam!
                        if(abductable == currentAbductableInBeam){
                            currentAbductableInBeam = null;
                        }
                    }
                }
            }
        }

        // Handle when the abduction is commenced.
        if(abductionCommenced){
            assert currentAbductableInBeam != null;
            Spatial spatial = currentAbductableInBeam.getAbductionSpatial();
            interpolatedUPAcceleration.set(0, 1, 0).multLocal(TRACTOR_PULL_POWER * timeDelta);
            abductableVelocity.addLocal(interpolatedUPAcceleration);

            // Go towards center.
            spatial.getLocalTranslation().x *= (1 - timeDelta);
            spatial.getLocalTranslation().z *= (1 - timeDelta);

            // Accelerate the abductable until it reaches the ship.                        
            interpolatedAbductableVelocity.set(abductableVelocity).multLocal(timeDelta);
            spatial.getLocalTranslation().addLocal(interpolatedAbductableVelocity);

            if(spatial.getLocalTranslation().y >= -14){
                gameAudioSystem.addPositionalTrack("res/sound/sheep-pop.wav", shipNode, 0.38f);
            	
                currentAbductableInBeam.abductionCompleted();
                abductables.remove(currentAbductableInBeam);
                abductableVelocity.zero();
                stackSheep(currentAbductableInBeam);                
                currentAbductableInBeam = null;
                abductionCommenced = false;
                
                if(stopBeamOnAbductionComplete){
                	gameAudioSystem.removePositionalLoopedTrack(abductorBeamSound);
                    gameAudioSystem.addPositionalTrack("res/sound/beam-end2.wav", abductorBeam, 0.38f);
                    abductorBeam.setActive(false);
                }
                stopBeamOnAbductionComplete = false;
                detachChild(spatial);
            }
        }


        // Make sure that the ship keeps distance to the ground.
        if (world != null) {
            World.SurfacePoint flightPoint  = world.getSurfacePointAt(getWorldTranslation(), true); // ignores volcano shaft
            World.SurfacePoint surfacePoint = world.getSurfacePointAt(getWorldTranslation());
            float shipYExtent = ((BoundingBox) model.getWorldBound()).yExtent;
            float groundY = flightPoint.getHeight();
            float newShipY = groundY + shipYExtent + DISTANCE_TO_GROUND;

            if(!isMoving()){
                // Add some random movements when hovering with no velocity.
                randomHoverPulseAngle += 3 * timeDelta;
                newShipY += FastMath.sin(randomHoverPulseAngle) * 2;                 
            } else {
                randomHoverPulseAngle = 0;
            }

            // Set new Y location.
             float filteredShipY;
             //if(surfacePoint.getType() == World.SurfaceType.IslandTerrain){
                  filteredShipY = previousShipY * 0.95f + newShipY * 0.05f;
                  
                  if (surfacePoint.getHeight() > (filteredShipY - shipYExtent)) {
                	  filteredShipY = surfacePoint.getHeight() + shipYExtent;
                  }
                  
                  getLocalTranslation().setY(filteredShipY);
                  previousShipY = filteredShipY;
             //}

            
            /*
            if(surfacePoint.getType() == World.SurfaceType.IslandTerrain){
                Quaternion q = getLocalRotation().clone();
                Vector3f normal = surfacePoint.getNormal();
                rotateUpTo(normal);

                // possibly interpolate to avoid jerkyness
                float q0Amount = FastMath.clamp(1f - timeDelta * 1.0f, 0.9f, 1);
                getLocalRotation().slerp(q, q0Amount);
                getLocalRotation().normalize();
            }
            */
        }
    }

    private boolean isAbductableInsideBeam(Abductable abductable){
        Spatial spatial = abductable.getAbductionSpatial();

        // Check ZX plane distance.
        tempSL.set(getWorldTranslation().x, getWorldTranslation().z);
        tempAL.set(spatial.getWorldTranslation().x, spatial.getWorldTranslation().z);
        tempAL.subtract(tempSL, tempDST);
        float zxPlaneDist = tempDST.length();
        return zxPlaneDist < ABDUCTABLE_BEAM_AREA_GROUND_RADIUS;
    }

    /**
     * processlean will adjust the angle of the ship model based on
     * a lean factor. We angle the ship rather than the Vehicle, as the
     * Vehicle is worried about position about the terrain.
     * @param time the time between frames
     */
    private void processLean(float time) {
        //check if we are leaning at all
        if(lean != 0) {
            if(lean == -1 && leanAngle < 0) {
                leanAngle += -lean * 4 * time;
            } else if(lean == 1 && leanAngle > 0) {
                leanAngle += -lean * 4 * time;
            } else {
                leanAngle += -lean * 2 * time;
            }

            //max lean is 1 and -1 (is based on the velocity)
            float leanMax = Math.min(Math.abs(velocity) / 600f, 1);

            if(leanAngle > leanMax) {
                leanAngle = leanMax;
            } else if(leanAngle < -leanMax) {
                leanAngle = -leanMax;
            }
        } else { //we are not leaning, so right ourself back up.
            if(leanAngle < LEAN_BUFFER && leanAngle > -LEAN_BUFFER) {
                leanAngle = 0;
            }
            else if(leanAngle < -FastMath.FLT_EPSILON) {
                leanAngle += time * 4;
            } else if(leanAngle > FastMath.FLT_EPSILON) {
                leanAngle -= time * 4;
            } else {
                leanAngle = 0;
            }
        }

        q.fromAngleAxis(leanAngle, leanAxis);
        modelNode.getLocalRotation().set(q);

        lean = 0;
    }

    /**
     * brake adjusts the velocity of the vehicle based on the braking speed. If the
     * velocity reaches 0, braking will put the vehicle in reverse up to the minimum
     * speed.
     * @param time the time between frames.
     */
    public void brake(float time) {
        velocity -= time * braking;
        if(velocity < -minSpeed) {
            velocity = -minSpeed;
        }

        if(accellerating){
            stopRearThruster();
            accellerating = false;
        }
    }

    /**
     * accelerate adjusts the velocity of the vehicle based on the acceleration. The velocity
     * will continue to raise until maxSpeed is reached, at which point it will stop.
     * @param time the time between frames.
     */
    public void accelerate(float time) {
        velocity += time * acceleration;
        if(velocity > maxSpeed) {
            velocity = maxSpeed;
        }

        accellerating = true;
    }

    /**
     * drift calculates what happens when the vehicle is neither braking or accelerating.
     * The vehicle will slow down based on its weight.
     * @param time the time between frames.
     */
    public void drift(float time) {
        if(velocity < -FastMath.FLT_EPSILON) {
            velocity += ((weight/5) * time);
            //we are drifting to a stop, so we shouldn't go
            //above 0
            if(velocity > 0) {
                velocity = 0;
            }
        } else if(velocity > FastMath.FLT_EPSILON){
            velocity -= ((weight/5) * time);
            //we are drifting to a stop, so we shouldn't go
            //below 0
            if(velocity < 0) {
                velocity = 0;
            }
        }
    }

    /**
     * Convience method that determines if the vehicle is moving or not. This is
     * given if the velocity is approximately zero, taking float point rounding
     * errors into account.
     * @return true if the vehicle is moving, false otherwise.
     */
    public boolean isMoving() {
        return velocity > FastMath.FLT_EPSILON
                || velocity < -FastMath.FLT_EPSILON;
    }

    public void setRotateOn(int modifier) {
        lean = modifier;
    }

    @Override
    public void draw(Renderer r) {
		rearThrusterRight.preDrawUpdate();
        rearThrusterLeft.preDrawUpdate();
        super.draw(r);
    }
    
    public void burnRearThruster(){
        rearThrusterRight.setBurning(true);
        rearThrusterLeft.setBurning(true);
    }

    public void stopRearThruster(){
        rearThrusterRight.setBurning(false);
        rearThrusterLeft.setBurning(false);
    }
    
    public void startAbductorBeam(){
        if(!abductionCommenced){
        	if(sheepStackCount < MAX_STACK_COUNT){
                abductorBeamSound = gameAudioSystem.addPositionalLoopedTrack("res/sound/beam-real-loop2.wav", abductorBeam, 0.3f);
                abductorBeam.setActive(true);
            } else{
                gameAudioSystem.addPositionalTrack("res/sound/fail.wav", this, 1f);
            }
        }
    }

    public void stopAbductorBeam(){
        if(!abductionCommenced && abductorBeam.isActive()){
        	gameAudioSystem.removePositionalLoopedTrack(abductorBeamSound);
            gameAudioSystem.addPositionalTrack("res/sound/beam-end2.wav", abductorBeam, 0.38f);
            abductorBeam.setActive(false);
            currentAbductableInBeam = null;            
        } else {
            stopBeamOnAbductionComplete = true;
        }
    }

    private void stackSheep(Abductable abductable){
        // Set translation to stack the spatial on the tailboard of the ship.
        modelNode.attachChild(abductable.getAbductionSpatial());
        abductable.getAbductionSpatial().getLocalTranslation().set(sheepStackLocations[sheepStackCount]);
        abductable.getAbductionSpatial().getLocalRotation().set(sheepStackRotations[sheepStackCount]);
        abductable.abductableStacked();
        stackedSheep.add(abductable);
        sheepStackCount++;
    }
    
    public ArrayList<Abductable> getStackedSheepList() {
    	return stackedSheep;
    }
    
    public boolean hasStackedSheep() {
    	return (sheepStackCount > 0);
    }
    
    public int unstackSheep() {
    	int		result = sheepStackCount;
    	
    	for (Abductable sheep : stackedSheep) {
    		modelNode.detachChild(sheep.getAbductionSpatial());
    	}
    	
    	stackedSheep.clear();
    	sheepStackCount = 0;
    	
    	return result;
    }

    public Spatial getShip() {
		return modelNode;
	}

	public Spatial getShipNode() {
		return shipNode;
	}

    public void addAbductable(Abductable abductable){
        abductables.add(abductable);
    }

    public void removeAbductable(Abductable abductable){
        abductables.remove(abductable);
    }
    
    public void removeAllAbducables() {
    	abductables.clear();
    }

    public AbductorBeam getAbductorBeam(){
        return abductorBeam;
    }

    public float getVelocity(){
        return velocity;
    }

    public float getTurnSpeed(){
        return turnSpeed;
    }

    public SpaceshipHandler getSpaceshipHandler(){
        return spaceshipHandler;
    }
}
