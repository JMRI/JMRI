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
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.util.PhysicalLocation;


// Usage:
// EngineSound() : constructor
// play() : plays short horn pop
// loop() : starts extended sustain horn
// stop() : ends extended sustain horn (plays end sound)

class DieselSound extends EngineSound {

    // Engine Sounds
    HashMap<Integer, SoundBite> notch_sounds;
    ArrayList<SoundBite> notchup_sounds;
    ArrayList<NotchTransition> transition_sounds;
    SoundBite notchup_sound;
    SoundBite start_sound;
    SoundBite shutdown_sound;
    NotchTransition notch_transition; // used for changing notches

    int current_notch = 1;

    public DieselSound(String name) {
	super(name);
    }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    @Override
    public void play() {
	log.debug("EngineSound Play: current_notch = " + current_notch);
	if (notch_sounds.containsKey(current_notch) && (engine_started || auto_start_engine)) {
	    notch_sounds.get(current_notch).play();
	    is_playing = true;
	}
    }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    @Override
    public void loop() {
	if (notch_sounds.containsKey(current_notch) && (engine_started || auto_start_engine)) {
	    notch_sounds.get(current_notch).play();
	    is_playing = true;
	}
    }

    @Override
    public void stop() {
	if (notch_sounds.containsKey(current_notch)) {
	    notch_sounds.get(current_notch).stop();
	}
	is_playing = false;
    }

    @Override
    public void changeNotch(int new_notch) {
	log.debug("EngineSound.changeNotch() current = " + current_notch + 
		  " new notch = " + new_notch);
	if (new_notch != current_notch) {
	    if (notch_sounds.containsKey(current_notch) && (engine_started || auto_start_engine))
		notch_sounds.get(current_notch).fadeOut();

	    notch_transition = findNotchTransient(current_notch, new_notch);
	    if (notch_transition != null) {
		log.debug("notch transition: name = " + notch_transition.getFileName() + " length = " + notch_transition.getLengthAsInt() +
			  "fade_length = " + fade_length);
		// Handle notch transition...
		t = newTimer(notch_transition.getLengthAsInt() - notch_sounds.get(new_notch).getFadeInTime(), false,
				   new ActionListener() {
				       public void actionPerformed(ActionEvent e) {
					   handleNotchTimerPop(e);
				       }
				   });
		t.start();
		notch_transition.fadeIn();
	    } else {
		log.debug("notch transition not found!");
		if (notch_sounds.containsKey(new_notch) && (engine_started || auto_start_engine))
		    notch_sounds.get(new_notch).fadeIn();
	    }
	    current_notch = new_notch;
	}
    }

    protected void handleNotchTimerPop(ActionEvent e) {
	// notch value has already been changed
	log.debug("Notch timer pop. nt.next_notch = " + notch_transition.getNextNotch() +
		  "file = " + notch_sounds.get(notch_transition.getNextNotch()).getFileName());
	if (notch_sounds.containsKey(notch_transition.getNextNotch()) && (engine_started || auto_start_engine)) {
	    notch_sounds.get(notch_transition.getNextNotch()).fadeIn();
	}
	notch_transition.fadeOut();
    }

    private NotchTransition findNotchTransient(int prev, int next) {
	log.debug("Looking for Transient: prev = " + prev + " next = " + next);
	for (NotchTransition nt : transition_sounds) {
	    log.debug("searching: nt.prev = " + nt.getPrevNotch() + " nt.next = " + nt.getNextNotch());
	    if ((nt.getPrevNotch() == prev) && (nt.getNextNotch() == next)) {
		log.debug("Found transient: prev = " + nt.getPrevNotch() + " next = " + nt.getNextNotch());
		return(nt);
	    }
	}
	// If we loop out, there's no transition that matches.
	return(null);
    }

    @Override
    public void startEngine() {
	start_sound.play();
	current_notch = calcEngineNotch(0.0f);
	t = newTimer(4500, false, new ActionListener() { 
                @Override
		public void actionPerformed(ActionEvent e) {
		    startToIdleAction(e);
		}
	    } );
	t.setInitialDelay(4500);
	t.setRepeats(false);
	log.debug("Starting Engine");
	t.start();
    }

    @Override
    public void stopEngine() {
	notch_sounds.get(current_notch).fadeOut();
	shutdown_sound.play();
	engine_started = false;
    }

    private void startToIdleAction(ActionEvent e) {
	log.debug("Starting idle sound notch = " + current_notch + " sound = " + notch_sounds.get(current_notch));
	notch_sounds.get(current_notch).loop();
	engine_started = true;
    }

    @Override
    public void shutdown() {
	for (SoundBite ns : notch_sounds.values()) {
	    ns.stop();
	}
	for (SoundBite nus : notchup_sounds) {
	    nus.stop();
	}
	for (NotchTransition nt : transition_sounds) {
	    nt.stop();
	}
	if (notchup_sound != null) notchup_sound.stop();
	if (start_sound != null) start_sound.stop();
	if (shutdown_sound != null) shutdown_sound.stop();
	
    }

    @Override
    public void mute(boolean m) {
	for (SoundBite ns : notch_sounds.values()) {
	    ns.mute(m);
	}
	for (SoundBite nus : notchup_sounds) {
	    nus.mute(m);
	}
	for (NotchTransition nt : transition_sounds) {
	    nt.mute(m);
	}
	if (notchup_sound != null) notchup_sound.mute(m);
	if (start_sound != null) start_sound.mute(m);
	if (shutdown_sound != null) shutdown_sound.mute(m);
	
    }

    @Override
    public void setVolume(float v) {
	for (SoundBite ns : notch_sounds.values()) {
	    ns.setVolume(v);
	}
	for (SoundBite nus : notchup_sounds) {
	    nus.setVolume(v);
	}
	for (NotchTransition nt : transition_sounds) {
	    nt.setVolume(v);
	}
	if (notchup_sound != null) notchup_sound.setVolume(v);
	if (start_sound != null) start_sound.setVolume(v);
	if (shutdown_sound != null) shutdown_sound.setVolume(v);
	
    }

    @Override
    public void setPosition(PhysicalLocation p) {
	for (SoundBite ns : notch_sounds.values()) {
	    ns.setPosition(p);
	}
	for (SoundBite nus : notchup_sounds) {
	    nus.setPosition(p);
	}
	for (NotchTransition nt : transition_sounds) {
	    nt.setPosition(p);
	}
	if (notchup_sound != null) notchup_sound.setPosition(p);
	if (start_sound != null) start_sound.setPosition(p);
	if (shutdown_sound != null) shutdown_sound.setPosition(p);
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
	//int num_notches;
	String fn;
	SoundBite sb;

	// Handle the common stuff.
	super.setXml(e, vf);
	
	log.debug("Diesel EngineSound: " + e.getAttribute("name").getValue());
	// Element "notches" is deprecated.  Just ignore it for now.
	/*
	String n = e.getChild("notches").getValue();
	if (n != null) {
	    num_notches = Integer.parseInt(n);
	    //log.debug("Number of notches: " + num_notches);
	}
	*/
	notch_sounds = new HashMap<Integer, SoundBite>();
	transition_sounds = new ArrayList<NotchTransition>();

	// Get the notch sounds
	Iterator<Element> itr =  (e.getChildren("notch-sound")).iterator();
	int i = 0; 
	while(itr.hasNext()) {
	    el = itr.next();
	    fn = el.getChildText("file");
	    int nn = Integer.parseInt(el.getChildText("notch"));
	    //log.debug("Notch: " + nn + " File: " + fn);
	    sb = new SoundBite(vf, fn, "Engine_n" + i, "Engine_" + i);
	    sb.setLooped(true);
	    sb.setFadeTimes(100, 100);
	    sb.setGain(setXMLGain(el));
	    // Store in the list.
	    notch_sounds.put(nn, sb);
	    i++;
	}

	// Get the notch transitions
	itr = (e.getChildren("notch-transition")).iterator();
	i = 0;
	NotchTransition nt;
	while(itr.hasNext()) {
	    el = itr.next();
	    fn = el.getChildText("file");
	    nt = new NotchTransition(vf, fn, "Engine_nt" + i, "Engine_nt" + i);
	    nt.setPrevNotch(Integer.parseInt(el.getChildText("prev-notch")));
	    nt.setNextNotch(Integer.parseInt(el.getChildText("next-notch")));
	    //log.debug("Transition: prev=" + nt.getPrevNotch() + " next=" + nt.getNextNotch() + " File: " + fn);
	    nt.setLength();
	    nt.setLooped(false);
	    nt.setFadeTimes(10, 100);
	    // Handle gain
	    nt.setGain(setXMLGain(el));
	    transition_sounds.add(nt);
	    i++;
	}

	// Get the start and stop sounds
	el = e.getChild("start-sound");
	if (el != null) {
	    fn = el.getChild("file").getValue();
	    //log.debug("Start sound: " + fn);
	    start_sound = new SoundBite(vf, fn, "Engine_start", 
					"Engine_Start");
	    // Handle gain
	    start_sound.setGain(setXMLGain(el));
	    start_sound.setLooped(false);
	}
	el = e.getChild("shutdown-sound");
	if (el != null) {
	    fn = el.getChild("file").getValue();
	    //log.debug("Shutdown sound: " + fn);
	    shutdown_sound = new SoundBite(vf, fn, "Engine_shutdown",
					   "Engine_Shutdown");
	    shutdown_sound.setLooped(false);
	    // Handle gain
	    shutdown_sound.setGain(setXMLGain(el));
	}
	itr = e.getChildren("notch-up-sound").iterator();
	i = 0;
	notchup_sounds = new ArrayList<SoundBite>();
	while(itr.hasNext()) {
	    el = itr.next();
	    // handle notch-up sounds... could be many.
	    // Note, there's not enough info here to truly
	    // do the notchup.  This is a placeholder.
	    fn = el.getChild("file").getValue();
	    //log.debug("Notchup sound: " + fn);
	    SoundBite ns = new SoundBite(vf, fn, "Engine_notch_up_n" + i,
					 "Engine_NotchUp_" + i);
	    ns.setLooped(false);
	    // Handle gain
	    ns.setGain(setXMLGain(el));
	    notchup_sounds.add(ns);
	    i++;
	}
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EngineSound.class.getName());

}