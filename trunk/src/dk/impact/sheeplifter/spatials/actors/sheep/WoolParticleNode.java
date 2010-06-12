package dk.impact.sheeplifter.spatials.actors.sheep;

import com.jmex.effects.particles.ParticleMesh;
import com.jmex.effects.particles.Particle;
import com.jmex.effects.particles.ParticleInfluence;
import com.jme.scene.Spatial;
import com.jme.scene.Node;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.ZBufferState;
import com.jme.renderer.Renderer;
import com.jme.system.DisplaySystem;
import dk.impact.sheeplifter.util.ModelLoader;

/**
 * @author Jeppe Schmidt <jeppe.schmidt@gmail.com>
 * @version $Revision$
 */
public class WoolParticleNode extends Node{

    private ParticleMesh woolParticleMesh;

    /**
     * Builds the wool particles displayed when the sheep get abducted.
     *
     * @param yThresholdNode            the particles will not fly above the world location the the <code>yThresholdNode</code>.
     * @param relThreshold              threshold relative to the y position of the <code>yThresholdNode</code>.
     */
    public WoolParticleNode(final Spatial yThresholdNode, final float relThreshold){
        super("woolParticleNode");
        setCullHint(Spatial.CullHint.Always);

        woolParticleMesh = (ParticleMesh) ModelLoader.loadJMEModel("res/geometry/wool_particles.jme");
        woolParticleMesh.setLocalScale(0.6f);        

        // Apply alpha, and ZBuffer render states
        final Renderer r = DisplaySystem.getDisplaySystem().getRenderer();
        final BlendState bs = r.createBlendState();
        woolParticleMesh.setRenderState(bs);
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        bs.setDestinationFunction(BlendState.DestinationFunction.One);
        bs.setTestEnabled(true);
        bs.setTestFunction(BlendState.TestFunction.GreaterThan);

        final ZBufferState zs = r.createZBufferState();
        woolParticleMesh.setRenderState(zs);
        zs.setWritable(false);
        zs.setEnabled(true);
        woolParticleMesh.setParticlesInWorldCoords(false);
        woolParticleMesh.setLocalTranslation(0, 0.65f, 0);

        // Make sure that the wool twister, does not go above the ship.
        woolParticleMesh.addInfluence(new ParticleInfluence(){
            public void apply(float dt, Particle particle, int index){
                float y = particle.getPosition().y * woolParticleMesh.getLocalScale().y;
                if(y + woolParticleMesh.getWorldTranslation().y >= yThresholdNode.getWorldTranslation().y + relThreshold){
                    particle.killParticle();
                }
            }
        });

        attachChild(woolParticleMesh);
    }

    public void draw(Renderer r) {
		// Fix particle system world rotation issue
    	// By calling loadIdentity(), particles are facing camera correctly at all times
		woolParticleMesh.getWorldRotation().loadIdentity();
    	super.draw(r);
    }
}
