package dk.impact.sheeplifter.test;

import bsh.Interpreter;
import bsh.util.JConsole;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.jme.app.SimpleGame;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.input.FirstPersonHandler;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.state.CullState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jme.util.geom.Debugger;
import com.jmex.terrain.TerrainBlock;
import com.jmex.terrain.util.MidPointHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;

import dk.impact.sheeplifter.terrain.HemisphereHeightMap;
import dk.impact.sheeplifter.terrain.ProductHeightMap;

/**
 */
public class TestIslandTerrain extends SimpleGame {

	/**
	 * Entry point for the test,
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		TestIslandTerrain app = new TestIslandTerrain();
		
		JConsole console = new JConsole();
		Interpreter i = new Interpreter(console);
		
		try {
			i.set("app", app);
			i.eval("setAccessibility(true)");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JFrame window = new JFrame();
		window.add(console);
		
		window.setVisible(true);
		window.setSize(640, 480);

		new Thread(i).start(); 
		
		app.setConfigShowMode(ConfigShowMode.AlwaysShow);		
		app.start();
	}

	private TerrainBlock tb;

	/**
	 * builds the trimesh.
	 * 
	 * @see com.jme.app.SimpleGame#initGame()
	 */
	protected void simpleInitGame() {
		display.setTitle("Terrain Test");
		cam.setLocation(new Vector3f(128 * 5, 260, 128 * 5));
		cam.setFrustumFar(3000.0f);
		cam.update();

		FogState fs = display.getRenderer().createFogState();
		fs.setEnabled(false);
		rootNode.setRenderState(fs);

		CullState cs = display.getRenderer().createCullState();
		cs.setCullFace(CullState.Face.Back);
		cs.setEnabled(true);

		lightState.setTwoSidedLighting(true);
		Debugger.AUTO_NORMAL_RATIO = .02f;

		((PointLight) lightState.get(0))
				.setLocation(new Vector3f(100, 500, 50));

		MidPointHeightMap heightMap = new MidPointHeightMap(128, 1.35f);
		HemisphereHeightMap damperMap = new HemisphereHeightMap(128, 0.7f);
		ProductHeightMap islandMap = new ProductHeightMap(heightMap, damperMap);

		Vector3f terrainScale = new Vector3f(20, 1.0f, 20);
		tb = new TerrainBlock("Terrain", islandMap.getSize(), terrainScale,
				islandMap.getHeightMap(), new Vector3f(0, 0, 0));
		tb.setDetailTexture(1, 32);
		tb.setModelBound(new BoundingBox());
		tb.updateModelBound();
		tb.setLocalTranslation(new Vector3f(0, 0, 0));
		rootNode.attachChild(tb);
		rootNode.setRenderState(cs);

		ProceduralTextureGenerator pt = new ProceduralTextureGenerator(
				islandMap);
		pt.addTexture(new ImageIcon(TestIslandTerrain.class.getClassLoader()
				.getResource("jmetest/data/texture/grassb.png")), -128, 0, 128);
		pt.addTexture(new ImageIcon(TestIslandTerrain.class.getClassLoader()
				.getResource("jmetest/data/texture/dirt.jpg")), 0, 128, 255);
		pt.addTexture(new ImageIcon(TestIslandTerrain.class.getClassLoader()
				.getResource("jmetest/data/texture/highest.jpg")), 128, 255,
				384);

		pt.createTexture(512);

		TextureState ts = display.getRenderer().createTextureState();
		ts.setEnabled(true);

		Texture t1 = TextureManager.loadTexture(pt.getImageIcon().getImage(),
				Texture.MinificationFilter.Trilinear,
				Texture.MagnificationFilter.Bilinear, true);
		t1.setStoreTexture(true);
		ts.setTexture(t1, 0);

		Texture t2 = TextureManager.loadTexture(TestIslandTerrain.class
				.getClassLoader()
				.getResource("jmetest/data/texture/Detail.jpg"),
				Texture.MinificationFilter.Trilinear,
				Texture.MagnificationFilter.Bilinear);

		ts.setTexture(t2, 1);
		t2.setWrap(Texture.WrapMode.Repeat);

		t1.setApply(Texture.ApplyMode.Combine);
		t1.setCombineFuncRGB(Texture.CombinerFunctionRGB.Modulate);
		t1.setCombineSrc0RGB(Texture.CombinerSource.CurrentTexture);
		t1.setCombineOp0RGB(Texture.CombinerOperandRGB.SourceColor);
		t1.setCombineSrc1RGB(Texture.CombinerSource.PrimaryColor);
		t1.setCombineOp1RGB(Texture.CombinerOperandRGB.SourceColor);

		t2.setApply(Texture.ApplyMode.Combine);
		t2.setCombineFuncRGB(Texture.CombinerFunctionRGB.AddSigned);
		t2.setCombineSrc0RGB(Texture.CombinerSource.CurrentTexture);
		t2.setCombineOp0RGB(Texture.CombinerOperandRGB.SourceColor);
		t2.setCombineSrc1RGB(Texture.CombinerSource.Previous);
		t2.setCombineOp1RGB(Texture.CombinerOperandRGB.SourceColor);
		rootNode.setRenderState(ts);

		rootNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);

		FirstPersonHandler firstPersonHandler = new FirstPersonHandler(cam,
				250, 1);
		input = firstPersonHandler;
	}
}
