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
 * @version			$Revision: 18481 $
 */

import org.jdom.Element;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import jmri.AudioManager;
import jmri.AudioException;
import jmri.util.PhysicalLocation;
import jmri.jmrit.audio.AudioBuffer;


// Usage:
// EngineSound() : constructor
// play() : plays short horn pop
// loop() : starts extended sustain horn
// stop() : ends extended sustain horn (plays end sound)

class Diesel3Sound extends EngineSound {

    // Engine Sounds
    HashMap<Integer, D3Notch> notch_sounds;
    SoundBite _sound;
    AudioBuffer start_buffer;
    AudioBuffer stop_buffer;
    AudioBuffer transition_buf;
    
    // Old sounds
    SoundBite notch_transition; // used for changing notches

    // Common variables
    Float throttle_setting;
    EnginePane engine_pane;

    int current_notch = 1;
    boolean changing_speed = false;
    boolean is_looping = false;

    Timer t;

    public Diesel3Sound(String name) {
	super(name);
    }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    @Override
    public void play() {
	this.loop();
	/*
	if (engine_started || auto_start_engine) {
	    _sound.play();
	    is_playing = true;
	}
	*/
    }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    @Override
    public void loop() {
	if (notch_sounds.containsKey(current_notch) && (engine_started || auto_start_engine)) {
	    log.debug("Looping.  Notch = " + current_notch);
	    D3Notch cn = notch_sounds.get(current_notch);
	    log.debug("Buffer " + cn.getLoopBuffer(cn.loopIndex()).getSystemName() + " length " + cn.getLoopBufferLength(cn.loopIndex()));
	    t = newTimer(cn.getLoopBufferLength(cn.loopIndex()) - 100, false, 
			 new ActionListener() { 
			     public void actionPerformed(ActionEvent e) {
				 handleLoopTimerPop(e);
			     }
			 });
	    if (_sound.getSource().numQueuedBuffers() < 2) {
		log.debug("Buffer count low.  Adding buffer.");
		AudioBuffer b = notch_sounds.get(current_notch).nextLoopBuffer();
		log.debug("Loop: Adding buffer " + b.getSystemName());
		_sound.queueBuffer(b);
	    }
	    if (!is_playing) {
		_sound.play();
		is_playing = true;
	    }
	    t.start();
	    is_looping = true;
	}
    }

    // WARNING: Potentially recursive.
    private void handleLoopTimerPop(ActionEvent e) {
	_sound.unqueueBuffers();
	log.debug("Removed used buffers.  Num Queued = " + _sound.numQueuedBuffers());
	//if (_sound.getSource().numQueuedBuffers() < 2) {
	//    AudioBuffer b = notch_sounds.get(current_notch).nextLoopBuffer();
	//    log.debug("Loop: Adding buffer " + b.getSystemName());
	//    _sound.queueBuffer(b);
	//}
	this.loop();
    }

    @Override
    public void stop() {
	// Stop the loop timer, in case it's running
	if (t != null)
	    t.stop();
	if (_sound != null)
	    _sound.stop();
	is_playing = false;
	is_looping = false;
    }

    public void stopLoop() {
	// Just stop the timer.  Let the sound continue to play.
	t.stop();
	is_looping = false;
    }

    @Override
    public void handleSpeedChange(Float s, EnginePane e) {
	throttle_setting = s;
	if (!changing_speed) 
	    changeSpeed(s, e);
    }

    // Responds to "CHANGE" trigger
    public void changeThrottle(float s) {
	D3Notch cn = notch_sounds.get(current_notch);
	log.debug("Change Throttle: " + s + " Accel Limit = " + cn.getAccelLimit() + " Decel Limit = " + cn.getDecelLimit());
	throttle_setting = s;
	// Really first, is this a "Panic Stop"?
	if (s < 0) {
	    // DO something to shut down
	    this.shutdown();
	    return;
	}
	// First, am I too fast or too slow for the current notch?
	if ((s <= cn.getAccelLimit()) && (s >= cn.getDecelLimit())) {
	    // Still (or now) in this notch.  Nothing to do.
	    if (changing_speed)
		_sound.queueBuffer(cn.nextLoopBuffer());
	    changing_speed = false;
	    log.debug("No Change");
	    if (!this.is_looping)
		this.loop();
	    return;
	}
	else if (s > cn.getAccelLimit()) {
	    // Too fast. Need to go to next notch up.
	    changing_speed = true;
	    transition_buf = cn.getAccelBuffer();
	    current_notch = cn.getNextNotch();
	    log.debug("Change up. notch=" + current_notch);
	} 
	else if (s < cn.getDecelLimit()) {
	    // Too slow.  Need to go to next notch down.
	    changing_speed = true;
	    transition_buf = cn.getDecelBuffer();
	    current_notch = cn.getPrevNotch();
	    log.debug("Change down. notch=" + current_notch);
	}
	// Now, regardless of whether we're going up or down, set the timer,
	// fade the current sound, and move on.
	if (transition_buf == null) {
	    // No transition sound to play.  Skip the timer bit.
	    // Recurse directly to try the next notch.
	    this.loop();
	    changeThrottle(s);
	    log.debug("No transition sound defined.");
	    return;
	} else {
	    // Stop the loop if it's running
	    this.stopLoop();
	    // queue up the transition sound buffer.
	    _sound.queueBuffer(transition_buf);
	    // Set up a timer to come in and loop.
	    t = newTimer(SoundBite.calcLength(transition_buf) - this.getFadeInTime(), false,
			 new ActionListener() {
			     public void actionPerformed(ActionEvent e) {
				 handleNotchTimerPop(e);
			     }
			 });
	    t.start();
	}
	return;
    }

    protected void changeSpeed(Float s, EnginePane e) {
	engine_pane = e; // this should probably be cleaned up.  It's here for the recursion.
	changeThrottle(s);
    }

    protected void handleNotchTimerPop(ActionEvent e) {
	// semi-Recursively call the speed change handler until it quits setting up timers.
	//notch_transition.fadeOut();
	//changeSpeed(throttle_setting, engine_pane);
	changeThrottle(throttle_setting);
    }

    @Override
    public void startEngine() {
	_sound.unqueueBuffers();
	_sound.queueBuffer(start_buffer);
	_sound.play();
	log.debug("Starting Engine. Sound buffers: " + _sound.numQueuedBuffers());
	current_notch = calcEngineNotch(0.0f);
	if (notch_sounds.containsKey(current_notch)) {
	    AudioBuffer b = notch_sounds.get(current_notch).nextLoopBuffer();
	    log.debug("Loop: Adding buffer " + b.getSystemName());
	    _sound.queueBuffer(b);
	}
	t = newTimer(Math.max(SoundBite.calcLength(start_buffer) - _sound.getFadeOutTime() + 100, 0), false, new ActionListener() { 
                @Override
		public void actionPerformed(ActionEvent e) {
		    startToIdleAction(e);
		}
	    } );
	t.setInitialDelay((int)(SoundBite.calcLength(start_buffer) - _sound.getFadeOutTime()));
	t.setRepeats(false);
	engine_started = true;
	log.debug("Starting Engine");
	t.start();
    }

    @Override
    public void stopEngine() {
	this.stopLoop();
	_sound.unqueueBuffers();
	log.debug("Removed used buffers.  Num Queued = " + _sound.numQueuedBuffers());
	log.debug("Stopping Engine. Sound buffers: " + _sound.numQueuedBuffers());
	_sound.queueBuffer(stop_buffer);
	engine_started = false;
    }

    private void startToIdleAction(ActionEvent e) {
	log.debug("Starting idle sound notch = " + current_notch + " sound = " + notch_sounds.get(current_notch));
	//_sound.queueBuffer(notch_sounds.get(current_notch).nextLoopBuffer());
	log.debug("Sound buffers: " + _sound.numQueuedBuffers());
	this.loop();
	engine_started = true;
    }

    @Override
    public void shutdown() {
	this.stop();
    }

    @Override
    public void mute(boolean m) {
	_sound.mute(m);
    }

    @Override
    public void setVolume(float v) {
	_sound.setVolume(v);
    }

    @Override
    public void setPosition(PhysicalLocation p) {
	_sound.setPosition(p);
    }

    protected Timer newTimer(long time, boolean repeat, ActionListener al) {
	time = Math.max(1, time);  // make sure the time is > zero
	t = new Timer((int)time, al);
	t.setInitialDelay((int)time);
	t.setRepeats(repeat);
	return(t);
    }


    @Override
    public Element getXml() {
	Element me = new Element("sound");
	me.setAttribute("name", this.getName());
	me.setAttribute("type", "engine");
	// Do something, eventually...
	return(me);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setXml(Element e, VSDFile vf) {
	Element el;
	String fn;
	D3Notch sb;

	// Handle the common stuff.
	super.setXml(e, vf);
	
	log.debug("Diesel EngineSound: " + e.getAttribute("name").getValue());
	_sound = new SoundBite(e.getAttributeValue("name"), SoundBite.BufferMode.QUEUE_MODE);
	notch_sounds = new HashMap<Integer, D3Notch>();

	// Get the notch sounds
	Iterator<Element> itr =  (e.getChildren("notch-sound")).iterator();
	int i = 0; 
	while(itr.hasNext()) {
	    el = itr.next();
	    sb = new D3Notch();
	    int nn = Integer.parseInt(el.getChildText("notch"));
	    sb.setNotch(nn);
	    List<Element> elist = (List<Element>)el.getChildren("file");
	    int j = 0;
	    for (Element fe : elist) {
		fn = fe.getText();
		AudioBuffer b = D3Notch.getBuffer(vf, fn, "Engine_n" + i + "_" + j, "Engine_" + i + "_" + j);
		log.debug("Buffer created: " + b + " name: " + b.getSystemName());
		sb.addLoopBuffer(b);
		j++;
	    }
	    //log.debug("Notch: " + nn + " File: " + fn);

	    // Gain is broken, for the moment.  Buffers don't have gain. Sources do.
	    //_sound.setGain(setXMLGain(el));
	    _sound.setGain(default_gain);

	    sb.setNextNotch(el.getChildText("next-notch"));
	    sb.setPrevNotch(el.getChildText("prev-notch"));
	    sb.setAccelLimit(el.getChildText("accel-limit"));
	    sb.setDecelLimit(el.getChildText("decel-limit"));
	    if (el.getChildText("accel-file") != null)
		sb.setAccelBuffer(D3Notch.getBuffer(vf, el.getChildText("accel-file"), "Engine_na" + i, "Engine_na" + i));
	    else
		sb.setAccelBuffer(null);
	    if (el.getChildText("decel-file") != null)
		sb.setDecelBuffer(D3Notch.getBuffer(vf, el.getChildText("decel-file"), "Engine_nd" + i, "Engine_nd" + i)); 
	    else
		sb.setDecelBuffer(null);
	    // Store in the list.
	    notch_sounds.put(nn, sb);
	    i++;
	}

	// Get the start and stop sounds
	el = e.getChild("start-sound");
	if (el != null) {
	    fn = el.getChild("file").getValue();
	    //log.debug("Start sound: " + fn);
	    start_buffer = D3Notch.getBuffer(vf, fn, "Engine_start", "Engine_Start");
	    // Handle gain
	    //start_sound.setGain(setXMLGain(el));
	    //start_sound.setFadeTimes(this.getFadeInTime(), this.getFadeOutTime());
	    //start_sound.setLooped(false);
	}
	el = e.getChild("shutdown-sound");
	if (el != null) {
	    fn = el.getChild("file").getValue();
	    //log.debug("Shutdown sound: " + fn);
	    stop_buffer = D3Notch.getBuffer(vf, fn, "Engine_shutdown", "Engine_Shutdown");
	    //shutdown_sound.setLooped(false);
	    // Handle gain
	    //shutdown_sound.setGain(setXMLGain(el));
	    //shutdown_sound.setFadeTimes(this.getFadeInTime(), this.getFadeOutTime());
	}

    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Diesel3Sound.class.getName());

}

class D3Notch {
    
    private AudioBuffer accel_buf;
    private AudioBuffer decel_buf;
    private int my_notch, next_notch, prev_notch;
    private float accel_limit, decel_limit;
    private int loop_index;
    private List<AudioBuffer> loop_bufs = new ArrayList<AudioBuffer>();

    public D3Notch() {
	this(1, 1, 1, null, null, null);
    }

    public D3Notch(int notch, int next, int prev) {
	this(notch, next, prev, null, null, null);
    }

    public D3Notch(int notch, int next, int prev, AudioBuffer accel, AudioBuffer decel, List<AudioBuffer> loop) {
	my_notch = notch;
	next_notch = next;
	prev_notch = prev;
	accel_buf = accel;
	decel_buf = decel;
	if (loop != null)
	    loop_bufs = loop;
	loop_index = 0;
    }

    public int getNextNotch() { return(next_notch); }
    public int getPrevNotch() { return(prev_notch); }
    public int getNotch() { return(my_notch); }
    public AudioBuffer getAccelBuffer() { return(accel_buf); }
    public AudioBuffer getDecelBuffer() { return(decel_buf); }
    public float getAccelLimit() { return(accel_limit); }
    public float getDecelLimit() { return(decel_limit); }
    public List<AudioBuffer> getLoopBuffers() { return(loop_bufs); }
    public AudioBuffer getLoopBuffer(int idx) { return(loop_bufs.get(idx)); }
    public long getLoopBufferLength(int idx) { return(SoundBite.calcLength(loop_bufs.get(idx))); }

    public void setNextNotch(int n) { next_notch = n; }
    public void setNextNotch(String s) { next_notch = setIntegerFromString(s); }
    public void setPrevNotch(int p) { prev_notch = p; }
    public void setPrevNotch(String s) { prev_notch = setIntegerFromString(s); }
    public void setAccelLimit(float l) { accel_limit = l; }
    public void setAccelLimit(String s) { accel_limit = setFloatFromString(s); }
    public void setDecelLimit(float l) { decel_limit = l; }
    public void setDecelLimit(String s) { decel_limit = setFloatFromString(s); }
    public void setNotch(int n) { my_notch = n; }
    public void setAccelBuffer(AudioBuffer b) { accel_buf = b; }
    public void setDecelBuffer(AudioBuffer b) { decel_buf = b; }
    public void addLoopBuffer(AudioBuffer b) { loop_bufs.add(b); }
    public void setLoopBuffers(List<AudioBuffer> l) { loop_bufs = l; }
    public void clearLoopBuffers() { loop_bufs.clear(); }
    public AudioBuffer nextLoopBuffer() { return(loop_bufs.get(incLoopIndex())); }

    public int loopIndex() { return(loop_index); }
    public int incLoopIndex() {
	// Increment
	loop_index++;
	// Correct for wrap.
	if (loop_index >= loop_bufs.size())
	    loop_index = 0;

	return(loop_index);
    }	

    private int setIntegerFromString(String s) {
	if (s == null) {
	    return(0);
	}
	try {
	    int n = Integer.parseInt(s);
	    return(n);
	} catch (NumberFormatException e) {
	    log.debug("Invalid integer: " + s);
	    return(0);
	}
    }

    private float setFloatFromString(String s) {
	if (s == null) {
	    return(0.0f);
	}
	try {
	    float f = Float.parseFloat(s) / 100.0f;
	    return(f);
	} catch (NumberFormatException e) {
	    log.debug("Invalid float: " + s);
	    return(0.0f);
	}
    }

    static public AudioBuffer getBuffer(VSDFile vf, String filename, String sname, String uname) {
	AudioBuffer buf = null;
	AudioManager am = jmri.InstanceManager.audioManagerInstance();
	try {
	    buf = (AudioBuffer) am.provideAudio(VSDSound.BufSysNamePrefix+sname);
	    buf.setUserName(VSDSound.BufUserNamePrefix+uname);
	    if (vf == null) {
		// Need to fix this.
		//buf.setURL(vsd_file_base + filename);
		log.debug("No VSD File");
		return(null);
	    } else {
		java.io.InputStream ins = vf.getInputStream(filename);
		if (ins != null) {
		    buf.setInputStream(ins);
		}
		else {
		    log.debug("Input Stream failed");
		    return(null);
		}
	    }
	} catch (AudioException ex) {
	    log.error("Problem creating SoundBite: " + ex);
	    return(null);
	}

	log.debug("Buffer created: " + buf + " name: " + buf.getSystemName());
	return(buf);
    }
 
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(D3Notch.class.getName());
   
}