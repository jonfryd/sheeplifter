package dk.impact.sheeplifter.spatials.actors.sheep;

import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.ZBufferState;
import com.jme.renderer.Renderer;
import com.jme.system.DisplaySystem;
import com.jmex.effects.particles.ParticleMesh;
import com.jmex.effects.particles.ParticleInfluence;
import com.jmex.effects.particles.Particle;
import dk.impact.sheeplifter.spatials.actors.Abductable;
import dk.impact.sheeplifter.util.ModelLoader;

/**
 *
 */
public class HollowAbductableSheep extends Node implements Abductable{

    private boolean beamPulling;

    private Spatial model;

    private ParticleMesh woolParticles;

    public HollowAbductableSheep(Node spaceShipNode){
        // Add a test sheep.
        model = ModelLoader.loadJMEModel("res/geometry/sheep.jme");

		//scale it to be MUCH smaller than it is originally
        model.setLocalScale(0.03f);

        //rotate
        Quaternion q = new Quaternion();
        q.fromAngles(-FastMath.PI * 0.5f, FastMath.PI, 0);
        model.setLocalRotation(q);

        // Get wool particles
        buildWoolParticles(spaceShipNode);        

        attachChild(model);
    }

    /**
     * Builds the wool particles displayed when the sheep get abducted.
     *
     * @param yThresholdNode            the particles will not fly above the world location the the <code>yThresholdNode</code>.
     */
    private void buildWoolParticles(final Node yThresholdNode){
        woolParticles = (ParticleMesh) ModelLoader.loadJMEModel("res/geometry/wool_particles.jme");
        woolParticles.setCullHint(Spatial.CullHint.Never);
        woolParticles.setLocalScale(0.1f);

        // Apply alpha, and ZBuffer render states
        final Renderer r = DisplaySystem.getDisplaySystem().getRenderer();
        final BlendState bs = r.createBlendState();
        woolParticles.setRenderState(bs);
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        bs.setDestinationFunction(BlendState.DestinationFunction.One);
        bs.setTestEnabled(true);
        bs.setTestFunction(BlendState.TestFunction.GreaterThan);

        final ZBufferState zs = r.createZBufferState();
        woolParticles.setRenderState(zs);
        zs.setWritable(false);
        zs.setEnabled(true);
        woolParticles.setParticlesInWorldCoords(false);
        woolParticles.setLocalTranslation(0, 0.65f, 0);

        woolParticles.addInfluence(new ParticleInfluence(){
            public void apply(float dt, Particle particle, int index){
                float y = particle.getPosition().y * woolParticles.getLocalScale().y;
                if(y + woolParticles.getWorldTranslation().y >= yThresholdNode.getWorldTranslation().y){
                    particle.killParticle();                    
                }
            }
        });
    }

    public boolean mayAbduct(){
        return true;
    }

    public void abductableStacked(){

    }    

    public void abductionCommenced(){
        beamPulling = true;
        attachChild(woolParticles);
    }

    public void abductionCompleted(){
        setCullHint(CullHint.Always);
    }

    public void abductionAborted(){
        
    }

    public float getWorldBoundsXExtend(){
        return ((BoundingBox) model.getWorldBound()).xExtent;
    }

    public float getWorldBoundsYExtend(){
        return ((BoundingBox) model.getWorldBound()).yExtent;
    }

    public float getWorldBoundsZExtend(){
        return ((BoundingBox) model.getWorldBound()).zExtent;
    }

    public Spatial getAbductionSpatial(){
        return this;
    }

    public void update(float timeDelta){
        if(beamPulling){
            localTranslation.y += -1 * timeDelta;
        }
    }

    public ParticleMesh getWoolParticles(){
        return woolParticles;
    }	
}
