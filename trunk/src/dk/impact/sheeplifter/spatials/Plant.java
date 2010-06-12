package dk.impact.sheeplifter.spatials;

import com.jme.image.Texture.WrapAxis;
import com.jme.image.Texture.WrapMode;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;

import dk.impact.sheeplifter.util.ModelLoader;

public class Plant extends Node {
	private static final long serialVersionUID = 1L;
    
    private Spatial 		model;
    private String			filename;
    private DisplaySystem 	display;
	
    public Plant(String name, String filename, DisplaySystem display) {
    	super(name);
    	this.filename = filename;
    	this.display = display;
    	
    	build();
    }
    
    protected void build() {
    	loadModel();
    	attachChild(model);
    }
    
	protected void loadModel() {
		model = ModelLoader.loadJMEModel(filename);

		TextureState ts = (TextureState) getModelTriMesh().getRenderState(RenderState.StateType.Texture);
		ts.getTexture(0).setWrap(WrapAxis.T, WrapMode.Clamp);
		
		//scale it to be smaller than it is originally
        model.setLocalScale(0.8f);

        //alpha blending needed
        BlendState bs = display.getRenderer().createBlendState();
        setRenderState(bs);
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        bs.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        bs.setTestEnabled(false);
        bs.setReference(0.5f);
        bs.setTestFunction(BlendState.TestFunction.GreaterThan);
        bs.setEnabled(true);

        final ZBufferState zs = display.getRenderer().createZBufferState();
        setRenderState(zs);
        zs.setWritable(false);
        zs.setEnabled(true);		
        
        setLightCombineMode(Spatial.LightCombineMode.Off);
        
        setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
		
        updateGeometricState(0, true);
        updateRenderState();        
	}
	
	public TriMesh getModelTriMesh() {
		Spatial	spatial = ((Node) model).getChild(0);
		
		if (spatial instanceof Node) {
			spatial = ((Node) spatial).getChild(0);
		}
		
		return (TriMesh) spatial;
	}

	public Spatial getModel() {
		return model;
	}
}
