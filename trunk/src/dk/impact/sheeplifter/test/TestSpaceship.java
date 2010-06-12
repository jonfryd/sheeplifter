package dk.impact.sheeplifter.test;

import com.jme.app.SimplePassGame;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.input.ChaseCamera;
import com.jme.input.thirdperson.ThirdPersonMouseLook;
import com.jme.light.PointLight;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.pass.RenderPass;
import com.jme.renderer.pass.ShadowedRenderPass;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.MaterialState;
import com.jme.util.TextureManager;
import dk.impact.sheeplifter.spatials.actors.sheep.HollowAbductableSheep;
import dk.impact.sheeplifter.spatials.actors.ship.Spaceship;

import java.util.HashMap;

/**
 * @author Jeppe Schmidt <jeppe.schmidt@gmail.com>
 * @version $Revision$
 */
public class TestSpaceship extends SimplePassGame{

    private ShadowedRenderPass shadowPass;

    private HollowAbductableSheep sheep;

    Quad ground;

    dk.impact.sheeplifter.spatials.actors.ship.Spaceship spaceship;

    ChaseCamera chaser;

    public static void main(String[] args) {
        TestSpaceship app = new TestSpaceship();
        app.setConfigShowMode(ConfigShowMode.AlwaysShow);
        app.start();
    }

    public TestSpaceship(){
        shadowPass = new ShadowedRenderPass();
        shadowPass.setRenderShadows(true);
        shadowPass.setLightingMethod(ShadowedRenderPass.LightingMethod.Modulative);

        stencilBits = 8; // we need a minimum stencil buffer at least.
    }

    protected void simpleInitGame(){
        // Add spaceship.
        spaceship = new Spaceship();
        spaceship.getLocalTranslation().setY(25);
        rootNode.attachChild(spaceship);

        // Add ground.
        ground = new Quad("ground", 1000, 1000);
        ground.setModelBound(new BoundingBox());
        ground.updateModelBound();
        ground.setLocalTranslation(0, 0, 0);
        Quaternion q = new Quaternion();
        q.fromAngles(-FastMath.PI * 0.5f, 0, 0);
        ground.setLocalRotation(q);

        Texture groundTex = TextureManager.loadTexture(TestSpaceship.class.getClassLoader().getResource("jmetest/data/images/Monkey.jpg"),
                                                    Texture.MinificationFilter.Trilinear,
                                                    Texture.MagnificationFilter.Bilinear);
        TextureState ts = display.getRenderer().createTextureState();
        ts.setTexture(groundTex);
        ts.setEnabled(true);
        ground.setRenderState(ts);
        MaterialState groundMat = display.getRenderer().createMaterialState();
        groundMat.setEmissive(new ColorRGBA(0, 0, 0, 0));
        groundMat.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, 0));
        groundMat.setDiffuse(new ColorRGBA(0.8f, 0.8f, 0.8f, 1));        
        groundMat.setEnabled(true);
        ground.setRenderState(groundMat);        
        rootNode.attachChild(ground);

        // Add a test sheep.
        sheep = new HollowAbductableSheep(spaceship);
        rootNode.attachChild(sheep);

        // Make the sheep abductable.
        spaceship.addAbductable(sheep);

        /** Set up a basic, default light. */
        lightState.detachAll();
        PointLight light = new PointLight();
        light.setSpecular( new ColorRGBA( 1, 1, 1, 1 ));
        light.setDiffuse( new ColorRGBA( 0.75f, 0.75f, 0.75f, 1 ) );
        light.setAmbient( new ColorRGBA( 0.35f, 0.35f, 0.35f, 1.0f ) );
        light.setLocation( new Vector3f( 0, 1350, 0 ) );
        light.setEnabled( true );
        light.setShadowCaster(true);

        /** Attach the light to a lightState and the lightState to rootNode. */
        lightState.attach( light );

        // Create a box so that we can see where the light is.
        Box lightBox = new Box("lightbox", new Vector3f(0, 0, 0), 2, 2, 2);
        lightBox.getLocalTranslation().set(light.getLocation());
        lightBox.setModelBound(new BoundingBox());
        lightBox.updateModelBound();

        rootNode.attachChild(lightBox);

        buildChaseCamera();


        setupRenderPasses();
    }

    protected void simpleUpdate(){
        float interpolation = timer.getTimePerFrame();
        spaceship.update(interpolation);
        chaser.update(interpolation);
    }

    private void buildChaseCamera() {
        Vector3f targetOffset = new Vector3f(0, 2, 0);

        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put(ThirdPersonMouseLook.PROP_MAXROLLOUT, "70");
        props.put(ThirdPersonMouseLook.PROP_MINROLLOUT, "20");
        props.put(ChaseCamera.PROP_TARGETOFFSET, targetOffset);
        props.put(ThirdPersonMouseLook.PROP_MAXASCENT, ""+45 * FastMath.DEG_TO_RAD);
        props.put(ChaseCamera.PROP_DAMPINGK, "" + 60);
        props.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(70, 0, 10 * FastMath.DEG_TO_RAD));
        props.put(ChaseCamera.PROP_TARGETOFFSET, targetOffset);
        chaser = new ChaseCamera(cam, spaceship.getShipNode(), props);
        chaser.setMaxDistance(120);
        chaser.setMinDistance(90);
    }


    private void setupRenderPasses() {
		// Setup the normal render pass
        RenderPass normalPass = new RenderPass();
		normalPass.add(sheep);
		pManager.add(normalPass);

        // Setup the shadow pass.
        shadowPass.add(ground);
        shadowPass.add(spaceship.getShipNode());
        shadowPass.addOccluder(spaceship.getShipNode());
        pManager.add(shadowPass);

        RenderPass alphaPass = new RenderPass();
        alphaPass.add(spaceship.getAbductorBeam());
        //alphaPass.add(spaceship.getAbductorBeam().getUpdriftParticleMesh());
        alphaPass.add(sheep.getWoolParticles());
        pManager.add(alphaPass);
    }


}
