package jmri.jmrit.vsdecoder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;

/**
 * Steam Sound initial version.
 *
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
 * @author Klaus Killinger Copyright (C) 2018-2021, 2025
 */
class SteamSound extends EngineSound {

    // Inner class for handling steam RPM sounds
    class RPMSound {

        private SoundBite sound;
        private int min_rpm;
        private int max_rpm;
        private boolean use_chuff;
        private javax.swing.Timer t;

        private RPMSound(SoundBite sb, int min_r, int max_r, boolean chuff) {
            sound = sb;
            min_rpm = min_r;
            max_rpm = max_r;
            use_chuff = chuff;
            if (use_chuff) {
                sound.setLooped(false);
                t = newTimer(1, true, new ActionListener() {
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

        private void setRPM(int rpm) {
            if (use_chuff) {
                t.setDelay(calcChuffInterval(rpm));
            }
        }

        private void startChuff() {
            if (!t.isRunning()) {
                t.start();
            }
        }

        private void stopChuff() {
            if (t.isRunning()) {
                t.stop();
            }
        }
    }

    // Engine Sounds
    private ArrayList<RPMSound> rpm_sounds;
    int top_speed;
    private int driver_diameter;
    private int num_cylinders;
    private RPMSound current_rpm_sound;

    public SteamSound(String name) {
        super(name);
    }

    @Override
    public void startEngine() {
        log.debug("Starting Engine");
        current_rpm_sound = getRPMSound(0);
        current_rpm_sound.sound.loop();
    }

    @Override
    public void stopEngine() {
        getCurrSound().fadeOut();
        if (current_rpm_sound.use_chuff) {
            current_rpm_sound.stopChuff();
        }
    }

    private SoundBite getCurrSound() {
        return current_rpm_sound.sound;
    }

    private RPMSound getRPMSound(int rpm) {
        int i = 1;
        for (RPMSound rps : rpm_sounds) {
            if ((rps.min_rpm <= rpm) && (rps.max_rpm >= rpm)) {
                if (engine_pane != null) {
                    engine_pane.setThrottle(i);
                }
                return rps;
            } else if (rpm > rpm_sounds.get(rpm_sounds.size() - 1).max_rpm) {
                return rpm_sounds.get(rpm_sounds.size() - 1);
            }
            i++;
        }
        // Didn't find anything
        return null;
    }

    private int calcRPM(float t) {
        // Speed = % of top_speed (mph)
        // RPM = speed * ((inches/mile) / (minutes/hour)) / (pi * driver_diameter)
        double rpm_f = speedCurve(t) * top_speed * 1056 / (Math.PI * driver_diameter);
        setActualSpeed((float) speedCurve(t));
        log.debug("RPM Calculated: {}, rounded: {}, actual speed: {}, speedCurve(t): {}", rpm_f, (int) Math.round(rpm_f), getActualSpeed(), speedCurve(t));
        return (int) Math.round(rpm_f);
    }

    private int calcChuffInterval(int rpm) {
        return 30000 / num_cylinders / rpm;
    }

    @Override
    public void changeThrottle(float t) {
        // Don't do anything, if engine is not started or auto-start is active.
        if (isEngineStarted()) {
            if (t < 0.0f) {
                // DO something to shut down
                //t = 0.0f;
                setActualSpeed(0.0f);
                getCurrSound().fadeOut();
                if (current_rpm_sound.use_chuff) {
                    current_rpm_sound.stopChuff();
                }
                current_rpm_sound = getRPMSound(0);
                current_rpm_sound.sound.loop();
            } else {
                RPMSound rps;
                rps = getRPMSound(calcRPM(t)); // Get the rpm sound.
                if (rps != null) {
                    // Yes, I'm checking to see if rps and current_rpm_sound are the *same object*
                    if (rps != current_rpm_sound) {
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
                    } else {
                        // *same object* - but possibly different rpm (speed) which affects the chuff interval
                        if (rps.use_chuff) {
                            rps.setRPM(calcRPM(t)); // Chuff interval need to be recalculated
                        }
                    }
                } else {
                    log.warn("No adequate sound file found for {}, RPM = {}", this, calcRPM(t));
                }
                log.debug("RPS: {}, RPM: {}, current_RPM: {}", rps, calcRPM(t), current_rpm_sound);
            }
        }
    }

    @Override
    public void shutdown() {
        for (RPMSound rps : rpm_sounds) {
            if (rps.use_chuff) rps.stopChuff();
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
            if (this.getTunnel()) {
                rps.sound.attachSourcesToEffects();
            } else {
                rps.sound.detachSourcesToEffects();
            }
        }
    }

    @Override
    public Element getXml() {
        // OUT OF DATE
        return super.getXml();
    }

    @Override
    public void setXml(Element e, VSDFile vf) {
        Element el;
        //int num_rpms;
        String fn, n;
        SoundBite sb;
        boolean buffer_ok = true;

        super.setXml(e, vf);

        log.debug("Steam EngineSound: {}, name: {}", e.getAttribute("name").getValue(), name);

        // Required values
        top_speed = Integer.parseInt(e.getChildText("top-speed"));
        log.debug("top speed forward: {} MPH", top_speed);

        n = e.getChildText("driver-diameter");
        if (n != null) {
            driver_diameter = Integer.parseInt(n);
            log.debug("Driver diameter: {} inches", driver_diameter);
        }
        n = e.getChildText("cylinders");
        if (n != null) {
            num_cylinders = Integer.parseInt(n);
            log.debug("Num Cylinders: {}", num_cylinders);
        }

        // Optional value
        // Allows to adjust speed via speedCurve(T).
        n = e.getChildText("exponent");
        if (n != null) {
            exponent = Float.parseFloat(n);
        } else {
            exponent = 2.0f; // default
        }
        log.debug("exponent: {}", exponent);

        is_auto_start = setXMLAutoStart(e);
        log.debug("config auto-start: {}", is_auto_start);

        rpm_sounds = new ArrayList<>();

        // Get the RPM steps
        Iterator<Element> itr = (e.getChildren("rpm-step")).iterator();
        int i = 0;
        while (itr.hasNext()) {
            el = itr.next();
            fn = el.getChildText("file");
            int min_r = Integer.parseInt(el.getChildText("min-rpm"));
            int max_r = Integer.parseInt(el.getChildText("max-rpm"));
            log.debug("file #: {}, file name: {}", i, fn);
            sb = new SoundBite(vf, fn, name + "_Steam_n" + i, name + "_Steam_" + i);
            if (sb.isInitialized()) {
                sb.setLooped(true);
                sb.setFadeTimes(100, 100);
                sb.setReferenceDistance(setXMLReferenceDistance(el)); // Handle reference distance
                sb.setGain(setXMLGain(el));
                // Store in the list.
                boolean chuff = false;
                Element c;
                if ((c = el.getChild("use-chuff-gen")) != null) {
                    log.debug("Use Chuff Generator: {}", c);
                    chuff = true;
                }

                rpm_sounds.add(new RPMSound(sb, min_r, max_r, chuff));
            } else {
                buffer_ok = false;
            }
            i++;
        }

        if (buffer_ok) {
            setBuffersFreeState(true);
            // Check auto-start setting
            autoStartCheck();
        } else {
            setBuffersFreeState(false);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SteamSound.class);

}
