package jmri.jmrit.vsdecoder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.nio.ByteBuffer;
import jmri.Audio;
import jmri.AudioException;
import jmri.AudioManager;
import jmri.jmrit.audio.AudioBuffer;
import jmri.util.PhysicalLocation;
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
 * @author Klaus Killinger Copyright (C) 2017-2019
 */
class Steam1Sound extends EngineSound {

    // Engine Sounds
    HashMap<Integer, S1Notch> notch_sounds;
    String _soundName;

    // Trigger Sounds
    HashMap<String, SoundBite> trigger_sounds;

    int top_speed;
    int top_speed_reverse;
    private float driver_diameter_float;
    private int num_cylinders;
    private float exponent;
    private int accel_rate;
    private int decel_rate;
    private int brake_time;
    private int decel_trigger_rpms;
    private int wait_factor;
    private SoundBite idle_sound;
    private SoundBite boiling_sound;
    private SoundBite brake_sound;
    private SoundBite pre_arrival_sound;
    private float engine_rd;
    private float engine_gain;

    S1LoopThread _loopThread = null;

    private javax.swing.Timer rpmTimer;
    private int accdectime;

    // Constructor
    public Steam1Sound(String name) {
        super(name);
        log.debug("New Steam1Sound name(param): {}, name(val): {}", name, this.getName());
    }

    private void startThread() {
        _loopThread = new S1LoopThread(this, _soundName, top_speed, top_speed_reverse,
                driver_diameter_float, num_cylinders, decel_trigger_rpms, true);
        log.debug("Loop Thread Started.  Sound name: {}", _soundName);
    }

    // Responds to "CHANGE" trigger (float)
    @Override
    public void changeThrottle(float s) {
        // This is all we have to do.  The loop thread will handle everything else
        if (_loopThread != null) {
            _loopThread.setThrottle(s);
        }
    }

    @Override
    public void changeLocoDirection(int dirfn) {
        log.debug("loco IsForward is {}", dirfn);
        if (_loopThread != null) {
            _loopThread.getLocoDirection(dirfn);
        }
    }

    @Override
    public void functionKey(String event, boolean value, String name) {
        log.debug("throttle function key {} pressed for {}: {}", event, name, value);
        if (_loopThread != null) {
            _loopThread.setFunction(event, value, name);
        }
    }

    @Override
    double speedCurve(float t) {
        return Math.pow(t, exponent);
    }

    private S1Notch getNotch(int n) {
        return notch_sounds.get(n);
    }

    private void initAccDecTimer() {
        rpmTimer = newTimer(1, true, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (_loopThread != null) {
                    rpmTimer.setDelay(accdectime); // Update delay time
                    _loopThread.updateRpm();
                }
            }
        });
        log.debug("timer {} initialized, delay: {}", rpmTimer, accdectime);
    }

    private void startAccDecTimer() {
        if (!rpmTimer.isRunning()) {
            rpmTimer.start();
            log.debug("timer {} started, delay: {}", rpmTimer, accdectime);
        }
    }

    private void stopAccDecTimer() {
        if (rpmTimer.isRunning()) {
            rpmTimer.stop();
            log.debug("timer {} stopped, delay: {}", rpmTimer, accdectime);
        }
    }

    @Override
    public void startEngine() {
        log.debug("startEngine. ID: {}", this.getName());
        if (_loopThread != null) {
            _loopThread.startEngine();
        }
    }

    @Override
    public void stopEngine() {
        log.debug("stopEngine. ID = {}", this.getName());
        if (_loopThread != null) {
            _loopThread.stopEngine();
        }
    }

    @Override
    // Called when deleting a VSDecoder or closing the VSDecoder Manager
    // There is one thread for every VSDecoder
    public void shutdown() {
        for (VSDSound vs : trigger_sounds.values()) {
            log.debug(" Stopping trigger sound: {}", vs.getName());
            vs.stop(); // SoundBite: Stop playing
        }
        if (rpmTimer != null) {
            stopAccDecTimer();
        }

        // Stop the loop thread, in case it's running
        if (_loopThread != null) {
            _loopThread.setRunning(false);
        }
    }

    @Override
    public void mute(boolean m) {
        if (_loopThread != null) {
            _loopThread.mute(m);
        }
    }

    @Override
    public void setVolume(float v) {
        if (_loopThread != null) {
            _loopThread.setVolume(v);
        }
    }

    @Override
    public void setPosition(PhysicalLocation p) {
        if (_loopThread != null) {
            _loopThread.setPosition(p);
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

    @Override
    public void setXml(Element e, VSDFile vf) {
        boolean buffer_ok = true;
        Element el;
        String fn, n;
        S1Notch sb;

        // Handle the common stuff
        super.setXml(e, vf);

        _soundName = this.getName() + ":LoopSound";
        log.debug("Steam1: name: {}, soundName: {}", this.getName(), _soundName);

        top_speed = Integer.parseInt(e.getChildText("top-speed")); // Required value
        log.debug("top speed forward: {} MPH", top_speed);

        // Steam locos can have different top speed reverse
        n = e.getChildText("top-speed-reverse"); // Optional value
        if ((n != null) && !(n.isEmpty())) {
            top_speed_reverse = Integer.parseInt(n);
        } else {
            top_speed_reverse = top_speed;  // Default for top_speed_reverse
        }
        log.debug("top speed reverse: {} MPH", top_speed_reverse);

        driver_diameter_float = Float.parseFloat(e.getChildText("driver-diameter-float"));
        log.debug("driver diameter: {} inches", driver_diameter_float);
        num_cylinders = Integer.parseInt(e.getChildText("cylinders"));
        log.debug("Number of cylinders defined: {}", num_cylinders);

        // Allows to adjust speed
        n = e.getChildText("exponent"); // Optional value
        if ((n != null) && !(n.isEmpty())) {
            exponent = Float.parseFloat(n);
        } else {
            exponent = 1.0f; // Default
        }
        log.debug("exponent: {}", exponent);

        // Acceleration and deceleration rate
        n = e.getChildText("accel-rate"); // Optional value
        if ((n != null) && !(n.isEmpty())) {
            accel_rate = Integer.parseInt(n);
        } else {
            accel_rate = 35; // Default
        }
        log.debug("accel rate: {}", accel_rate);

        n = e.getChildText("decel-rate"); // Optional value
        if ((n != null) && !(n.isEmpty())) {
            decel_rate = Integer.parseInt(n);
        } else {
            decel_rate = 18; // Default
        }
        log.debug("decel rate: {}", decel_rate);

        n = e.getChildText("brake-time"); // Optional value
        if ((n != null) && !(n.isEmpty())) {
            brake_time = Integer.parseInt(n);
        } else {
            brake_time = 0;  // Default
        }
        log.debug("brake time: {}", brake_time);

        // auto-start
        is_auto_start = setXMLAutoStart(e); // Optional value
        log.debug("config auto-start: {}", is_auto_start);

        // Allows to adjust OpenAL attenuation
        // Sounds with distance to listener position lower than reference-distance will not have attenuation
        engine_rd = setXMLReferenceDistance(e); // Optional value
        log.debug("engine-sound referenceDistance: {}", engine_rd);

        // Allows to adjust the engine gain
        n = e.getChildText("engine-gain"); // Optional value
        if ((n != null) && !(n.isEmpty())) {
            engine_gain = Float.parseFloat(n);
            // Make some restrictions, since engine_gain is used for calculations later
            if ((engine_gain < default_gain - 0.4f) || (engine_gain > default_gain + 0.2f)) {
                log.info("Invalid engine gain {} was set to default {}", engine_gain, default_gain);
                engine_gain = default_gain;
            }
        } else {
            engine_gain = default_gain;
        }
        log.debug("engine gain: {}", engine_gain);

        // Defines how many loops (50ms) to be subtracted from interval to calculate wait-time
        // The lower the wait-factor, the more effect it has
        // Better to take a higher value when running VSD on old/slow computers
        n = e.getChildText("wait-factor"); // Optional value
        if ((n != null) && !(n.isEmpty())) {
            wait_factor = Integer.parseInt(n);
            // Make some restrictions to protect the loop-player
            if (wait_factor < 5 || wait_factor > 40) {
                log.info("Invalid wait-factor {} was set to default 18", wait_factor);
                wait_factor = 18;
            }
        } else {
            wait_factor = 18; // Default
        }
        log.debug("number of loops to subtract from interval: {}", wait_factor);

        // Defines how many rpms in 0.5 seconds will trigger decel actions like braking
        n = e.getChildText("decel-trigger-rpms"); // Optional value
        if ((n != null) && !(n.isEmpty())) {
            decel_trigger_rpms = Integer.parseInt(n);
        } else {
            decel_trigger_rpms = 999; // Default (need a value)
        }
        log.debug("number of rpms to trigger decelerating actions: {}", decel_trigger_rpms);

        // Get the sounds.
        // Note: each sound must have equal attributes, e.g. 16-bit, 44100 Hz
        // Get the files and create a buffer and byteBuffer for each file
        // For each notch there must be <num_cylinders * 2> chuff files
        notch_sounds = new HashMap<>();
        int nn = 1; // notch number (visual)

        // Get the notch-sounds
        Iterator<Element> itr = (e.getChildren("s1notch-sound")).iterator();
        while (itr.hasNext()) {
            el = itr.next();
            sb = new S1Notch(nn);

            // Get the medium/standard chuff sounds
            List<Element> elist = el.getChildren("notch-file");
            for (Element fe : elist) {
                fn = fe.getText();
                log.debug("notch: {}, file: {}", nn, fn);
                sb.addChuffData(AudioUtil.getWavData(S1Notch.getWavStream(vf, fn)));
            }
            log.debug("Number of chuff medium/standard sounds for notch {} defined: {}", nn, elist.size());

            // Filler sound, coasting sound and helpers are bound to the first notch only
            // VSDFile validation makes sure that there is at least one notch
            if (nn == 1) {
                // Take the first notch-file to determine the audio formats (format, frequence and framesize)
                // All files of notch_sounds must have the same audio formats
                fn = el.getChildText("notch-file");
                int[] formats;
                formats = AudioUtil.getWavFormats(S1Notch.getWavStream(vf, fn));
                sb.setBufferFmt(formats[0]);
                sb.setBufferFreq(formats[1]);
                sb.setBufferFrameSize(formats[2]);

                log.debug("WAV audio formats - format: {}, frequence: {}, frame size: {}",
                        sb.getBufferFmt(), sb.getBufferFreq(), sb.getBufferFrameSize());

                // Create a filler Buffer for queueing and a ByteBuffer for length modification
                fn = el.getChildText("notchfiller-file");
                if (fn != null) {
                    log.debug("notch filler file: {}", fn);
                    sb.setNotchFillerData(AudioUtil.getWavData(S1Notch.getWavStream(vf, fn)));
                } else {
                    log.debug("no notchfiller available.");
                    sb.setNotchFillerData(null);
                }

                // Get the coasting sounds.
                List<Element> elistc = el.getChildren("coast-file");
                for (Element fe : elistc) {
                    fn = fe.getText();
                    log.debug("coasting file: {}", fn);
                    sb.addCoastData(AudioUtil.getWavData(S1Notch.getWavStream(vf, fn)));
                }
                log.debug("Number of coasting sounds for notch {} defined: {}", nn, elistc.size());

                // Create a filler Buffer for queueing and a ByteBuffer for length modification
                fn = el.getChildText("coastfiller-file");
                if (fn != null) {
                    log.debug("coasting filler file: {}", fn);
                    sb.setCoastFillerData(AudioUtil.getWavData(S1Notch.getWavStream(vf, fn)));
                } else {
                    log.debug("no coastfiller available.");
                    sb.setCoastFillerData(null);
                }

                // Add some helper Buffers. They are needed for creating
                // variable sound clips in length. Ten helper buffers should
                // serve well for that purpose.
                for (int j = 0; j < 10; j++) {
                    AudioBuffer bh = S1Notch.getBufferHelper(name + "_BUFFERHELPER_" + j, name + "_BUFFERHELPER_" + j);
                    if (bh != null) {
                        log.debug("buffer helper created: {}, name: {}", bh, bh.getSystemName());
                        sb.addHelper(bh);
                    } else {
                        buffer_ok = false;
                    }
                }
            }

            sb.setMinLimit(Integer.parseInt(el.getChildText("min-rpm")));
            sb.setMaxLimit(Integer.parseInt(el.getChildText("max-rpm")));

            // Store in the list
            notch_sounds.put(nn, sb);
            nn++;
        }
        log.debug("Number of notches defined: {}", notch_sounds.size());

        // Get the trigger sounds
        // Note: other than notch sounds, trigger sounds can have different attributes
        trigger_sounds = new HashMap<>();

        // Get the idle sound
        el = e.getChild("idle-sound");
        if (el != null) {
            fn = el.getChild("sound-file").getValue();
            log.debug("idle sound: {}", fn);
            idle_sound = new SoundBite(vf, fn, _soundName + "_IDLE", _soundName + "_Idle");
            idle_sound.setGain(setXMLGain(el)); // Handle gain
            log.debug("idle sound gain: {}", idle_sound.getGain());
            idle_sound.setLooped(true);
            idle_sound.setFadeTimes(500, 500);
            idle_sound.setReferenceDistance(setXMLReferenceDistance(el)); // Handle reference distance
            log.debug("idle-sound reference distance: {}", idle_sound.getReferenceDistance());
            trigger_sounds.put("idle", idle_sound);
            log.debug("trigger idle sound: {}", trigger_sounds.get("idle"));
        }

        // Get the boiling sound
        el = e.getChild("boiling-sound");
        if (el != null) {
            fn = el.getChild("sound-file").getValue();
            boiling_sound = new SoundBite(vf, fn, name + "_BOILING", name + "_Boiling");
            boiling_sound.setGain(setXMLGain(el)); // Handle gain
            boiling_sound.setLooped(true);
            boiling_sound.setFadeTimes(500, 500);
            boiling_sound.setReferenceDistance(setXMLReferenceDistance(el));
            trigger_sounds.put("boiling", boiling_sound);
        }

        // Get the brake sound
        el = e.getChild("brake-sound");
        if (el != null) {
            fn = el.getChild("sound-file").getValue();
            brake_sound = new SoundBite(vf, fn, _soundName + "_BRAKE", _soundName + "_Brake");
            brake_sound.setGain(setXMLGain(el));
            brake_sound.setLooped(false);
            brake_sound.setFadeTimes(500, 500);
            brake_sound.setReferenceDistance(setXMLReferenceDistance(el));
            trigger_sounds.put("brake", brake_sound);
        }

        // Get the pre-arrival sound
        el = e.getChild("pre-arrival-sound");
        if (el != null) {
            fn = el.getChild("sound-file").getValue();
            pre_arrival_sound = new SoundBite(vf, fn, _soundName + "_PRE-ARRIVAL", _soundName + "_Pre-arrival");
            pre_arrival_sound.setGain(setXMLGain(el));
            pre_arrival_sound.setLooped(false);
            pre_arrival_sound.setFadeTimes(500, 500);
            pre_arrival_sound.setReferenceDistance(setXMLReferenceDistance(el));
            trigger_sounds.put("pre_arrival", pre_arrival_sound);
        }

        if (buffer_ok) {
            // Kick-start the loop thread
            this.startThread();

            // Check auto-start setting
            autoStartCheck();
        } else {
            log.warn("Engine cannot be started due to buffer exceeding");
        }
    }

    private static final Logger log = LoggerFactory.getLogger(Steam1Sound.class);

    private static class S1Notch {

        private int my_notch;
        private int min_rpm, max_rpm;
        private int buffer_fmt;
        private int buffer_freq;
        private int buffer_frame_size;
        private ByteBuffer notchfiller_data;
        private ByteBuffer coastfiller_data;
        private List<AudioBuffer> bufs_helper = new ArrayList<>();
        private List<ByteBuffer> chuff_bufs_data = new ArrayList<>();
        private List<ByteBuffer> coast_bufs_data = new ArrayList<>();

        private S1Notch(int notch) {
            my_notch = notch;
        }

        private int getNotch() {
            return my_notch;
        }

        private int getMaxLimit() {
            return max_rpm;
        }

        private int getMinLimit() {
            return min_rpm;
        }

        private void setMinLimit(int l) {
            min_rpm = l;
        }

        private void setMaxLimit(int l) {
            max_rpm = l;
        }

        private Boolean isInLimits(int val) {
            return val >= min_rpm && val <= max_rpm;
        }

        private void setBufferFmt(int fmt) {
            buffer_fmt = fmt;
        }

        private int getBufferFmt() {
            return buffer_fmt;
        }

        private void setBufferFreq(int freq) {
            buffer_freq = freq;
        }

        private int getBufferFreq() {
            return buffer_freq;
        }

        private void setBufferFrameSize(int framesize) {
            buffer_frame_size = framesize;
        }

        private int getBufferFrameSize() {
            return buffer_frame_size;
        }

        private void setNotchFillerData(ByteBuffer dat) {
            notchfiller_data = dat;
        }

        private ByteBuffer getNotchFillerData() {
            return notchfiller_data;
        }

        private void setCoastFillerData(ByteBuffer dat) {
            coastfiller_data = dat;
        }

        private ByteBuffer getCoastFillerData() {
            return coastfiller_data;
        }

        private void addChuffData(ByteBuffer dat) {
            chuff_bufs_data.add(dat);
        }

        private void addCoastData(ByteBuffer dat) {
            coast_bufs_data.add(dat);
        }

        private void addHelper(AudioBuffer b) {
            bufs_helper.add(b);
        }

        static private AudioBuffer getBufferHelper(String sname, String uname) {
            AudioBuffer bf = null;
            AudioManager am = jmri.InstanceManager.getDefault(jmri.AudioManager.class);
            try {
                bf = (AudioBuffer) am.provideAudio(VSDSound.BufSysNamePrefix + sname);
                bf.setUserName(VSDSound.BufUserNamePrefix + uname);
            } catch (AudioException | IllegalArgumentException ex) {
                log.warn("problem creating SoundBite", ex);
                return null;
            }
            log.debug("empty buffer created: {}, name: {}", bf, bf.getSystemName());
            return bf;
        }

        static private java.io.InputStream getWavStream(VSDFile vf, String filename) {
            java.io.InputStream ins = vf.getInputStream(filename);
            if (ins != null) {
                return ins;
            } else {
                log.warn("input Stream failed for {}", filename);
                return null;
            }
        }

        private static final Logger log = LoggerFactory.getLogger(S1Notch.class);

    }

    private static class S1LoopThread extends Thread {

        Steam1Sound _parent;
        S1Notch _notch;
        S1Notch notch1;
        SoundBite _sound;
        float _throttle;
        private float last_throttle;
        private boolean is_running = false;
        private boolean is_looping = false;
        private boolean is_dying = false;
        private boolean is_auto_coasting;
        private boolean is_key_coasting;
        private boolean is_idling;
        private boolean is_braking;
        private boolean is_half_speed;
        private boolean is_in_rampup_mode;
        private int lastRpm;
        private int rpm_dirfn;
        private long timeOfLastSpeedCheck;
        private int chuff_index;
        private int helper_index;
        private int rpm_nominal; // Nominal value
        private int rpm; // Actual value
        private int topspeed;
        private int _top_speed;
        private int _top_speed_reverse;
        private float _driver_diameter_float;
        private int _num_cylinders;
        private int _decel_trigger_rpms;
        private int acc_time;
        private int dec_time;
        private int count_pre_arrival;
        private int queue_limit;
        private int wait_loops;
        public static final int SLEEP_INTERVAL = 50;

        public S1LoopThread(Steam1Sound d, String s, int ts, int tsr, float dd, 
                int nc, int dtr, boolean r) {
            super();
            _parent = d;
            _top_speed = ts;
            _top_speed_reverse = tsr;
            _driver_diameter_float = dd;
            _num_cylinders = nc;
            _decel_trigger_rpms = dtr;
            is_running = r;
            is_looping = false;
            is_dying = false;
            is_auto_coasting = false;
            is_key_coasting = false;
            is_idling = false;
            is_braking = false;
            is_in_rampup_mode = false;
            lastRpm = 0;
            rpm_dirfn = 0;
            timeOfLastSpeedCheck = 0;
            _throttle = 0.0f;
            last_throttle = 0.0f;
            _notch = null;
            _sound = new SoundBite(s + "_QUEUE"); // Sound for queueing
            _sound.setGain(_parent.engine_gain); // All chuff sounds will have this gain
            count_pre_arrival = 1;
            queue_limit = 2;
            wait_loops = 0;
            if (r) {
                this.start();
            }
        }

        private void setRunning(boolean r) {
            is_running = r;
        }

        private void setThrottle(float t) {
            // Don't do anything, if engine is not started
            // Another required value is a S1Notch (should have been set at engine start)
            if (_parent.engine_started) {
                if (t < 0.0f) {
                    // DO something to shut down
                    setRpmNominal(0);
                    _parent.accdectime = 0;
                    _parent.startAccDecTimer();
                    updateRpm();
                } else {
                    _throttle = t;
                    last_throttle = t;

                    // handle half-speed
                    if (is_half_speed) {
                        _throttle = _throttle / 2;
                    }

                    // Calculate the nominal speed (Revolutions Per Minute)
                    setRpmNominal(calcRPM(_throttle));

                    // Speeding up or slowing down?
                    if (getRpmNominal() < lastRpm) {
                        //
                        // Slowing down
                        //
                        _parent.accdectime = dec_time;
                        log.debug("decelerate from {} to {}", lastRpm, getRpmNominal());

                        if ((getRpmNominal() < 23) && is_auto_coasting && (count_pre_arrival > 0) && 
                                _parent.trigger_sounds.containsKey("pre_arrival") && (dec_time < 250)) {
                            _parent.trigger_sounds.get("pre_arrival").fadeIn();
                            count_pre_arrival--;
                        }

                        // Calculate how long it's been since we lastly checked speed
                        long currentTime = System.currentTimeMillis();
                        float timePassed = currentTime - timeOfLastSpeedCheck;
                        timeOfLastSpeedCheck = currentTime;
                        // Prove the trigger for decelerating actions (braking, coasting)
                        if (((lastRpm - getRpmNominal()) > _decel_trigger_rpms) && (timePassed < 500.0f)) {
                            log.debug("Time passed {}", timePassed);
                            if ((getRpmNominal() < 30) && (dec_time < 250)) { // Braking sound only when speed is low (, but not to low)
                                if (_parent.trigger_sounds.containsKey("brake")) {
                                    _parent.trigger_sounds.get("brake").fadeIn();
                                    is_braking = true;
                                    log.debug("braking activ!");
                                }
                            } else if (notch1.coast_bufs_data.size() > 0 && !is_key_coasting) {
                                is_auto_coasting = true;
                                log.debug("auto-coasting active");
                            }
                        }
                    } else {
                        //
                        // Speeding up.
                        //
                        _parent.accdectime = acc_time;
                        log.debug("accelerate from {} to {}", lastRpm, getRpmNominal());
                        if (is_braking) {
                            stopBraking(); // Revoke possible brake sound
                        }
                        if (is_auto_coasting) {
                            stopAutoCoasting(); // This makes chuff sound hearable again
                        }
                    }
                    _parent.startAccDecTimer(); // Start, if not already running
                    lastRpm = getRpmNominal();
                }
            }
        }

        private void stopBraking() {
            if (is_braking) {
                if (_parent.trigger_sounds.containsKey("brake")) {
                    _parent.trigger_sounds.get("brake").fadeOut();
                    is_braking = false;
                    log.debug("braking sound stopped.");
                }
            }
        }

        private void startBoilingSound() {
            if (_parent.trigger_sounds.containsKey("boiling")) {
                _parent.trigger_sounds.get("boiling").setLooped(true);
                _parent.trigger_sounds.get("boiling").play();
                log.debug("boiling sound playing");
            }
        }

        private void stopBoilingSound() {
            if (_parent.trigger_sounds.containsKey("boiling")) {
                _parent.trigger_sounds.get("boiling").setLooped(false);
                _parent.trigger_sounds.get("boiling").fadeOut();
                log.debug("boiling sound stopped.");
            }
        }

        private void stopAutoCoasting() {
            if (is_auto_coasting) {
                is_auto_coasting = false;
                log.debug("auto-coasting sound stopped.");
            }
        }

        private void getLocoDirection(int d) {
            // If loco direction was changed we need to set topspeed of the loco to new value 
            // (this is necessary, when topspeed-forward and topspeed-reverse differs)
            if (d == 1) {  // loco is going forward
                topspeed = _top_speed;
            } else {
                topspeed = _top_speed_reverse;
            }
            log.debug("loco direction: {}, top speed: {}", d, topspeed);
            // Re-calculate accel-time and decel-time, hence topspeed may have changed
            acc_time = calcAccDecTime(_parent.accel_rate);
            dec_time = calcAccDecTime(_parent.decel_rate);

            // Handle throttle forward and reverse action
            // nothing to do if loco is not running or just in ramp-up-mode
            if (getRpm() > 0 && _parent.engine_started && !is_in_rampup_mode) {
                rpm_dirfn = getRpm(); // save rpm for ramp-up
                log.debug("rpm {} saved", rpm_dirfn);
                is_in_rampup_mode = true; // set a flag for the ramp-up
                setRpmNominal(0);
                _parent.startAccDecTimer();
                updateRpm();
            }
        }

        private void setFunction(String event, boolean is_true, String name) {
            // This throttle function key handling differs to configurable sounds:
            // Do something following certain conditions, when a throttle function key is pressed.
            // Note: throttle will send initial value(s) before thread is started! 
            log.debug("throttle function key pressed: {} is {}, function: {}", event, is_true, name);
            if (name.equals("COAST")) {
                // Handle key-coasting on/off.
                log.debug("COAST key pressed");
                // Set coasting TRUE, if COAST key is pressed. Requires sufficient coasting sounds (chuff_index will rely on that).
                if (notch1 == null) {
                    notch1 = _parent.getNotch(1); // Because of initial send of throttle key, COAST function key could be "true"
                }
                if (is_true && notch1.coast_bufs_data.size() > 0) {
                    is_key_coasting = true; // When idling is active, key-coasting will start after it.
                } else {
                    is_key_coasting = false; // Stop the key-coasting sound
                }
                log.debug("is COAST: {}", is_key_coasting);
            }

            // Speed change if HALF_SPEED key is pressed
            if (name.equals("HALF_SPEED")) {
                log.debug("HALF_SPEED key pressed is {}", is_true);
                if (_parent.engine_started) {
                    if (is_true) {
                        is_half_speed = true;
                    } else {
                        is_half_speed = false;
                    }
                    setThrottle(last_throttle); // Trigger a speed update
                }
            }

            // Set Accel/Decel off or to lower value
            if (name.equals("BRAKE_KEY")) {
                log.debug("BRAKE_KEY pressed is {}", is_true);
                if (_parent.engine_started) {
                    if (is_true) {
                        if (_parent.brake_time == 0) {
                            acc_time = 0;
                            dec_time = 0;
                        } else {
                            dec_time = calcAccDecTime(_parent.brake_time);
                        }
                        _parent.accdectime = dec_time;
                        log.debug("accdectime: {}", _parent.accdectime);
                    } else {
                        acc_time = calcAccDecTime(_parent.accel_rate);
                        dec_time = calcAccDecTime(_parent.decel_rate);
                        _parent.accdectime = dec_time;
                    }
                }
            }
            // Other throttle function keys may follow ...
        }

        private void startEngine() {
            _sound.unqueueBuffers();
            log.debug("thread: start engine ...");
            _notch = _parent.getNotch(1); // Initial value
            notch1 = _parent.getNotch(1);
            if (_parent.engine_pane != null) {
                _parent.engine_pane.setThrottle(1); // Set EnginePane (DieselPane) notch
            } 
            _sound.setReferenceDistance(_parent.engine_rd);
            setRpm(0);
            setRpmNominal(0);
            helper_index = -1; // Prepare helper buffer start. Index will be incremented before first use
            setWait(0);
            startBoilingSound();
            startIdling();
            acc_time = calcAccDecTime(_parent.accel_rate); // Calculate acceleration time
            dec_time = calcAccDecTime(_parent.decel_rate); // Calculate deceleration time
            _parent.initAccDecTimer();
        }

        private void stopEngine() {
            log.debug("thread: stop engine ...");
            if (is_looping) {
                is_looping = false; // Stop the loop player
            }
            is_dying = true;
            stopBraking();
            stopAutoCoasting();
            stopBoilingSound();
            stopIdling();
            _parent.stopAccDecTimer();
            _throttle = 0.0f; // Clear it, just in case the engine was stopped at speed > 0
        }

        private int calcAccDecTime(int accdec_rate) {
            // Handle Momentum
            // Regard topspeed, which may be different on forward or reverse direction
            int topspeed_rpm = (int) Math.round(topspeed * 1056 / (Math.PI * _driver_diameter_float));
            return 896 * accdec_rate / topspeed_rpm; // NMRA value 896 in ms
        }

        private void startIdling() {
            is_idling = true;
            if (_parent.trigger_sounds.containsKey("idle")) {
                _parent.trigger_sounds.get("idle").setLooped(true);
                if (!_parent.trigger_sounds.get("idle").isPlaying()) {
                    _parent.trigger_sounds.get("idle").play();
                }
            }
            log.debug("start idling ...");
        }

        private void stopIdling() {
            if (is_idling) {
                is_idling = false;
                if (_parent.trigger_sounds.containsKey("idle")) {
                    _parent.trigger_sounds.get("idle").fadeOut();
                    log.debug("idling stopped.");
                }
            }
        }

        //
        //   LOOP-PLAYER
        //
        @Override
        public void run() {
            try {
                while (is_running) {
                    if (is_looping) {
                        if (_sound.getSource().numProcessedBuffers() > 0) {
                            _sound.unqueueBuffers();
                        }
                        log.debug("run loop. Buffers queued: {}", _sound.getSource().numQueuedBuffers());
                        if ((_sound.getSource().numQueuedBuffers() < queue_limit) && (getWait() == 0)) {
                            setSound(selectData()); // Select appropriate WAV data, handle sound and filler and queue the sound
                        }
                        if (_sound.getSource().getState() != Audio.STATE_PLAYING) {
                            _sound.play(); // Starts the Sound. Maybe also re-starts the sound
                            if (getRpm() > _parent.getNotch(1).getMinLimit()) {
                                log.info("loop sound re-started. Possibly queue underrun at rpm: {}", getRpm());
                            }
                        }
                    } else {
                        // Quietly wait for the sound to get turned on again
                        // Once we've stopped playing, kill the thread
                        if (_sound.getSource().numProcessedBuffers() > 0) {
                            _sound.unqueueBuffers();
                        }
                        if (is_dying && (_sound.getSource().getState() != Audio.STATE_PLAYING)) {
                            _sound.stop(); // good reason to get rid of SoundBite.is_playing variable!
                            //return;
                        }
                    }
                    sleep(SLEEP_INTERVAL);
                    updateWait();
                }
                // Note: if (is_running == false) we'll exit the endless while and the Thread will die
                return;
            } catch (InterruptedException ie) {
                //Thread.currentThread().interrupt();
                log.error("execption", ie);
                return;
                // probably should do something. Not sure what
            }
        }

        private ByteBuffer selectData() {
            ByteBuffer data;
            if (is_key_coasting || is_auto_coasting) {
                data = notch1.coast_bufs_data.get(incChuffIndex()); // Take the coasting sound
            } else {
                data = _notch.chuff_bufs_data.get(incChuffIndex()); // Take the standard chuff sound
            }
            return data;
        }

        private void changeNotch() {
            int new_notch = _notch.getNotch();
            log.debug("changing notch ... rpm: {}, notch: {}, chuff index: {}", 
                    getRpm(), _notch.getNotch(), chuff_index);
            if ((getRpm() > _notch.getMaxLimit()) && (new_notch < _parent.notch_sounds.size())) {
                // Too fast. Need to go to next notch up
                new_notch++;
                log.debug("change up. notch: {}", new_notch);
                _notch = _parent.getNotch(new_notch);
            } else if ((getRpm() < _notch.getMinLimit()) && (new_notch > 1)) {
                // Too slow.  Need to go to next notch down
                new_notch--;
                log.debug("change down. notch: {}", new_notch);
                _notch = _parent.getNotch(new_notch);
            }
            _parent.engine_pane.setThrottle(new_notch); // Update EnginePane (DieselPane) notch
            return;
        }

        private int getRpm() {
            return rpm; // Actual Revolution per Minute
        }

        private void setRpm(int r) {
            rpm = r;
        }

        private int getRpmNominal() {
            return rpm_nominal; // Nominal Revolution per Minute
        }

        private void setRpmNominal(int rn) {
            rpm_nominal = rn;
        }

        private void updateRpm() {
            if (getRpmNominal() > getRpm()) {
                // Actual rpm should not exceed highest max-rpm defined in config.xml
                if (getRpm() < _parent.getNotch(_parent.notch_sounds.size()).getMaxLimit()) {
                    setRpm(getRpm() + 1);
                } else {
                    log.debug("actual rpm not increased. Value: {}", getRpm());
                } 
                log.debug("accel - nominal RPM: {}, actual RPM: {}", getRpmNominal(), getRpm());
            } else if (getRpmNominal() < getRpm()) {
                setRpm(getRpm() - 1);
                if (getRpm() < 0) {
                    setRpm(0);
                }
                log.debug("decel - nominal RPM: {}, actual RPM: {}", getRpmNominal(), getRpm());
            } else {
                _parent.stopAccDecTimer(); // Speed is unchanged, nothing to do
            }

            // Start or Stop the LOOP-PLAYER
            checkState();

            // Are we in the right notch?
            if ((getRpm() >= notch1.getMinLimit()) && (!_notch.isInLimits(getRpm()))) {
                log.debug("Notch change! Notch: {}, RPM nominal: {}, RPM actual: {}", _notch.getNotch(), getRpmNominal(), getRpm());
                changeNotch();
            }
        }

        private void checkState() {
            if (is_looping) {
                if (getRpm() < notch1.getMinLimit()) {
                    is_looping = false; // Stop the loop player
                    setWait(0);
                    log.debug("change from chuff or coast to idle.");
                    stopAutoCoasting(); // Automatic coasting is stopped here
                    stopBraking(); 
                    startIdling();
                }
            } else {
                if (_parent.engine_started && (getRpm() >= notch1.getMinLimit())) {
                    stopIdling();
                    // Now prepare to start the chuff sound (or coasting sound)
                    _notch = _parent.getNotch(1); // Initial notch value
                    chuff_index = -1; // Index will be incremented before first usage
                    count_pre_arrival = 1;
                    is_looping = true; // Start the loop player
                }

                // Handle a throttle forward or reverse change
                if (is_in_rampup_mode && getRpm() == 0) {
                    log.debug("now ramp-up to rpm {}", rpm_dirfn);
                    setRpmNominal(rpm_dirfn);
                    _parent.startAccDecTimer();
                    is_in_rampup_mode = false;
                    updateRpm();
                }
            }

            // Allow more buffers to be queued at higher speed
            if (getRpm() > 250) {
                queue_limit = 5;
            } else if ( getRpm() > 62) {
                queue_limit = 3;
            } else {
                queue_limit = 2;
            }
        }

        private void updateWait() {
            if (getWait() > 0) {
                setWait(getWait() - 1);
            }
        }

        private void setWait(int wait) {
            wait_loops = wait;
        }

        private int getWait() {
            return wait_loops;
        }

        private int incChuffIndex() {
            chuff_index++;
            // Correct for wrap.
            if (chuff_index >= (_num_cylinders * 2)) {
                chuff_index = 0;
            }
            log.debug("new chuff index: {}", chuff_index);
            return chuff_index;
        }

        private int incHelperIndex() {
            helper_index++;
            // Correct for wrap.
            if (helper_index >= notch1.bufs_helper.size()) {
                helper_index = 0;
            }
            return helper_index;
        }

        private int calcRPM(float t) {
            // speed = % of topspeed (mph)
            // RPM = speed * ((inches/mile) / (minutes/hour)) / (pi * driver_diameter_float)
            return (int) Math.round(_parent.speedCurve(t) * topspeed * 1056 / (Math.PI * _driver_diameter_float));
        }

        private int calcChuffInterval(int revpm) {
            //  chuff interval will be calculated based on revolutions per minute (revpm)
            //  note: interval time includes the sound duration!
            //  chuffInterval = time in msec per revolution of the driver wheel: 
            //      60,000 msec / revpm / number of cylinders / 2 (because cylinders are double-acting)
            return (int) Math.round(60000.0 / revpm / _num_cylinders / 2.0);
        }

        private void setSound(ByteBuffer data) {
            AudioBuffer buf = notch1.bufs_helper.get(incHelperIndex()); // buffer for the queue
            int sbl = 0;
            if (notch1.getBufferFreq() > 0) {
                sbl = (1000 * data.limit()/notch1.getBufferFrameSize()) / notch1.getBufferFreq(); // calculate the length of the clip in milliseconds
            }
            log.debug("sbl: {}", sbl);
            // Time in msec from chuff start up to begin of the next chuff, limited to a minimum
            int interval = Math.max(calcChuffInterval(getRpm()), SLEEP_INTERVAL);
            int bbufcount = notch1.getBufferFrameSize() * ((interval) * notch1.getBufferFreq() / 1000);
            ByteBuffer bbuf = ByteBuffer.allocateDirect(bbufcount); // Target

            if (interval > sbl) {
                // Regular queueing. Whole sound clip goes to the queue. Low notches
                // Prepare the sound and transfer it to the target ByteBuffer bbuf
                int bbufcount2 = notch1.getBufferFrameSize() * (sbl * notch1.getBufferFreq() / 1000);
                byte[] bbytes2 = new byte[bbufcount2];
                data.get(bbytes2); // Same as: data.get(bbytes2, 0, bbufcount2);
                data.rewind();
                bbuf.order(data.order()); // Set new buffer's byte order to match source buffer.
                bbuf.put(bbytes2); // Same as: bbuf.put(bbytes2, 0, bbufcount2);

                // Handle filler for the remaining part of the AudioBuffer
                if (bbuf.hasRemaining()) {
                    log.debug("remaining: {}", bbuf.remaining());
                    ByteBuffer dataf;
                    if (is_key_coasting || is_auto_coasting) {
                        dataf = notch1.getCoastFillerData();
                    } else {
                        dataf = notch1.getNotchFillerData();
                    }
                    if (dataf == null) {
                        log.debug("No filler sound found");
                        // Nothing to do on 16-bit, because 0 is default for "silence"; 8-bit-mono needs 128, otherwise it's "noisy"
                        if (notch1.getBufferFmt() == com.jogamp.openal.AL.AL_FORMAT_MONO8) {
                            byte[] bbytesfiller = new byte[bbuf.remaining()];
                            for (int i = 0; i < bbytesfiller.length; i++) {
                                bbytesfiller[i] = (byte) 0x80; // fill array with "silence"
                            }
                            bbuf.put(bbytesfiller);
                        }
                    } else {
                        // Filler sound found
                        log.debug("data limit: {}, remaining: {}", dataf.limit(), bbuf.remaining());
                        byte[] bbytesfiller2 = new byte[bbuf.remaining()];
                        if (dataf.limit() >= bbuf.remaining()) {
                            dataf.get(bbytesfiller2);
                            dataf.rewind();
                            bbuf.put(bbytesfiller2);
                        } else {
                            log.debug("not enough filler length");
                            byte[] bbytesfillerpart = new byte[dataf.limit()];
                            dataf.get(bbytesfillerpart);
                            dataf.rewind();
                            int k = 0;
                            for (int i = 0; i < bbytesfiller2.length; i++) {
                                bbytesfiller2[i] = bbytesfillerpart[k];
                                k++;
                                if (k == dataf.limit()) {
                                    k = 0;
                                }
                            }
                            bbuf.put(bbytesfiller2);
                        }
                    }
                }
            } else {
                // Need to cut the SoundBite to new length of interval
                log.debug("need to cut sound clip from {} to length {}", sbl, interval); 
                byte[] bbytes = new byte[bbufcount];
                data.get(bbytes); // Same as: data.get(bbytes, 0, bbufcount);
                data.rewind();
                bbuf.order(data.order()); // Set new buffer's byte order to match source buffer
                bbuf.put(bbytes); // Same as: bbuf.put(bbytes, 0, bbufcount);
            }
            bbuf.rewind();
            buf.loadBuffer(bbuf, notch1.getBufferFmt(), notch1.getBufferFreq());
            _sound.queueBuffer(buf);
            log.debug("buffer queued. Length: {}", (int)SoundBite.calcLength(buf));

            // wait some loops to get up-to-date speed value
            setWait((interval - SLEEP_INTERVAL * _parent.wait_factor) / SLEEP_INTERVAL);
            if (getWait() < 3) {
                setWait(0);
            }
        }

        public void mute(boolean m) {
            _sound.mute(m);
            for (SoundBite ts : _parent.trigger_sounds.values()) {
                ts.mute(m);
            }
        }

        public void setVolume(float v) {
            _sound.setVolume(v);
            for (SoundBite ts : _parent.trigger_sounds.values()) {
                ts.setVolume(v);
            }
        }

        public void setPosition(PhysicalLocation p) {
            _sound.setPosition(p);
            for (SoundBite ts : _parent.trigger_sounds.values()) {
                ts.setPosition(p);
            }
        }

        private static final Logger log = LoggerFactory.getLogger(S1LoopThread.class);

    }
}
