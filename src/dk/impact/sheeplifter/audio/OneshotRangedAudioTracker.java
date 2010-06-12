/*
 * Copyright (c) 2003-2008 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package dk.impact.sheeplifter.audio;

import java.util.logging.Logger;

import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import com.jmex.audio.AudioTrack;
import com.jmex.audio.MusicTrackQueue;
import com.jmex.audio.RangedAudioTracker;
import com.jmex.audio.event.TrackStateAdapter;
import dk.impact.sheeplifter.audio.openal.SheepAudioSystem;

public class OneshotRangedAudioTracker extends RangedAudioTracker {
    private static final Logger logger = Logger.getLogger(OneshotRangedAudioTracker.class.getName());

    private	boolean		played = false;
    
    public OneshotRangedAudioTracker(AudioTrack track) {
    	super(track);
    }

    public OneshotRangedAudioTracker(AudioTrack track, float playRange,
            float stopRange) {
    	super(track, playRange, stopRange);
    }

    public OneshotRangedAudioTracker(AudioTrack track, float playRange,
            float stopRange, Spatial toTrack) {
    	super(track, playRange, stopRange, toTrack);
    }

    public void checkTrackAudible(Vector3f from) {
        if (hasPlayed()) {
        	return;
        }
        
        // update position as needed
        if (getToTrack() != null) {
            setPosition(getToTrack().getWorldTranslation());
        }
        
        if (getAudioTrack().isPlaying()) {
            return;
        }
        
        switch (getAudioTrack().getType()) {
            case MUSIC:
                MusicTrackQueue q = SheepAudioSystem.getSystem().getMusicQueue();
                q.addTrack(getAudioTrack());

                if (!(q.isPlaying() && q.getCurrentTrack() == getAudioTrack())) {
                    logger.info("I should start playing music: " + getAudioTrack().getResource());
                    q.setCurrentTrack(getAudioTrack());
                }
                break;
            case ENVIRONMENT:
                SheepAudioSystem.getSystem().getEnvironmentalPool().addTrack(getAudioTrack());
                getAudioTrack().setEnabled(true);
                logger.info("I should start playing environment: " + getAudioTrack().getResource());
                break;
            case HEADSPACE:
            case POSITIONAL:
                getAudioTrack().fadeIn(getFadeTime(), getMaxVolume());
                getAudioTrack().addTrackStateListener(new TrackStateAdapter() {
                    @Override
                    public void trackPlayed(AudioTrack track) {
                        setPlayed(true);
                    }
                });
                getAudioTrack().play();
                logger.info("I should start playing sound: " + getAudioTrack().getResource());
                break;
        }
    }

	public void setPlayed(boolean played) {
		this.played = played;
	}

	public boolean hasPlayed() {
		return played;
	}
}
