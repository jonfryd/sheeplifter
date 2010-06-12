package dk.impact.sheeplifter;

import com.jme.image.Texture;
import com.jme.input.InputHandler;
import com.jme.math.FastMath;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jmex.audio.AudioTrack;
import com.jmex.effects.transients.Fader;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameState;
import com.jmex.game.state.GameStateManager;

import dk.impact.sheeplifter.audio.GameAudioSystem;
import dk.impact.sheeplifter.util.FixedFontTextureState;

public class TitleState extends BasicGameState {
	/** Our display system. */
	private DisplaySystem display;

	private GameAudioSystem gameAudioSystem;
	
	private Fader backgroundImage;
	
    private Text text;
    private Text text2;

    private InputHandler input;
    
    private AudioTrack titleMusicTrack;
    private AudioTrack silenceTrack;

    public TitleState(GameAudioSystem gameAudioSystem) {
        super(GameStates.TITLE_STATE);
        this.gameAudioSystem = gameAudioSystem;
        
        display = DisplaySystem.getDisplaySystem();
        initInput();
        initBackground();
        initText();

        titleMusicTrack = gameAudioSystem.registerMusic("res/sound/sheeplifter-menu-vandloop-wet.ogg");
        silenceTrack = gameAudioSystem.registerMusic("res/sound/silence.ogg");

		rootNode.setLightCombineMode(Spatial.LightCombineMode.Off);
        rootNode.setRenderQueueMode(Renderer.QUEUE_ORTHO);
        rootNode.updateRenderState();
        rootNode.updateGeometricState(0, true);
    }
	
	/**
	 * Inits the input handler we will use for navigation of the menu.
	 */
	protected void initInput() {
		input = new TitleHandler( this );
	}
	
	private void initBackground() {
		backgroundImage = new Fader("ground", display.getWidth(), display.getHeight(), new ColorRGBA(1.0f, 1.0f, 1.0f, 0.0f), 1.0f);
		backgroundImage.setLocalTranslation(display.getWidth() / 2, display.getHeight() / 2, 0);
		backgroundImage.setAlpha(0.0f);
		
        Texture groundTex = TextureManager.loadTexture(TitleState.class.getClassLoader().getResource("res/maps/sheeplifter.jpg"),
                                                    Texture.MinificationFilter.Trilinear,
                                                    Texture.MagnificationFilter.Bilinear);
        TextureState ts = display.getRenderer().createTextureState();
        ts.setTexture(groundTex);
        ts.setEnabled(true);
        backgroundImage.setRenderState(ts);
        
        rootNode.attachChild(backgroundImage);  
	}

	private void initText() {
        text = Text.createDefaultTextLabel( "info" );
        text.print( "<ENTER>: BEGIN" );
        text.setRenderState(FixedFontTextureState.getFixedFontTextureState());
        text.updateRenderState();
        
        text2 = Text.createDefaultTextLabel( "info" );
        text2.print( "<ESC>: QUIT" );
        text2.setRenderState(FixedFontTextureState.getFixedFontTextureState());
        text2.updateRenderState();        
		
        rootNode.attachChild( text );
        rootNode.attachChild( text2 );
	}
	
	/**
	 * @param tpf The time since last frame.
	 * @see GameState#update(float)
	 */
	public void update(float tpf) {
		// Check if the button has been pressed.
		input.update(tpf);

		float size = FastMath.sin(Timer.getTimer().getTimeInSeconds() * 4f) * 0.06f + 1.0f;
		size *= display.getWidth() / 640f;
        float textOffset = -80 * size;
        float textOffset2 = -62 * size;
        
        text.getLocalTranslation().set( display.getWidth() * 0.155f + textOffset, display.getHeight() * 0.45f, 0 );
        text.getLocalScale().set(size, size, 0);
        text2.getLocalTranslation().set( display.getWidth() * 0.1582f + textOffset2, display.getHeight() * 0.40f, 0 );
        text2.getLocalScale().set(size, size, 0);

		if ((backgroundImage.getFadeMode() == Fader.FadeMode.FadeIn) && (backgroundImage.getAlpha() == 0.0f)) {
			// no need to waste cycles
			super.setActive(false);
			text.setCullHint(CullHint.Always);
			text2.setCullHint(CullHint.Always);
			GameStateManager.getInstance().activateChildNamed(GameStates.IN_GAME_STATE);
		} else if ((backgroundImage.getFadeMode() == Fader.FadeMode.FadeOut) && (backgroundImage.getAlpha() == 1.0f)) {
			if (GameStateManager.getInstance().getChild(GameStates.IN_GAME_STATE).isActive()) {
				GameStateManager.getInstance().deactivateChildNamed(GameStates.IN_GAME_STATE);
			}
		}		

        text.getTextColor().set(1, 1, 1, backgroundImage.getAlpha() * 0.85f);
        text2.getTextColor().set(1, 1, 1, backgroundImage.getAlpha() * 0.85f);

		rootNode.updateGeometricState(tpf, true);
	}

	@Override
	public void setActive(boolean active) {
		if (active) {
			gameAudioSystem.playMusic(titleMusicTrack);
			backgroundImage.setMode(Fader.FadeMode.FadeOut);
			text.setCullHint(CullHint.Never);
			text2.setCullHint(CullHint.Never);

			super.setActive(active);
		} else {
			backgroundImage.setMode(Fader.FadeMode.FadeIn);
			gameAudioSystem.playMusic(silenceTrack);			
		}
	}
}