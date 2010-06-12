package dk.impact.sheeplifter.spatials;

import javax.swing.ImageIcon;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.state.CullState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.terrain.TerrainBlock;
import com.jmex.terrain.util.CombinerHeightMap;
import com.jmex.terrain.util.MidPointHeightMap;
import com.jmex.terrain.util.ProceduralTextureGenerator;

import dk.impact.sheeplifter.terrain.*;

public class IslandTerrain extends Node {
	private static final long serialVersionUID = 1L;
	private static final String	textureDir = "res/maps/";

	private float highestY;
	
	private TerrainBlock tb;
	
	private Vector3f volcanoHeighestPoint;
	
	public IslandTerrain(String name, DisplaySystem display) {
		super(name);
		
		build(display);
	}
	
	protected void build(DisplaySystem display) {
		MidPointHeightMap terrainMap = new MidPointHeightMap(128, 0.95f);
		HemisphereHeightMap volcanoMap = new HemisphereHeightMap(128, 30f);
		CombinerHeightMap combinedMap = new CombinerHeightMap(volcanoMap,
				terrainMap, CombinerHeightMap.ADDITION);		
		combinedMap.setFactors(0.1f, 0.9f);
		combinedMap.load();
		
		HemisphereHeightMap damperMap = new HemisphereHeightMap(128, 0.7f);
		ShaftHeightMap shaftMap = new ShaftHeightMap(128, 240.0f);
		ProductHeightMap shapingMap = new ProductHeightMap(damperMap, shaftMap);
		ProductHeightMap islandMap = new ProductHeightMap(combinedMap, shapingMap);
		CombinerHeightMap elevatedIslandMap = new CombinerHeightMap(islandMap,
				damperMap, CombinerHeightMap.ADDITION);
		elevatedIslandMap.setFactors(0.85f, 0.15f);
		elevatedIslandMap.load();

		Vector3f terrainScale = new Vector3f(40, 1.7f, 40);
		tb = new TerrainBlock("Terrain", elevatedIslandMap.getSize(),
				terrainScale, elevatedIslandMap.getHeightMap(), new Vector3f(0,
						0, 0));
		
		tb.setDetailTexture(1, 32);
		tb.setModelBound(new BoundingBox());
		tb.updateModelBound();
		tb.setLocalTranslation(new Vector3f(0, 0, 0));
		
		highestY = ((BoundingBox) tb.getModelBound()).yExtent * 2f;
		
		volcanoHeighestPoint = shaftMap.getHeighestPosition();
		volcanoHeighestPoint.multLocal(terrainScale);
		volcanoHeighestPoint.setY(tb.getHeight(volcanoHeighestPoint));

		ProceduralTextureGenerator pt = new ProceduralTextureGenerator(
				elevatedIslandMap);
		pt.addTexture(new ImageIcon(IslandTerrain.class.getClassLoader()
				.getResource(textureDir + "grassb.png")), -128, 0, 128);
		pt.addTexture(new ImageIcon(IslandTerrain.class.getClassLoader()
				.getResource(textureDir + "dirt.jpg")), 0, 128, 255);
		pt.addTexture(new ImageIcon(IslandTerrain.class.getClassLoader()
				.getResource(textureDir + "highest.jpg")), 128, 255,
				384);

		pt.createTexture(256);

		TextureState ts = display.getRenderer().createTextureState();
		ts.setEnabled(true);

		Texture t1 = TextureManager.loadTexture(pt.getImageIcon().getImage(),
				Texture.MinificationFilter.Trilinear,
				Texture.MagnificationFilter.Bilinear, true);
		t1.setStoreTexture(true);
		ts.setTexture(t1, 0);

		Texture t2 = TextureManager.loadTexture(IslandTerrain.class
				.getClassLoader()
				.getResource(textureDir + "Detail.jpg"),
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
		tb.setRenderState(ts);

		CullState cs = display.getRenderer().createCullState();
		cs.setCullFace(CullState.Face.Back);
		cs.setEnabled(true);

		tb.setRenderState(cs);
		
		attachChild(tb);
	}	
	
	public TerrainBlock getTerrainBlock() {
		return tb;
	}
	
	public Vector3f getVolcanoHeight() {
		return volcanoHeighestPoint;
	}
	
	public float getHighestY() {
		return highestY;
	}
}
