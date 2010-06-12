package dk.impact.sheeplifter.spatials;

import com.jme.image.Texture;
import com.jme.scene.Skybox;
import com.jme.scene.Spatial;
import com.jme.scene.state.CullState;
import com.jme.scene.state.FogState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

public class Sky extends Skybox {
	private static final long serialVersionUID = 1L;
	
	private static final String	textureDir = "res/maps/";
	
	public Sky(String name, float xExtent, float yExtent, float zExtent, DisplaySystem display) {
		super(name, xExtent, yExtent, zExtent);
		
		build(display);
	}
	
	protected void build(DisplaySystem display) {
	      Texture north = TextureManager.loadTexture(Sky.class
	              .getClassLoader().getResource(textureDir + "3.png"),
	              Texture.MinificationFilter.BilinearNearestMipMap,
	              Texture.MagnificationFilter.Bilinear);
	      Texture south = TextureManager.loadTexture(Sky.class
	              .getClassLoader().getResource(textureDir + "1.png"),
	              Texture.MinificationFilter.BilinearNearestMipMap,
	              Texture.MagnificationFilter.Bilinear);
	      Texture east = TextureManager.loadTexture(Sky.class
	              .getClassLoader().getResource(textureDir + "4.png"),
	              Texture.MinificationFilter.BilinearNearestMipMap,
	              Texture.MagnificationFilter.Bilinear);
	      Texture west = TextureManager.loadTexture(Sky.class
	              .getClassLoader().getResource(textureDir + "2.png"),
	              Texture.MinificationFilter.BilinearNearestMipMap,
	              Texture.MagnificationFilter.Bilinear);
	      Texture up = TextureManager.loadTexture(Sky.class
	              .getClassLoader().getResource(textureDir + "6.png"),
	              Texture.MinificationFilter.BilinearNearestMipMap,
	              Texture.MagnificationFilter.Bilinear);
	      Texture down = TextureManager.loadTexture(Sky.class
	              .getClassLoader().getResource(textureDir + "5.png"),
	              Texture.MinificationFilter.BilinearNearestMipMap,
	              Texture.MagnificationFilter.Bilinear);

	      setTexture(Skybox.Face.North, north);
	      setTexture(Skybox.Face.West, west);
	      setTexture(Skybox.Face.South, south);
	      setTexture(Skybox.Face.East, east);
	      setTexture(Skybox.Face.Up, up);
	      setTexture(Skybox.Face.Down, down);
	      preloadTextures();

	      CullState cullState = display.getRenderer().createCullState();
	      cullState.setCullFace(CullState.Face.None);
	      cullState.setEnabled(true);
	      setRenderState(cullState);

	      ZBufferState zState = display.getRenderer().createZBufferState();
	      zState.setEnabled(false);
	      setRenderState(zState);

	      FogState fs = display.getRenderer().createFogState();
	      fs.setEnabled(false);
	      setRenderState(fs);

	      setLightCombineMode(Spatial.LightCombineMode.Off);
	      setCullHint(Spatial.CullHint.Never);
	      setTextureCombineMode(TextureCombineMode.Replace);
	      updateRenderState();

	      lockBounds();
	      lockMeshes();
	}
}
