package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author   Mark Underwood Copyright (C) 2011
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Usage:
// EngineSound() : constructor
// play() : plays short horn pop
// loop() : starts extended sustain horn
// stop() : ends extended sustain horn (plays end sound)
class Diesel2Sound extends EngineSound {

    // Engine Sounds
    HashMap<Integer, NotchSound> notch_sounds;
    SoundBite start_sound;
    SoundBite shutdown_sound;
    SoundBite notch_transition; // used for changing notches
    Float throttle_setting;
    EnginePane engine_pane;

    int current_notch = 1;
    boolean changing_speed = false;

    public Diesel2Sound(String name) {
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
    public void handleSpeedChange(Float s, EnginePane e) {
        throttle_setting = s;
        if (!changing_speed) {
            changeSpeed(s, e);
        }
    }

    // Responds to "CHANGE" trigger
    @Override
    public void changeThrottle(float s) {
        NotchSound cn = notch_sounds.get(current_notch);
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
            changing_speed = false;
            log.debug("No Change");
            cn.fadeIn();
            //cn.play();
            return;
        } else if (s > cn.getAccelLimit()) {
            // Too fast. Need to go to next notch up.
            changing_speed = true;
            notch_transition = cn.getAccelSound();
            current_notch = cn.getNextNotch();
            log.debug("Change up. notch=" + current_notch);
        } else if (s < cn.getDecelLimit()) {
            // Too slow.  Need to go to next notch down.
            changing_speed = true;
            notch_transition = cn.getDecelSound();
            current_notch = cn.getPrevNotch();
            log.debug("Change down. notch=" + current_notch);
        }
        // Now, regardless of whether we're going up or down, set the timer,
        // fade the current sound, and move on.
        if (notch_transition == null) {
            // No transition sound to play.  Skip the timer bit.
            // Recurse directly to try the next notch.
            //cn.fadeOut();
            cn.play();
            changeThrottle(s);
            log.debug("No transition sound defined.");
            return;
        }
        t = newTimer(notch_transition.getLengthAsInt() - this.getFadeInTime(), false,
                new ActionListener() {
            @Override
                    public void actionPerformed(ActionEvent e) {
                        handleNotchTimerPop(e);
                    }
                });
        t.start();
        //cn.fadeOut();
        cn.stop();
        //notch_transition.fadeIn();
        notch_transition.play();
        // Regardless, set the throttle to the (possibly new) current notch.
    }

    protected void changeSpeed(Float s, EnginePane e) {
        engine_pane = e; // this should probably be cleaned up.  It's here for the recursion.
        changeThrottle(s);
    }

    protected void handleNotchTimerPop(ActionEvent e) {
        // semi-Recursively call the speed change handler until it quits setting up timers.
        notch_transition.fadeOut();
        changeSpeed(throttle_setting, engine_pane);
    }

    @Override
    public void startEngine() {
        start_sound.play();
        current_notch = calcEngineNotch(0.0f);
        t = newTimer(start_sound.getLengthAsInt() - start_sound.getFadeOutTime(), false, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startToIdleAction(e);
            }
        });
        t.setInitialDelay(start_sound.getLengthAsInt() - start_sound.getFadeOutTime());
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
        for (NotchSound ns : notch_sounds.values()) {
            ns.stop();
        }
        if (start_sound != null) {
            start_sound.stop();
        }
        if (shutdown_sound != null) {
            shutdown_sound.stop();
        }
    }

    @Override
    public void mute(boolean m) {
        for (NotchSound ns : notch_sounds.values()) {
            ns.mute(m);
        }
        if (start_sound != null) {
            start_sound.mute(m);
        }
        if (shutdown_sound != null) {
            shutdown_sound.mute(m);
        }
    }

    @Override
    public void setVolume(float v) {
        for (NotchSound ns : notch_sounds.values()) {
            ns.setVolume(v);
        }
        if (start_sound != null) {
            start_sound.setVolume(v);
        }
        if (shutdown_sound != null) {
            shutdown_sound.setVolume(v);
        }
    }

    @Override
    public void setPosition(PhysicalLocation p) {
        for (NotchSound ns : notch_sounds.values()) {
            ns.setPosition(p);
        }
        if (start_sound != null) {
            start_sound.setPosition(p);
        }
        if (shutdown_sound != null) {
            shutdown_sound.setPosition(p);
        }
    }

    @Override
    public Element getXml() {
        Element me = new Element("sound");
        me.setAttribute("name", this.getName());
        me.setAttribute("type", "engine");
        // Do something, eventually...
        return (me);
    }

    @Override
    public void setXml(Element e, VSDFile vf) {
        Element el;
        String fn;
        NotchSound sb;

        // Handle the common stuff.
        super.setXml(e, vf);

        log.debug("Diesel EngineSound: " + e.getAttribute("name").getValue());
        notch_sounds = new HashMap<Integer, NotchSound>();

        // Get the notch sounds
        Iterator<Element> itr = (e.getChildren("notch-sound")).iterator();
        int i = 0;
        while (itr.hasNext()) {
            el = itr.next();
            fn = el.getChildText("file");
            int nn = Integer.parseInt(el.getChildText("notch"));
            //log.debug("Notch: " + nn + " File: " + fn);
            sb = new NotchSound(vf, fn, "Engine_n" + i, "Engine_" + i);
            sb.setLooped(true);
            sb.setFadeTimes(this.getFadeInTime(), this.getFadeOutTime());
            sb.setGain(setXMLGain(el));
            sb.setNextNotch(el.getChildText("next-notch"));
            sb.setPrevNotch(el.getChildText("prev-notch"));
            sb.setAccelLimit(el.getChildText("accel-limit"));
            sb.setDecelLimit(el.getChildText("decel-limit"));
            if (el.getChildText("accel-file") != null) {
                sb.setAccelSound(new SoundBite(vf, el.getChildText("accel-file"), "Engine_na" + i, "Engine_na" + i));
            } else {
                sb.setAccelSound(null);
            }
            if (el.getChildText("decel-file") != null) {
                sb.setDecelSound(new SoundBite(vf, el.getChildText("decel-file"), "Engine_nd" + i, "Engine_nd" + i));
            } else {
                sb.setDecelSound(null);
            }
            // Store in the list.
            notch_sounds.put(nn, sb);
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
            start_sound.setFadeTimes(this.getFadeInTime(), this.getFadeOutTime());
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
            shutdown_sound.setFadeTimes(this.getFadeInTime(), this.getFadeOutTime());
        }

    }

    private static final Logger log = LoggerFactory.getLogger(EngineSound.class);

}
