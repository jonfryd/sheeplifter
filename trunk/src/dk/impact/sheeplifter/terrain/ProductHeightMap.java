package dk.impact.sheeplifter.terrain;

import java.util.logging.Logger;

import com.jme.system.JmeException;
import com.jmex.terrain.util.AbstractHeightMap;

/**
 */
public class ProductHeightMap extends AbstractHeightMap {
    private static final Logger logger = Logger
            .getLogger(ProductHeightMap.class.getName());
    
	//the two maps.
	private AbstractHeightMap map1;
	private AbstractHeightMap map2;

	/**
	 */
	public ProductHeightMap(
		AbstractHeightMap map1,
		AbstractHeightMap map2) {

		//insure all parameters are valid.
		if (null == map1 || null == map2) {
			throw new JmeException("Height map may not be null");
		}

		if (map1.getSize() != map2.getSize()) {
			throw new JmeException("The two maps must be of the same size");
		}

		this.size = map1.getSize();
		this.map1 = map1;
		this.map2 = map2;

		load();
	}

	/**
	 * <code>setHeightMaps</code> sets the height maps to combine.
	 * The size of the height maps must be the same.
	 * @param map1 the first height map.
	 * @param map2 the second height map.
	 * @throws JmeException if the either heightmap is null, or their
	 * 		sizes do not match.
	 */
	public void setHeightMaps(AbstractHeightMap map1, AbstractHeightMap map2) {
		if (null == map1 || null == map2) {
			throw new JmeException("Height map may not be null");
		}

		if (map1.getSize() != map2.getSize()) {
			throw new JmeException("The two maps must be of the same size");
		}

		this.size = map1.getSize();
		this.map1 = map1;
		this.map2 = map2;
	}

	/**
	 * <code>load</code> builds a new heightmap based on the combination of
	 * two other heightmaps.
	 *
	 * @return boolean if the heightmap was successfully created.
	 */
	public boolean load() {
        float[][] tempBuffer;

        if (null != heightData) {
			unloadHeightMap();
		}

        tempBuffer = new float[size][size];
		heightData = new float[size*size];

		float[] temp1 = map1.getHeightMap();
		float[] temp2 = map2.getHeightMap();

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				tempBuffer[i][j] = temp1[i + (j*size)] * temp2[i + (j*size)];
			}
		}
		
        normalizeTerrain(tempBuffer);

        //transfer the new terrain into the height map.
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                setHeightAtPoint((float)tempBuffer[i][j], i, j);
            }
        }		

		logger.info("Created product heightmap");

		return true;
	}

}
