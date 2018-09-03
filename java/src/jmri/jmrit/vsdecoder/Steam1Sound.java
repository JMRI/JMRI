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
 * @author Mark Underwood Copyright (C) 2011
 * @author Klaus Killinger Copyright (C) 2017, 2018
 */
class Steam1Sound extends EngineSound {

    // Engine Sounds
    HashMap<Integer, S1Notch> notch_sounds;
    String _soundName;

    // Trigger Sounds
    HashMap<String, SoundBite> trigger_sounds;

    int top_speed;
    private float driver_diameter_float;
    private int num_cylinders;
    private float exponent;
    private int accel_rate;
    private int decel_rate;
    private int brake_time;
    private int decel_trigger_rpms;
    private SoundBite idle_sound;
    private SoundBite boiling_sound;
    private SoundBite brake_sound;
    private SoundBite pre_arrival_sound;
    float engine_rd;
    float engine_gain;

    // Common variables
    boolean is_looping = false;
    S1LoopThread _loopThread = null;

    private javax.swing.Timer rpmTimer;
    int accdectime;

    // Constructor
    public Steam1Sound(String name) {
        super(name);
        log.debug("New Steam1Sound name(param): {}, name(val): {}", name, this.getName());
    }

    private void startThread() {
        _loopThread = new S1LoopThread(this, _soundName, top_speed,
                driver_diameter_float, num_cylinders, decel_trigger_rpms, true);
        log.debug("Loop Thread Started.  Sound name: {}", _soundName);
    }

    @Override
    public void stop() {
        // Stop the loop thread, in case it's running
        if (_loopThread != null) {
            _loopThread.setRunning(false);
        }
        is_looping = false;
    }

    // Responds to "CHANGE" trigger (float)
    @Override
    public void changeThrottle(float s) {
        // This is all we have to do.  The loop thread will handle everything else
        if (_loopThread != null) {
            _loopThread.setThrottle(s);
        }
    }

    // Responds to throttle function key (see EngineSound.java and EngineSoundEvent.java)
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

    // Called from thread
    public S1Notch getNotch(int n) {
        return notch_sounds.get(n);
    }

    // Called from thread
    void initAccDecTimer() {
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

    // Called from thread
    void startAccDecTimer() {
        if (!rpmTimer.isRunning()) {
            rpmTimer.start();
            log.debug("timer {} started, delay: {}", rpmTimer, accdectime);
        }
    }

    // Called from thread
    void stopAccDecTimer() {
        if (rpmTimer.isRunning()) {
            rpmTimer.stop();
            log.debug("timer {} stopped, delay: {}", rpmTimer, accdectime);
        }
    }

    @Override
    public void startEngine() {
        log.debug("startEngine. ID = {}", this.getName());
        _loopThread.startEngine();
    }

    @Override
    public void stopEngine() {
        log.debug("stopEngine. ID = {}", this.getName());
        if (_loopThread != null) {
            _loopThread.stopEngine();
        }
    }

    @Override
    public void shutdown() {
        for (VSDSound vs : trigger_sounds.values()) {
            log.debug(" Stopping trigger sound: {}", vs.getName());
            vs.stop(); // SoundBite: Stop playing
        }
        if (rpmTimer != null) {
            stopAccDecTimer();
        }
        this.stop();
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
        Element el;
        String fn, n;
        S1Notch sb;

        // Handle the common stuff
        super.setXml(e, vf);

        _soundName = this.getName();
        log.debug("Steam1: name: {}, soundName: {}", this.getName(), _soundName);

        // Required values
        top_speed = Integer.parseInt(e.getChildText("top-speed"));
        log.debug("top speed forward: {} MPH", top_speed);
        driver_diameter_float = Float.parseFloat(e.getChildText("driver-diameter-float"));
        log.debug("driver diameter: {} inches", driver_diameter_float);
        num_cylinders = Integer.parseInt(e.getChildText("cylinders"));
        log.debug("Number of cylinders defined: {}", num_cylinders);

        // Optional value
        // Allows to adjust speed
        n = e.getChildText("exponent"); // Optional value
        if (n != null) {
            exponent = Float.parseFloat(n);
        } else {
            exponent = 1.0f; // Default
        }
        log.debug("exponent: {}", exponent);

        // Optional value
        // Acceleration and deceleration rate
        n = e.getChildText("accel-rate"); // Optional value
        if (n != null) {
            accel_rate = Integer.parseInt(n);
        } else {
            accel_rate = 35; // Default
        }
        log.debug("accel rate: {}", accel_rate);

        n = e.getChildText("decel-rate"); // Optional value
        if (n != null) {
            decel_rate = Integer.parseInt(n);
        } else {
            decel_rate = 18; // Default
        }
        log.debug("decel rate: {}", decel_rate);

        n = e.getChildText("brake-time"); // Optional value
        if (n != null) {
            brake_time = Integer.parseInt(n);
        } else {
            brake_time = 0;  // Default
        }
        log.debug("brake time: {}", brake_time);

        // Optional value 
        // auto-start
        is_auto_start = setXMLAutoStart(e);
        log.debug("config auto-start: {}", is_auto_start);

        // Optional value
        // Allows to adjust OpenAL attenuation
        // Sounds with distance to listener position lower than reference-distance will not have attenuation
        engine_rd = setXMLReferenceDistance(e); // Handle reference distance
        log.debug("engine-sound referenceDistance: {}", engine_rd);

        // Optional value
        // Allows to adjust the engine gain
        n = e.getChildText("engine-gain");
        if ((n != null) && (!n.isEmpty())) {
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

        // Optional value
        // Defines how many rpms in 0.5 seconds will trigger decel actions like braking
        n = e.getChildText("decel-trigger-rpms");
        if (n != null) {
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
        int i = 0; // notch number (index)
        int j = 0; // chuff or coast number (index)
        int fmt = 0; // Sound sample format
        int nn = 1; // notch number (visual)

        // Get the notch-sounds.
        Iterator<Element> itr = (e.getChildren("s1notch-sound")).iterator();
        while (itr.hasNext()) {
            el = itr.next();
            sb = new S1Notch();
            sb.setNotch(nn);

            // Get the chuff sounds
            List<Element> elist = el.getChildren("notch-file");
            j = 0;
            for (Element fe : elist) {
                fn = fe.getText();
                log.debug("notch: {}, file: {}", nn, fn);
                AudioBuffer b = S1Notch.getBuffer(vf, fn, _soundName + 
                        "_NOTCH_" + i + "_" + j, _soundName + "_NOTCH_" + i + "_" + j);
                log.debug("buffer created: {}, name: {}, format: {}", b, 
                        b.getSystemName(), b.getFormat());
                sb.addChuffBuffer(b);
                if (fmt == 0) {
                    // Get the format of the (first) WAV file
                    // Since all WAV files of the notches MUST have the same format,
                    // I asume this format for all WAV files for now
                    fmt = AudioUtil.getWavFormat(S1Notch.getWavStream(vf, fn)); 
                    log.debug("fmt: {}", fmt);
                }
                ByteBuffer data = AudioUtil.getWavData(S1Notch.getWavStream(vf, fn));
                sb.addChuffData(data);
                j++;
            }
            log.debug("Number of chuff sounds for notch {} defined: {}", nn, j);

            // Create a filler Buffer for queueing and a ByteBuffer for length modification
            fn = el.getChildText("notchfiller-file");
            if (fn != null) {
                log.debug("notch filler file: {}", fn);
                AudioBuffer bnf = S1Notch.getBuffer(vf, 
                        el.getChildText("notchfiller-file"), _soundName + 
                        "_NOTCHFILLER_" + i, _soundName + "_NOTCHFILLER_" + i);
                log.debug("buffer created: {}, name: {}, format: {}", bnf, bnf.getSystemName(), bnf.getFormat());
                sb.setNotchFillerBuffer(bnf);
                sb.setNotchFillerData(AudioUtil.getWavData(S1Notch.getWavStream(vf, fn)));
            } else {
                log.debug("no notchfiller available.");
                sb.setNotchFillerBuffer(null);
            }

            // Coasting sound and helpers are bound to first notch only
            // VSDFile validation makes sure that there is at least one notch
            if (nn == 1) {
                // Get the coasting sounds
                j = 0;
                List<Element> elistc = el.getChildren("coast-file");
                for (Element fe : elistc) {
                    fn = fe.getText();
                    log.debug("coasting file: {}", fn);
                    AudioBuffer bc = S1Notch.getBuffer(vf, fn, _soundName + 
                            "_COAST_" + j, _soundName + "_COAST_" + j);
                    log.debug("buffer created: {}, name: {}, format: {}", 
                            bc, bc.getSystemName(), bc.getFormat());
                    sb.addCoastBuffer(bc); // WAV in Buffer for queueing
                    ByteBuffer datac = AudioUtil.getWavData(S1Notch.getWavStream(vf, fn));
                    sb.addCoastData(datac); // WAV data in ByteBuffer for length modification
                    j++;
                }
                log.debug("Number of coasting sounds for notch {} defined: {}", nn, j);

                // Create a filler Buffer for queueing and a ByteBuffer for length modification
                fn = el.getChildText("coastfiller-file");
                if (fn != null) {
                    log.debug("coasting filler file: {}", fn);
                    AudioBuffer bcf = S1Notch.getBuffer(vf, fn, _soundName +
                            "_COASTFILLER", _soundName + "_COASTFILLER");
                    log.debug("buffer created: {}, name: {}, format: {}", bcf, 
                            bcf.getSystemName(), bcf.getFormat());
                    sb.setCoastFillerBuffer(bcf);
                    sb.setCoastFillerData(AudioUtil.getWavData(S1Notch.getWavStream(vf, fn)));
                } else {
                    log.debug("no coastfiller available.");
                    sb.setCoastFillerBuffer(null);
                }

                // Add some helper Buffers. They are needed for creating
                // variable sound clips in length. Ten helper buffers should
                // serve well for that purpose. These buffers are bound to notch 1
                for (int jk = 0; jk < 10; jk++) {
                    AudioBuffer bh = S1Notch.getBufferHelper(_soundName + 
                            "_BUFFERHELPER_" + jk, _soundName + "_BUFFERHELPER_" + jk);
                    log.debug("buffer helper created: {}, name: {}", bh, bh.getSystemName());
                    sb.addHelper(bh);
                }
            }

            sb.setMinLimit(Integer.parseInt(el.getChildText("min-rpm")));
            sb.setMaxLimit(Integer.parseInt(el.getChildText("max-rpm")));
            sb.setBufferFmt(fmt);
            log.debug("sample format for notch {}: {}", nn, fmt);

            // Store in the list
            notch_sounds.put(nn, sb);
            i++;
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
            log.debug("boiling-sound: {}", fn);
            boiling_sound = new SoundBite(vf, fn, name + "_BOILING", name + "_Boiling");
            boiling_sound.setGain(setXMLGain(el)); // Handle gain
            log.debug("boiling-sound gain: {}", boiling_sound.getGain());
            boiling_sound.setLooped(true);
            boiling_sound.setFadeTimes(500, 500);
            boiling_sound.setReferenceDistance(setXMLReferenceDistance(el));
            log.debug("boiling-sound reference distance: {}", boiling_sound.getReferenceDistance());
            trigger_sounds.put("boiling", boiling_sound);
            log.debug("trigger boiling sound: {}", trigger_sounds.get("boiling"));
        }

        // Get the brake sound
        el = e.getChild("brake-sound");
        if (el != null) {
            fn = el.getChild("sound-file").getValue();
            log.debug("brake sound: {}", fn);
            brake_sound = new SoundBite(vf, fn, _soundName + "_BRAKE", _soundName + "_Brake");
            brake_sound.setGain(setXMLGain(el));
            log.debug("brake sound gain: {}", brake_sound.getGain());
            brake_sound.setLooped(false);
            brake_sound.setFadeTimes(500, 500);
            brake_sound.setReferenceDistance(setXMLReferenceDistance(el));
            log.debug("brake-sound reference distance: {}", brake_sound.getReferenceDistance());
            trigger_sounds.put("brake", brake_sound);
            log.debug("trigger brake sound: {}", trigger_sounds.get("brake"));
        }

        // Get the pre-arrival sound
        el = e.getChild("pre-arrival-sound");
        if (el != null) {
            fn = el.getChild("sound-file").getValue();
            log.debug("pre-arrival sound: {}", fn);
            pre_arrival_sound = new SoundBite(vf, fn, _soundName + "_PRE-ARRIVAL", 
                    _soundName + "_Pre-arrival");
            pre_arrival_sound.setGain(setXMLGain(el));
            log.debug("pre-arrival sound gain: {}", pre_arrival_sound.getGain());
            pre_arrival_sound.setLooped(false);
            pre_arrival_sound.setFadeTimes(500, 500);
            pre_arrival_sound.setReferenceDistance(setXMLReferenceDistance(el));
            log.debug("pre-arrival-sound reference distance: {}", pre_arrival_sound.getReferenceDistance());
            trigger_sounds.put("pre_arrival", pre_arrival_sound);
            log.debug("trigger pre_arrival sound : {}", trigger_sounds.get("pre_arrival"));
        }

        // Kick-start the loop thread
        this.startThread();

        // Check auto-start setting
        autoStartCheck();
    }

    private static final Logger log = LoggerFactory.getLogger(Steam1Sound.class);

    private static class S1Notch {

        private AudioBuffer filler_bufn;
        private AudioBuffer filler_bufc;
        private int my_notch;
        private int min_rpm, max_rpm;
        private int buffer_fmt;
        private ByteBuffer notchfiller_data;
        private ByteBuffer coastfiller_data;
        private List<AudioBuffer> chuff_bufs = new ArrayList<>();
        private List<AudioBuffer> bufs_helper = new ArrayList<>();
        private List<ByteBuffer> chuff_bufs_data = new ArrayList<>();
        private List<AudioBuffer> coast_bufs = new ArrayList<>();
        private List<ByteBuffer> coast_bufs_data = new ArrayList<>();

        public S1Notch() {
            this(1, null, null, null, null);
        }

        public S1Notch(int notch, AudioBuffer notchfiller, AudioBuffer coastfiller, 
                List<AudioBuffer> chuff, List<AudioBuffer> coast) {
            my_notch = notch;
            filler_bufn = notchfiller;
            filler_bufc = coastfiller;
            if (chuff != null) {
                chuff_bufs = chuff;
            }
            if (coast != null) {
                coast_bufs = coast;
            }
        }

        public int getNotch() {
            return my_notch;
        }

        public int getMaxLimit() {
            return max_rpm;
        }

        public int getMinLimit() {
            return min_rpm;
        }

        public void setNotch(int n) {
            my_notch = n;
        }

        public void setMinLimit(int l) {
            min_rpm = l;
        }

        public void setMaxLimit(int l) {
            max_rpm = l;
        }

        public Boolean isInLimits(int val) {
            return val >= min_rpm && val <= max_rpm;
        }

        public void setBufferFmt(int fmt) {
            buffer_fmt = fmt;
        }

        public int getBufferFmt() {
            return buffer_fmt;
        }

        public void setNotchFillerBuffer(AudioBuffer b) {
            filler_bufn = b;
        }

        public AudioBuffer getNotchFillerBuffer() {
            return filler_bufn;
        }

        public void setCoastFillerBuffer(AudioBuffer b) {
            filler_bufc = b;
        }

        public AudioBuffer getCoastFillerBuffer() {
            return filler_bufc;
        }

        public void setNotchFillerData(ByteBuffer dat) {
            notchfiller_data = dat;
        }

        public ByteBuffer getNotchFillerData() {
            return notchfiller_data;
        }

        public void setCoastFillerData(ByteBuffer dat) {
            coastfiller_data = dat;
        }

        public ByteBuffer getCoastFillerData() {
            return coastfiller_data;
        }

        public void addChuffBuffer(AudioBuffer b) {
            chuff_bufs.add(b);
        }

        public void addChuffData(ByteBuffer dat) {
            chuff_bufs_data.add(dat);
        }

        public void addCoastBuffer(AudioBuffer b) {
            coast_bufs.add(b);
        }

        public void addCoastData(ByteBuffer dat) {
            coast_bufs_data.add(dat);
        }

        public void addHelper(AudioBuffer b) {
            bufs_helper.add(b);
        }

        static public AudioBuffer getBuffer(VSDFile vf, String filename, 
                    String sname, String uname) {
            AudioBuffer b = null;
            AudioManager am = jmri.InstanceManager.getDefault(jmri.AudioManager.class);
            try {
                b = (AudioBuffer) am.provideAudio(VSDSound.BufSysNamePrefix + sname);
                b.setUserName(VSDSound.BufUserNamePrefix + uname);
                if (vf == null) {
                    log.warn("No VSD File");
                    return null;
                } else {
                    java.io.InputStream ins = vf.getInputStream(filename);
                    if (ins != null) {
                        b.setInputStream(ins);
                    } else {
                        log.warn("input Stream failed for {}", filename);
                        return null;
                    }
                }
            } catch (AudioException | IllegalArgumentException ex) {
                log.warn("problem creating SoundBite", ex);
                return null;
            }

            log.debug("buffer created: {}, name: {}, format: {}", 
                    b, b.getSystemName(), b.getFormat());
            return b;
        }

        static public AudioBuffer getBufferHelper(String sname, String uname) {
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

        static public java.io.InputStream getWavStream(VSDFile vf, String filename) {
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
        S1Notch coast_notch; // Needed for coasting sounds and buffers
        S1Notch helper_notch; // Needed for helper buffers
        SoundBite _sound;
        float _throttle;

        private boolean is_running = false;
        private boolean is_looping = false;
        private boolean is_dying = false;
        private boolean is_auto_coasting;
        private boolean is_key_coasting;
        private boolean is_idling;
        private boolean is_braking;
        private int lastRpm;
        private long timeOfLastSpeedCheck;
        private int chuff_index;
        private int helper_index;
        private boolean waitForFiller;
        private boolean is_half_speed;
        private int rpm_nominal; // Nominal value
        private int rpm; // Actual value
        private int _top_speed;
        private float _driver_diameter_float;
        private int _num_cylinders;
        private int _decel_trigger_rpms;
        private int acc_time;
        private int dec_time;
        private int count_pre_arrival;
        private int queue_limit;
        private int waitFiller;
        private int sbl_fill;
        private int wait_notch;
        public static final int SLEEP_INTERVAL = 50;

        public S1LoopThread(Steam1Sound d, String s, int ts, float dd, 
                int nc, int dtr, boolean r) {
            super();
            _parent = d;
            _top_speed = ts;
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
            waitForFiller = false;
            lastRpm = 0;
            timeOfLastSpeedCheck = 0;
            _throttle = 0.0f;
            _notch = null;
            coast_notch = null;
            helper_notch = null;
            _sound = new SoundBite(s + "_QUEUE"); // Sound for queueing
            _sound.setGain(_parent.engine_gain); // All chuff sounds will have this gain
            count_pre_arrival = 1;
            queue_limit = 2;
            waitFiller = 0;
            sbl_fill = 0;
            wait_notch = 1;
            if (r) {
                this.start();
            }
        }

        public void setRunning(boolean r) {
            is_running = r;
        }

        public void setThrottle(float t) {
            // Don't do anything, if engine is not started
            // Another required value is a S1Notch (should have been set at engine start)
            if (_parent.engine_started) {
                if (t < 0.0f) {
                    // DO something to shut down
                    _sound.stop();
                    is_running = false;
                    log.info("emergency Stop");
                    //return; // klk: This will tear down VSD
                    // probably should do something. Not sure what
                    stopBraking();
                    stopAutoCoasting();
                    stopIdling();
                    _throttle = 0.0f;
                    log.info("Throttle set to {}", _throttle);
                } else {
                    _throttle = t;
                }

                if (is_half_speed) {
                    _throttle = _throttle / 2;
                }
                // Calculate the nominal speed (Revolutions Per Minute)
                setRpmNominal(calcRPM(_throttle));

                // Speeding up or slowing down?
                if (getRpmNominal() < lastRpm) {
                    //
                    // Slowing down.
                    //
                    _parent.accdectime = dec_time;
                    log.debug("decelerate from {} to {}", lastRpm, getRpmNominal());

                    if ((getRpmNominal() < 23) && is_auto_coasting && (count_pre_arrival > 0) && 
                            _parent.trigger_sounds.containsKey("pre_arrival")) {
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
                        if (getRpmNominal() < 30) { // Braking sound only when speed is low (, but not to low)
                            if (_parent.trigger_sounds.containsKey("brake")) {
                                _parent.trigger_sounds.get("brake").fadeIn();
                                is_braking = true;
                                log.debug("braking activ!");
                            }
                        } else if (coast_notch.coast_bufs.size() > 0 && !is_key_coasting) {
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

        private void setFunction(String event, boolean is_true, String name) {
            // This throttle function key handling differs to configurable sounds:
            // Do something following certain conditions, when a throttle function key is pressed.
            // Note: throttle will send initial value(s) before thread is started! 
            log.debug("throttle function key pressed: {} is {}, function: {}", event, is_true, name);
            if (name.equals("COAST")) {
                // Handle key-coasting on/off.
                log.debug("COAST key pressed");
                // Set coasting TRUE, if COAST key is pressed. Requires sufficient coasting sounds (chuff_index will rely on that).
                if (coast_notch == null) {
                    coast_notch = _parent.getNotch(1); // Because of initial send of throttle key, COAST function key could be "true"
                }
                if (is_true && coast_notch.coast_bufs.size() > 0) {
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
                }
            }

            // Set Accel/Decel off
            if (name.equals("BRAKE_KEY")) {
                log.debug("BRAKE_KEY pressed is {}", is_true);
                if (_parent.engine_started) {
                    if (is_true) {
                        if (_parent.brake_time == 0) {
                            acc_time = 0;
                            dec_time = 0;
                        } else {
                            dec_time = _parent.brake_time;
                        }
                        _parent.accdectime = dec_time;
                    } else {
                        setupAccDec();
                        _parent.accdectime = dec_time; // ist das noetig?
                    }
                }
            }
            // Other throttle function keys may follow ...
        }

        public void startEngine() {
            _sound.unqueueBuffers();
            log.debug("thread: start engine ...");
            coast_notch  = _parent.getNotch(1); // Coast sounds are bound to notch 1
            helper_notch = _parent.getNotch(1); // Helper buffers are bound to notch 1
            _notch = _parent.getNotch(1); // Initial value
            _parent.engine_pane.setThrottle(1); // Set EnginePane (DieselPane) notch
            _sound.setReferenceDistance(_parent.engine_rd);
            setRpm(0);
            setRpmNominal(0);
            setupAccDec(); // Setup acceleration and deceleration factors
            helper_index = -1; // Prepare helper buffer start. Index will be incremented before first use
            setWait(0);
            startBoilingSound();
            startIdling();
            _parent.initAccDecTimer();
        }

        public void stopEngine() {
            log.debug("thread: stop engine ...");
            if (is_looping) {
                is_looping = false; // Stop the loop player
            }
            is_dying = true;
            stopBraking();
            stopAutoCoasting();
            stopBoilingSound();
            stopIdling();
            _throttle = 0.0f; // Clear it, just in case the engine was stopped at speed > 0
        }

        private void setupAccDec() {
            // Momentum
            int topspeed_rpm = (int) Math.round(_top_speed * 1056 / (Math.PI * _driver_diameter_float)); // Top-speed in RPM
            int time_factor = 896; // NMRA value in ms
            acc_time = time_factor * _parent.accel_rate / topspeed_rpm; // NMRA CV#3
            dec_time = time_factor * _parent.decel_rate / topspeed_rpm; // NMRA CV#4
            _parent.accdectime = acc_time;
            log.debug("setup acc time: {}, dec time: {}", acc_time, dec_time);
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
                            AudioBuffer b;
                            if (is_key_coasting || is_auto_coasting) {
                                // Take the coasting sound. Yes, use same index as for chuffs
                                b = coast_notch.coast_bufs.get(incChuffIndex());
                            } else {
                                // Take the standard chuff sound
                                b = _notch.chuff_bufs.get(incChuffIndex());
                            }
                            setSound(b); // Queue the sound and, if necessary, a filler sound
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
                is_running = false;
                return;
                // probably should do something. Not sure what
            }
        }

        private void changeNotch() {
            int new_notch = _notch.getNotch();
            log.debug("changing notch ... rpm: {}, notch: {}, chuff index: {}", 
                    getRpm(), _notch.getNotch(), chuffIndex());
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
            if ((getRpm() >= _parent.getNotch(1).getMinLimit()) && (!_notch.isInLimits(getRpm()))) {
                log.debug("Notch change! Notch: {}, RPM nominal: {}, RPM actual: {}", _notch.getNotch(), getRpmNominal(), getRpm());
                changeNotch();
            }
        }

        private void checkState() {
            if (is_looping) {
                if (getRpm() < _parent.getNotch(1).getMinLimit()) {
                    is_looping = false; // Stop the loop player
                    setWait(0);
                    log.debug("change from chuff or coast to idle.");
                    stopAutoCoasting(); // Automatic coasting is stopped here
                    stopBraking(); 
                    startIdling();
                }
            } else {
                if (_parent.engine_started && (getRpm() >= _parent.getNotch(1).getMinLimit())) {
                    stopIdling();
                    // Now prepare to start the chuff sound (or coasting sound)
                    _notch = _parent.getNotch(1); // Initial notch value
                    chuff_index = -1; // Index will be incremented before first usage
                    count_pre_arrival = 1;
                    is_looping = true; // Start the loop player
                }
            }

            if (getRpm() > 62) {
                queue_limit = 3; // Allow more buffers to be queued at higher speed
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
            waitFiller = wait;
        }

        private int getWait() {
            return waitFiller;
        }

        // Place next two methods here (other than in Diesel3Sound)
        // I want the chuffs in 1-2-3-4 series regardless the notch
        private int chuffIndex() {
            return chuff_index;
        }

        private int incChuffIndex() {
            chuff_index++;
            // Correct for wrap.
            if (chuff_index >= (_num_cylinders * 2)) {
                chuff_index = 0;
            }
            log.debug("new chuff index: {}", chuffIndex());
            return chuff_index;
        }

        private int incHelperIndex() {
            helper_index++;
            // Correct for wrap.
            if (helper_index >= helper_notch.bufs_helper.size()) {
                helper_index = 0;
            }
            return helper_index;
        }

        private int calcRPM(float t) {
            // speed = % of topspeed (mph)
            // RPM = speed * ((inches/mile) / (minutes/hour)) / (pi * driver_diameter_float)
            return (int) Math.round(_parent.speedCurve(t) * _top_speed * 1056 / (Math.PI * _driver_diameter_float));
        }

        private int calcChuffInterval(int revpm) {
            //  chuff interval will be calculated based on revolutions per minute (revpm)
            //  note: interval time includes the sound duration!
            //  chuffInterval = time in msec per revolution of the driver wheel: 
            //      60,000 msec / revpm / number of cylinders / 2 (because cylinders are double-acting)
            return (int) Math.round(60000.0 / revpm / _num_cylinders / 2.0);
        }

        private void setSound(AudioBuffer b) {
            int interval = calcChuffInterval(getRpm()); // Time in msec from chuff start up to begin of next chuff
            int sbl = (int)SoundBite.calcLength(b); // Length of WAV file in msec
            // Look if a filler is pending
            if (waitForFiller) { 
                // Filler waiting time is over. Go to queue the filler sound now
                //  Only do this if we are in the same notch
                if (wait_notch == _notch.getNotch()) {
                    setFiller(sbl_fill);
                    log.debug("wait filler set");
                }
                waitForFiller = false; // Done.
            }

            if (interval >= sbl) {
                // Regular queueing. Whole sound clip goes to the queue. Low notches
                _sound.queueBuffer(b);
                log.debug("chuff or coast buffer queued. Interval: {}", interval);
                setWait((sbl - SLEEP_INTERVAL * 4) / SLEEP_INTERVAL);
                if (getWait() < 3) {
                    setWait(0);
                } else {
                    sbl_fill = sbl; // Base for filler length calculation
                    waitForFiller = true;
                }
            } else {
                // Need to cut the SoundBite to new length of interval
                // To avoid queue underrun, interval should be a bit longer than SLEEP_INTERVAL
                // SLEEP_INTERVAL + 10 should be lower than interval of max_rpm (e.g. max_rpm=214 -> interval=70)
                if (interval > (SLEEP_INTERVAL + 10)) {
                    log.debug("need to cut sound clip from {} to length {}", (int)SoundBite.calcLength(b), interval); 
                    setWait((interval - SLEEP_INTERVAL * 8) / SLEEP_INTERVAL);
                    if (getWait() < 4) {
                        setWait(0);
                    }
                    // Take <interval> ms of the buffer. Regard sample size
                    int bbufcount = b.getFrameSize() * (interval * b.getFrequency() / 1000);
                    // Empty buffer (bound to the coast notch, notch = 1)
                    AudioBuffer buf = helper_notch.bufs_helper.get(incHelperIndex());
                    byte[] bbytes = new byte[bbufcount];
                    ByteBuffer data;
                    if (is_key_coasting || is_auto_coasting) {
                        // Take coasting sound (WAV data)
                        data = coast_notch.coast_bufs_data.get(chuffIndex());
                    } else {
                        // Take chuff sound (WAV data)
                        data = _notch.chuff_bufs_data.get(chuffIndex());
                    }
                    data.get(bbytes); // Same as: data.get(bbytes, 0, bbufcount);
                    data.rewind();
                    ByteBuffer bbuf = ByteBuffer.allocateDirect(bbufcount); // Target
                    bbuf.order(data.order()); // Set new buffer's byte order to match source buffer
                    bbuf.put(bbytes); // Same as: bbuf.put(bbytes, 0, bbufcount);
                    bbuf.rewind();
                    buf.loadBuffer(bbuf, _notch.getBufferFmt(), b.getFrequency());
                    _sound.queueBuffer(buf);
                    log.debug("cut buffer queued. Length: {}", (int)SoundBite.calcLength(buf));
                    // No filler needed here.
                }
            }
        }

        private void setFiller(int lenx) {
            // Fills time after a chuff up to the next chuff with a sound provided by the VSD file
            // Since the filler can be a small amount of time, it might be queued several times
            AudioBuffer fill_buf;
            if (is_key_coasting || is_auto_coasting) {
                fill_buf = coast_notch.getCoastFillerBuffer();
            } else {
                fill_buf = _notch.getNotchFillerBuffer();
            }
            if (fill_buf != null) {
                int filler_length = (int)SoundBite.calcLength(fill_buf);
                int interv_wo_chuff = calcChuffInterval(getRpm()) - lenx;
                log.debug("filler length: {}, sound clip length: {}, interval: {}", filler_length, lenx, calcChuffInterval(getRpm()));
                int im = interv_wo_chuff / filler_length; // How many fill_buf do we need?
                int imrest = interv_wo_chuff - (im * filler_length); // Calculate rest
                log.debug("interval without sound clip: {}, #buffers needed: {}, rest: {}", interv_wo_chuff, im, imrest);
                int k = 0;
                for (int i = 0; i < im; i++) {
                    _sound.queueBuffer(fill_buf);
                    k++;
                }
                log.debug("{} new buffers queued. Total buffers queued now: {}", 
                        k, _sound.getSource().numQueuedBuffers());
                // Create a buffer to queue rest of the filling time. Ignore small sound bites
                if (imrest > (SLEEP_INTERVAL + 10)) {
                    setWait((imrest - SLEEP_INTERVAL * 4) / SLEEP_INTERVAL);
                    if (getWait() < 3) {
                        setWait(0);
                    }
                    int bbufcount = fill_buf.getFrameSize() * (imrest * fill_buf.getFrequency() / 1000);
                    log.debug("chuff_index: {}", chuffIndex());
                    // Empty buffer (bound to notch = 1)
                    AudioBuffer buf  = helper_notch.bufs_helper.get(incHelperIndex());
                    byte[] bbytes = new byte[bbufcount];
                    ByteBuffer dataf;
                    if (is_key_coasting || is_auto_coasting) {
                        dataf = coast_notch.getCoastFillerData();
                    } else {
                        dataf = _notch.getNotchFillerData();
                    }
                    dataf.get(bbytes); // Same as: data.get(bbytes, 0, bbufcount);
                    dataf.rewind();
                    ByteBuffer bbuf = ByteBuffer.allocate(bbufcount);
                    bbuf.order(dataf.order()); // Set new buffer's byte order to match source buffer
                    bbuf.put(bbytes); // Same as: bbuf.put(bbytes, 0, bbufcount);
                    log.debug("bbuf after put: {}, order: {}", bbuf, bbuf.order());
                    bbuf.rewind();
                    buf.loadBuffer(bbuf, _notch.getBufferFmt(), fill_buf.getFrequency());
                    _sound.queueBuffer(buf);
                    log.debug("filler rest buffer queued. Length: {}", (int)SoundBite.calcLength(buf));
                } else {
                    log.debug("no filler rest buffer queued.");
                }
            } else {
                log.warn("filler buffer missing.");
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
