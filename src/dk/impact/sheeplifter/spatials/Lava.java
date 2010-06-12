package dk.impact.sheeplifter.spatials;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.math.FastMath;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.bounding.BoundingBox;

import dk.impact.sheeplifter.TitleState;

import java.util.ArrayList;

public class Lava extends Node {
	private static final long serialVersionUID = 1L;
	
	private Quad			lavaQuad;
	private DisplaySystem	display;

    private ArrayList <LavaBurst> lavaBursts = new ArrayList<LavaBurst>();

    private Texture texture1;
    private Texture texture2;
    private float angle2;
    private float angle1;
    private float transAngle;

    public Lava(String name) {
		super(name);
		display = DisplaySystem.getDisplaySystem(); 
		buildQuad();
        buildBursts(1);
        updateRenderState();
    }

    protected void buildBursts(int count){
        for(int i=0; i<count; i++){
            LavaBurst lavaBurst =  new LavaBurst(lavaQuad);
            attachChild(lavaBurst);
            lavaBursts.add(lavaBurst);
        }
    }

    protected void buildQuad() {
    	lavaQuad = new Quad("LavaQuad");
    	
    	lavaQuad.setLightCombineMode(Spatial.LightCombineMode.Off);

    	// Load texture 1.
        texture1 = TextureManager.loadTexture(TitleState.class.getClassLoader().getResource("res/maps/lava1.png"),
                                                  Texture.MinificationFilter.Trilinear,
                                                  Texture.MagnificationFilter.Bilinear);
        texture1.setTranslation(new Vector3f());
        texture1.setWrap(Texture.WrapMode.Repeat);        
        texture1.setApply(Texture.ApplyMode.Decal);
        texture1.setScale(new Vector3f(3.5f, 3.5f, 3.5f));

        // Load texture 2.
        texture2 = TextureManager.loadTexture(TitleState.class.getClassLoader().getResource("res/maps/lava2.jpg"),
                                                  Texture.MinificationFilter.Trilinear,
                                                  Texture.MagnificationFilter.Bilinear);
        texture2.setTranslation(new Vector3f());
        texture2.setWrap(Texture.WrapMode.Repeat);
        texture2.setScale(new Vector3f(3.5f, 3.5f, 3.5f));


        TextureState ts = display.getRenderer().createTextureState();
        ts.setTexture(texture2, 0);
        ts.setTexture(texture1, 1);

        ts.setEnabled(true);

        lavaQuad.setRenderState(ts);
		lavaQuad.rotateUpTo(new Vector3f(0, 0, 1));
		lavaQuad.updateRenderState();
		lavaQuad.updateGeometricState(0, true);

        lavaQuad.copyTextureCoordinates(0, 1, 1.0f);
        lavaQuad.setLightCombineMode(LightCombineMode.Off);

        lavaQuad.setModelBound(new BoundingBox());
        lavaQuad.updateModelBound();

        attachChild(lavaQuad);
    }


    
    public void update(float timeDelta) {
        // Animate Textures
        texture1.getTranslation().x = FastMath.cos(angle1) * 0.2f;
        texture1.getTranslation().y = FastMath.sin(angle1) * 0.3f;
        angle1 += 0.04f * timeDelta;

        texture2.getTranslation().x = FastMath.sin(angle2) * 0.5f;
        texture2.getTranslation().y = FastMath.cos(angle2) * 0.5f;
        angle2 += 0.065 * timeDelta;
        
        // Update bursts.
        for(LavaBurst burst : lavaBursts){
            burst.update(timeDelta);
        }

        // Wave effect.
        /*
        getLocalTranslation().y += FastMath.sin(transAngle) * 0.0004;
        transAngle += 0.2f * timeDelta;
        */
    }

    public Quad getLavaQuad() {
    	return lavaQuad;
    }
}
