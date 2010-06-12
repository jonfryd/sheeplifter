package dk.impact.sheeplifter.test;

import com.jme.app.SimpleGame;
import com.jme.math.Quaternion;
import com.jme.math.FastMath;
import com.jme.util.Timer;
import dk.impact.sheeplifter.spatials.Lava;

/**
 * Created by IntelliJ IDEA.
 * User: Jeppe Schmidt
 * Date: 29-01-2009
 * Time: 21:15:16
 * To change this template use File | Settings | File Templates.
 */
public class TestLava extends SimpleGame{

    private Lava lava;

    public static void main(String[] args) {
        TestLava app = new TestLava();
        app.setConfigShowMode(ConfigShowMode.AlwaysShow);
        app.start();
    }

    public TestLava(){
        samples = 4;        
    }

    protected void simpleInitGame(){
        lava = new Lava("lava");
        /*
        Quaternion q = new Quaternion();
        q.fromAngles(-FastMath.PI / 2, 0, 0);
        lava.setLocalRotation(q);
        */
        lava.getLavaQuad().resize(50, 50);
        lava.setLocalScale(0.2f);
        rootNode.attachChild(lava);
    }

    protected void simpleUpdate(){
        super.simpleUpdate();
        lava.update(Timer.getTimer().getTimePerFrame());        
    }
}
