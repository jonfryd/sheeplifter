package dk.impact.sheeplifter.spatials.actors.ship;

import com.jme.animation.SpatialTransformer;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Controller;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.effects.particles.ParticleMesh;
import com.jmex.model.animation.KeyframeController;
import dk.impact.sheeplifter.util.ModelLoader;

/**
 * @author Jeppe Schmidt <js@certus.dk>
 * @version $Revision$
 */
public class AbductorBeam extends Node {

    public static final int CONE_AXIS_SAMPLES = 2;

    public static final int CONE_RADIAL_SAMPLES = 40;

    public static final float CONE_FULL_HEIGHT = 25 * 5f;

    public static final float CONE_BOTTOM_RADIUS = 5f * 5f;

    public static final float CONE_TOP_RADIUS = 2.5f * 5f;

    public static final float CONE_BEAM_END_RADIUS = 0.5f * 5f;

    private KeyframeController startKFController = new KeyframeController();

    private SpatialTransformer startSPTransformer = new SpatialTransformer(1);

    private KeyframeController endKFController = new KeyframeController();

    private SpatialTransformer endSPTransformer = new SpatialTransformer(1);

    private Texture beamTexture;

    private boolean active;

    private Cylinder morphingMesh;

    private Cylinder fullBeamCone;

    private Cylinder startBeamCone;

    private ParticleMesh updriftParticleMesh;

    public AbductorBeam(){
        super("AbductorBeam");

        morphingMesh = new Cylinder("morphingMesh", CONE_AXIS_SAMPLES, CONE_RADIAL_SAMPLES, 0, 0);
        startBeamCone = new Cylinder("startCone", CONE_AXIS_SAMPLES, CONE_RADIAL_SAMPLES, CONE_BEAM_END_RADIUS, CONE_BEAM_END_RADIUS);
        fullBeamCone = new Cylinder("endCone", CONE_AXIS_SAMPLES, CONE_RADIAL_SAMPLES, CONE_BOTTOM_RADIUS, CONE_TOP_RADIUS, CONE_FULL_HEIGHT, false, false);

        buildBeamStartAnimation();
        buildBeamEndAnimation();
        setupAppearance();
        buildUpdriftParticles();
        attachChild(morphingMesh);

        startKFController.setActive(false);
        startSPTransformer.setActive(false);
        endKFController.setActive(false);
        endSPTransformer.setActive(false);
    }        

    private void buildBeamStartAnimation(){

        // Setup morphing.
        startKFController.setMorphingMesh(morphingMesh);
        startKFController.setKeyframe(0, startBeamCone);
        startKFController.setKeyframe(0.175f, fullBeamCone);
        startKFController.setRepeatType(Controller.RT_CLAMP);
        morphingMesh.addController(startKFController);

        // Fix orientation of the morphed mesh.
        Quaternion q = new Quaternion();
        q.fromAngles(FastMath.PI / 2, 0, 0);

        startSPTransformer.setObject(morphingMesh, 0, -1);
        startSPTransformer.setRotation(0, 0, q);
        startSPTransformer.setPosition(0, 0, new Vector3f(0, -2, 0));
        startSPTransformer.setPosition(0, 0.175f, new Vector3f(0, (float) -CONE_FULL_HEIGHT / 2f - 2, 0));
        startSPTransformer.setRepeatType(Controller.RT_CLAMP);
        startSPTransformer.interpolateMissing();
        morphingMesh.addController(startSPTransformer);
    }

    private void buildBeamEndAnimation(){
        Cylinder slimCone = new Cylinder("slimCone", CONE_AXIS_SAMPLES, CONE_RADIAL_SAMPLES, CONE_BEAM_END_RADIUS, CONE_FULL_HEIGHT);

        endKFController.setMorphingMesh(morphingMesh);
        endKFController.setKeyframe(0, fullBeamCone);
        endKFController.setKeyframe(0.4f, slimCone);
        endKFController.setKeyframe(1.06f, startBeamCone);
        endKFController.setRepeatType(Controller.RT_CLAMP);
        morphingMesh.addController(endKFController);

        // Fix orientation of the morphed mesh.
        Quaternion q = new Quaternion();
        q.fromAngles(FastMath.PI / 2, 0, 0);

        endSPTransformer.setObject(morphingMesh, 0, -1);
        endSPTransformer.setRotation(0, 0, q);
        endSPTransformer.setPosition(0, 0, new Vector3f(0, (float) -CONE_FULL_HEIGHT / 2f - 2, 0));
        endSPTransformer.setPosition(0, 0.4f, new Vector3f(0, (float) -CONE_FULL_HEIGHT / 2f - 2, 0));
        endSPTransformer.setPosition(0, 1.06f, new Vector3f(0, -2, 0));
        endSPTransformer.setRepeatType(Controller.RT_CLAMP);
        endSPTransformer.interpolateMissing();
        morphingMesh.addController(endSPTransformer);
    }

    private void setupAppearance(){
        setLightCombineMode(LightCombineMode.Off);

        // Set the cones material and appearance.
        // Apply alpha, and ZBuffer render states
        final Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        final BlendState blendState = renderer.createBlendState();
        morphingMesh.setRenderState(blendState);
        blendState.setBlendEnabled(true);
        blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        blendState.setDestinationFunction(BlendState.DestinationFunction.One);
        blendState.setTestEnabled(true);
        blendState.setTestFunction(BlendState.TestFunction.GreaterThan);


        final ZBufferState zBufferState = renderer.createZBufferState();
        morphingMesh.setRenderState(zBufferState);
        zBufferState.setWritable(false);
        zBufferState.setEnabled(true);

        /*
        MaterialState material = renderer.createMaterialState();
        material.setAmbient(new ColorRGBA(0.1f, 0.1f, 0.1f, 0.0f));
        material.setDiffuse(new ColorRGBA(0.1f, 0.1f, 0.1f, 0.0f));
        material.setSpecular(new ColorRGBA(0.1f, 0.1f, 0.1f, 0.0f));
        material.setEmissive(new ColorRGBA(0.1f, 0.1f, 0.1f, 0.0f));               
        material.setShininess(0);
        morphingMesh.setRenderState(material);
        */


        // Add texture
        beamTexture = TextureManager.loadTexture(AbductorBeam.class.getClassLoader().getResource("res/geometry/beam.png"),
                                                    Texture.MinificationFilter.Trilinear,
                                                    Texture.MagnificationFilter.Bilinear);
        beamTexture.setEnvironmentalMapMode(Texture.EnvironmentalMapMode.SphereMap);
        beamTexture.setTranslation(new Vector3f());
        beamTexture.setWrap(Texture.WrapMode.Repeat);

        TextureState ts = renderer.createTextureState();
        ts.setTexture(beamTexture);
        ts.setEnabled(true);
        ts.apply();
        morphingMesh.setRenderState(ts);

        setModelBound(new BoundingBox());
        updateModelBound();

        morphingMesh.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
        morphingMesh.updateRenderState();

        setCullHint(CullHint.Never);
    }

    private void buildUpdriftParticles(){
        updriftParticleMesh = (ParticleMesh) ModelLoader.loadJMEModel("res/geometry/beam_particles.jme");
        updriftParticleMesh.setCullHint(Spatial.CullHint.Always);
        updriftParticleMesh.getParticleController().setControlFlow(true);
        updriftParticleMesh.setReleaseRate(1000);
        //updriftParticleMesh.setSpeed(0.05f);

        // Apply alpha, and ZBuffer render states
        final Renderer r = DisplaySystem.getDisplaySystem().getRenderer();
        final BlendState bs = r.createBlendState();
        updriftParticleMesh.setRenderState(bs);
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        bs.setDestinationFunction(BlendState.DestinationFunction.One);
        bs.setTestEnabled(true);
        bs.setTestFunction(BlendState.TestFunction.GreaterThan);

        final ZBufferState zs = r.createZBufferState();
        updriftParticleMesh.setRenderState(zs);
        zs.setWritable(false);
        zs.setEnabled(true);

        updriftParticleMesh.setParticlesInWorldCoords(false);

        updriftParticleMesh.setStartSize(2.7f);
        updriftParticleMesh.setEndSize(2.7f);
        updriftParticleMesh.setMinimumLifeTime(410);
        updriftParticleMesh.setMaximumLifeTime(10);
        updriftParticleMesh.updateRenderState();
        updriftParticleMesh.setLocalScale(0.55f);
        updriftParticleMesh.setLocalTranslation(0, -CONE_FULL_HEIGHT, 0);

        attachChild(updriftParticleMesh);
    }
    
    public void update(float timeDelta){
        beamTexture.getTranslation().y += 0.3f * timeDelta;
        if(beamTexture.getTranslation().y > 1){
            beamTexture.getTranslation().y = 0;
        }
        if(active && startSPTransformer.getCurTime() >= 0.15 && updriftParticleMesh.getCullHint() != CullHint.Never){            
            updriftParticleMesh.setCullHint(CullHint.Never);
        }
    }

    public void setActive(boolean active){
        this.active = active;
        if(active){
            endKFController.setActive(false);
            endSPTransformer.setActive(false);

            startKFController.setCurTime(0);
            startKFController.setActive(true);
            startSPTransformer.setCurTime(0);
            startSPTransformer.setActive(true);

        } else {
            updriftParticleMesh.setCullHint(CullHint.Always);
            startKFController.setActive(false);
            startSPTransformer.setActive(false);

            endKFController.setCurTime(0);
            endKFController.setActive(true);
            endSPTransformer.setCurTime(0);
            endSPTransformer.setActive(true);
        }
    }

    public boolean isActive(){
        return active;
    }
    
    public void draw(Renderer r) {
        updriftParticleMesh.getWorldRotation().loadIdentity();
        super.draw(r);
    }
}
