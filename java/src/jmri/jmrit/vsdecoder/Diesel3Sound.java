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
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.Timer;
import jmri.Audio;
import jmri.AudioException;
import jmri.AudioManager;
import jmri.jmrit.audio.AudioBuffer;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Usage:
// EngineSound() : constructor
// play() : plays short horn pop
// loop() : starts extended sustain horn
// stop() : ends extended sustain horn (plays end sound)
// Suppressing "unused" warnings throughout. There are a dozen 
// methods, ctors and values that aren't used and don't have
// outside access.
@SuppressWarnings("unused")
class Diesel3Sound extends EngineSound {

    // Engine Sounds
    HashMap<Integer, D3Notch> notch_sounds;
    SoundBite _sound;
    String _soundName;
    AudioBuffer start_buffer;
    AudioBuffer stop_buffer;
    AudioBuffer transition_buf;

    // Common variables
    Float throttle_setting; // used for handling speed changes
    EnginePane engine_pane;

    int current_notch = 1;
    boolean changing_speed = false;
    boolean is_looping = false;
    D3LoopThread _loopThread = null;

    Timer t;

    public Diesel3Sound(String name) {
        super(name);
        //_loopThread = new D3LoopThread(this);
        //_loopThread.start();
        log.debug("New Diesel3Sound name(param) = " + name + " name(val) " + this.getName());
    }

    private void startThread() {
        _loopThread = new D3LoopThread(this, notch_sounds.get(current_notch), _soundName, true);
        log.debug("Loop Thread Started.  Sound name = " + _soundName);
    }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    @Override
    public void play() {
        this.loop();
    }

    // Note:  Play and Loop do the same thing, since all of the notch sounds are set to loop.
    @Override

    public void loop() {
        if (notch_sounds.containsKey(current_notch) && (engine_started || auto_start_engine)) {
            // Really, nothing to do here.  The engine should be started/stopped through
            // the startEngine() and stopEngine() calls.  Maybe should clean that up or have
            // play() == loop() == startEngine() and stop == stopEngine() some time.
            if ((_loopThread != null) && (_loopThread.isRunning())) {
                _loopThread.setRunning(true);
            }
        }
    }

    @Override
    public void stop() {
        // Stop the loop thread, in case it's running
        if (_loopThread != null) {
            _loopThread.setRunning(false);
        }
        is_looping = false;
    }

    @Override
    public void handleSpeedChange(Float s, EnginePane e) {
        //log.debug("Handling SpeedSetting event. speed = " + s);
        throttle_setting = s;
        if (!changing_speed) {
            changeSpeed(s, e);
        }
    }

    // Responds to "CHANGE" trigger
    @Override
    public void changeThrottle(float s) {
        // This is all we have to do.  The loop thread will handle everything else.
        if (_loopThread != null) {
            _loopThread.setThrottle(s);
        }
    }

    protected void changeSpeed(Float s, EnginePane e) {
        engine_pane = e; // this should probably be cleaned up.  It's here for the recursion.
        changeThrottle(s);
    }

    public D3Notch getNotch(int n) {
        return (notch_sounds.get(n));
    }

    @Override
    public void startEngine() {
        log.debug("startEngine.  ID = " + this.getName());
        //_loopThread = new D3LoopThread(this, notch_sounds.get(current_notch), _soundName, true);
        _loopThread.startEngine(start_buffer);
    }

    @Override
    public void stopEngine() {
        log.debug("stopEngine.  ID = " + this.getName());
        if (_loopThread != null) {
            _loopThread.stopEngine(stop_buffer);
        }
    }

    @Override
    public void shutdown() {
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

    protected Timer newTimer(long time, boolean repeat, ActionListener al) {
        time = Math.max(1, time);  // make sure the time is > zero
        t = new Timer((int) time, al);
        t.setInitialDelay((int) time);
        t.setRepeats(repeat);
        return (t);
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
        D3Notch sb;

        // Handle the common stuff.
        super.setXml(e, vf);

        //log.debug("Diesel EngineSound: " + e.getAttribute("name").getValue());
        _soundName = this.getName() + ":LoopSound";
        log.debug("get name: " + this.getName() + " soundName " + _soundName + " name: " + name);
        notch_sounds = new HashMap<Integer, D3Notch>();
        String in = e.getChildText("idle-notch");
        Integer idle_notch = null;
        if (in != null) {
            idle_notch = Integer.parseInt(in);
        } else {
            // leave idle_notch null for now. We'll use it at the end to trigger a "grandfathering" action
            log.warn("No Idle Notch Specified!");
        }

        // Get the notch sounds
        Iterator<Element> itr = (e.getChildren("notch-sound")).iterator();
        int i = 0;
        while (itr.hasNext()) {
            el = itr.next();
            sb = new D3Notch();
            int nn = Integer.parseInt(el.getChildText("notch"));
            sb.setNotch(nn);
            if ((idle_notch != null) && (nn == idle_notch)) {
                sb.setIdleNotch(true);
                log.debug("This Notch (" + nn + ") is Idle.");
            }
            List<Element> elist = el.getChildren("file");
            int j = 0;
            for (Element fe : elist) {
                fn = fe.getText();
                //AudioBuffer b = D3Notch.getBuffer(vf, fn, "Engine_n" + i + "_" + j, "Engine_" + i + "_" + j);
                //log.debug("Buffer created: " + b + " name: " + b.getSystemName());
                //sb.addLoopBuffer(b);
                List<AudioBuffer> l = D3Notch.getBufferList(vf, fn, "n" + i + "_" + j, i + "_" + j);
                log.debug("Buffers Created: ");
                for (AudioBuffer b : l) {
                    log.debug("\tSubBuffer: " + b.getSystemName());
                }
                sb.addLoopBuffers(l);
                j++;
            }
            //log.debug("Notch: " + nn + " File: " + fn);

            // Gain is broken, for the moment.  Buffers don't have gain. Sources do.
            //_sound.setGain(setXMLGain(el));
            //_sound.setGain(default_gain);
            sb.setNextNotch(el.getChildText("next-notch"));
            sb.setPrevNotch(el.getChildText("prev-notch"));
            sb.setAccelLimit(el.getChildText("accel-limit"));
            sb.setDecelLimit(el.getChildText("decel-limit"));
            if (el.getChildText("accel-file") != null) {
                sb.setAccelBuffer(D3Notch.getBuffer(vf, el.getChildText("accel-file"), name + "_na" + i, "_na" + i));
            } else {
                sb.setAccelBuffer(null);
            }
            if (el.getChildText("decel-file") != null) {
                sb.setDecelBuffer(D3Notch.getBuffer(vf, el.getChildText("decel-file"), name + "_nd" + i, "_nd" + i));
            } else {
                sb.setDecelBuffer(null);
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
            start_buffer = D3Notch.getBuffer(vf, fn, name + "_start", name + "_Start");
        }
        el = e.getChild("shutdown-sound");
        if (el != null) {
            fn = el.getChild("file").getValue();
            //log.debug("Shutdown sound: " + fn);
            stop_buffer = D3Notch.getBuffer(vf, fn, name + "_shutdown", name + "_Shutdown");
        }

        // Handle "grandfathering the idle notch indication
        // If the VSD designer did not explicitly designate an idle notch...
        // Find the Notch with the lowest notch number, and make it the idle notch.
        // If there's a tie, this will take the first value, but the notches /should/
        // all have unique notch numbers.
        if (idle_notch == null) {
            D3Notch min_notch = null;
            // No, this is not a terribly efficient "min" operation.  But that's OK.
            for (D3Notch n : notch_sounds.values()) {
                if ((min_notch == null) || (n.getNotch() < min_notch.getNotch())) {
                    min_notch = n;
                }
            }
            log.debug("No Idle Notch Specified.  Choosing Notch (" + (min_notch != null ? min_notch.getNotch() : "min_notch not set") + ") to be the Idle Notch.");
            if (min_notch != null) {
                min_notch.setIdleNotch(true);
            } else {
                log.warn("Could not set idle notch because min_notch was still null");
            }
        }

        // Kick-start the loop thread.
        this.startThread();
    }

    private static final Logger log = LoggerFactory.getLogger(Diesel3Sound.class);

    private static class D3Notch {

        private AudioBuffer accel_buf;
        private AudioBuffer decel_buf;
        private int my_notch, next_notch, prev_notch;
        private float accel_limit, decel_limit;
        private int loop_index;
        private List<AudioBuffer> loop_bufs = new ArrayList<AudioBuffer>();
        private Boolean is_idle;

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
            if (loop != null) {
                loop_bufs = loop;
            }
            loop_index = 0;
        }

        public int getNextNotch() {
            return (next_notch);
        }

        public int getPrevNotch() {
            return (prev_notch);
        }

        public int getNotch() {
            return (my_notch);
        }

        public AudioBuffer getAccelBuffer() {
            return (accel_buf);
        }

        public AudioBuffer getDecelBuffer() {
            return (decel_buf);
        }

        public float getAccelLimit() {
            return (accel_limit);
        }

        public float getDecelLimit() {
            return (decel_limit);
        }

        public Boolean isInLimits(float val) {
            return ((val >= decel_limit) && (val <= accel_limit));
        }

        public List<AudioBuffer> getLoopBuffers() {
            return (loop_bufs);
        }

        public AudioBuffer getLoopBuffer(int idx) {
            return (loop_bufs.get(idx));
        }

        public long getLoopBufferLength(int idx) {
            return (SoundBite.calcLength(loop_bufs.get(idx)));
        }

        public Boolean isIdleNotch() {
            return (is_idle);
        }

        public void setNextNotch(int n) {
            next_notch = n;
        }

        public void setNextNotch(String s) {
            next_notch = setIntegerFromString(s);
        }

        public void setPrevNotch(int p) {
            prev_notch = p;
        }

        public void setPrevNotch(String s) {
            prev_notch = setIntegerFromString(s);
        }

        public void setAccelLimit(float l) {
            accel_limit = l;
        }

        public void setAccelLimit(String s) {
            accel_limit = setFloatFromString(s);
        }

        public void setDecelLimit(float l) {
            decel_limit = l;
        }

        public void setDecelLimit(String s) {
            decel_limit = setFloatFromString(s);
        }

        public void setNotch(int n) {
            my_notch = n;
        }

        public void setAccelBuffer(AudioBuffer b) {
            accel_buf = b;
        }

        public void setDecelBuffer(AudioBuffer b) {
            decel_buf = b;
        }

        public void addLoopBuffer(AudioBuffer b) {
            loop_bufs.add(b);
        }

        public void addLoopBuffers(List<AudioBuffer> l) {
            loop_bufs.addAll(l);
        }

        public void setLoopBuffers(List<AudioBuffer> l) {
            loop_bufs = l;
        }

        public void clearLoopBuffers() {
            loop_bufs.clear();
        }

        public AudioBuffer nextLoopBuffer() {
            return (loop_bufs.get(incLoopIndex()));
        }

        public void setIdleNotch(Boolean i) {
            is_idle = i;
        }

        public int loopIndex() {
            return (loop_index);
        }

        public int incLoopIndex() {
            // Increment
            loop_index++;
            // Correct for wrap.
            if (loop_index >= loop_bufs.size()) {
                loop_index = 0;
            }

            return (loop_index);
        }

        private int setIntegerFromString(String s) {
            if (s == null) {
                return (0);
            }
            try {
                int n = Integer.parseInt(s);
                return (n);
            } catch (NumberFormatException e) {
                log.debug("Invalid integer: " + s);
                return (0);
            }
        }

        private float setFloatFromString(String s) {
            if (s == null) {
                return (0.0f);
            }
            try {
                float f = Float.parseFloat(s) / 100.0f;
                return (f);
            } catch (NumberFormatException e) {
                log.debug("Invalid float: " + s);
                return (0.0f);
            }
        }

        static public List<AudioBuffer> getBufferList(VSDFile vf, String filename, String sname, String uname) {
            List<AudioBuffer> buflist = null;
            java.io.InputStream ins = vf.getInputStream(filename);
            if (ins != null) {
                //buflist = AudioUtil.getSplitInputStream(VSDSound.BufSysNamePrefix+filename, ins, 250, 100);
                buflist = AudioUtil.getAudioBufferList(VSDSound.BufSysNamePrefix + filename, ins, 250, 100);
            } else {
                log.debug("Input Stream failed");
                return (null);
            }
            return (buflist);
        }

        static public AudioBuffer getBuffer(VSDFile vf, String filename, String sname, String uname) {
            AudioBuffer buf = null;
            AudioManager am = jmri.InstanceManager.getDefault(jmri.AudioManager.class);
            try {
                buf = (AudioBuffer) am.provideAudio(VSDSound.BufSysNamePrefix + sname);
                buf.setUserName(VSDSound.BufUserNamePrefix + uname);
                java.io.InputStream ins = vf.getInputStream(filename);
                if (ins != null) {
                    buf.setInputStream(ins);
                } else {
                    log.debug("Input Stream failed");
                    return (null);
                }
            } catch (AudioException | IllegalArgumentException ex) {
                log.error("Problem creating SoundBite: " + ex);
                return (null);
            }

            log.debug("Buffer created: " + buf + " name: " + buf.getSystemName());
            return (buf);
        }

        private static final Logger log = LoggerFactory.getLogger(D3Notch.class);
    }

    private static class D3LoopThread extends Thread {

        private boolean is_running = false;
        private boolean is_looping = false;
        private boolean is_dying = false;
        Diesel3Sound _parent;
        D3Notch _notch;
        SoundBite _sound;
        float _throttle;

        public static final int SLEEP_INTERVAL = 50;

        public D3LoopThread(Diesel3Sound p) {
            super();
            is_running = false;
            is_looping = false;
            is_dying = false;
            _notch = null;
            _sound = null;
            _parent = p;
            _throttle = 0.0f;
        }

        public D3LoopThread(Diesel3Sound d, D3Notch n, String s, boolean r) {
            super();
            is_running = r;
            is_looping = false;
            is_dying = false;
            _notch = n;
            _sound = new SoundBite(s);
            _sound.setGain(0.8f);
            _parent = d;
            _throttle = 0.0f;
            if (r) {
                this.start();
            }
        }

        public void setNotch(D3Notch n) {
            _notch = n;
        }

        public D3Notch getNotch() {
            return (_notch);
        }

        public void setSound(SoundBite s) {
            _sound = s;
        }

        public void setRunning(boolean r) {
            is_running = r;
        }

        public boolean isRunning() {
            return (is_running);
        }

        public void setThrottle(float t) {
            _throttle = t;
            log.debug("Throttle set: " + _throttle);
        }

        public void startEngine(AudioBuffer start_buf) {
            _sound.unqueueBuffers();
            // Adjust the current notch to match the throttle setting
            log.debug("Notch = " + _notch.getNotch() + " prev = " + _notch.getPrevNotch() + " next = " + _notch.getNextNotch());
            
            // If we're out of whack, find the right notch for the current throttle setting.
            while (!_notch.isInLimits(_throttle)) {
                if (_throttle > _notch.getAccelLimit()) {
                    _notch = _parent.getNotch(_notch.getNextNotch());
                } else if (_throttle < _notch.getDecelLimit()) {
                    _notch = _parent.getNotch(_notch.getPrevNotch());
                }
            }
            
            // Only queue the start buffer if we know we're in the idle notch.
            // This is indicated by prevNotch == self.
            if (_notch.isIdleNotch()) {
                _sound.queueBuffer(start_buf);
            } else {
                _sound.queueBuffer(_notch.nextLoopBuffer());
            }
            // Follow up with another loop buffer.
            _sound.queueBuffer(_notch.nextLoopBuffer());
            is_looping = true;
            if (!_sound.isPlaying()) {
                _sound.play();
            }
        }

        public void stopEngine(AudioBuffer stop_buf) {
            is_looping = false; // stop the loop player
            is_dying = true;
            _sound.queueBuffer(stop_buf);
            if (!_sound.isPlaying()) {
                _sound.play();
            }
        }

        @Override
        public void run() {
            try {
                while (is_running) {
                    if (is_looping) {
                        if (_sound.getSource().numProcessedBuffers() > 0) {
                            _sound.unqueueBuffers();
                        }
                        //log.debug("D3Loop"+ _sound.getName() + "Run loop. Buffers: " + _sound.getSource().numQueuedBuffers());
                        if (!_notch.isInLimits(_throttle)) {
                            //log.debug("Notch Change! throttle = " + _throttle);
                            changeNotch();
                        }
                        if (_sound.getSource().numQueuedBuffers() < 2) {
                            //log.debug("D3Loop"+ _sound.getName() + "Buffer count low (" + _sound.getSource().numQueuedBuffers() + ").  Adding buffer. Throttle = " + _throttle);
                            AudioBuffer b = _notch.nextLoopBuffer();
                            //log.debug("D3Loop"+ _sound.getName() + "Loop: Adding buffer " + b.getSystemName());
                            _sound.queueBuffer(b);
                        }
                        if (_sound.getSource().getState() != Audio.STATE_PLAYING) {
                            _sound.play();
                            log.info("loop sound re-started. Possibly queue underrun or audio shutdown");
                        }
                    } else {
                        // Quietly wait for the sound to get turned on again
                        // Once we've stopped playing, kill the thread.
                        if (_sound.getSource().numProcessedBuffers() > 0) {
                            _sound.unqueueBuffers();
                        }
                        if (is_dying && (_sound.getSource().getState() != Audio.STATE_PLAYING)) {
                            _sound.stop(); // good reason to get rid of SoundBite.is_playing variable!
                            //return;
                        }
                    }
                    sleep(SLEEP_INTERVAL);
                }
                // Note: if (is_running == false) we'll exit the endless while and the Thread will die.
                return;
            } catch (InterruptedException ie) {
                //_notch = _parent.getCurrentNotch();
                is_running = false;
                return;
                // probably should do something. Not sure what.
            }
        }

        private void changeNotch() {
            AudioBuffer transition_buf = null;
            int new_notch = _notch.getNotch();

            log.debug("D3Thread Change Throttle: " + _throttle + " Accel Limit = " + _notch.getAccelLimit() + " Decel Limit = " + _notch.getDecelLimit());
            if (_throttle < 0) {
                // DO something to shut down
                _sound.stop();
                is_running = false;
                return;
            }
            if (_throttle > _notch.getAccelLimit()) {
                // Too fast. Need to go to next notch up.
                transition_buf = _notch.getAccelBuffer();
                new_notch = _notch.getNextNotch();
                //log.debug("Change up. notch=" + new_notch);
            } else if (_throttle < _notch.getDecelLimit()) {
                // Too slow.  Need to go to next notch down.
                transition_buf = _notch.getDecelBuffer();
                new_notch = _notch.getPrevNotch();
                log.debug("Change down. notch=" + new_notch);
            }
            // Now, regardless of whether we're going up or down, set the timer,
            // fade the current sound, and move on.
            if (transition_buf == null) {
                // No transition sound to play.  Skip the timer bit.
                // Recurse directly to try the next notch.
                _notch = _parent.getNotch(new_notch);
                log.debug("No transition sound defined.");
                return;
            } else {
                // Stop the loop if it's running
                //this.stopLoop();
                // queue up the transition sound buffer.
                _notch = _parent.getNotch(new_notch);
                _sound.queueBuffer(transition_buf);
                try {
                    sleep(SoundBite.calcLength(transition_buf) - 50);
                } catch (InterruptedException e) {
                }
            }
            return;
        }

        public void mute(boolean m) {
            _sound.mute(m);
        }

        public void setVolume(float v) {
            _sound.setVolume(v);
        }

        public void setPosition(PhysicalLocation p) {
            _sound.setPosition(p);
        }

        public void kill() {
            is_running = false;
            _notch = null;
            _sound = null;
        }
        private static final Logger log = LoggerFactory.getLogger(D3LoopThread.class);

    }
}
