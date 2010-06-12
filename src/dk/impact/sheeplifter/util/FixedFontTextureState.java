package dk.impact.sheeplifter.util;

import java.net.URL;
import java.util.logging.Logger;

import com.jme.image.Image;
import com.jme.image.Texture;
import com.jme.scene.Text;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;

public class FixedFontTextureState {
    private static final Logger logger = Logger.getLogger(Text.class.getName());

    private static TextureState fontTextureState;
	private static final String FONT = "res/maps/fixedfont.png";

	public static TextureState getFixedFontTextureState() {
        if ( fontTextureState == null ) {
            fontTextureState = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
           final URL defaultUrl = FixedFontTextureState.class.getClassLoader().getResource(FONT);
           if ( defaultUrl == null )
           {
              logger.warning("Font not found: " + FONT);
           }
           fontTextureState.setTexture( TextureManager.loadTexture(defaultUrl, Texture.MinificationFilter.Trilinear,
                    Texture.MagnificationFilter.Bilinear, Image.Format.GuessNoCompression, 1.0f, true ) );
            fontTextureState.setEnabled( true );
        }
        return fontTextureState;
    }
}
