package dk.impact.sheeplifter.audio.openal;

import com.jmex.audio.AudioSystem;


public abstract class SheepAudioSystem extends AudioSystem {
    private static SheepAudioSystem system;
	
	public static synchronized SheepAudioSystem getSystem() {
        if (system == null)
            system = new SheepOpenALSystem();
        return system;
    }		
}