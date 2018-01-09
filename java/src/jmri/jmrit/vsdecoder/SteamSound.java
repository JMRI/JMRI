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
 * @author   Mark Underwood Copyright (C) 2011
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Usage:
// SteamSound() : constructor
// play() : plays short horn pop
// loop() : starts extended sustain horn
// stop() : ends extended sustain horn (plays end sound)
class SteamSound extends EngineSound {

    // Inner class for handling steam RPM sounds
    class RPMSound {

        public SoundBite sound;
        public int min_rpm;
        public int max_rpm;
        public boolean use_chuff;
        private javax.swing.Timer t;

        public RPMSound(SoundBite sb, int min_r, int max_r, boolean chuff) {
            sound = sb;
            min_rpm = min_r;
            max_rpm = max_r;
            use_chuff = chuff;
            if (use_chuff) {
                sound.setLooped(false);
                t = newTimer(1000, true, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        doChuff();
                    }
                });
            }
        }

        private void doChuff() {
            sound.play();
        }

        public void setRPM(int rpm) {
            if (use_chuff) {
                t.setDelay(calcChuffInterval(rpm));
            }
        }

        public void startChuff() {
            if (!t.isRunning()) {
                t.start();
            }
        }

        public void stopChuff() {
            t.stop();
        }
    }

    // Engine Sounds
    ArrayList<RPMSound> rpm_sounds;
    SoundBite idle_sound;
    int top_speed;
    int driver_diameter;
    int num_cylinders;
    RPMSound current_rpm_sound;
    int current_chuff_time;

    public SteamSound(String name) {
        super(name);
    }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    @Override
    public void play() {
        log.debug("EngineSound Play");
        if (engine_started || auto_start_engine) {
            current_rpm_sound.sound.play();
            is_playing = true;
        }
    }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    @Override
    public void loop() {
        if ((engine_started || auto_start_engine)) {
            current_rpm_sound.sound.play();
            is_playing = true;
        }
    }

    @Override
    public void stop() {
        current_rpm_sound.sound.stop();
        is_playing = false;
    }

    @Override
    public void startEngine() {
        log.debug("Starting Engine");
        current_rpm_sound = getRPMSound(0);
        current_rpm_sound.sound.loop();
        engine_started = true;
    }

    @Override
    public void stopEngine() {
        current_rpm_sound.sound.fadeOut();
        engine_started = false;
    }

    private RPMSound getRPMSound(int rpm) {
        for (RPMSound rps : rpm_sounds) {
            if ((rps.min_rpm <= rpm) && (rps.max_rpm >= rpm)) {
                return (rps);
            }
        }
        // Didn't find anything
        return (null);
    }

    private int calcRPM(float t) {
        // Speed = % of top_speed (mph)
        // RPM = speed * ((inches/mile) / (minutes/hour)) / (pi * driver_diameter)
        double rpm_f = speedCurve(t) * top_speed * 1056 / (Math.PI * driver_diameter);
        log.debug("RPM Calculated: " + rpm_f + " (int) " + (int) Math.round(rpm_f));
        return ((int) Math.round(rpm_f));
    }

    private double speedCurve(float t) {
        return (Math.pow(t, 2.0) / 1.0);
    }

    private int calcChuffInterval(int rpm) {
        return (1000 * num_cylinders / rpm);
    }

    @Override
    public void changeThrottle(float t) {
        RPMSound rps;
        // Yes, I'm checking to see if rps and current_rpm_sound are the *same object*
        if (((rps = getRPMSound(calcRPM(t))) != null) && (rps != current_rpm_sound)) {
            // Stop the current sound
            if ((current_rpm_sound != null) && (current_rpm_sound.sound != null)) {
                current_rpm_sound.sound.fadeOut();
                if (current_rpm_sound.use_chuff) {
                    current_rpm_sound.stopChuff();
                }
            }
            // Start the new sound.
            current_rpm_sound = rps;
            if (rps.use_chuff) {
                rps.setRPM(calcRPM(t));
                rps.startChuff();
            }
            rps.sound.fadeIn();
        }
        log.debug("RPS = " + rps + " RPM = " + calcRPM(t) + " current_RPM = " + current_rpm_sound);
    }

    @Override
    public void shutdown() {
        for (RPMSound rps : rpm_sounds) {
            rps.sound.stop();
        }
    }

    @Override
    public void mute(boolean m) {
        for (RPMSound rps : rpm_sounds) {
            rps.sound.mute(m);
        }
    }

    @Override
    public void setVolume(float v) {
        for (RPMSound rps : rpm_sounds) {
            rps.sound.setVolume(v);
        }
    }

    @Override
    public void setPosition(PhysicalLocation p) {
        for (RPMSound rps : rpm_sounds) {
            rps.sound.setPosition(p);
        }
    }

    @Override
    public Element getXml() {
        // OUT OF DATE
        return (super.getXml());
    }

    @Override
    public void setXml(Element e, VSDFile vf) {
        Element el;
        //int num_rpms;
        String fn;
        SoundBite sb;

        super.setXml(e, vf);

        log.debug("Steam EngineSound: {}, name: {}", e.getAttribute("name").getValue(), name);
        String n = e.getChild("top-speed").getValue();
        if (n != null) {
            top_speed = Integer.parseInt(n);
            //log.debug("Top speed: " + top_speed + " MPH");
        }
        n = e.getChildText("driver-diameter");
        if (n != null) {
            driver_diameter = Integer.parseInt(n);
            //log.debug("Driver diameter: " + driver_diameter + " inches");
        }
        n = e.getChildText("cylinders");
        if (n != null) {
            num_cylinders = Integer.parseInt(n);
            //log.debug("Num Cylinders: " + num_cylinders);
        }
        // For now, num_rpms is not used.  
        /*
         n = e.getChild("rpm-steps").getValue();
         if (n != null) {
         num_rpms = Integer.parseInt(n);
         //log.debug("Number of rpm steps: " + num_rpms);
         }
         */

        rpm_sounds = new ArrayList<RPMSound>();

        // Get the RPM steps
        Iterator<Element> itr = (e.getChildren("rpm-step")).iterator();
        int i = 0;
        while (itr.hasNext()) {
            el = itr.next();
            fn = el.getChildText("file");
            int min_r = Integer.parseInt(el.getChildText("min-rpm"));
            int max_r = Integer.parseInt(el.getChildText("max-rpm"));
            //log.debug("Notch: " + nn + " File: " + fn);
            sb = new SoundBite(vf, fn, name + "Steam_n" + i, name + "Steam_" + i);
            sb.setLooped(true);
            sb.setFadeTimes(100, 100);
            sb.setGain(setXMLGain(el));
            // Store in the list.
            boolean chuff = false;
            Element c;
            if ((c = el.getChild("use-chuff-gen")) != null) {
                log.debug("Use Chuff Generator " + c.toString());
                chuff = true;
            }

            rpm_sounds.add(new RPMSound(sb, min_r, max_r, chuff));
            i++;
        }

        /*
         // Get the start and stop sounds
         el = e.getChild("start-sound");
         if (el != null) {
         fn = el.getChild("file").getValue();
         log.debug("Start sound: " + fn);
         start_sound = new SoundBite(vf, fn, "Engine_start", 
         "Engine_Start");
         // Handle gain
         start_sound.setGain(setXMLGain(el));
         start_sound.setLooped(false);
         }
         */
    }

    private static final Logger log = LoggerFactory.getLogger(SteamSound.class);

}
