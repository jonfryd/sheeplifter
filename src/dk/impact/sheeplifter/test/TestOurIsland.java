package dk.impact.sheeplifter.test;

import com.jme.app.SimplePassGame;
import com.jme.input.FirstPersonHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.light.PointLight;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.renderer.pass.RenderPass;
import com.jme.renderer.pass.ShadowedRenderPass;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.state.FogState;
import com.jme.scene.state.RenderState;
import com.jme.util.TextureManager;

import dk.impact.sheeplifter.audio.GameAudioSystem;
import dk.impact.sheeplifter.spatials.CollisionManager;
import dk.impact.sheeplifter.spatials.IslandTerrain;
import dk.impact.sheeplifter.spatials.Ocean;
import dk.impact.sheeplifter.spatials.Plant;
import dk.impact.sheeplifter.spatials.PlantArea;
import dk.impact.sheeplifter.spatials.Sky;
import dk.impact.sheeplifter.spatials.Tree;
import dk.impact.sheeplifter.spatials.World;
import dk.impact.sheeplifter.spatials.actors.sheep.Sheep;
import dk.impact.sheeplifter.spatials.actors.sheep.SheepBody;
import dk.impact.sheeplifter.util.Debugging;

/**
 */
public class TestOurIsland extends SimplePassGame {
	private static final float farPlane = 3000.0f;

	private Sky 				sky;
	private IslandTerrain 		islandTerrain;
	private Ocean 				ocean;
	
	private World				world;

	private Node				sheepGroup;
	private Sheep				sheep[];

	private Node				planting;
	
	private Node 				scene;
	private ShadowedRenderPass 	shadowPass;

	private GameAudioSystem		gameAudioSystem;
	
	private CollisionManager	collisionManager;
	
	private Text				sheepDebugText;
	

	public TestOurIsland() {
		scene = new Node("scene");
		shadowPass = new ShadowedRenderPass();
        shadowPass.setRenderShadows(true);
        shadowPass.setLightingMethod(ShadowedRenderPass.LightingMethod.Modulative);
		
        // stencil buffer needed for shadows
        stencilBits = 8; 
        
        // no texture compression by default - looks damn ugly!
        TextureManager.COMPRESS_BY_DEFAULT = false;
	}
	
	/**
	 * Entry point for the test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		TestOurIsland app = new TestOurIsland();
		
		if (args.length > 0 && args[0].equals("-console")) {
			Debugging.openConsole(app);
		}
		
		app.setConfigShowMode(ConfigShowMode.AlwaysShow, TestOurIsland.class.getClassLoader().getResource("res/maps/2.png"));
		app.start();
	}
	
	protected void simpleUpdate() {
        float interpolation = timer.getTimePerFrame();
		
		if (KeyBindingManager.getKeyBindingManager().isValidCommand("g", false)) {
			detachLandscape();
			buildLandscape();
			attachLandscape();
			buildPlanting();
			// update render state after replacing landscape
			rootNode.updateRenderState();
		}

        //We don't want the chase camera to go below the world, so always keep 
        //it 7 units above the level.
        World.SurfacePoint surfacePoint = world.getSurfacePointAt(cam.getLocation());
		
        if(cam.getLocation().y < (surfacePoint.getHeight() + 7)) {
            cam.getLocation().y = (surfacePoint.getHeight() + 7);
        }		
		
		for (int i = 0; i < sheep.length; i++) {
			sheep[i].update(interpolation);
		}
		
		collisionHandling();

		// Move sheep a bit closer to each other
		for (int i = 0; i < sheep.length; i++) {
			for (int j = (i + 1); j < sheep.length; j++) {
				Vector3f dir = sheep[j].getBody().getLocalTranslation().subtract(sheep[i].getBody().getLocalTranslation());
				sheep[j].getBody().getLocalTranslation().subtractLocal(dir.mult(0.01f));
				sheep[i].getBody().getLocalTranslation().addLocal(dir.mult(0.01f));
			}
		}
		
		sky.getLocalTranslation().set(cam.getLocation());
		cam.update();
		
        //Because we are changing the scene (moving the skybox and player) we need to update
        //the graph.
        rootNode.updateGeometricState(interpolation, true);		
        scene.updateGeometricState(interpolation, true);        
        
        //update audio system
        gameAudioSystem.updateAudio();
        
        //
        sheepDebugText.print(sheep[0].toString());
	}
	
	private void collisionHandling() {
		boolean 	repeat;
		int 		iter = 10;
		
		do {
			repeat = collisionManager.handleCollisions();
		} while(repeat && (iter-- > 0));
	}

	protected void cleanup() {
		super.cleanup();
		
		if (ocean != null) {
			ocean.getWaterRenderPass().cleanup();
		}
		if (gameAudioSystem != null) {
			gameAudioSystem.cleanup();		
		}
	}

	/**
	 * builds the trimesh.
	 * 
	 * @see com.jme.app.SimpleGame#initGame()
	 */
	protected void simpleInitGame() {
		display.setTitle("Our Island Test");
		cam.setLocation(new Vector3f(128 * 5, 260, 128 * 5));
		cam.setFrustumPerspective(45.0f, (float) display.getWidth()
				/ (float) display.getHeight(), 1f, farPlane);
		cam.update();

		setupKeyBindings();

		setupLighting();
		setupFog();
		
		collisionManager = new CollisionManager();
		
		gameAudioSystem = new GameAudioSystem(cam);
		gameAudioSystem.playMusic(gameAudioSystem.registerMusic("res/sound/sheep_game_capture03.ogg"));

		buildLandscape();
		buildSkyBox();
		buildOcean();
		
		world = new World(islandTerrain, ocean);
		buildSheep();
		
		setupSceneGraph();
		
		rootNode.updateGeometricState(0, true);		
		
		buildPlanting();
		rootNode.attachChild(planting);
	
		setupRenderPasses();
	
		rootNode.setCullHint(Spatial.CullHint.Dynamic);
		
		input = new FirstPersonHandler(cam, 250, 1);
	}

	private void setupSceneGraph() {
		rootNode.attachChild(ocean); 
		ocean.getReflectedNode().attachChild(scene); // node used to render reflection effect in ocean
		
		scene.attachChild(sheepGroup);
		attachLandscape();
	}

	private void setupLighting() {
		lightState.setTwoSidedLighting(true);
		((PointLight) lightState.get(0)).setLocation(new Vector3f(2500, 2500, 2500));
		lightState.get(0).setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
		lightState.get(0).setShadowCaster(true);
	}

	private void setupRenderPasses() {
        pManager.add(ocean.getWaterRenderPass());
		
		RenderPass rootPass = new RenderPass();
		rootPass.add(sky);
		rootPass.add(ocean);
		pManager.add(rootPass);
		
        shadowPass.add(scene);
        shadowPass.addOccluder(sheepGroup);
        pManager.add(shadowPass);	
        
		RenderPass statPass = new RenderPass();
		statPass.add(planting);
		statPass.add(statNode);
		pManager.add(statPass);	
	}
	
	private void setupFog() {
		FogState fogState = display.getRenderer().createFogState();
		fogState.setDensity(1.0f);
		fogState.setEnabled(true);
		fogState.setColor(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
		fogState.setEnd(farPlane);
		fogState.setStart(farPlane / 10.0f);
		fogState.setDensityFunction(FogState.DensityFunction.Linear);
		fogState.setQuality(FogState.Quality.PerVertex);
		rootNode.setRenderState(fogState);
	}

	private void setupKeyBindings() {
		KeyBindingManager.getKeyBindingManager().set("g", KeyInput.KEY_G);

		sheepDebugText = Text.createDefaultTextLabel("Text", "hest");
		sheepDebugText.setRenderQueueMode(Renderer.QUEUE_ORTHO);
		sheepDebugText.setLightCombineMode(Spatial.LightCombineMode.Off);
		sheepDebugText.setLocalTranslation(new Vector3f(0, 20, 1));
        statNode.attachChild(sheepDebugText);	
	}

	private void buildSkyBox() {
		sky = new Sky("sky", farPlane * 0.5f, farPlane * 0.5f, farPlane * 0.5f, display);
	}

	private void detachLandscape() {
		scene.detachChild(islandTerrain);
		world.setIslandTerrain(null);
		ocean.getProjectedGrid().setTerrainBlock(null);
	}

	private void attachLandscape() {
		scene.attachChild(islandTerrain);
		world.setIslandTerrain(islandTerrain);
		ocean.getProjectedGrid().setTerrainBlock(islandTerrain.getTerrainBlock());
	}

	private void buildLandscape() {
		islandTerrain = new IslandTerrain("island", display);
		islandTerrain.setLocalTranslation(0, -20, 0);
	}
	
	private void buildOcean() {
		ocean = new Ocean(cam, sky);
	}
	
	private void buildSheep() {
		sheepGroup = new Node("sheep group");
		
		sheep = new Sheep[1];
		
		for(int i = 0; i < sheep.length; i++) {
			sheep[i] = new Sheep("sheep" + (i + 1), gameAudioSystem, world);
			sheep[i].getBody().setLocalTranslation(128 * 5, 0, 128 * 5);
			sheepGroup.attachChild(sheep[i].getBody());
		}
		
		// add collision checks for all sheep against each other
		for (int i = 0; i < sheep.length; i++) {
			for (int j = (i + 1); j < sheep.length; j++) {
				collisionManager.addCheck(sheep[i].getBody(), sheep[j].getBody(), sheep[i].getBody());
			}
		}
	}
	
	private void buildPlanting() {
		if (planting == null) {
			planting = new Node("planting");
		} 
		else {
			planting.detachAllChildren();
		}

		Plant grassTypes[] = new Plant[6];
		Plant bushTypes[] = new Plant[9];
		Plant treeTypes[] = new Plant[3];
		
		for (int i = 0; i < grassTypes.length; i++) {
			grassTypes[i] = new Plant("grass" + (i + 1), "res/geometry/grass_" + (i + 1) + ".jme", display);
		}
		for (int i = 0; i < bushTypes.length; i++) {
			bushTypes[i] = new Plant("bush" + (i + 1), "res/geometry/bush" + (i + 1) + ".jme", display);
		}
		
		treeTypes[0] = new Tree("tree1", "res/geometry/tree2.jme", display);
		treeTypes[1] = new Tree("tree2", "res/geometry/bigtree1.jme", display);
		treeTypes[2] = new Tree("tree3", "res/geometry/bigtree2.jme", display);

		for (int i = 1; i <= 40; i++) {
			int			plantType = (int) (3 * Math.random());
			Plant		plantArray[] = null;			
			int 		groupSize = 0;
			
			float x = 128 * 5 + (float) (4600f * Math.random()) - 300f;
			float z = 128 * 5 + (float) (4600f * Math.random()) - 300f;
			
			switch(plantType) {
				case 0:
					plantArray = grassTypes;
					groupSize = (int) (150 * Math.random() + 30);					
					break;
				case 1:
					plantArray = bushTypes;
					groupSize = (int) (15 * Math.random() + 10);					
					break;
				case 2:
					plantArray = treeTypes;
					groupSize = (int) (10 * Math.random() + 3);					
					break;			
			}
			
			int	subType = (int) (plantArray.length * Math.random());

			planting.attachChild(new PlantArea("PlantArea" + i, plantArray[subType], groupSize, new Vector2f(x, z), islandTerrain));
		}
		
		// inherit render state, light combine mode, etc.
		planting.setRenderState(grassTypes[0].getRenderState(RenderState.RS_BLEND));
		planting.setRenderState(grassTypes[0].getRenderState(RenderState.RS_ZBUFFER));
		planting.setLightCombineMode(grassTypes[0].getLocalLightCombineMode());
		planting.setRenderQueueMode(grassTypes[0].getRenderQueueMode());
		
		planting.updateRenderState();
		planting.updateModelBound();
	}
}
