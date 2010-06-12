package dk.impact.sheeplifter.spatials;

import java.util.Vector;

import com.jme.bounding.BoundingBox;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial;
import com.jme.util.Timer;
import com.jmex.effects.water.ImprovedNoise;

import dk.impact.sheeplifter.util.PerlinTurbulence;

public class PlantArea extends Node {
	private static final long serialVersionUID = 1L;
	
	private static final float	WIND_TURBULENCE_LOW_FREQ = 1f;
	private static final float	WIND_TURBULENCE_HIGH_FREQ = 2f;
	
	private Plant				plant;
	private int					numberToPlant;
	private Vector2f			seedPoint;
	private IslandTerrain		island;
	private float 				maxExtent;		

	private Vector<SharedMesh>	plantVector;
    private float 				originalEulerAngles[][];
    private float 				newEulerAngles[];
    private Quaternion 			tempWindResult = new Quaternion();
	
	public PlantArea(String name, Plant plant, int numberToPlant, Vector2f seedPoint, IslandTerrain island) {
		super(name);
		this.plant = plant;
		this.numberToPlant = numberToPlant;
		this.seedPoint = seedPoint;
		this.island = island;
		this.maxExtent = 0;
		
		plantVector = new Vector<SharedMesh>();
		
		build();
		initWindSim();
	}
	
    protected void build() {
		float xExtent = ((BoundingBox) plant.getModelTriMesh().getWorldBound()).xExtent;
		float zExtent = ((BoundingBox) plant.getModelTriMesh().getWorldBound()).zExtent;

		xExtent *= plant.getModel().getLocalScale().getX();
		zExtent *= plant.getModel().getLocalScale().getZ();
		
		maxExtent = Math.max(xExtent, zExtent);		
    	
    	for (int i = 1; i <= numberToPlant; i++) {
    		SharedMesh	sm = createSharedInstance();
    		sm.setName("Planting" + i);

    		plantVector.add(sm);
    		
        	attachChild(sm);    		
    	}

    	// add bounding box
        setModelBound(new BoundingBox());
        updateModelBound();
    }	
    
    protected SharedMesh createSharedInstance() {
		SharedMesh 	sm = new SharedMesh(plant.getModelTriMesh());

		float ang = (float) (Math.random() * Math.PI * 2.0);
		float radius = (float) (Math.sqrt(Math.random()) * maxExtent * Math.sqrt(numberToPlant));
		
		float x = seedPoint.getX() + (float) (Math.cos(ang) * radius);
		float z = seedPoint.getY() + (float) (Math.sin(ang) * radius);
		
		float rot = (float) (Math.PI * 2.0 * Math.random());
		float scaleY = (float) (0.7 * Math.random() + 0.3);
		float scaleX = (float) (0.4 * Math.random() + 0.8) * scaleY;
		float scaleZ = (float) (0.4 * Math.random() + 0.8) * scaleY;
		
		sm.setLocalTranslation(x, 0, z);
		sm.getLocalTranslation().setY(island.getTerrainBlock().getHeight(sm.getLocalTranslation()) + island.getLocalTranslation().getY());
		sm.getLocalTranslation().addLocal(plant.getModel().getLocalTranslation());
		
        Quaternion q = new Quaternion();
        q.fromAngles(0, rot, 0);
        sm.setLocalRotation(q.mult(plant.getModel().getLocalRotation()));
		
        Vector3f scaleVector = new Vector3f(scaleX, scaleY, scaleZ);
        sm.setLocalScale(scaleVector.mult(plant.getModel().getLocalScale()));

        return sm;
    }

    public void initWindSim() {
    	originalEulerAngles = new float[plantVector.size()][3];
        newEulerAngles = new float[3];
    	
    	for (int i = 0; i < plantVector.size(); i++) {
    		SharedMesh	plant = plantVector.get(i);

    		originalEulerAngles[i] = plant.getLocalRotation().toAngles(null);
    	}
    }
    
    public void simWindCurrent() {
    	float	currentTime = Timer.getTimer().getTimeInSeconds() * 0.5f;
    	
    	for (int i = 0; i < plantVector.size(); i++) {
    		SharedMesh	plant = plantVector.get(i);
    		Vector3f	point1 = plant.getWorldTranslation().add(currentTime, 0, 0);
    		Vector3f	point2 = plant.getWorldTranslation().add(0, currentTime, 0);
    		Vector3f	point3 = plant.getWorldTranslation().add(0, 0, currentTime);
    		
            newEulerAngles[0] = (float) (originalEulerAngles[i][0] + PerlinTurbulence.turbulence(point1, WIND_TURBULENCE_LOW_FREQ, WIND_TURBULENCE_HIGH_FREQ) * 0.05f - 0.25f);
            newEulerAngles[1] = (float) (originalEulerAngles[i][1] + PerlinTurbulence.turbulence(point2, WIND_TURBULENCE_LOW_FREQ, WIND_TURBULENCE_HIGH_FREQ) * 0.05f - 0.25f);
            newEulerAngles[2] = (float) (originalEulerAngles[i][2] + PerlinTurbulence.turbulence(point3, WIND_TURBULENCE_LOW_FREQ, WIND_TURBULENCE_HIGH_FREQ) * 0.05f - 0.25f);
            
            tempWindResult.fromAngles(newEulerAngles);
            
            plant.setLocalRotation(tempWindResult);
    	}
    }
    
}
