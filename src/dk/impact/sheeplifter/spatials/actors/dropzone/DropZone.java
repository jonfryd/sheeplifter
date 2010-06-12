package dk.impact.sheeplifter.spatials.actors.dropzone;

import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.effects.particles.ParticleMesh;

import dk.impact.sheeplifter.audio.GameAudioSystem;
import dk.impact.sheeplifter.util.ModelLoader;

/**
 * @author Jeppe Schmidt
 * @version $Revision$
 */
public class DropZone extends Node{
    private float startDelayElapsed;
    private static final float START_DELAY_DURATION = 1.5f;

    private enum States{
        startDelay,
        startZoom,
        normal,
        centrifuge
    }

    private States state;

    private Texture sphereTexture;

    private ParticleMesh swarmParticleMesh;

    private Sphere sphere;

    private float scaleAngle;    

    private Centrifuge centrifuge;      
    
    private GameAudioSystem gameAudioSystem;

    public DropZone(Camera cam, GameAudioSystem gameAudioSystem){
        super("dropzone");
        this.gameAudioSystem = gameAudioSystem;
        buildSphere();
        buildParticles();
        buildTrail(cam);
        setState(States.startDelay);
        setLocalScale(0);
    }

    private void buildTrail(Camera cam){
        // Create the trail
        centrifuge = new Centrifuge(5, 25, 0.3f, cam, gameAudioSystem);
        attachChild(centrifuge);
    }

    private void buildParticles(){
        swarmParticleMesh = (ParticleMesh) ModelLoader.loadJMEModel("res/geometry/dropzone_particles.jme");
        swarmParticleMesh.setCullHint(Spatial.CullHint.Never);
        swarmParticleMesh.setParticlesInWorldCoords(false);

        // Apply alpha, and ZBuffer render states
        final Renderer r = DisplaySystem.getDisplaySystem().getRenderer();
        final BlendState bs = r.createBlendState();
        swarmParticleMesh.setRenderState(bs);
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        bs.setDestinationFunction(BlendState.DestinationFunction.One);
        bs.setTestEnabled(true);
        bs.setTestFunction(BlendState.TestFunction.GreaterThan);

        final ZBufferState zs = r.createZBufferState();
        swarmParticleMesh.setRenderState(zs);
        zs.setWritable(false);
        zs.setEnabled(true);

        swarmParticleMesh.updateRenderState();
        swarmParticleMesh.setLocalScale(0.1f);
        attachChild(swarmParticleMesh);
    }

    private void buildSphere(){
        sphere = new Sphere("sp1", 35, 35, 6);
        sphere.setLightCombineMode(LightCombineMode.Off);

        // Apply alpha, and ZBuffer render states
        final Renderer renderer = DisplaySystem.getDisplaySystem().getRenderer();
        final BlendState bs = renderer.createBlendState();
        sphere.setRenderState(bs);
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        bs.setDestinationFunction(BlendState.DestinationFunction.One);
        bs.setTestEnabled(true);
        bs.setTestFunction(BlendState.TestFunction.GreaterThan);

        final ZBufferState zs = renderer.createZBufferState();
        sphere.setRenderState(zs);
        zs.setWritable(false);
        zs.setEnabled(true);        

        // Add texture
        sphereTexture = TextureManager.loadTexture(DropZone.class.getClassLoader().getResource("res/geometry/dropspheremap.png"),
                                                           Texture.MinificationFilter.Trilinear,
                                                           Texture.MagnificationFilter.Bilinear);
        sphereTexture.setEnvironmentalMapMode(Texture.EnvironmentalMapMode.ReflectionMap);
        sphereTexture.setTranslation(new Vector3f());
        sphereTexture.setWrap(Texture.WrapMode.Repeat);

        TextureState ts = renderer.createTextureState();
        ts.setTexture(sphereTexture);
        ts.setEnabled(true);
        ts.apply();
        sphere.setRenderState(ts);

        sphere.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);

        attachChild(sphere);
    }

    public void update(float timeDelta){
        // Control the scales in different states.
        if(state == States.startDelay){
            setLocalScale(0);
            startDelayElapsed += timeDelta;
            if(startDelayElapsed >= START_DELAY_DURATION){
                setState(States.startZoom);
            }
        } else if(state == States.normal){
            setLocalScale(1 + FastMath.sin(scaleAngle) * 0.06f );
            scaleAngle += 4f * timeDelta;
        } else if(state == States.startZoom){
            setLocalScale(FastMath.sin(scaleAngle) * (1 + 0.06f));
            scaleAngle += 3.3f * timeDelta;

            if(scaleAngle >= FastMath.PI / 2){
                setState(States.normal);
            }
        }

        // Animate texture.
        sphereTexture.getTranslation().y += 0.2f * timeDelta;
        sphereTexture.getTranslation().x -= 0.3f * timeDelta;
        if(sphereTexture.getTranslation().y > 1){
            sphereTexture.getTranslation().y = 0;
        }
        if(sphereTexture.getTranslation().x < 0){
            sphereTexture.getTranslation().x = 1;
        }

        // Update the centrifuge wrappers.
        centrifuge.update(timeDelta);
    }

    public void draw(Renderer r) {
        swarmParticleMesh.getWorldRotation().loadIdentity();
        super.draw(r);
    }

    public void setState(States state){
        this.state = state;
        if(state == States.startZoom){
            scaleAngle = 0;
            gameAudioSystem.addPositionalTrack("res/sound/vortex.wav", this, 0.7f);
        } else if(state == States.normal){
            scaleAngle = FastMath.PI / 2;
        }
    }

    public void resizeGeometry(float scale){
        sphere.setLocalScale(scale);
        swarmParticleMesh.setLocalScale(scale * 0.1f);
        centrifuge.resizeGeometry(scale);
    }

    public void addCentrifugeTrail(Spatial spatial){
        centrifuge.addTrailingSpatial(spatial);        
    }

    public void setSpeed(float centrifugeSpeed, float particleSpeed){
        centrifuge.setStartSpeed(centrifugeSpeed);
        swarmParticleMesh.setSpeed(particleSpeed);
    }
    
    public Centrifuge getCentrifuge() {
    	return centrifuge;
    }
}
