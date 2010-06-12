package dk.impact.sheeplifter.terrain;

import java.util.logging.Logger;

import com.jme.math.FastMath;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jmex.terrain.util.AbstractHeightMap;

/**
 */
public class ShaftHeightMap extends AbstractHeightMap {
    private static final Logger logger = Logger
            .getLogger(ShaftHeightMap.class.getName());

    private static final float ELEVATION_NEAR_HOLE = 0.8f; // [0.5, 1]
    
    private float exponent;
    
    private	Vector3f heighestPosition;

	/**
	 */
    public ShaftHeightMap(int size, float exponent) {
        this.size = size;
        setExponent(exponent);
        
        heighestPosition = new Vector3f();

        load();
    }

	/**
     */
    public boolean load() {
        float[][] tempBuffer;

        if (null != heightData) {
            unloadHeightMap();
        }

        tempBuffer = new float[size][size];
        heightData = new float[size * size];

        // Generate warped hemisphere form
        for(int i = 0; i < size; i++) {
        	float u = FastMath.sin (((float) i / (float) (size - 1)) * FastMath.PI);
        	
            for(int j = 0; j < size; j++) {
            	float v = FastMath.sin ((float) j / (float) (size - 1) * FastMath.PI);
            	
            	float tmp = 1f - FastMath.pow (u * v, exponent);
            	
            	if (tmp > ELEVATION_NEAR_HOLE) {
            		tmp = 2f * ELEVATION_NEAR_HOLE - tmp;
            	}
            	
            	tempBuffer[j][i] = tmp;
            	
            	if (tmp > heighestPosition.getY()) {
            		heighestPosition.set(j, tmp, i);
            	}            	
            }        	
        }

        normalizeTerrain(tempBuffer);

        //transfer the new terrain into the height map.
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                setHeightAtPoint((float)tempBuffer[i][j], i, j);
            }
        }

        logger.info("Created shaft heightmap");
        return true;
    }
    
    
	public float getExponent() {
		return exponent;
	}

	public void setExponent(float exponent) {
		this.exponent = exponent;
	}

	protected void setHeighestPosition(Vector3f heighestPosition) {
		this.heighestPosition = heighestPosition;
	}

	public Vector3f getHeighestPosition() {
		return heighestPosition;
	}    
}
