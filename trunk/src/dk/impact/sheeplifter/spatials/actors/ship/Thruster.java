package dk.impact.sheeplifter.spatials.actors.ship;

import com.jmex.effects.particles.ParticleMesh;
import com.jmex.effects.particles.ParticleInfluence;
import com.jmex.effects.particles.Particle;
import com.jme.scene.Spatial;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.ZBufferState;
import com.jme.renderer.Renderer;
import com.jme.renderer.ColorRGBA;
import com.jme.system.DisplaySystem;
import com.jme.math.Vector3f;
import dk.impact.sheeplifter.util.ModelLoader;

/**
 * @author Jeppe Schmidt <jeppe.schmidt@gmail.com>
 * @version $Revision$
 */
public class Thruster{

    public static final float PARTICLE_MAX_LIFE_TIME = 150;

    public static final float PARTICLE_MIN_LIFE_TIME = 50;

    private ParticleMesh particleMesh;

    private Spaceship spaceship;

    private Vector3f emissionDirection;

    private Vector3f store = new Vector3f();

    private boolean burning;

    private int count;

    public Thruster(Spaceship spaceship, float scale, Vector3f emissionDirection, Vector3f localTranslation){
        this.spaceship = spaceship;
        this.emissionDirection = emissionDirection;
        particleMesh = createParticleMesh();
        particleMesh.setLocalScale(scale);
        particleMesh.setEmissionDirection(emissionDirection);
        particleMesh.setLocalTranslation(localTranslation);
        particleMesh.setMaximumLifeTime(PARTICLE_MAX_LIFE_TIME);
        particleMesh.setMinimumLifeTime(PARTICLE_MIN_LIFE_TIME);
        setBurning(false);
    }        

    protected ParticleMesh createParticleMesh(){
        ParticleMesh particleMesh = (ParticleMesh) ModelLoader.loadJMEModel("res/geometry/thruster.jme");
        particleMesh.setCullHint(Spatial.CullHint.Never);
        particleMesh.setSpeed(0.6f);
        particleMesh.getParticleController().setControlFlow(true);
        particleMesh.setReleaseRate(1300);

        particleMesh.setParticlesInWorldCoords(false);

        // Make the ships velocity influence the particles.
        /*
        particleMesh.addInfluence(new ParticleInfluence(){
            private Vector3f interpolatedShipVel = new Vector3f();

            public void apply(float dt, Particle particle, int index){
                interpolatedShipVel.set(spaceship.getVelocity());
                interpolatedShipVel.multLocal(dt * 0f);
                particle.getPosition().addLocal(interpolatedShipVel);
            }
        });
        */

        // Apply alpha, and ZBuffer render states
        final Renderer r = DisplaySystem.getDisplaySystem().getRenderer();
        final BlendState bs = r.createBlendState();
        particleMesh.setRenderState(bs);
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        bs.setDestinationFunction(BlendState.DestinationFunction.One);
        bs.setTestEnabled(true);
        bs.setTestFunction(BlendState.TestFunction.GreaterThan);

        final ZBufferState zs = r.createZBufferState();
        particleMesh.setRenderState(zs);
        zs.setWritable(false);
        zs.setEnabled(true);
        
        final FogState fs = r.createFogState();
        fs.setEnabled(false);
        particleMesh.setRenderState(fs);
        
        particleMesh.updateRenderState();
        
        particleMesh.setEndColor(ColorRGBA.blue.clone());        
        return particleMesh;
    }

    public void update(float td){
        spaceship.getShipNode().getWorldRotation().mult(emissionDirection, store);
		particleMesh.setEmissionDirection(store);
    }

    public void preDrawUpdate() {
    	// Fix particle system world rotation issue
    	// By calling loadIdentity(), particles are facing camera correctly at all times
		particleMesh.getWorldRotation().loadIdentity();
    }
    
    public ParticleMesh getParticleMesh(){
        return particleMesh;
    }

    public void setBurning(boolean burning){
        this.burning = burning;
        if(burning){
            particleMesh.setCullHint(Spatial.CullHint.Never);
        } else {
            particleMesh.setCullHint(Spatial.CullHint.Always);
        }
    }

    public boolean isBurning(){
        return burning;
    }
}
