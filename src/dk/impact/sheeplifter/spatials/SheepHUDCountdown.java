package dk.impact.sheeplifter.spatials;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.scene.Text;
import com.jme.system.DisplaySystem;

import dk.impact.sheeplifter.spatials.actors.sheep.Sheep;

public class SheepHUDCountdown extends Node {
	private static final long serialVersionUID = 1L;

	private Sheep[]	sheep;
	private Text[]	countdownText;

	private DisplaySystem	display;
	
	private DecimalFormat	timeFormatter;
	
	public SheepHUDCountdown(String name, Sheep[] sheep) {
		super(name);
		this.sheep = sheep;
		
        timeFormatter = new DecimalFormat("00");
        timeFormatter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		
		display = DisplaySystem.getDisplaySystem(); 
		build();
	}

    protected void build() {
    	countdownText = new Text[sheep.length];
    	
    	for (int i = 0; i < sheep.length; i++) {
    		Text countdown = Text.createDefaultTextLabel("Text", ""); 

    		countdownText[i] = countdown;
    		
    		attachChild(countdown);
    	}
    	
    	updateRenderState();
    }
    
	public void update() {
		Vector3f	store = new Vector3f();
		
    	for (int i = 0; i < sheep.length; i++) {
    		if (!sheep[i].hasAbductionCommenced() && sheep[i].getBody().isDrowning() && !sheep[i].getBody().hasDrowned()) {
        		float	textZoom = 0;
        		float 	remainingTime = sheep[i].getBody().getTimeUntilDrowning();

        		display.getScreenCoordinates(sheep[i].getBody().getWorldTranslation(), store);
        		
        		if (store.getZ() > 1) {
        			countdownText[i].print("");
        			continue;
        		}
        		
        		float	distanceToCam = display.getRenderer().getCamera().getLocation().distance(sheep[i].getBody().getWorldTranslation());
        		float	farPlane = display.getRenderer().getCamera().getFrustumFar();
        		
            	float 	textSize = display.getWidth() / 640f * 0.9f;
            	textSize *= 1f - FastMath.clamp(distanceToCam, 0, farPlane) / farPlane;
            	
            	float textOffset = -14 * textSize;
            	
	    		textZoom = (remainingTime % 1);
	        	
	    		if (textZoom > 0.5) {
	    			textZoom = 1 - textZoom;
	        	}
	    		
	    		textZoom *= 2f;
        		
        		if (remainingTime <= 10) {
        			countdownText[i].getTextColor().set(1, 0, 0, 0.8f);
        		}
        		else {
        			countdownText[i].getTextColor().set(0, 1, 0, 0.8f);
        		}

        		textZoom = (1 - FastMath.pow(FastMath.sin(FastMath.clamp(textZoom, 0, 1)), 2.0f) * (float) Math.PI * 0.5f) * 0.5f;

        		float scaleFactor = (1 + textZoom * 0.2f);
        		
        		countdownText[i].print(timeFormatter.format(remainingTime));
        		countdownText[i].setLocalScale(textSize * scaleFactor);
        		
        		countdownText[i].setLocalTranslation(store.getX() + textOffset * scaleFactor, store.getY() + 20, 0);
    		}
    		else {
    			countdownText[i].print("");
    		}
    	}
    	
    	updateRenderState();
	}
}
