package dk.impact.sheeplifter.terrain;

import java.util.logging.Logger;

import com.jme.math.FastMath;
import com.jme.system.JmeException;
import com.jmex.terrain.util.AbstractHeightMap;

/**
 */
public class HemisphereHeightMap extends AbstractHeightMap {
    private static final Logger logger = Logger
            .getLogger(HemisphereHeightMap.class.getName());

    private float exponent;

	/**
	 */
    public HemisphereHeightMap(int size, float exponent) {
        this.size = size;
        setExponent(exponent);

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
            	
            	tempBuffer[j][i] = FastMath.pow (u * v, exponent);
            }        	
        }

        normalizeTerrain(tempBuffer);

        //transfer the new terrain into the height map.
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                setHeightAtPoint((float)tempBuffer[i][j], i, j);
            }
        }

        logger.info("Created dome heightmap");
        return true;
    }
    
    
	public float getExponent() {
		return exponent;
	}

	public void setExponent(float exponent) {
		this.exponent = exponent;
	}    
}
