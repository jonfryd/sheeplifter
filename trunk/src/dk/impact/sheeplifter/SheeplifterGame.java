package dk.impact.sheeplifter;

import java.util.logging.Level;
import java.util.logging.Logger;

import dk.impact.sheeplifter.audio.GameAudioSystem;
import dk.impact.sheeplifter.util.Debugging;
import com.jme.util.TextureManager;
import com.jme.app.SimpleGame;
import com.jme.input.KeyBindingManager;
import com.jmex.game.state.GameStateManager;

public class SheeplifterGame extends SimpleGame {
	private static final boolean 	ENABLE_STATS = false; // Set to false for release builds
	private static final Level 		LOGGER_LEVEL = Level.WARNING;
	
	private GameAudioSystem			gameAudioSystem;

	private InGameState				inGameState;
	private TitleState				titleState;
	
	/** Only used in the static exit method. */
	private static SheeplifterGame 	instance;	
	
    public SheeplifterGame(int aaSamples){
        super();
    	
        Logger.getLogger("").setLevel(LOGGER_LEVEL);
    	
        if (!ENABLE_STATS) {
        	// NOTE:  Disables access to the 'F4' profiling/stats stuff.
        	System.clearProperty("jme.stats");
        }
    	
        // anti-aliasing samples (0=disable super sampling)
        samples = aaSamples;
        
        // stencil buffer needed for shadows
        stencilBits = 0;

        // no texture compression by default - looks damn ugly!
        TextureManager.COMPRESS_BY_DEFAULT = false;
    }

    /**
     * Entry point for the test
     *
     * @param args
     */
    public static void main(String[] args){
    	int		aaSamples = 4;
    	boolean	showDebuggingConsole = false;
    	
    	for (int i = 0; i < args.length; i++) {
    		if (args[i].equals("-console")) {
    			showDebuggingConsole = true;
    		}
    		else if (args[i].equals("-noaa")) {
    			aaSamples = 0;
    		}
    	}

    	SheeplifterGame app = new SheeplifterGame(aaSamples);

        if(showDebuggingConsole){
            Debugging.openConsole(app);
        }

        app.setConfigShowMode(ConfigShowMode.AlwaysShow, SheeplifterGame.class.getClassLoader().getResource("res/maps/optionscreen.png"));
        app.start();
    }

    /**
     * Overrides the first-person-input-handler of the <code>BaseSimpleGame</code> so that it won't disturb our chasecam.
     */
    protected void updateInput(){
    }

    protected void simpleUpdate() {
        float interpolation = timer.getTimePerFrame();

        updateSound(interpolation);
        
        GameStateManager.getInstance().update(interpolation);
    }

    protected void simpleRender() {
        float interpolation = timer.getTimePerFrame();

        GameStateManager.getInstance().render(interpolation);
    }

    private void updateSound(float timeDelta){
        //update audio system
        gameAudioSystem.updateAudio();
    }

    protected void cleanup(){
        super.cleanup();

        if (inGameState != null) {
        	inGameState.cleanup();
        }
        if (gameAudioSystem != null) {
            gameAudioSystem.cleanup();
        }
    }

    /**
     * @see com.jme.app.SimpleGame#initGame()
     */
    protected void simpleInitGame(){
		instance = this;
        display.setTitle("Sheeplifter");

        setupKeyBindings();

        gameAudioSystem = new GameAudioSystem(cam);

        inGameState = new InGameState(gameAudioSystem, cam, settings);
        titleState = new TitleState(gameAudioSystem);
        
        GameStateManager.create();
		GameStateManager.getInstance().attachChild(titleState);
		GameStateManager.getInstance().attachChild(inGameState);
				
		inGameState.setActive(false);
		titleState.setActive(true);
    }

    private void setupKeyBindings(){
		KeyBindingManager.getKeyBindingManager().remove("exit");
    }
    
	/**
	 * Static method to finish this application.
	 */
	public static void exit() {
		// Only exit if in-game state is inactive
		if (!instance.inGameState.isActive()) {
			instance.finish();
		}
	}    
}
    