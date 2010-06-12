package dk.impact.sheeplifter.spatials;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.scene.Node;
import com.jme.scene.Text;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.Timer;

import dk.impact.sheeplifter.spatials.actors.sheep.Sheep;

public class SheepIcons extends Node {
	private static final long serialVersionUID = 1L;

	private Sheep[]	sheep;
	
	private Quad[]	normalQuads;
	private Quad[]	ghostedQuads;
	private Quad[]	drowningQuads;
	private Text[]	countdownText;
	
	private float	iconSizeX;
	private float	iconSizeY;
	private float	iconSpaceX;
	
	private float	textSize;
	private float 	textOffset;
	
	private float[]	originalTextPos;
	private float[]	originalScale;
	
	private float	width;
	
	private DisplaySystem	display;
	
	private DecimalFormat	timeFormatter;
	
	public SheepIcons(String name, Sheep[] sheep) {
		super(name);
		this.sheep = sheep;
		
        timeFormatter = new DecimalFormat("00");
        timeFormatter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		
		display = DisplaySystem.getDisplaySystem(); 
		build();
	}

    protected void build() {
    	iconSizeX = display.getWidth() * 0.045f;
    	iconSizeY = iconSizeX * 4f / 3f;
    	iconSpaceX = display.getWidth() * 0.005f;

    	textSize = display.getWidth() / 640f * 0.9f;
    	textOffset = -12 * textSize;
    	
    	normalQuads = new Quad[sheep.length];
    	ghostedQuads = new Quad[sheep.length];
    	drowningQuads = new Quad[sheep.length];
    	
    	countdownText = new Text[sheep.length];
    	originalTextPos = new float[sheep.length];
    	originalScale = new float[sheep.length];
    	
    	float posX = 0;
    	
    	for (int i = 0; i < sheep.length; i++) {
    		String name = "SheepIcon" + (i + 1);
    		
    		Quad normalQuad = buildIcon(name, "res/maps/HUD_sheep.png");
    		Quad ghostedQuad = buildIcon(name, "res/maps/HUD_sheep_ghost.png");
    		Quad drowningQuad = buildIcon(name, "res/maps/HUD_sheep_drowning.png");
    		Text countdown = Text.createDefaultTextLabel("Text", ""); 
    		countdown.setLocalScale(textSize);
    		
    		originalTextPos[i] = posX;
    		originalScale[i] = (float) Math.random() * 0.2f + 0.8f;

    		normalQuad.getLocalTranslation().set(posX, 0, 0);
    		ghostedQuad.getLocalTranslation().set(posX, 0, 0);
    		drowningQuad.getLocalTranslation().set(posX, 0, 0);
    		countdown.getLocalTranslation().set(originalTextPos[i] + textOffset, 0, 0);

    		normalQuads[i] = normalQuad;    		
    		ghostedQuads[i] = ghostedQuad;    		
    		drowningQuads[i] = drowningQuad;
    		countdownText[i] = countdown;
    		
    		attachChild(normalQuad);
    		attachChild(ghostedQuad);
    		attachChild(drowningQuad);
    		attachChild(countdown);
    		
    		posX += iconSizeX + iconSpaceX;
    	}
    	
    	width = sheep.length * iconSizeX + iconSpaceX * (sheep.length - 1);
    	
    	updateRenderState();
    }

	protected Quad buildIcon(String name, String file) {
        Quad icon = new Quad(name, iconSizeX, iconSizeY);
		
        Texture iconTexture = TextureManager.loadTexture(SheepIcons.class.getClassLoader().getResource(file),
                                                    Texture.MinificationFilter.Trilinear,
                                                    Texture.MagnificationFilter.Bilinear);
        TextureState ts = display.getRenderer().createTextureState();
        ts.setTexture(iconTexture);
        ts.setEnabled(true);
        icon.setRenderState(ts);
        
        //alpha blending needed
        BlendState bs = display.getRenderer().createBlendState();
        bs.setBlendEnabled(true);
        bs.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        bs.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        bs.setTestEnabled(false);
        icon.setRenderState(bs);
        
        icon.updateRenderState();
        
        icon.setCullHint(CullHint.Always);
        
        return icon;
    }
    
	public void update() {
    	for (int i = 0; i < sheep.length; i++) {
    		float 	scale = originalScale[i];
    		
    		if (!sheep[i].hasAbductionCommenced() && sheep[i].getBody().isDrowning() && !sheep[i].getBody().hasDrowned()) {
        		float	textZoom = 0;
        		float 	remainingTime = sheep[i].getBody().getTimeUntilDrowning();
        		
	    		textZoom = (remainingTime % 1);
	        	
	    		if (textZoom > 0.5) {
	    			textZoom = 1 - textZoom;
	        	}
	    		
	    		textZoom *= 2f;
        		
        		if (remainingTime <= 10) {
        			countdownText[i].getTextColor().set(1, 0, 0, 0.85f);
        		}
        		else {
        			countdownText[i].getTextColor().set(0, 0, 0, 0.85f);
        		}

        		textZoom = (1 - FastMath.pow(FastMath.sin(FastMath.clamp(textZoom, 0, 1)), 2.0f) * (float) Math.PI * 0.5f) * 0.5f;

        		float scaleFactor = (1 + textZoom * 0.2f);
        		
        		countdownText[i].print(timeFormatter.format(remainingTime));
        		countdownText[i].setLocalScale(textSize * scaleFactor);
        		countdownText[i].setLocalTranslation(originalTextPos[i] + textOffset * scaleFactor, 0, 0);
    		}
    		else {
    			countdownText[i].print("");
    		}
    		
    		if (sheep[i].hasAbductionCompleted()) {
    			normalQuads[i].setCullHint(CullHint.Never);
    			ghostedQuads[i].setCullHint(CullHint.Always);
    			drowningQuads[i].setCullHint(CullHint.Always);
    		}
    		else if (sheep[i].getBody().isDrowning()) {
    			normalQuads[i].setCullHint(CullHint.Always);
    			ghostedQuads[i].setCullHint(CullHint.Always);
      			drowningQuads[i].setCullHint(CullHint.Never);
    		}
    		else {
    			normalQuads[i].setCullHint(CullHint.Always);
    			ghostedQuads[i].setCullHint(CullHint.Never);
    			drowningQuads[i].setCullHint(CullHint.Always);
    		}
    		
    		if (sheep[i].hasAbductionCommenced() && !sheep[i].hasAbductionCompleted()) {
        		scale = (float) Math.sin(Timer.getTimer().getTimeInSeconds() * 6f) * 0.075f + originalScale[i];
    		}
    		
    		normalQuads[i].setLocalScale(scale);
    		ghostedQuads[i].setLocalScale(scale);
    		drowningQuads[i].setLocalScale(scale);
    	}
    	
    	updateRenderState();
	}
	
	public float getIconSizeX() {
		return iconSizeX;
	}

	public float getIconSizeY() {
		return iconSizeY;
	}
	
	public float getWidth() {
		return width;
	}
}
