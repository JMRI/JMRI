package jmri.jmrit.vsdecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import jmri.Audio;
import jmri.AudioException;
import jmri.jmrit.audio.AudioBuffer;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diesel Sound version 3.
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
 * @author Klaus Killinger Copyright (C) 2018-2020
 */
class Diesel3Sound extends EngineSound {

    // Engine Sounds
    private HashMap<Integer, D3Notch> notch_sounds;
    private String _soundName;

    private AudioBuffer start_buffer;
    private AudioBuffer stop_buffer;
    private Integer idle_notch;
    int top_speed;

    // Common variables
    private int current_notch = 1;
    private D3LoopThread _loopThread = null;

    public Diesel3Sound(String name) {
        super(name);
        log.debug("New Diesel3Sound name(param): {}, name(val): {}", name, this.getName());
    }

    private void startThread() {
        _loopThread = new D3LoopThread(this, notch_sounds.get(current_notch), _soundName, true);
        log.debug("Loop Thread Started.  Sound name: {}", _soundName);
    }

    // Responds to "CHANGE" trigger
    @Override
    public void changeThrottle(float s) {
        // This is all we have to do.  The loop thread will handle everything else.
        if (_loopThread != null) {
            _loopThread.setThrottle(s);
        }
    }

    // Responds to throttle loco direction key (see EngineSound.java and EngineSoundEvent.java)
    @Override
    public void changeLocoDirection(int dirfn) {
        log.debug("loco IsForward is {}", dirfn);
        if (_loopThread != null) {
            _loopThread.getLocoDirection(dirfn);
        }
    }

    private D3Notch getNotch(int n) {
        return notch_sounds.get(n);
    }

    @Override
    public void startEngine() {
        log.debug("startEngine.  ID: {}", this.getName());
        if (_loopThread != null) {
            _loopThread.startEngine(start_buffer);
        }
    }

    @Override
    public void stopEngine() {
        log.debug("stopEngine.  ID: {}", this.getName());
        if (_loopThread != null) {
            _loopThread.stopEngine(stop_buffer);
        }
    }

    @Override
    public void shutdown() {
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
        Element el;
        String fn;
        String in;
        D3Notch sb;

        // Handle the common stuff.
        super.setXml(e, vf);

        _soundName = this.getName() + ":LoopSound";
        log.debug("get name: {}, soundName: {}, name: {}", this.getName(), _soundName, name);

        // Optional value
        in = e.getChildText("top-speed");
        if (in != null) {
            top_speed = Integer.parseInt(in);
        } else {
            top_speed = 0; // default
        }
        log.debug("top speed forward: {} MPH", top_speed);

        notch_sounds = new HashMap<>();
        in = e.getChildText("idle-notch");
        idle_notch = null;
        if (in != null) {
            idle_notch = Integer.parseInt(in);
            log.debug("Notch {} is Idle.", idle_notch);
        } else {
            // leave idle_notch null for now. We'll use it at the end to trigger a "grandfathering" action
            log.warn("No Idle Notch Specified!");
        }

        is_auto_start = setXMLAutoStart(e);
        log.debug("config auto-start: {}", is_auto_start);

        // Optional value
        // Allows to adjust OpenAL attenuation
        // Sounds with distance to listener position lower than reference-distance will not have attenuation
        engine_rd = setXMLEngineReferenceDistance(e); // Handle engine reference distance
        log.debug("engine-sound referenceDistance: {}", engine_rd);

        // Optional value
        // Allows to adjust the engine gain
        in = e.getChildText("engine-gain");
        if ((in != null) && (!in.isEmpty())) {
            engine_gain = Float.parseFloat(in);
        } else {
            engine_gain = default_gain;
        }
        log.debug("engine gain: {}", engine_gain);

        sleep_interval = setXMLSleepInterval(e); // Optional value
        log.debug("sleep interval: {}", sleep_interval);

        // Get the notch sounds
        Iterator<Element> itr = (e.getChildren("notch-sound")).iterator();
        int i = 0;
        while (itr.hasNext()) {
            el = itr.next();
            sb = new D3Notch();
            int nn = Integer.parseInt(el.getChildText("notch"));
            sb.setNotch(nn);
            sb.setIdleNotch(false);
            if ((idle_notch != null) && (nn == idle_notch)) {
                sb.setIdleNotch(true);
                log.debug("Notch {} set to Idle.", nn);
            }
            List<Element> elist = el.getChildren("file");
            for (Element fe : elist) {
                fn = fe.getText();
                List<AudioBuffer> l = D3Notch.getBufferList(vf, fn, name + "_n" + i);
                log.debug("Buffers Created from file {}:", fn);
                for (AudioBuffer b : l) {
                    log.debug("\tSubBuffer: {}, length: {} ms", b.getSystemName(), SoundBite.calcLength(b));
                }
                sb.addLoopBuffers(l);
            }

            sb.setNextNotch(el.getChildText("next-notch"));
            sb.setPrevNotch(el.getChildText("prev-notch"));
            sb.setAccelLimit(el.getChildText("accel-limit"));
            sb.setDecelLimit(el.getChildText("decel-limit"));
            if (el.getChildText("accel-file") != null) {
                sb.setAccelBuffer(D3Notch.getBuffer(vf, el.getChildText("accel-file"), name + "_na" + i, name + "_na" + i));
            } else {
                sb.setAccelBuffer(null);
            }
            if (el.getChildText("decel-file") != null) {
                sb.setDecelBuffer(D3Notch.getBuffer(vf, el.getChildText("decel-file"), name + "_nd" + i, name + "_nd" + i));
            } else {
                sb.setDecelBuffer(null);
            }
            // Store in the list.
            notch_sounds.put(nn, sb);
            i++;
        }

        // Get the start and stop sounds (both sounds are optional)
        el = e.getChild("start-sound");
        if (el != null) {
            fn = el.getChild("file").getValue();
            start_buffer = D3Notch.getBuffer(vf, fn, name + "_start", name + "_Start");
            log.debug("Start sound: {}, buffer {} created, length: {}", fn, start_buffer, SoundBite.calcLength(start_buffer));
        }
        el = e.getChild("shutdown-sound");
        if (el != null) {
            fn = el.getChild("file").getValue();
            stop_buffer = D3Notch.getBuffer(vf, fn, name + "_shutdown", name + "_Shutdown");
            log.debug("Shutdown sound: {}, buffer {} created, length: {}", fn, stop_buffer, SoundBite.calcLength(stop_buffer));
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
            log.info("No Idle Notch Specified.  Choosing Notch {} to be the Idle Notch.", (min_notch != null ? min_notch.getNotch() : "min_notch not set"));
            if (min_notch != null) {
                min_notch.setIdleNotch(true);
                idle_notch = min_notch.getNotch();
            } else {
                log.warn("Could not set idle notch because min_notch was still null");
            }
        }

        // Kick-start the loop thread.
        this.startThread();

        // Check auto-start setting
        autoStartCheck();
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

        private D3Notch() {
            this(1, 1, 1, null, null, null);
        }

        private D3Notch(int notch, int next, int prev) {
            this(notch, next, prev, null, null, null);
        }

        private D3Notch(int notch, int next, int prev, AudioBuffer accel, AudioBuffer decel, List<AudioBuffer> loop) {
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

        private int getNextNotch() {
            return next_notch;
        }

        private int getPrevNotch() {
            return prev_notch;
        }

        private int getNotch() {
            return my_notch;
        }

        private AudioBuffer getAccelBuffer() {
            return accel_buf;
        }

        private AudioBuffer getDecelBuffer() {
            return decel_buf;
        }

        private float getAccelLimit() {
            return accel_limit;
        }

        private float getDecelLimit() {
            return decel_limit;
        }

        private Boolean isInLimits(float val) {
            return (val >= decel_limit) && (val <= accel_limit);
        }

        private Boolean isIdleNotch() {
            return is_idle;
        }

        private void setNextNotch(String s) {
            next_notch = setIntegerFromString(s);
        }

        private void setPrevNotch(String s) {
            prev_notch = setIntegerFromString(s);
        }

        private void setAccelLimit(String s) {
            accel_limit = setFloatFromString(s);
        }

        private void setDecelLimit(String s) {
            decel_limit = setFloatFromString(s);
        }

        private void setNotch(int n) {
            my_notch = n;
        }

        private void setAccelBuffer(AudioBuffer b) {
            accel_buf = b;
        }

        private void setDecelBuffer(AudioBuffer b) {
            decel_buf = b;
        }

        private void addLoopBuffers(List<AudioBuffer> l) {
            loop_bufs.addAll(l);
        }

        private AudioBuffer nextLoopBuffer() {
            return loop_bufs.get(incLoopIndex());
        }

        private void setIdleNotch(Boolean i) {
            is_idle = i;
        }

        private int incLoopIndex() {
            // Increment
            loop_index++;
            // Correct for wrap.
            if (loop_index >= loop_bufs.size()) {
                loop_index = 0;
            }
            return loop_index;
        }

        private int setIntegerFromString(String s) {
            if (s == null) {
                return 0;
            }
            try {
                int n = Integer.parseInt(s);
                return n;
            } catch (NumberFormatException e) {
                log.debug("Invalid integer: {}", s);
                return 0;
            }
        }

        private float setFloatFromString(String s) {
            if (s == null) {
                return 0.0f;
            }
            try {
                float f = Float.parseFloat(s) / 100.0f;
                return f;
            } catch (NumberFormatException e) {
                log.debug("Invalid float: {}", s);
                return 0.0f;
            }
        }

        static private List<AudioBuffer> getBufferList(VSDFile vf, String filename, String sname) {
            List<AudioBuffer> buflist = null;
            java.io.InputStream ins = vf.getInputStream(filename);
            if (ins != null) {
                buflist = AudioUtil.getAudioBufferList(VSDSound.BufSysNamePrefix + sname, ins, 250, 100);
            } else {
                log.debug("Input Stream failed");
                return null;
            }
            return buflist;
        }

        static private AudioBuffer getBuffer(VSDFile vf, String filename, String sname, String uname) {
            AudioBuffer buf = null;
            jmri.AudioManager am = jmri.InstanceManager.getDefault(jmri.AudioManager.class);
            try {
                buf = (AudioBuffer) am.provideAudio(VSDSound.BufSysNamePrefix + sname);
                buf.setUserName(VSDSound.BufUserNamePrefix + uname);
                java.io.InputStream ins = vf.getInputStream(filename);
                if (ins != null) {
                    buf.setInputStream(ins);
                } else {
                    log.debug("Input Stream failed");
                    return null;
                }
            } catch (AudioException | IllegalArgumentException ex) {
                log.error("Problem creating SoundBite", ex);
                return null;
            }

            log.debug("Buffer created: {}, name: {}", buf, buf.getSystemName());
            return buf;
        }

        private static final Logger log = LoggerFactory.getLogger(D3Notch.class);
    }

    private static class D3LoopThread extends Thread {

        private boolean is_running;
        private boolean is_looping;
        private boolean is_in_rampup_mode;
        private Diesel3Sound _parent;
        private D3Notch _notch;
        private SoundBite _sound;
        private float _throttle;
        private float rpm_dirfn;

        private D3LoopThread(Diesel3Sound d, D3Notch n, String s, boolean r) {
            super();
            is_running = r;
            is_looping = false;
            is_in_rampup_mode = false;
            _parent = d;
            _notch = n;
            _sound = new SoundBite(s);
            _sound.setGain(_parent.engine_gain);
            _throttle = 0.0f;
            rpm_dirfn = 0.0f;
            if (r) {
                this.start();
            }
        }

        private void setRunning(boolean r) {
            is_running = r;
        }

        private void setThrottle(float t) {
            if (_parent.isEngineStarted()) {
                if (t < 0.0f) {
                    t = 0.0f;
                    is_in_rampup_mode = false; // interrupt ramp-up
                }
                _throttle = t;
            }
            log.debug("Throttle set: {}", _throttle);
        }

        private void getLocoDirection(int d) {
            log.debug("loco direction: {}", d);

            // React to a change in direction to slow down,
            // then change direction, then ramp-up to the old speed
            if (_throttle > 0.0f && _parent.isEngineStarted() && !is_in_rampup_mode) {
                rpm_dirfn = _throttle; // save rpm for ramp-up
                log.debug("speed {} saved", rpm_dirfn);
                is_in_rampup_mode = true; // set a flag for the ramp-up
                _throttle = 0.0f;
                changeNotch(); // will slow down to 0
            }
        }

        public void startEngine(AudioBuffer start_buf) {
            _sound.unqueueBuffers();
            log.debug("thread: start engine ...");
            _parent.engine_pane.setThrottle(1); // Set EnginePane (DieselPane) notch
            // Adjust the current notch to match the throttle setting
            log.debug("Notch: {}, prev: {}, next: {}", _notch.getNotch(), _notch.getPrevNotch(), _notch.getNextNotch());

            _sound.setReferenceDistance(_parent.engine_rd);
            log.debug("set reference distance to {} for engine sound", _sound.getReferenceDistance());

            // If we're out of whack, find the right notch for the current throttle setting.
            while (!_notch.isInLimits(_throttle)) {
                if (_throttle > _notch.getAccelLimit()) {
                    _notch = _parent.getNotch(_notch.getNextNotch());
                } else if (_throttle < _notch.getDecelLimit()) {
                    _notch = _parent.getNotch(_notch.getPrevNotch());
                }
            }

            log.debug("sound: {}, engine pane: {}", _parent, _parent.engine_pane);
            if (_parent.engine_pane != null) {
                _parent.engine_pane.setButtonDelay(SoundBite.calcLength(start_buf));
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
            if (_sound.getSource().getState() != Audio.STATE_PLAYING) {
                _sound.play();
            }
        }

        public void stopEngine(AudioBuffer stop_buf) {
            log.debug("thread: stop engine ...");
            is_looping = false; // stop the loop player
            _throttle = 0.0f; // Clear it, just in case the engine was stopped at speed > 0
            if (_parent.engine_pane != null) {
                _parent.engine_pane.setButtonDelay(SoundBite.calcLength(stop_buf));
            }
            _sound.queueBuffer(stop_buf);
            if (_sound.getSource().getState() != Audio.STATE_PLAYING) {
                _sound.play();
            }
        }

        @Override
        public void run() {
            try {
                while (is_running) {
                    if (is_looping && AudioUtil.isAudioRunning()) {
                        if (_sound.getSource().numProcessedBuffers() > 0) {
                            _sound.unqueueBuffers();
                        }
                        //log.debug("D3Loop {} Run loop. Buffers: {}", _sound.getName(), _sound.getSource().numQueuedBuffers());
                        if (!_notch.isInLimits(_throttle)) {
                            //log.debug("Notch Change! throttle: {}", _throttle);
                            changeNotch();
                        }
                        if (_sound.getSource().numQueuedBuffers() < 2) {
                            //log.debug("D3Loop {} Buffer count low ({}).  Adding buffer. Throttle: {}", _sound.getName(), _sound.getSource().numQueuedBuffers(), _throttle);
                            AudioBuffer b = _notch.nextLoopBuffer();
                            //log.debug("D3Loop {} Loop: Adding buffer {}", _sound.getName(), b.getSystemName());
                            _sound.queueBuffer(b);
                        }
                        checkAudioState();
                    } else {
                        if (_sound.getSource().numProcessedBuffers() > 0) {
                            _sound.unqueueBuffers();
                        }
                    }
                    sleep(_parent.sleep_interval);
                    checkState();
                }
            } catch (InterruptedException ie) {
                log.error("execption", ie);
            }
        }

        private void checkAudioState() {
            if (_sound.getSource().getState() != Audio.STATE_PLAYING) {
                _sound.play();
                log.info("loop sound re-started");
            }
        }

        private void changeNotch() {
            AudioBuffer transition_buf = null;
            int new_notch = _notch.getNotch();

            log.debug("D3Thread Change Throttle: {}, Accel Limit: {}, Decel Limit: {}", _throttle, _notch.getAccelLimit(), _notch.getDecelLimit());
            if (_throttle > _notch.getAccelLimit()) {
                // Too fast. Need to go to next notch up.
                transition_buf = _notch.getAccelBuffer();
                new_notch = _notch.getNextNotch();
                //log.debug("Change up. notch: {}", new_notch);
            } else if (_throttle < _notch.getDecelLimit()) {
                // Too slow.  Need to go to next notch down.
                transition_buf = _notch.getDecelBuffer();
                new_notch = _notch.getPrevNotch();
                log.debug("Change down. notch: {}", new_notch);
            }
            _parent.engine_pane.setThrottle(new_notch); // Update EnginePane (DieselPane) notch
            // Now, regardless of whether we're going up or down, set the timer,
            // fade the current sound, and move on.
            if (transition_buf == null) {
                // No transition sound to play.  Skip the timer bit
                // Recurse directly to try the next notch
                _notch = _parent.getNotch(new_notch);
                log.debug("No transition sound defined.");
            } else {
                // queue up the transition sound buffer
                _notch = _parent.getNotch(new_notch);
                _sound.queueBuffer(transition_buf);
                if (SoundBite.calcLength(transition_buf) > 50) {
                    try {
                        sleep(SoundBite.calcLength(transition_buf) - 50);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        private void checkState() {
            // Handle a throttle forward or reverse change
            if (is_in_rampup_mode && _throttle == 0.0f && _notch.getNotch() == _parent.idle_notch) {
                log.debug("now ramp-up to speed {}", rpm_dirfn);
                is_in_rampup_mode = false;
                _throttle = rpm_dirfn;
            }
        }

        private void mute(boolean m) {
            _sound.mute(m);
        }

        private void setVolume(float v) {
            _sound.setVolume(v);
        }

        private void setPosition(PhysicalLocation p) {
            _sound.setPosition(p);
        }

        private static final Logger log = LoggerFactory.getLogger(D3LoopThread.class);

    }
}
