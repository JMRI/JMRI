package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision$
 */

import jmri.AudioException;
import jmri.AudioManager;
import jmri.jmrit.audio.AudioBuffer;
import jmri.jmrit.audio.AudioSource;
import jmri.util.PhysicalLocation;

class SoundBite extends VSDSound {

    String filename, system_name, user_name;
    AudioBuffer sound_buf;
    AudioSource sound_src;
    boolean initialized = false;
    boolean looped = false;
    boolean buf_loaded = false;
    int minloops;
    int maxloops;
    long length;

    public SoundBite(String name) {
	super(name);
    }

    public SoundBite(VSDFile vf, String filename, String sname, String uname) {
	super(uname);
	this.filename = filename;
	system_name = sname;
	user_name = uname;
	initialized = init(vf);
    }

    public String getFileName() { return(filename); }
    public String getSystemName() { return(system_name); }
    public String getUserName() { return(user_name); }
    public boolean isInitialized() { return(initialized); }

    public final boolean init(VSDFile vf) {
        AudioManager am = jmri.InstanceManager.audioManagerInstance();
	if (!initialized) {
            try {
                sound_src = (AudioSource) am.provideAudio(SrcSysNamePrefix+system_name);
                sound_src.setUserName(BufUserNamePrefix+user_name);
                sound_buf = (AudioBuffer) am.provideAudio(BufSysNamePrefix+system_name);
                sound_buf.setUserName(BufUserNamePrefix+user_name);
                if (vf == null) {
                    sound_buf.setURL(vsd_file_base + filename);
                } else {
                    sound_buf.setInputStream(vf.getInputStream(filename));
                }
                sound_src.setAssignedBuffer(sound_buf);
		setLength();
                setLooped(false);
            } catch (AudioException ex) {
                log.warn("Problem creating SoundBite: " + ex);
            }
	}
	return(true);
    }

    // Direct access to the underlying source.  use with caution.
    public AudioSource getSource() { return(sound_src); }
    // WARNING: This will go away when we go to shared buffers... or at least it will
    // have to do the name lookup on behalf of the caller...
    public AudioBuffer getBuffer() { return(sound_buf); }

    // These can(?) be used to get the underlying AudioSource and AudioBuffer objects
    // from the DefaultAudioManager.
    public String getSourceSystemName() { return(SrcSysNamePrefix+system_name); }
    public String getSourceUserName() { return(SrcUserNamePrefix+user_name); }
    public String getBufferSystemName() { return(BufSysNamePrefix+system_name); }
    public String getBufferUserName() { return(BufUserNamePrefix+user_name); }

    /*
    public void reset() {
	initialized = false;
	initialized = init();
    }
    */

    public void setLooped(boolean loop, int minloops, int maxloops) {
	this.looped = loop;
	this.minloops = minloops;
	this.maxloops = maxloops;
	sound_src.setLooped(looped);
	sound_src.setMinLoops(minloops);
	sound_src.setMaxLoops(maxloops);
    }

    public void setLooped(boolean loop) {
	if (loop) {
	    this.setLooped(true, AudioSource.LOOP_CONTINUOUS, AudioSource.LOOP_CONTINUOUS);
	} else {
	    this.setLooped(false, AudioSource.LOOP_NONE, AudioSource.LOOP_NONE);
	}
    }

    public boolean isLooped() { return(looped); }

    public int getFadeInTime() { return(sound_src.getFadeIn()); }
    public int getFadeOutTime() { return(sound_src.getFadeOut()); }
    public void setFadeInTime(int t) { sound_src.setFadeIn(t); }
    public void setFadeOutTime(int t) { sound_src.setFadeOut(t); }
    public void setFadeTimes(int in, int out) {
	sound_src.setFadeIn(in);
	sound_src.setFadeOut(out);
    }

    public void shutdown() { }

    public void mute(boolean m) {
	if (m) {
	    volume = sound_src.getGain();
	    sound_src.setGain(0);
	} else
	    sound_src.setGain(volume);
    }

    public void setVolume(float v) {
	volume = v*gain;
	sound_src.setGain(volume);
    }

    public void play() {
	sound_src.play();
	is_playing = false;
    }

    public void loop() {
	sound_src.play();
    }

    public void stop() {
	sound_src.stop();
    }

    public void pause() {
	sound_src.pause();
    }

    public void rewind() {
	sound_src.rewind();
    }

    public void fadeOut() {
	sound_src.fadeOut();
    }

    public void fadeIn() {
	sound_src.fadeIn();
	//sound_src.play();
    }

    @Override
    public void setPosition(PhysicalLocation v) {
	super.setPosition(v);
	sound_src.setPosition(v);
    }

    public void setURL(String filename) {
	this.filename = filename;
	sound_buf.setURL(vsd_file_base + filename);
	    
    }

    public long getLength() { return(length); }
    public int getLengthAsInt() {
	// Note:  this only works for positive lengths...
	// Timer only takes an int... cap the length at MAXINT
	if (length > Integer.MAX_VALUE)
	    return(Integer.MAX_VALUE);
	else
	    // small enough to safely cast.
	    return((int)length);
    }
    public void setLength(long p) { length = p; } 
    public void setLength() {
	length = calcLength(this);
    }

    public static long calcLength(SoundBite s) {
	// Assumes later getBuffer() will find the buffer from AudioManager instead
	// of the current local reference... that's why I'm not directly using sound_buf here.

	// Required buffer functions not yet implemented
	AudioBuffer buf = s.getBuffer();

	long num_frames = buf.getLength();
	int frequency = buf.getFrequency();

	
	/*
	long num_frames = 1;
	long frequency = 125;
	*/
	if (frequency <= 0) {
	    // Protect against divide-by-zero errors
	    return(0l);
	}
	else {
	    return((1000 * num_frames) / frequency);
	}
    }


    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SoundBite.class.getName());
}    
	