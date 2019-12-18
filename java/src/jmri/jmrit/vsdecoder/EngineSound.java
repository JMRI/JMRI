package jmri.jmrit.vsdecoder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
 * @author Mark Underwood Copyright (C) 2011
 * @author Klaus Killinger Copyright (C) 2018
 */
// Usage:
// EngineSound() : constructor
// play() : plays short horn pop
// loop() : starts extended sustain horn
// stop() : ends extended sustain horn (plays end sound)
class EngineSound extends VSDSound {

    // Engine Sounds
    boolean initialized = false;
    boolean engine_started = false;
    boolean auto_start_engine = false;
    boolean is_auto_start; // Can be used in config.xml
    boolean is_first = false;

    int fade_length = 100;
    int fade_in_time = 100;
    int fade_out_time = 100;

    EnginePane engine_pane;

    public EngineSound(String name) {
        super(name);
        is_playing = false;
        engine_started = false;
        initialized = init();
    }

    public boolean init() {
        auto_start_engine = VSDecoderManager.instance().getVSDecoderPreferences().isAutoStartingEngine();
        return true;
    }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    @Override
    public void play() {
        log.debug("EngineSound Play");
        //if (engine_started || auto_start_engine) {
        //    is_playing = true;
        //}
    }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    @Override
    public void loop() {
        log.debug("EngineSound Loop");
        //if (engine_started || auto_start_engine) {
        //    is_playing = true;
        //}
    }

    @Override
    public void stop() {
        log.info("Emergency Stop called!");
        is_playing = false;
    }

    @Override
    public void fadeIn() {
        this.play();
    }

    @Override
    public void fadeOut() {
        this.stop();
    }

    public int getFadeInTime() {
        return this.fade_in_time;
    }

    public int getFadeOutTime() {
        return this.fade_out_time;
    }

    protected void setFadeInTime(int t) {
        this.fade_in_time = t;
    }

    protected void setFadeInTime(String s) {
        if (s == null) {
            log.debug("setFadeInTime null string");
            return;
        }
        try {
            this.setFadeInTime(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            log.debug("setFadeInTime Failed to parse int from: {}", s);
        }
    }

    protected void setFadeOutTime(int t) {
        this.fade_out_time = t;
    }

    protected void setFadeOutTime(String s) {
        if (s == null) {
            log.debug("setFadeInTime null string");
            return;
        }

        try {
            this.setFadeOutTime(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            log.debug("setFadeOutTime Failed to parse int from: {}", s);
        }
    }

    static final public int calcEngineNotch(final float throttle) {
        // This will convert to a value 0-8.
        int notch = ((int) Math.rint(throttle * 8)) + 1;
        if (notch < 1) {
            notch = 1;
        }
        log.debug("Throttle: {}, Notch: {}", throttle, notch);
        return notch;
    }

    static final public int calcEngineNotch(final double throttle) {
        // This will convert from a % to a value 0-8.
        int notch = ((int) Math.rint(throttle * 8)) + 1;
        if (notch < 1) {
            notch = 1;
        }
        //log.warn("Throttle: {}, Notch: {}", throttle, notch);
        return notch;
    }

    // This is the default behavior.  Subclasses can do fancier things
    // if they want.
    public void handleSpeedChange(Float s, EnginePane e) {
        engine_pane = e;
        engine_pane.setSpeed(s);
    }

    void setFirstSpeed(boolean f) {
        is_first = f;
    }

    boolean getFirstSpeed() {
        return is_first;
    }

    public void startEngine() {
        log.debug("Starting Engine");
    }

    public void stopEngine() {
    }

    public boolean isEngineStarted() {
        return engine_started;
    }

    public void setEngineStarted(boolean es) {
        engine_started = es;
    }

    public void functionKey(String e, boolean v, String n) {
    }

    public void changeLocoDirection(int d) {
    }

    @Override
    public void shutdown() {
        // do nothing.
    }

    @Override
    public void mute(boolean m) {
        // do nothing.
    }

    @Override
    public void setVolume(float v) {
        // do nothing.
    }

    // Note: We have to invoke engine_pane later because everything's not really setup yet
    // Need some more time to get the speed from the assigned throttle
    void autoStartCheck() {
        if (auto_start_engine || is_auto_start) {
            SwingUtilities.invokeLater(() -> {
                t = newTimer(40, false, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (engine_pane != null && getFirstSpeed()) {
                            engine_pane.startButtonClick();
                        } else {
                            log.warn("engine pane or speed not found");
                        }
                    }
                });
                t.start();
            });
        }
    }

    protected boolean setXMLAutoStart(Element e) {
        String a = e.getChildText("auto-start");
        if ((a != null) && (a.equals("yes"))) {
            return true;
        } else {
            return false;
        }
    }

    protected float setXMLGain(Element e) {
        String g = e.getChildText("gain");
        log.debug("  gain: {}", g);
        if ((g != null) && !(g.isEmpty())) {
            return Float.parseFloat(g);
        } else {
            return default_gain;
        }
    }

    protected float setXMLReferenceDistance(Element e) {
        String a = e.getChildText("reference-distance");
        if ((a != null) && (!a.isEmpty())) {
            return Float.parseFloat(a);
        } else {
            return default_reference_distance;
        }
    }

    @Override
    public Element getXml() {
        Element me = new Element("sound");
        me.setAttribute("name", this.getName());
        me.setAttribute("type", "engine");
        // Do something, eventually...
        return me;
    }

    public void setXml(Element e, VSDFile vf) {
        // Do only the stuff common...
        if (this.getName() == null) {
            this.setName(e.getAttributeValue("name"));
        }
        //log.debug("EngineSound: " + this.getName());
        this.setFadeInTime(e.getChildText("fade-in-time"));
        this.setFadeOutTime(e.getChildText("fade-out-time"));
        log.debug("Name: {}, Fade-In-Time: {}, Fade-Out-Time: {}", this.getName(),
            this.getFadeInTime(), this.getFadeOutTime());
    }

    private static final Logger log = LoggerFactory.getLogger(EngineSound.class);

}
