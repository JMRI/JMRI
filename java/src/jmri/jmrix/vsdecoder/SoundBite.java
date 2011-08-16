package jmri.jmrix.vsdecoder;

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

import jmri.jmrit.audio.*;

class SoundBite extends VSDSound {

    String filename, system_name, user_name;
    AudioBuffer sound_buf;
    AudioSource sound_src;
    boolean initialized = false;
    boolean looped = false;
    boolean buf_loaded = false;
    int minloops;
    int maxloops;

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

    public boolean init(VSDFile vf) {
	jmri.jmrit.audio.AudioFactory af = jmri.InstanceManager.audioManagerInstance().getActiveAudioFactory();
	if (!initialized) {
	    sound_src = af.createNewSource(SrcSysNamePrefix+system_name, 
					   BufUserNamePrefix+user_name);
	    sound_buf = af.createNewBuffer(BufSysNamePrefix+system_name,
					   BufUserNamePrefix+user_name);
	    if (vf == null) {
		sound_buf.setURL(vsd_file_base + filename);
	    } else {
		sound_buf.setInputStream(vf.getInputStream(filename));
	    }
	    sound_src.setAssignedBuffer(sound_buf);
	    setLooped(false);
	}
	return(true);
    }

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
	looped = loop;
	minloops = minloops;
	maxloops = maxloops;
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
	//sound_src.fadeIn();
	sound_src.play();
    }

    public void setURL(String filename) {
	this.filename = filename;
	sound_buf.setURL(vsd_file_base + filename);
	    
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SoundBite.class.getName());
}    
	