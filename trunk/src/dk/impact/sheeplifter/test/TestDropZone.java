package dk.impact.sheeplifter.test;

import com.jme.app.SimpleGame;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.ZBufferState;
import com.jme.bounding.BoundingBox;
import com.jme.renderer.Renderer;
import com.jmex.effects.TrailMesh;
import dk.impact.sheeplifter.spatials.actors.dropzone.DropZone;

/**
 * Created by IntelliJ IDEA.
 * User: Jeppe Schmidt
 * Date: 27-01-2009
 * Time: 20:16:14
 * To change this template use File | Settings | File Templates.
 */
public class TestDropZone extends SimpleGame{

    private DropZone dropZone;

    public static void main(String[] args) {
        TestDropZone app = new TestDropZone();
        app.setConfigShowMode(ConfigShowMode.AlwaysShow);
        app.start();
    }

    public TestDropZone(){
        samples = 4;
    }

    protected void simpleInitGame(){
        dropZone = new DropZone(cam, null);        
        dropZone.resizeGeometry(1.1f);
        rootNode.attachChild(dropZone);

        KeyBindingManager.getKeyBindingManager().set("1", KeyInput.KEY_1);
    }

    protected void simpleUpdate(){
        float interpolation = timer.getTimePerFrame();
        super.simpleUpdate();    //To change body of overridden methods use File | Settings | File Templates.

        if (KeyBindingManager.getKeyBindingManager().isValidCommand("1", false)) {
            for(int i=0; i<5; i++){
                Sphere testSphere = new Sphere("testsp", 20, 20, 1);
                testSphere.setModelBound(new BoundingBox());
                testSphere.updateModelBound();

                ZBufferState zs = display.getRenderer().createZBufferState();
                testSphere.setRenderState(zs);
                zs.setWritable(true);
                zs.setEnabled(true);
                testSphere.setRenderQueueMode(Renderer.QUEUE_OPAQUE);

                dropZone.addCentrifugeTrail(testSphere);
            }
            dropZone.updateRenderState();
        }

        dropZone.update(interpolation);
    }        

}
