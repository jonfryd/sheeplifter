package dk.impact.sheeplifter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;

import com.jme.bounding.BoundingBox;
import com.jme.curve.CatmullRomCurve;
import com.jme.input.ChaseCamera;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.thirdperson.ThirdPersonMouseLook;
import com.jme.light.PointLight;
import com.jme.math.FastMath;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.renderer.pass.BasicPassManager;
import com.jme.renderer.pass.RenderPass;
import com.jme.renderer.pass.ShadowedRenderPass;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import com.jme.system.GameSettings;
import com.jme.util.Timer;
import com.jmex.audio.AudioTrack;
import com.jmex.effects.transients.Fader;
import com.jmex.effects.water.ImprovedNoise;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameStateManager;

import dk.impact.sheeplifter.audio.GameAudioSystem;
import dk.impact.sheeplifter.spatials.CollisionManager;
import dk.impact.sheeplifter.spatials.IslandTerrain;
import dk.impact.sheeplifter.spatials.Lava;
import dk.impact.sheeplifter.spatials.Ocean;
import dk.impact.sheeplifter.spatials.Plant;
import dk.impact.sheeplifter.spatials.PlantArea;
import dk.impact.sheeplifter.spatials.SheepHUDCountdown;
import dk.impact.sheeplifter.spatials.SheepIcons;
import dk.impact.sheeplifter.spatials.Sky;
import dk.impact.sheeplifter.spatials.Tree;
import dk.impact.sheeplifter.spatials.World;
import dk.impact.sheeplifter.spatials.World.SurfacePoint;
import dk.impact.sheeplifter.spatials.actors.Abductable;
import dk.impact.sheeplifter.spatials.actors.dropzone.DropZone;
import dk.impact.sheeplifter.spatials.actors.sheep.Sheep;
import dk.impact.sheeplifter.spatials.actors.ship.Spaceship;
import dk.impact.sheeplifter.util.FixedFontTextureState;

public class InGameState extends BasicGameState {
	private static final float 		farPlane = 2500.0f;
    private static final boolean	shadowRendering = false;
    
    private static final int		MIN_SHEEP = 10;
    private static final int		MAX_SHEEP = 15;
    
    private static final float		WATER_RISE_TIME = 200; // time to complete flooding
    
    private static final float		INITIAL_ISLAND_LOWERING = -20;
    
    private static final float		WATER_INITIAL_VOLUME = 0.125f;
    private static final float		WATER_MAX_VOLUME = 0.25f;
    
    private static final float		EARTHQUAKE_MAX_VOLUME = 0.9f;

    private static final int 		TRIG_UNSTACK_DISTANCE = 40;
    
    private static final float		SHEEP_PRESENTATION_START_TIME = 0.0f;
    private static final float		SHEEP_PRESENTATION_END_TIME = SHEEP_PRESENTATION_START_TIME + 5.0f;

    private static final float		VOLCANO_PRESENTATION_START_TIME = SHEEP_PRESENTATION_END_TIME;
    private static final float		VOLCANO_PRESENTATION_END_TIME = VOLCANO_PRESENTATION_START_TIME + 5.0f;
    
    private static final float		WOLF_TELEPORT_START_TIME = VOLCANO_PRESENTATION_END_TIME + 4f;
    private static final float		WOLF_TELEPORT_END_TIME = WOLF_TELEPORT_START_TIME + 0.3f;

    private static final float		SHIP_ACCELERATE_TIME = WOLF_TELEPORT_END_TIME + 2f;
    
    private static final float		GO_TEXT_START_TIME = SHIP_ACCELERATE_TIME + 0.25f;
    private static final float		GO_TEXT_END_TIME = GO_TEXT_START_TIME + 3f;
    
    private GameAudioSystem		gameAudioSystem;

	private BasicPassManager 	pManager;

	private Sky 				sky;
	private IslandTerrain 		islandTerrain;
	private Lava				lava;
	private Ocean 				ocean;
	private DropZone			dropZone;
	
	private AudioTrack			waterAmbientSound;
	private AudioTrack			earthquakeAmbientSound;

	private World				world;

	private Node				sheepGroup;
	private Sheep				sheep[];
	private int					totalUnstacked;

	private Node				planting;

	private Node 				scene;
	private ShadowedRenderPass 	shadowPass;

	private CollisionManager	collisionManager;

	private SheepHUDCountdown	sheepHUDCountdown;
	private Text				timeLeftText; // time before flooding
	private Text				goText;
	private SheepIcons			sheepIcons;
	private Text				sheepDebugText;
	
	private DisplaySystem		display;

	private Node 				statNode;
	
	private Camera				cam;

    private GameSettings        settings;

    /**
     * A lightstate to turn on and off for the rootNode
     */
    protected LightState 		lightState;
	
    private Spaceship 			spaceship;
    
	private CatmullRomCurve 	cameraCurve;

    private ChaseCamera 		chaser;
    
    private AudioTrack			mainThemeTrack;
    private AudioTrack			gameOverTrack;
    private AudioTrack			gameWonTrack;
    
    private float				beginTime;
    private float				endTime;
    private float				timeLeft;
	private DecimalFormat		timeFormatter;
	
	private boolean				isGameOver;
	private float				timeGameOver;

    private boolean				hasSwitchedToTitle;
    
    private boolean				volcanoSequenceHasShown;
    private boolean				volcanoSequenceShowing;
    
    private boolean				sheepSequenceHasShown;
    private boolean				sheepSequenceBaaPlayed;
    private boolean				sheepSequenceShowing;
    
    private boolean				wolfHasTeleported;
    private boolean				wolfTeleporting;
	
    private boolean				goTextHasShown;
    private boolean				goTextShowing;
    
    private boolean				gameStarted;    
    
    private Fader				fader;
    
	public InGameState(GameAudioSystem gameAudioSystem, Camera cam, GameSettings settings) {
		super(GameStates.IN_GAME_STATE);
		this.display = DisplaySystem.getDisplaySystem();
		this.gameAudioSystem = gameAudioSystem;
		this.cam = cam;
        this.settings = settings;

        pManager = new BasicPassManager();

        scene = new Node("scene");
        
        shadowPass = new ShadowedRenderPass();
        shadowPass.setRenderShadows(shadowRendering);
        shadowPass.setLightingMethod(ShadowedRenderPass.LightingMethod.Modulative);
        shadowPass.cleanUp();
        
        mainThemeTrack = gameAudioSystem.registerMusic("res/sound/sheep_game_capture03.ogg");
        gameOverTrack = gameAudioSystem.registerMusic("res/sound/gameover-wierd.ogg");
        gameWonTrack = gameAudioSystem.registerMusic("res/sound/gamewon.ogg");
        
        waterAmbientSound = gameAudioSystem.createAmbientSound("res/sound/vandloop.ogg");
        earthquakeAmbientSound = gameAudioSystem.createAmbientSound("res/sound/earthquake01.wav");
        
        timeFormatter = new DecimalFormat("00");
        timeFormatter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
        
        init();
	}
    
    protected void init() {
        cam.setLocation(new Vector3f(128 * 5, 260, 128 * 5));
        cam.setFrustumPerspective(45.0f, (float) display.getWidth()
                / (float) display.getHeight(), 1f, farPlane);
        cam.update();
        
        statNode = new Node( "Stats node" );
        statNode.setCullHint( Spatial.CullHint.Never );
        statNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);        
        
        /** Set up a basic, default light. */
        PointLight light = new PointLight();
        light.setDiffuse( new ColorRGBA( 1.0f, 0.80f, 0.55f, 0.9f ) );
        light.setAmbient( new ColorRGBA( 0.5f, 0.50f, 0.30f, 1.0f ) );
        light.setLocation( new Vector3f( 100, 100, 100 ) );
        light.setEnabled( true );
        
        /** Attach the light to a lightState and the lightState to rootNode. */
        lightState = display.getRenderer().createLightState();
        lightState.setEnabled( true );
        lightState.attach( light );
        rootNode.setRenderState( lightState );        
        
        setupLighting();
        setupFog();

        collisionManager = new CollisionManager();

        buildLandscape();
        buildSkyBox();
        buildOcean();

        world = new World(islandTerrain, ocean, islandTerrain.getVolcanoHeight().clone());

        buildAndAttachLava();
        buildDropZone();
        buildSpaceship();
        buildSheep();
        
        buildChaseCamera();

        setupSceneGraph();

        rootNode.updateGeometricState(0, true);

        buildPlanting();
        rootNode.attachChild(planting);
        rootNode.attachChild(dropZone);

        setupRenderPasses();
        
        rootNode.setCullHint(Spatial.CullHint.Dynamic);

        buildHUD();

		fader = new Fader("fader", display.getWidth(), display.getHeight(), new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f), 1.4f);
		fader.setLocalTranslation(display.getWidth() / 2, display.getHeight() / 2, 1);
		fader.setCullHint(Spatial.CullHint.Never);
		fader.setAlpha(1.0f);   
		fader.setMode(Fader.FadeMode.FadeIn);
		rootNode.attachChild(fader);
        
        statNode.updateGeometricState( 0.0f, true );
        statNode.updateRenderState();
        rootNode.updateGeometricState( 0.0f, true );
        rootNode.updateRenderState();

        // Bind the exit action to the escape key.
        KeyBindingManager.getKeyBindingManager().set(
            "exitgame",
            KeyInput.KEY_ESCAPE);
    }

	public void cleanup() {
		if(shadowPass != null) {
			shadowPass.clearOccluders();
			shadowPass.removeAll();
	        shadowPass.cleanUp();
		}
        if(ocean != null){
            ocean.getWaterRenderPass().cleanup();
        }
	}

    public void update(float tpf) {
		float currentTime = Timer.getTimer().getTimeInSeconds();

		super.update(tpf);

		if (KeyBindingManager.getKeyBindingManager().isValidCommand("exitgame", false) || (isGameOver && ((currentTime - timeGameOver) > 10))) {
			// Here we switch to the menu state which is already loaded
			gameOver();			
			return;
		}
    	
		timeLeft = endTime - currentTime;
		
		if (timeLeft <= 0) {
			// game over!
			timeLeft = 0;
		}
		
		float quakeAmplitude = 0;
		
		if (volcanoSequenceShowing) {
			quakeAmplitude = FastMath.clamp(FastMath.sin(currentTime * 0.2f) * 20f - 10f, 0, 10f);
		}
		
		float dispNormalizedAmplitude = quakeAmplitude / 10;		
		float percentageSinked = 0;

		if (volcanoSequenceShowing) {
			float dispX = (float) ImprovedNoise.noise(currentTime * 15f, 0, 0) * quakeAmplitude;
			float dispZ = (float) ImprovedNoise.noise(0, 0, currentTime * 15f) * quakeAmplitude;
			
			percentageSinked = (currentTime - beginTime) / (endTime - beginTime);
			
			float sinkY = FastMath.LERP(percentageSinked, INITIAL_ISLAND_LOWERING, -islandTerrain.getHighestY());
			
	        islandTerrain.setLocalTranslation(dispX, INITIAL_ISLAND_LOWERING + sinkY, dispZ);
	        planting.setLocalTranslation(dispX, 0 + sinkY, dispZ);
	        world.getVolcanoHeight().setY(islandTerrain.getVolcanoHeight().getY() + sinkY);
		}
		
        if (!isGameOver) {
    		float earthQuakeVolume = Math.max(FastMath.LERP(percentageSinked, EARTHQUAKE_MAX_VOLUME * dispNormalizedAmplitude, 0), 0);
    		
    		if (volcanoSequenceShowing && !volcanoSequenceHasShown) {
    			earthQuakeVolume = (1f - fader.getAlpha()) * EARTHQUAKE_MAX_VOLUME;
    		}
    		
	        earthquakeAmbientSound.setVolume(earthQuakeVolume);						
            waterAmbientSound.setVolume(Math.min(FastMath.LERP(percentageSinked, WATER_INITIAL_VOLUME, WATER_MAX_VOLUME), WATER_MAX_VOLUME));        	
        }
        
        updateEnvironment(tpf);
        updateActors(tpf);
        updateHUD(tpf);
        updateGameState();

		rootNode.updateGeometricState(tpf, true);
		statNode.updateGeometricState(tpf, true);
        
		pManager.updatePasses(tpf);
    }

	private void gameOver() {
		if (!hasSwitchedToTitle) {
			GameStateManager.getInstance().activateChildNamed(GameStates.TITLE_STATE);
			statNode.setCullHint(CullHint.Always);
			hasSwitchedToTitle = true;
		}
	}

    public void render(float tpf) {
        Renderer r = display.getRenderer();
        // Clears the previously rendered information.
        r.clearBuffers();

        // Have the PassManager render
        pManager.renderPasses(r);
    }    
    
    private void updateActors(float timeDelta){
        for(int i = 0; i < sheep.length; i++){
            sheep[i].update(timeDelta);
        }

        collisionHandling();

        spaceship.getSpaceshipHandler().update(timeDelta);
        
        lava.update(timeDelta);        	
        
        // Re-position drop zone relative to ground
    	if (volcanoSequenceHasShown) {
	        SurfacePoint pt = world.getSurfacePointAt(dropZone.getWorldTranslation());
	    	dropZone.getLocalTranslation().setY(pt.getHeight() + Spaceship.DISTANCE_TO_GROUND + 20f);
	        dropZone.update(timeDelta);
    	}
       
        // Are we inside drop zone
        Vector3f dropZonePosition = dropZone.getWorldTranslation();
        Vector3f spaceshipPosition = spaceship.getWorldTranslation();
        
        float distance = dropZonePosition.distance(spaceshipPosition);
        
        if (distance < TRIG_UNSTACK_DISTANCE) {
        	if (spaceship.hasStackedSheep()) {
            	for (Abductable sheep : spaceship.getStackedSheepList()) {
            		dropZone.addCentrifugeTrail(sheep.getAbductionSpatial());
            	}
            	
            	totalUnstacked += spaceship.unstackSheep();
            	gameAudioSystem.addPositionalTrack("res/sound/sheep-drop.wav", spaceship, 0.5f);        		
        	}        	
        }
        
    	float	timePassed = Timer.getTimer().getTimeInSeconds() - beginTime;
    	
        if (!wolfHasTeleported) {
        	if (timePassed > WOLF_TELEPORT_START_TIME) {
        		if (!wolfTeleporting) {
            		wolfTeleporting = true;
            		gameAudioSystem.addPositionalTrack("res/sound/wulf.wav", dropZone, 0.5f);
        		}        		
        		
        		float	scaleTotalTime = WOLF_TELEPORT_END_TIME - WOLF_TELEPORT_START_TIME;
            	float	scale = FastMath.LERP((timePassed - WOLF_TELEPORT_START_TIME) / scaleTotalTime, 0, 1);                	
            	scale = FastMath.clamp(scale, 0, 1);
            	
                spaceship.setLocalScale(scale);
        	}
        	if (timePassed > WOLF_TELEPORT_END_TIME) {
        		wolfHasTeleported = true;
        	}
        }
        
        if (!gameStarted) {
            if (timePassed > SHIP_ACCELERATE_TIME) {
            	gameStarted = true;
        		statNode.setCullHint(Spatial.CullHint.Never);
        		spaceship.getSpaceshipHandler().setEnabled(true);
        		spaceship.accelerate(0.6f);        	
        		gameAudioSystem.playMusic(mainThemeTrack);
            }        	
        }
        
        if (!sheepSequenceHasShown) {
        	if (timePassed > SHEEP_PRESENTATION_START_TIME) {
        		Vector3f sheepPosition = sheep[0].getBody().getCameraLookAtNode().getWorldTranslation();

        		if (!sheepSequenceShowing) {
            		sheepSequenceShowing = true;
            		
                	// 1. stop et får - indfør switch?!
                	// 2. skift dens animation
            		sheep[0].enableStonedMode();
            		for (int i = 1; i < sheep.length; i++) {
            			sheep[i].getBody().setCullHint(Spatial.CullHint.Always);
                		sheep[i].enableStonedMode();
            		}
            		
                	// 3. find kurvepunkter til kamerabevægelse på baggrund af fårets position og orientering
            		float sheepHeading = sheep[0].getBody().getHeading();
            		
            		Vector3f[] curvePoints = new Vector3f[3];
            		
            		curvePoints[0] = new Vector3f(sheepPosition.getX() + FastMath.cos(sheepHeading - FastMath.HALF_PI * 0.5f) * 30, sheepPosition.getY() + 10, sheepPosition.getZ() - FastMath.sin(sheepHeading - FastMath.HALF_PI * 0.5f) * 30);
            		curvePoints[1] = new Vector3f(sheepPosition.getX() + FastMath.cos(sheepHeading) * 30, sheepPosition.getY() + 10, sheepPosition.getZ() - FastMath.sin(sheepHeading) * 30);
            		curvePoints[2] = new Vector3f(sheepPosition.getX() + FastMath.cos(sheepHeading + FastMath.HALF_PI * 0.5f) * 30, sheepPosition.getY() + 10, sheepPosition.getZ() - FastMath.sin(sheepHeading + FastMath.HALF_PI * 0.5f) * 30);

                	// 4. lav spline på baggrund af (3)        
            		cameraCurve = new CatmullRomCurve("CameraCurve", curvePoints);
            		cameraCurve.setSteps(512);    
        		}
        		
        		float	scaleTotalTime = SHEEP_PRESENTATION_END_TIME - SHEEP_PRESENTATION_START_TIME;
            	float	time = FastMath.LERP((timePassed - SHEEP_PRESENTATION_START_TIME) / scaleTotalTime, 0, 1);                	
            	time = FastMath.clamp(time, 0, 1); 
            	
            	// 5. start sekvens:
            	// 6.   interpoler kamera-positon
            	Vector3f curvePoint = cameraCurve.getPoint(time);
            	
            	// 7.   udregn retning i forhold til fåret
            	// 8.   sæt position og retning
        		cam.getLocation().set(curvePoint);
        		cam.lookAt(sheepPosition, new Vector3f(0, 1, 0));
        		cam.update();
        		
        		if (!sheepSequenceBaaPlayed && (time >= 0.5f)) {
        			sheepSequenceBaaPlayed = true;
                	int sound = (int) (Math.random() * 13 + 1);
        			gameAudioSystem.addPositionalTrack("res/sound/maeh" + sound + ".wav", sheep[0].getBody(), 0.5f);
        		}
        	}
        	if (timePassed > SHEEP_PRESENTATION_END_TIME - fader.getFadeTimeInSeconds()) {
        		fader.setMode(Fader.FadeMode.FadeOut);
        	}
        	if (timePassed > SHEEP_PRESENTATION_END_TIME) {        	
        		sheepSequenceHasShown = true;
            	// 9. sæt får tilbage i normaltilstand
        		sheep[0].disableStonedMode();
        		for (int i = 1; i < sheep.length; i++) {
        			sheep[i].getBody().setCullHint(Spatial.CullHint.Dynamic);
            		sheep[i].disableStonedMode();
        		}        		
        		
        		fader.setMode(Fader.FadeMode.FadeIn);
        	}
        }
        
        if (!volcanoSequenceHasShown) {
        	if (timePassed > VOLCANO_PRESENTATION_START_TIME) {
        		Vector3f volcanoTopPosition = islandTerrain.getTerrainBlock().getWorldBound().getCenter();
        		volcanoTopPosition.setY(world.getVolcanoHeight().getY());

        		if (!volcanoSequenceShowing) {
        			volcanoSequenceShowing = true;
            		
            		Vector3f cameraBasePoint = world.getVolcanoHeight();

            		Vector3f[] curvePoints = new Vector3f[3];
            		
            		curvePoints[0] = new Vector3f(cameraBasePoint.getX() - 400, cameraBasePoint.getY() + 50, cameraBasePoint.getZ() - 400);
            		curvePoints[1] = new Vector3f(cameraBasePoint.getX(), cameraBasePoint.getY() + 250, cameraBasePoint.getZ() - 200);
            		curvePoints[2] = new Vector3f(cameraBasePoint.getX() + 400, cameraBasePoint.getY() + 50, cameraBasePoint.getZ() - 400);

            		cameraCurve = new CatmullRomCurve("CameraCurve", curvePoints);
            		cameraCurve.setSteps(512);    
        		}
        		
        		float	scaleTotalTime = VOLCANO_PRESENTATION_END_TIME - VOLCANO_PRESENTATION_START_TIME;
            	float	time = FastMath.LERP((timePassed - VOLCANO_PRESENTATION_START_TIME) / scaleTotalTime, 0, 1);                	
            	time = FastMath.clamp(time, 0, 1); 
            	
            	Vector3f curvePoint = cameraCurve.getPoint(time);
            	
        		cam.getLocation().set(curvePoint);
        		cam.lookAt(volcanoTopPosition, new Vector3f(0, 1, 0));
        		cam.update();
        	}
        	if (timePassed > VOLCANO_PRESENTATION_END_TIME - fader.getFadeTimeInSeconds()) {
        		fader.setMode(Fader.FadeMode.FadeOut);
        	}
        	if (timePassed > VOLCANO_PRESENTATION_END_TIME) {        	
        		volcanoSequenceHasShown = true;
        		fader.setMode(Fader.FadeMode.FadeIn);
        	}
        }
        
    }

    private void updateHUD(float timeDelta) {
    	if (!goTextHasShown) {
        	float	timePassed = Timer.getTimer().getTimeInSeconds() - beginTime;
        	
        	if ((timePassed > GO_TEXT_START_TIME) && !goTextShowing) {
        		goText.print("ABDUCT " + sheep.length + " SHEEP!");
        		goText.getTextColor().set(1, 1, 1, 1);
        		goText.updateRenderState();
        		
        		goText.setCullHint(Spatial.CullHint.Never);
        		
        		goTextShowing = true;
        	}
        	if (goTextShowing) {
        		float	scaleTotalTime = GO_TEXT_END_TIME - GO_TEXT_START_TIME;
            	float	scale = FastMath.LERP((timePassed - GO_TEXT_START_TIME) / scaleTotalTime, 0, 1);                	
            	scale = FastMath.pow(FastMath.sin(FastMath.clamp(scale, 0, 1) * FastMath.PI), 2.0f) * 3f;
            	scale *= display.getWidth() / 640f;      
            	
        		goText.setLocalTranslation(new Vector3f(display.getWidth() * 0.5f + scale * -70, display.getHeight() * 0.55f, 1));
        		goText.setLocalScale(scale);
        	}
        	if (timePassed > GO_TEXT_END_TIME) {
        		goTextHasShown = true;
        		goText.setCullHint(Spatial.CullHint.Always);
        	}    		
    	}
    	
    	if (timeLeft > 0) {
	    	int		mins = (int) (timeLeft / 60);
	    	int		secs = (int) (timeLeft % 60);
	    	
	    	float	zoom;
	
	    	if (timeLeft <= 30) {
	    		timeLeftText.getTextColor().set(1, 0, 0, 0.8f);
	
	    		zoom = (timeLeft % 1);
	        	
	    		if (zoom > 0.5) {
	        		zoom = 1 - zoom;
	        	}
	    		
	    		zoom *= 2f;
	    	}
	    	else {
	    		timeLeftText.getTextColor().set(1, 1, 1, 0.8f);

	    		zoom = (timeLeft % 10);
	
	        	if (zoom > 9) {
	        		zoom = 10 - zoom;
	        	}
	    	}
	    	
	    	zoom = (1 - FastMath.pow(FastMath.sin(FastMath.clamp(zoom, 0, 1)), 2.0f) * (float) Math.PI * 0.5f) * 0.5f;
	    	
	    	timeLeftText.setLocalScale((display.getWidth() / 640f * 1.2f) + zoom);
    	
        	timeLeftText.print(mins + ":" + timeFormatter.format(secs));
    	}
    	else {
    		statNode.detachChild(timeLeftText);
    	}
    	
    	sheepIcons.update();
    	sheepHUDCountdown.update();

    	//debug text
        //sheepDebugText.print(sheep[0].toString());
        //sheepDebugText.print(display.getScreenCoordinates(sheep[0].getBody().getWorldTranslation()).toString());
    }

    private void updateEnvironment(float timeDelta){
    	//chaser.setEnableSpring(false);
    	if (volcanoSequenceHasShown) {
            chaser.update(timeDelta);
    	}
        
        //simulate wind effect
        for (Spatial sp : planting.getChildren()) {
        	((PlantArea) sp).simWindCurrent();
        }
        
        //We don't want the chase camera to go below the world, so always keep
        //it 7 units above the level.
        World.SurfacePoint surfacePoint = world.getSurfacePointAt(cam.getLocation());

        if(cam.getLocation().y < (surfacePoint.getHeight() + 7)){
            cam.getLocation().y = (surfacePoint.getHeight() + 7);
        }

        sky.getLocalTranslation().set(cam.getLocation());
        cam.update();
    }	
	
    private void updateGameState() {
    	if (!isGameOver && isActive()) {
    		String	message = "";
    		String	message2 = "";
            float	textSize = 3f;
            float	textSize2 = 2f;
    		float	textOffset = 0;
    		float	textOffset2 = 0;
            
    		textSize *= display.getWidth() / 640f;
    		textSize2 *= display.getWidth() / 640f;
    		
        	for (int i = 0; i < sheep.length; i++) {
        		isGameOver |= sheep[i].getBody().hasDrowned();
        	}
        	
        	if (isGameOver) {
        		// game lost :-(
        		gameAudioSystem.playMusic(gameOverTrack); 
        		message = "GAME OVER";                
                textOffset = -50 * textSize;
        		message2 = "A sheep has drowned!";                
                textOffset2 = -100 * textSize2;
        	}
        	else if ((totalUnstacked == sheep.length) && dropZone.getCentrifuge().hasNoTrailingSpatials()) {
        		// game won :-)
        		isGameOver = true;
        		gameAudioSystem.playMusic(gameWonTrack);  
        		message = "SHEEPTASTIC!";
                textOffset = -65 * textSize;
        	}

        	if (isGameOver) {        	
        		waterAmbientSound.fadeOut(1f);
        		earthquakeAmbientSound.fadeOut(1f);
        		
        		statNode.detachAllChildren();
        		
                Text gameOverText = Text.createDefaultTextLabel("Text", message);
                Text gameOverText2 = Text.createDefaultTextLabel("Text", message2);
              
                gameOverText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
                gameOverText.setLightCombineMode(Spatial.LightCombineMode.Off);
                gameOverText.setLocalTranslation(new Vector3f(display.getWidth() * 0.5f + textOffset, display.getHeight() * 0.65f, 1));
                gameOverText.setLocalScale(textSize);
                gameOverText.setRenderState(FixedFontTextureState.getFixedFontTextureState());

                gameOverText2.setRenderQueueMode(Renderer.QUEUE_ORTHO);
                gameOverText2.setLightCombineMode(Spatial.LightCombineMode.Off);
                gameOverText2.setLocalTranslation(new Vector3f(display.getWidth() * 0.5f + textOffset2, display.getHeight() * 0.4f, 1));
                gameOverText2.setLocalScale(textSize2);
                gameOverText2.setRenderState(FixedFontTextureState.getFixedFontTextureState());
                
                gameOverText.updateRenderState();
                gameOverText2.updateRenderState();
                
                statNode.attachChild(gameOverText);        		
                statNode.attachChild(gameOverText2);        		
                
                timeGameOver = Timer.getTimer().getTimeInSeconds();
        	}
    	}
    }
    
    private void collisionHandling(){
        boolean repeat;
        int iter = 10;

        if (sheepSequenceHasShown) {
            do{
                repeat = collisionManager.handleCollisions();
            } while(repeat && (iter-- > 0));        	
        }
    }

    private void setupSceneGraph(){
        rootNode.attachChild(ocean);
        ocean.getReflectedNode().attachChild(scene); // node used to render reflection effect in ocean

        scene.attachChild(sheepGroup);
        
        attachLandscape();        
    }

    private void setupLighting(){
        lightState.setTwoSidedLighting(true);
        ((PointLight) lightState.get(0)).setLocation(new Vector3f(2500, 2500, 2500));
        lightState.get(0).setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
        lightState.get(0).setShadowCaster(true);
    }

    private void setupRenderPasses(){
        pManager.add(ocean.getWaterRenderPass());
        
        shadowPass.add(rootNode);
        shadowPass.addOccluder(sheepGroup);
        shadowPass.addOccluder(spaceship.getShip());
        pManager.add(shadowPass);
       
        RenderPass statPass = new RenderPass();
        statPass.add(statNode);
        pManager.add(statPass);
    }

    private void setupFog(){
        FogState fogState = display.getRenderer().createFogState();
        fogState.setDensity(1.0f);
        fogState.setEnabled(true);
        fogState.setColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        fogState.setEnd(farPlane);
        fogState.setStart(farPlane / 4.0f);
        fogState.setDensityFunction(FogState.DensityFunction.Linear);
        fogState.setQuality(FogState.Quality.PerVertex);
        rootNode.setRenderState(fogState);
    }

    private void buildSkyBox(){
        sky = new Sky("sky", farPlane * 0.5f, farPlane * 0.5f, farPlane * 0.5f, display);
    }

    private void attachLandscape(){
        scene.attachChild(islandTerrain);
        world.setIslandTerrain(islandTerrain);
        ocean.getProjectedGrid().setTerrainBlock(islandTerrain.getTerrainBlock());
    }

    private void buildLandscape(){
        islandTerrain = new IslandTerrain("island", display);
        islandTerrain.setLocalTranslation(0, INITIAL_ISLAND_LOWERING, 0);
    }

    private void buildOcean(){
        ocean = new Ocean(cam, sky);
    }
    
    private void buildAndAttachLava() {
		Vector3f 	center = islandTerrain.getTerrainBlock().getWorldBound().getCenter();		

		lava = new Lava("VolcanoLava");
		
		float maxDim =  world.getXZDistanceVolcanoToCenter() * 2;		
    	float lavaHeight = world.getVolcanoHeight().getY() * 0.5f + world.getIslandHeight(center) * 0.5f;
    	
    	lava.getLavaQuad().getLocalScale().set(maxDim, maxDim, 1);
		lava.setLocalTranslation(center.getX(), lavaHeight, center.getZ());

    	islandTerrain.attachChild(lava);       
    }

    private void buildSheep(){
		Vector3f 	center = islandTerrain.getTerrainBlock().getWorldBound().getCenter();		
    	
        sheepGroup = new Node("sheep group");

        int noSheep = MIN_SHEEP + (int) Math.round(Math.random() * (MAX_SHEEP - MIN_SHEEP));
        sheep = new Sheep[noSheep];

        int count = noSheep;
        
        while(count > 0) {
        	int		flockSize = 1 + (int) Math.round(Math.random() * 3);
        	
        	float	radius = (float) Math.random() * 1100f + 500f;
        	float	deg = (float) ((float) Math.random() * Math.PI);

    		float	x = (float) (Math.cos(deg) * radius + center.getX());
    		float	z = (float) (Math.sin(deg) * radius + center.getZ());
    		
        	while((flockSize-- > 0) && (count-- > 0)) {
            	sheep[count] = new Sheep("sheep" + count, gameAudioSystem, world, spaceship.getShipNode(), collisionManager);
                sheep[count].getBody().setLocalTranslation(x, 0, z);
                sheep[count].getBody().getLocalRotation().fromAngles(0, (float) Math.random() * FastMath.PI, 0);
                spaceship.addAbductable(sheep[count]);

                sheepGroup.attachChild(sheep[count].getBody());
        	}
        }

        // add collision checks for all sheep against each other
        for(int i = 0; i < sheep.length; i++){
            for(int j = (i + 1); j < sheep.length; j++){
                collisionManager.addCheck(sheep[i].getBody(), sheep[j].getBody(), sheep[i].getBody());
            }
        }
    }
    
    private void buildDropZone() {
		Vector3f 	center = islandTerrain.getTerrainBlock().getWorldBound().getCenter();		

		dropZone = new DropZone(cam, gameAudioSystem);
    	dropZone.resizeGeometry(14f);
        dropZone.setSpeed(7, 0.4f);

        float	radius = (float) Math.random() * 1700f + 500f;
    	float	deg = (float) ((float) Math.random() * Math.PI);

		float	x = (float) (Math.cos(deg) * radius + center.getX());
		float	z = (float) (Math.sin(deg) * radius + center.getZ());

    	dropZone.setLocalTranslation(x, 0, z);

        FogState fs = display.getRenderer().createFogState();
        fs.setEnabled(false);
        dropZone.setRenderState(fs);
    }

    private void buildChaseCamera() {
        Vector3f targetOffset = new Vector3f(0, 2, 0);

        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put(ThirdPersonMouseLook.PROP_MAXROLLOUT, "70");
        props.put(ThirdPersonMouseLook.PROP_MINROLLOUT, "20");
        props.put(ThirdPersonMouseLook.PROP_MOUSEXMULT, "0.5");
        props.put(ThirdPersonMouseLook.PROP_MOUSEYMULT, "15");
        props.put(ChaseCamera.PROP_TARGETOFFSET, targetOffset);
        props.put(ThirdPersonMouseLook.PROP_MAXASCENT, ""+45 * FastMath.DEG_TO_RAD);
        //props.put(ChaseCamera.PROP_DAMPINGK, "" + 60);
        props.put(ChaseCamera.PROP_STAYBEHINDTARGET, "false");
        props.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(90 * 4, 0, 25 * FastMath.DEG_TO_RAD));
        props.put(ChaseCamera.PROP_TARGETOFFSET, targetOffset);
        chaser = new ChaseCamera(cam, spaceship.getShipNode(), props);
        chaser.setMaxDistance(120 * 4);
        chaser.setMinDistance(90 * 4);
    }

	private void buildSpaceship() {
        // Add spaceship.
        spaceship = new Spaceship(settings, gameAudioSystem, world);
        spaceship.getLocalTranslation().set(dropZone.getWorldTranslation());
        spaceship.getLocalRotation().fromAngles(0, (float) ((float) Math.random() * Math.PI), 0);
        spaceship.setLocalScale(0);
        scene.attachChild(spaceship);
	}

    private void buildPlanting(){
        if(planting == null){
            planting = new Node("planting");
        } else{
            planting.detachAllChildren();
        }

        Plant grassTypes[] = new Plant[4];
        Plant bushTypes[] = new Plant[9];
        Plant treeTypes[] = new Plant[1];

        for(int i = 0; i < grassTypes.length; i++){
            grassTypes[i] = new Plant("grass" + (i + 1), "res/geometry/grass_" + (i + 2) + ".jme", display);
        }
        //for(int i = 0; i < bushTypes.length; i++){
        //    bushTypes[i] = new Plant("bush" + (i + 1), "res/geometry/bush" + (i + 1) + ".jme", display);
        //}

        /*
        treeTypes[0] = new Tree("tree1", "res/geometry/deadtree.jme", display);
        treeTypes[0].getModel().setLocalTranslation(0, -7.5f, 0);
        */
        treeTypes[0] = new Tree("tree2", "res/geometry/palmtree.jme", display);
        treeTypes[0].getModel().setLocalTranslation(0, -4f, 0);
        treeTypes[0].getModel().setLocalScale(0.4f);
        treeTypes[0].getModel().getLocalRotation().fromAngles(0, 0, FastMath.HALF_PI * 0.25f);

		Vector3f 	center = islandTerrain.getTerrainBlock().getWorldBound().getCenter();
		float		sizeX = ((BoundingBox) islandTerrain.getTerrainBlock().getWorldBound()).xExtent * 0.75f;
		float		sizeZ = ((BoundingBox) islandTerrain.getTerrainBlock().getWorldBound()).xExtent * 0.75f;

		float		volcanoLimit = world.getXZDistanceVolcanoToCenter() * 1.3f;

        for(int i = 1; i <= 55; i++){
            float	x, z;

        	int plantType = (int) (3 * Math.random());
            Plant plantArray[] = null;
            int groupSize = 0;

            do {
                x = (float) (2f * sizeX * Math.random()) + (center.getX() - sizeX);
                z = (float) (2f * sizeZ * Math.random()) + (center.getZ() - sizeZ);
            }
            while(world.getXZDistanceToCenter(new Vector3f(x, 0, z)) < volcanoLimit);

            switch(plantType){
                case 0:
                    plantArray = grassTypes;
                    groupSize = (int) (12 * Math.random() + 15);
                    break;
/*                case 1:
                    plantArray = bushTypes;
                    groupSize = (int) (15 * Math.random() + 10);
                    break;*/
                case 1:
                case 2:
                    plantArray = treeTypes;
                    groupSize = (int) (4 * Math.random() + 3);
                    break;
            }

            int subType = (int) (plantArray.length * Math.random());

            planting.attachChild(new PlantArea("PlantArea" + i, plantArray[subType], groupSize, new Vector2f(x, z), islandTerrain));
        }

        // inherit render state, light combine mode, etc.
        planting.setRenderState(grassTypes[0].getRenderState(RenderState.StateType.Blend));
        planting.setRenderState(grassTypes[0].getRenderState(RenderState.StateType.ZBuffer));
        planting.setLightCombineMode(grassTypes[0].getLocalLightCombineMode());
        planting.setRenderQueueMode(grassTypes[0].getRenderQueueMode());

        planting.updateRenderState();
        planting.updateModelBound();
    }

	private void buildHUD() {
		sheepHUDCountdown = new SheepHUDCountdown("SheepHUDCountdown", sheep);		
		sheepHUDCountdown.setRenderQueueMode(Renderer.QUEUE_ORTHO);
		sheepHUDCountdown.setLightCombineMode(Spatial.LightCombineMode.Off);
		sheepHUDCountdown.getLocalTranslation().set(0, 0, 1);
        statNode.attachChild(sheepHUDCountdown);  
		
		Quad topBar = new Quad(name, display.getWidth(), 40);
		
		topBar.setDefaultColor(new ColorRGBA(0, 0, 0, 0.2f));
		
        //alpha blending needed
        BlendState bs = display.getRenderer().createBlendState();
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        bs.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        bs.setTestEnabled(false);
        topBar.setRenderState(bs);
        
        topBar.updateRenderState();
		
        topBar.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        topBar.setLightCombineMode(Spatial.LightCombineMode.Off);
        topBar.getLocalTranslation().set(display.getWidth() * 0.5f, display.getHeight() - 25, 1);
        statNode.attachChild(topBar);  

        timeLeftText = Text.createDefaultTextLabel("Text");
        timeLeftText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        timeLeftText.setLightCombineMode(Spatial.LightCombineMode.Off);
        timeLeftText.setRenderState(FixedFontTextureState.getFixedFontTextureState());
        statNode.attachChild(timeLeftText);

        goText = Text.createDefaultTextLabel("Text", "");
        goText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        goText.setLightCombineMode(Spatial.LightCombineMode.Off);
        goText.setRenderState(FixedFontTextureState.getFixedFontTextureState());
        statNode.attachChild(goText);
        
        sheepIcons = new SheepIcons("SheepStateIcons", sheep);
        sheepIcons.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        sheepIcons.setLightCombineMode(Spatial.LightCombineMode.Off);
        sheepIcons.getLocalTranslation().set(display.getWidth() - sheepIcons.getWidth() - 20, display.getHeight() - 2 - sheepIcons.getIconSizeY() * 0.5f, 1);
        statNode.attachChild(sheepIcons);  
        
        sheepDebugText = Text.createDefaultTextLabel("Text");
        sheepDebugText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        sheepDebugText.setLightCombineMode(Spatial.LightCombineMode.Off);
        sheepDebugText.setLocalTranslation(new Vector3f(0, 20, 1));
        sheepDebugText.setRenderState(FixedFontTextureState.getFixedFontTextureState());
        statNode.attachChild(sheepDebugText);
        
        // update top bar
        float barHeight = Math.max(40, sheepIcons.getIconSizeY() + 6);
        topBar.resize(topBar.getWidth(), barHeight);
        topBar.getLocalTranslation().setY(display.getHeight() - barHeight * 0.5f);
        
    	float textSize = display.getWidth() / 640f * 1.2f;
    	float textOffset = 7 * textSize;        
        
        timeLeftText.setLocalTranslation(new Vector3f(20, display.getHeight() - barHeight * 0.5f - textOffset, 1));
        timeLeftText.setLocalScale(textSize);
	}
    
    private void reInit() {
		spaceship.removeAllAbducables();
		scene.detachAllChildren();
		sheepGroup.detachAllChildren();
		fader.clearControllers();
		rootNode.detachAllChildren();
		statNode.detachAllChildren();
		collisionManager.removeAllChecks();
		pManager.cleanUp();
		pManager.clearAll();
		cleanup();
		init(); 
		
		statNode.setCullHint(Spatial.CullHint.Always);
		spaceship.getSpaceshipHandler().setEnabled(false);
		
		beginTime = Timer.getTimer().getTimeInSeconds();
		endTime = beginTime + WATER_RISE_TIME + SHIP_ACCELERATE_TIME;
		
		isGameOver = false;
		hasSwitchedToTitle = false;
		volcanoSequenceHasShown = false;
		volcanoSequenceShowing = false;
		sheepSequenceHasShown = false;
		sheepSequenceBaaPlayed = false;
		sheepSequenceShowing = false;
		wolfHasTeleported = false;
		wolfTeleporting = false;
		goTextHasShown = false;
		goTextShowing = false;
		gameStarted = false;
		
		totalUnstacked = 0;
    }
    
    public void startSound() {
		waterAmbientSound.fadeIn(2, WATER_INITIAL_VOLUME);
		waterAmbientSound.play();
		earthquakeAmbientSound.play();		
    }

    public void stopSound() {
    	waterAmbientSound.stop();
    	earthquakeAmbientSound.stop();
    }
    
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		
		if (active) {
			reInit();
			
			startSound();
		} else {
			stopSound();
		}
	}
	
	public int getNoFreeSheep() {
		int	result = 0;
		
		for (int i = 0; i < sheep.length; i++) {
			if (!sheep[i].hasAbductionCompleted()) {
				result++;
			}
		}
		
		return result;
	}
}
