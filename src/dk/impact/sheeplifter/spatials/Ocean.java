package dk.impact.sheeplifter.spatials;

import com.jme.math.FastMath;
import com.jme.math.Plane;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jmex.effects.water.WaterHeightGenerator;
import com.jmex.effects.water.WaterRenderPass;

import dk.impact.sheeplifter.effects.ProjectedGridFixed;

public class Ocean extends Node {
	private static final long serialVersionUID = 1L;

	private static final float GLOBAL_MAX_WAVE_AMPLITUDE = 10.0f;
	
	private WaterRenderPass waterEffectRenderPass;
	private ProjectedGridFixed projectedGrid;
	private WaterHeightGenerator heightGenerator;
	private Node reflectedNode;

	public Ocean(Camera cam, Node sky) {
		super("Ocean");

		// can't seem to find a nicer way to do this...
		WaterRenderPass.foamMapTextureString = "res/maps/oceanfoam.png";

		build(cam, sky);
	}
	
	protected void build(Camera cam, Node sky) {
		reflectedNode = new Node("reflectNode");
		reflectedNode.attachChild(sky);
		attachChild(reflectedNode);

		waterEffectRenderPass = new WaterRenderPass(cam, 4, true, false);
		waterEffectRenderPass.setClipBias(0.5f);
		waterEffectRenderPass.useFadeToFogColor(true);
		waterEffectRenderPass.setWaterMaxAmplitude(GLOBAL_MAX_WAVE_AMPLITUDE);
		waterEffectRenderPass.setWaterPlane(new Plane(new Vector3f(0.0f, 1.0f, 0.0f), 0.0f));

		heightGenerator = new WaterHeightGenerator();

		projectedGrid = new ProjectedGridFixed("ProjectedGrid", cam, 50, 50, 0.01f, heightGenerator);
		// or implement your own waves like this(or in a separate class)...
		// projectedGrid = new ProjectedGrid( "ProjectedGrid", cam, 50, 50,
		// 0.01f, new HeightGenerator() {
		// public float getHeight( float x, float z, float time ) {
		// return
		// FastMath.sin(x*0.05f+time*2.0f)+FastMath.cos(z*0.1f+time*4.0f)*2;
		// }
		// } );

		waterEffectRenderPass.setReflectedScene(reflectedNode);
		waterEffectRenderPass.setWaterEffectOnSpatial(projectedGrid);
		waterEffectRenderPass.setSkybox(sky);

		attachChild(projectedGrid);

		// Max wave amplitude is the default
		setWaveHeight(GLOBAL_MAX_WAVE_AMPLITUDE); 
	}
	
	public Node getReflectedNode() {
		return reflectedNode;
	}
	
	public WaterRenderPass getWaterRenderPass() {
		return waterEffectRenderPass;
	}
	
	public ProjectedGridFixed getProjectedGrid() {
		return projectedGrid;
	}
	
	public WaterHeightGenerator getWaterHeightGenerator() {
		return heightGenerator;
	}
	
	public void setWaveHeight(float height) {
		height = FastMath.abs(height); //positive values only!
		
		if (height > GLOBAL_MAX_WAVE_AMPLITUDE) {
			height = GLOBAL_MAX_WAVE_AMPLITUDE;
		}
		
		heightGenerator.setHeightbig(height);
		heightGenerator.setHeightsmall(height * 0.3f);
	}
	
	public void setWorldWaterLevel(float level) {
		reflectedNode.setLocalTranslation(0, -level, 0);		
	}
}
