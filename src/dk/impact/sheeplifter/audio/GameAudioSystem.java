package dk.impact.sheeplifter.audio;

import java.net.URL;
import java.util.ArrayList;

import com.jme.renderer.Camera;
import com.jme.scene.Spatial;
import com.jmex.audio.AudioTrack;
import com.jmex.audio.RangedAudioTracker;
import com.jmex.audio.AudioTrack.TrackType;
import com.jmex.audio.MusicTrackQueue.RepeatType;
import dk.impact.sheeplifter.audio.openal.SheepAudioSystem;

public class GameAudioSystem {
	private ArrayList<RangedAudioTracker> trackers = new ArrayList<RangedAudioTracker>();
	
	private final static int MAX_SOUND_EFFECTS = 16;
	
	private SheepAudioSystem 	audio;
	private Camera				cam;
	
	public GameAudioSystem(Camera cam) {
		this.cam = cam;
		setupAudio();
	}
	
	public void cleanup() {
		for (AudioTrack t : audio.getMusicQueue().getTrackList()) {
			t.getPlayer().cleanup();
		}
		
		audio.cleanup();
	}
	
	private void setupAudio() {
        // grab a handle to our audio system.
        audio = SheepAudioSystem.getSystem();
        
        // setup our ear tracker to track the camera's position and orientation.
        audio.getEar().trackOrientation(cam);
        audio.getEar().trackPosition(cam);	
    }
	
	public void updateAudio() {
        audio.update();

        int noTrackers = trackers.size();
        
        for (int x = noTrackers; --x >= 0; ) {
            RangedAudioTracker t = trackers.get(x);
 
            t.checkTrackAudible(cam.getLocation());
            
            //System.out.println(t);
            
            // removed dead OneshotAudioTrackers
            if (t instanceof OneshotRangedAudioTracker) {
            	if (((OneshotRangedAudioTracker) t).hasPlayed()) {
            		trackers.remove(x);
            		t.setMaxVolume(0);
            	}
            }
        }        
	}
	
	public void stopCurrent() {

	}
	
	public void playMusic(AudioTrack track) {
		updateAudio(); // update timing so fading works correctly
		
		audio.getMusicQueue().setCurrentTrack(track);		
		track.setTargetVolume(0.5f);
        
        if (!audio.getMusicQueue().isPlaying()) {
        	audio.getMusicQueue().play();
        }
	}
	
	public AudioTrack registerMusic(String musicResource) {
        // setup a music score for our demo
        AudioTrack music = getMusic(GameAudioSystem.class.getClassLoader().getResource(musicResource));
        audio.getMusicQueue().setRepeatType(RepeatType.ONE);
        audio.getMusicQueue().setCrossfadeinTime(1f);
        audio.getMusicQueue().setCrossfadeoutTime(1f);
        audio.getMusicQueue().addTrack(music);
        
        return music;
	}
	
	public void addPositionalTrack(String soundResource, Spatial toTrack, float volume) {
        if (trackers.size() < MAX_SOUND_EFFECTS) {
    		AudioTrack sfx = getSFX(GameAudioSystem.class.getClassLoader().getResource(soundResource));
    		sfx.setType(TrackType.POSITIONAL);
    		sfx.setRelative(false);
    		sfx.setLooping(false);

            OneshotRangedAudioTracker track = new OneshotRangedAudioTracker(sfx, 750, 1000);
            track.setToTrack(toTrack);
            track.setTrackIn3D(true);
            track.setMaxVolume(volume);
            track.setFadeTime(0.3f);
            trackers.add(track);        
        }
	}
	
	public RangedAudioTracker addPositionalLoopedTrack(String soundResource, Spatial toTrack, float volume) {
        if (trackers.size() < MAX_SOUND_EFFECTS) {
    		AudioTrack sfx = getSFX(GameAudioSystem.class.getClassLoader().getResource(soundResource));
    		sfx.setType(TrackType.POSITIONAL);
    		sfx.setRelative(false);
    		sfx.setLooping(true);

            RangedAudioTracker track = new RangedAudioTracker(sfx, 750, 1000);
            track.setToTrack(toTrack);
            track.setTrackIn3D(true);
            track.setMaxVolume(volume);
            track.setFadeTime(0.3f);
            trackers.add(track);        
            
            return track;
        }
        
        return null;
	}	

	public void removePositionalLoopedTrack(RangedAudioTracker tracker) {
		if (tracker != null) {
			tracker.getAudioTrack().stop();
			trackers.remove(tracker);			
		}
	}
	
	public AudioTrack createAmbientSound(String soundResource) {
		AudioTrack sfx = getSFX(GameAudioSystem.class.getClassLoader().getResource(soundResource));
		sfx.setRelative(false);
		sfx.setLooping(true);
		sfx.setType(TrackType.ENVIRONMENT);
		
		return sfx;
	}
	
    private AudioTrack getMusic(URL resource) {
        // Create a non-streaming, non-looping, relative sound clip.
        AudioTrack sound = SheepAudioSystem.getSystem().createAudioTrack(resource, true);
        sound.setType(TrackType.MUSIC);
        sound.setRelative(false);
        sound.setLooping(true);
        return sound;
    }
    
    private AudioTrack getSFX(URL resource) {
        // Create a non-streaming, positional sound clip.
        AudioTrack sound = SheepAudioSystem.getSystem().createAudioTrack(resource, false);
        return sound;
    }	

}
