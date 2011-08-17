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

import jmri.jmrit.Sound;
import java.awt.event.*;
import java.io.File;
import jmri.jmrit.audio.*;
import javax.swing.Timer;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Content;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;


// Usage:
// EngineSound() : constructor
// play() : plays short horn pop
// loop() : starts extended sustain horn
// stop() : ends extended sustain horn (plays end sound)

class EngineSound extends VSDSound {

    // Engine Sounds
    HashMap<Integer, SoundBite> notch_sounds;
    ArrayList<SoundBite> notchup_sounds;
    SoundBite notchup_sound;
    SoundBite start_sound;
    SoundBite shutdown_sound;

    int current_notch = 0;
    boolean initialized = false;
    boolean engine_started = false;
    boolean auto_start_engine = false;

    javax.swing.Timer t;

    public EngineSound(String name) {
	super(name);

	t = new javax.swing.Timer(136, new ActionListener() { 
		public void actionPerformed(ActionEvent e) {
		    handleTimerPop(e);
		}
	    });
	is_playing = false;
	engine_started = false;
	initialized = init();
	
    }

    public boolean init() {
	auto_start_engine = VSDecoderManager.instance().getVSDecoderPreferences().isAutoStartingEngine();
	return(true);
    }

    public void handleTimerPop(ActionEvent e) { }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    public void play() {
	log.debug("EngineSound Play: current_notch = " + current_notch);
	if (notch_sounds.containsKey(current_notch) && (engine_started || auto_start_engine)) {
	    notch_sounds.get(current_notch).play();
	    is_playing = true;
	}
    }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    public void loop() {
	if (notch_sounds.containsKey(current_notch) && (engine_started || auto_start_engine)) {
	    notch_sounds.get(current_notch).play();
	    is_playing = true;
	}
    }

    public void stop() {
	if (notch_sounds.containsKey(current_notch)) {
	    notch_sounds.get(current_notch).stop();
	}
	is_playing = false;
    }

    public void fadeIn() {
	this.play();
    }

    public void fadeOut() {
	this.stop();
    }

    // Note:  Using fade out/in of old/new sound as proxy for acceleration.

    public int getNotch() {
	return(current_notch);
    }

    public void changeNotch(int new_notch) {
	log.debug("EngineSound.changeNotch() current = " + current_notch + " new notch = " + new_notch);
	if (notch_sounds.containsKey(current_notch) && (engine_started || auto_start_engine))
	    notch_sounds.get(current_notch).fadeOut();
	if (notch_sounds.containsKey(new_notch) && (engine_started || auto_start_engine))
	    notch_sounds.get(new_notch).fadeIn();
	current_notch = new_notch;
    }

    public void startEngine() {
	// This doesn't really work yet.  Needs to auto-transition to notch[0]
	// at the end of the start sound.
	start_sound.play();
	current_notch = VSDecoder.calcEngineNotch(0.0f);
	t.setInitialDelay(4500);
	t.setDelay(4500);
	t.setRepeats(false);
	t.addActionListener(new ActionListener() { 
		public void actionPerformed(ActionEvent e) {
		    startToIdleAction(e);
		}
	    });
	log.debug("Starting Engine");
	t.start();
    }

    public void stopEngine() {
	notch_sounds.get(current_notch).fadeOut();
	shutdown_sound.play();
	engine_started = false;
    }

    private void startToIdleAction(ActionEvent e) {
	log.debug("Starting idle sound");
	notch_sounds.get(current_notch).loop();
	engine_started = true;
    }

    public boolean isEngineStarted() {
	return(engine_started);
    }

    public void setEngineStarted(boolean es) {
	engine_started = es;
    }
    
    public Element getXml() {
	Element me = new Element("sound");
	me.setAttribute("name", this.getName());
	me.setAttribute("type", "engine");
	// Do something, eventually...
	return(me);
    }

    public void setXml(Element e, VSDFile vf) {
	Element el;
	int num_notches;
	String fn;
	SoundBite sb;

	this.setName(e.getAttribute("name").getValue());
	log.debug("Diesel EngineSound: " + e.getAttribute("name").getValue());
	String n = e.getChild("notches").getValue();
	if (n != null) {
	    num_notches = Integer.parseInt(n);
	    log.debug("Number of notches: " + num_notches);
	}

	notch_sounds = new HashMap<Integer, SoundBite>();

	// Get the notch sounds
	Iterator itr = (e.getChildren("notch-sound")).iterator();
	int i = 0; 
	while(itr.hasNext()) {
	    el =(Element)itr.next();
	    fn = el.getChild("file").getValue();
	    int nn = Integer.parseInt(el.getChild("notch").getValue());
	    log.debug("Notch: " + nn + " File: " + fn);
	    sb = new SoundBite(vf, fn, "Engine_n" + i, "Engine_" + i);
	    sb.setLooped(true);
	    notch_sounds.put(nn, sb);
	    i++;
	}

	// Get the start and stop sounds
	el = e.getChild("start-sound");
	if (el != null) {
	    fn = el.getChild("file").getValue();
	    log.debug("Start sound: " + fn);
	    start_sound = new SoundBite(vf, fn, "Engine_start", 
					"Engine_Start");
	    start_sound.setLooped(false);
	}
	el = e.getChild("shutdown-sound");
	if (el != null) {
	    fn = el.getChild("file").getValue();
	    log.debug("Shutdown sound: " + fn);
	    shutdown_sound = new SoundBite(vf, fn, "Engine_shutdown",
					   "Engine_Shutdown");
	    shutdown_sound.setLooped(false);
	}
	itr = e.getChildren("notch-up-sound").iterator();
	i = 0;
	notchup_sounds = new ArrayList<SoundBite>();
	while(itr.hasNext()) {
	    el = (Element)itr.next();
	    // handle notch-up sounds... could be many.
	    // Note, there's not enough info here to truly
	    // do the notchup.  This is a placeholder.
	    fn = el.getChild("file").getValue();
	    log.debug("Notchup sound: " + fn);
	    SoundBite ns = new SoundBite(vf, fn, "Engine_notch_up_n" + i,
					 "Engine_NotchUp_" + i);
	    ns.setLooped(false);
	    notchup_sounds.add(ns);
	    i++;
	}
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EngineSound.class.getName());

}