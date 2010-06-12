package dk.impact.sheeplifter.spatials;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.util.Timer;

public class World {
	private IslandTerrain 		islandTerrain;
	private Ocean 				ocean;
	
	private	Vector3f			volcanoHeight;
	
	private Timer				timer;

	public enum SurfaceType {
		IslandTerrain,
		Ocean
	};
	
	public class SurfacePoint {
		Vector3f	surfacePoint;
		Vector3f	surfaceNormal;
		SurfaceType	surfaceType;
		
		SurfacePoint(float x, float y, float z, Vector3f normal, SurfaceType surfaceType) {
			this.surfacePoint = new Vector3f(x, y, z);
			this.surfaceNormal = normal;
			this.surfaceType = surfaceType;
		}
		
		public Vector3f getPoint() {
			return surfacePoint;
		}

		public Vector3f getNormal() {
			return surfaceNormal;
		}
		
		protected void setHeight(float height) {
			surfacePoint.setY(height);
		}
		
		public float getHeight() {
			return surfacePoint.getY();
		}
		
		public SurfaceType getType() {
			return surfaceType;
		}
	}
	
	public World(IslandTerrain islandTerrain, Ocean ocean) {
		this.islandTerrain = islandTerrain;
		this.ocean = ocean;
		this.timer = Timer.getTimer();
	}
	
	public World(IslandTerrain islandTerrain, Ocean ocean, Vector3f volcanoHeight) {
		this.islandTerrain = islandTerrain;
		this.ocean = ocean;
		this.volcanoHeight = volcanoHeight;
		this.timer = Timer.getTimer();
	}
	
	public float getWaterHeight(Vector3f pt) {
		return getWaterHeight(pt.getX(), pt.getZ());		
	}
	
	public float getWaterHeight(float x, float z) {
		return ocean.getWaterHeightGenerator().getHeight(x, z, timer.getTimeInSeconds()) + ocean.getWorldTranslation().getY();
	}
	
	public float getIslandHeight(Vector3f pt) {
		return getIslandHeight(pt.getX(), pt.getZ());		
	}

	public float getIslandHeight(float x, float z) {
		return islandTerrain.getTerrainBlock().getHeight(x, z) + islandTerrain.getWorldTranslation().getY();		
	}
	
	public static boolean isHeightValid(float height) {
		return !Float.isInfinite(height) && !Float.isNaN(height);
	}

	public Vector3f getWaterSurfaceNormal(float x, float z, Vector3f store) {
		return ocean.getProjectedGrid().getSurfaceNormal(x, z, store);
	}

	public Vector3f getIslandSurfaceNormal(float x, float z, Vector3f store) {
		return islandTerrain.getTerrainBlock().getSurfaceNormal(x, z, store);
	}

	public SurfacePoint getSurfacePointAt(Vector3f pt) {
		return getSurfacePointAt(pt.getX(), pt.getZ());
	}
	
	public SurfacePoint getSurfacePointAt(float x, float z) {
        float 		islandHeight = getIslandHeight(x, z);
        float 		waterHeight = getWaterHeight(x, z);
        Vector3f	normal = new Vector3f();

        if (!isHeightValid(islandHeight) || (waterHeight > islandHeight)) {
            // got ocean
        	getWaterSurfaceNormal(x, z, normal);
        	
        	return new SurfacePoint(x, waterHeight, z, normal, SurfaceType.Ocean);
        }		

        // ok, island terrain point
        getIslandSurfaceNormal(x, z, normal);
        
    	return new SurfacePoint(x, islandHeight, z, normal, SurfaceType.IslandTerrain);
	}
	
	public SurfacePoint getSurfacePointAt(Vector3f pt, boolean noVolcanoDescent) {
		SurfacePoint	surfPt = getSurfacePointAt(pt);

		if (noVolcanoDescent) {
			float		distanceFromCenter = getXZDistanceToCenter(pt);
			float		maxDistanceFromCenter = getXZDistanceVolcanoToCenter();

			if ((distanceFromCenter < maxDistanceFromCenter) && (volcanoHeight.getY() > surfPt.getHeight())) {
				surfPt.setHeight(volcanoHeight.getY());
			}		
		}
					
		return surfPt;
	}
	
	public float getXZDistanceToCenter(Vector3f pt) {
		Vector3f 	center = islandTerrain.getTerrainBlock().getWorldBound().getCenter();		
		float		dx = center.getX() - pt.getX();
		float		dz = center.getZ() - pt.getZ();
		
		return FastMath.sqrt(dx * dx + dz * dz);
	}
	
	public float getXZDistanceVolcanoToCenter() {
		Vector3f 	center = islandTerrain.getTerrainBlock().getWorldBound().getCenter();		
		float		maxDx = center.getX() - volcanoHeight.getX();
		float		maxDz = center.getZ() - volcanoHeight.getZ();
		
		return FastMath.sqrt(maxDx * maxDx + maxDz * maxDz);
	}	
	
	public IslandTerrain getIslandTerrain() {
		return islandTerrain;
	}

	public void setIslandTerrain(IslandTerrain islandTerrain) {
		this.islandTerrain = islandTerrain;
	}

	public Ocean getOcean() {
		return ocean;
	}

	public void setOcean(Ocean ocean) {
		this.ocean = ocean;
	}
	
	public Vector3f getVolcanoHeight() {
		return volcanoHeight;
	}
	
	public void setVolcanoHeight(Vector3f point) {
		this.volcanoHeight = point;
	}
}
