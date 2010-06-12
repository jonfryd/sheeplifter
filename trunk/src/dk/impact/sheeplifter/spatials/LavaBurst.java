package dk.impact.sheeplifter.spatials;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jmex.effects.particles.ParticleMesh;
import dk.impact.sheeplifter.util.ModelLoader;

/**
 * @author Jeppe Schmidt <jeppe.schmidt@gmail.com>
 * @version $Revision$
 */
public class LavaBurst extends Node{

    private Quad lavaQuad;

    private ParticleMesh particleMesh;

    private float burstTimeElapsed;
    private float idleTimeElapsed;
    private float burstDuration;
    private float burstDelayDuration;
    private boolean bursting;

    public LavaBurst(Quad lavaQuad){
        this.lavaQuad = lavaQuad;
        buildParticles();
        //stopBurst();
        startBurst();
        setCullHint(CullHint.Never);
    }

    public void buildParticles(){
        particleMesh = (ParticleMesh) ModelLoader.loadJMEModel("res/geometry/lava_particles.jme");
        particleMesh.setCullHint(Spatial.CullHint.Never);
        particleMesh.setParticlesInWorldCoords(false);        
        particleMesh.setLocalTranslation(0, -2.3f, 0);
        particleMesh.setSpeed(0.1f);
        particleMesh.getParticleController().setControlFlow(true);
        particleMesh.setReleaseRate(400);

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

        attachChild(particleMesh);
    }

    public void update(float timeDelta){
        // Control lava particle-bursts
        /*
        if(bursting){
            // Bursting...
            if(burstTimeElapsed >= burstDuration){
                stopBurst();
            }
            burstTimeElapsed += timeDelta;
        } else {
            // Idling....
            if(idleTimeElapsed >= burstDelayDuration){
                startBurst();
            }
            idleTimeElapsed += timeDelta;
        }
        */
    }

    private void startBurst(){
        particleMesh.setEmissionDirection(new Vector3f(0, 1, 0));
        float rndSize = 3f; // - FastMath.nextRandomFloat() * 0.1f;
        particleMesh.setLocalScale(rndSize);
        particleMesh.getLocalTranslation().x = (-(lavaQuad.getWidth() / 2) + lavaQuad.getWidth() * FastMath.rand.nextFloat()) * 0f;
        particleMesh.getLocalTranslation().z = (-(lavaQuad.getHeight() / 2) + lavaQuad.getHeight() * FastMath.rand.nextFloat()) * 0f;
        particleMesh.setReleaseVariance(1);
        float rndVel = 0.2f; // + FastMath.nextRandomFloat() * 0.05f;
        particleMesh.setInitialVelocity(rndVel);

        burstDuration = 6 + FastMath.rand.nextFloat() * 8;

        idleTimeElapsed = 0;
        bursting = true;
        //System.out.println("start burst");
    }

    private void stopBurst(){
        //System.out.println("stop burst");
        particleMesh.setEmissionDirection(new Vector3f(0, -1, 0));

        particleMesh.setInitialVelocity(0.1f);
        burstDelayDuration = 5 + FastMath.nextRandomFloat() * 10;

        burstTimeElapsed = 0;
        bursting = false;
    }

    public void draw(Renderer r) {
        particleMesh.getWorldRotation().loadIdentity();
        super.draw(r);
    }
}
