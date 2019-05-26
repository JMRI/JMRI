package jmri.jmrit.vsdecoder;

import jmri.AudioException;
import jmri.AudioManager;
import jmri.jmrit.audio.AudioBuffer;
import jmri.jmrit.audio.AudioSource;
import jmri.util.PhysicalLocation;
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
 * @author   Mark Underwood Copyright (C) 2011
 */
class SoundBite extends VSDSound {

    public static enum BufferMode {

        BOUND_MODE, QUEUE_MODE
    }

    String filename, system_name, user_name;
    AudioBuffer sound_buf;
    AudioSource sound_src;
    boolean initialized = false;
    boolean looped = false;
    int minloops;
    int maxloops;
    float rd;
    long length;
    BufferMode bufferMode;

    // Constructor for QUEUE_MODE.
    public SoundBite(String name) {
        super(name);
        system_name = name;
        user_name = name;
        bufferMode = BufferMode.QUEUE_MODE;
        initialized = init(null, bufferMode);
    }

    // Constructor for BOUND_MODE.
    public SoundBite(VSDFile vf, String filename, String sname, String uname) {
        super(uname);
        this.filename = filename;
        system_name = sname;
        user_name = uname;
        bufferMode = BufferMode.BOUND_MODE;
        initialized = init(vf, bufferMode);
    }

    public String getFileName() {
        return filename;
    }

    public String getSystemName() {
        return system_name;
    }

    public String getUserName() {
        return user_name;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public final boolean init(VSDFile vf, BufferMode mode) {
        AudioManager am = jmri.InstanceManager.getDefault(jmri.AudioManager.class);
        if (!initialized) {
            try {
                sound_src = (AudioSource) am.provideAudio(SrcSysNamePrefix + system_name);
                sound_src.setUserName(SrcUserNamePrefix + user_name);
                setLooped(false);
                if (mode == BufferMode.BOUND_MODE) {
                    sound_buf = (AudioBuffer) am.provideAudio(BufSysNamePrefix + system_name);
                    sound_buf.setUserName(BufUserNamePrefix + user_name);
                    if (vf == null) {
                        log.debug("No VSD File! Filename: {}", filename);
                        sound_buf.setURL(filename); // Path must be provided by caller.
                    } else {
                        java.io.InputStream ins = vf.getInputStream(filename);
                        if (ins != null) {
                            sound_buf.setInputStream(ins);
                        } else {
                            return false;
                        }
                    }
                    sound_src.setAssignedBuffer(sound_buf);
                    setLength();
                }
            } catch (AudioException | IllegalArgumentException ex) {
                log.warn("Problem creating SoundBite", ex);
            }
        }
        return true;
    }

    public void queueBuffer(AudioBuffer b) {
        if (bufferMode == BufferMode.QUEUE_MODE) {
            if (b == null) {
                log.debug("queueAudioBuffer with null buffer input");
                return;
            }
            if (sound_src == null) {
                log.debug("queueAudioBuffer with null sound_src");
                return;
            }
            log.debug("Queueing Buffer: {}", b.getSystemName());
            sound_src.queueBuffer(b);
        } else {
            log.warn("Attempted to Queue buffer to a Bound SoundBite.");
        }
    }

    public void unqueueBuffers() {
        if (bufferMode == BufferMode.QUEUE_MODE) {
            sound_src.unqueueBuffers();
        }
    }

    public int numQueuedBuffers() {
        if (bufferMode == BufferMode.QUEUE_MODE) {
            return sound_src.numQueuedBuffers();
        } else {
            return 0;
        }
    }

    // Direct access to the underlying source.  use with caution.
    public AudioSource getSource() {
        return sound_src;
    }

    // WARNING: This will go away when we go to shared buffers... or at least it will
    // have to do the name lookup on behalf of the caller...
    public AudioBuffer getBuffer() {
        return sound_buf;
    }

    // These can(?) be used to get the underlying AudioSource and AudioBuffer objects
    // from the DefaultAudioManager.
    public String getSourceSystemName() {
        return SrcSysNamePrefix + system_name;
    }

    public String getSourceUserName() {
        return SrcUserNamePrefix + user_name;
    }

    public String getBufferSystemName() {
        return BufSysNamePrefix + system_name;
    }

    public String getBufferUserName() {
        return BufUserNamePrefix + user_name;
    }

    public void setLooped(boolean loop, int minloops, int maxloops) {
        this.looped = loop;
        this.minloops = minloops;
        this.maxloops = maxloops;
        sound_src.setLooped(looped);
        sound_src.setMinLoops(minloops);
        sound_src.setMaxLoops(maxloops);
    }

    public void setLooped(boolean loop) {
        if (loop) {
            this.setLooped(true, AudioSource.LOOP_CONTINUOUS, AudioSource.LOOP_CONTINUOUS);
        } else {
            this.setLooped(false, AudioSource.LOOP_NONE, AudioSource.LOOP_NONE);
        }
    }

    public boolean isLooped() {
        return looped;
    }

    public int getFadeInTime() {
        return sound_src.getFadeIn();
    }

    public int getFadeOutTime() {
        return sound_src.getFadeOut();
    }

    public void setFadeInTime(int t) {
        sound_src.setFadeIn(t);
    }

    public void setFadeOutTime(int t) {
        sound_src.setFadeOut(t);
    }

    public void setFadeTimes(int in, int out) {
        sound_src.setFadeIn(in);
        sound_src.setFadeOut(out);
    }

    public float getReferenceDistance() {
        return sound_src.getReferenceDistance();
    }

    public void setReferenceDistance(float r) {
        this.rd = r;
        sound_src.setReferenceDistance(rd);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void mute(boolean m) {
        if (m) {
            volume = sound_src.getGain();
            sound_src.setGain(0);
        } else {
            sound_src.setGain(volume);
        }
    }

    @Override
    public void setVolume(float v) {
        volume = v * gain;
        sound_src.setGain(volume);
    }

    @Override
    public void play() {
        sound_src.play();
        is_playing = true;
    }

    @Override
    public void loop() {
        sound_src.play();
        is_playing = true;
    }

    @Override
    public void stop() {
        sound_src.stop();
        is_playing = false;
    }

    public void pause() {
        sound_src.pause();
        is_playing = false;
    }

    public void rewind() {
        sound_src.rewind();
    }

    @Override
    public void fadeOut() {
        // Skip the fade action if the fade out time is zero.
        if (sound_src.getFadeOut() == 0) {
            sound_src.stop();
        } else {
            sound_src.fadeOut();
        }
        is_playing = false;
    }

    @Override
    public void fadeIn() {
        // Skip the fade action if the fade in time is zero.
        if (sound_src.getFadeIn() == 0) {
            sound_src.play();
        } else {
            sound_src.fadeIn();
        }
        is_playing = true;
    }

    @Override
    public void setPosition(PhysicalLocation v) {
        super.setPosition(v);
        sound_src.setPosition(v);
    }

    public void setURL(String filename) {
        this.filename = filename;
        sound_buf.setURL(filename); // Path must be provided by caller.
    }

    public long getLength() {
        return length;
    }

    public int getLengthAsInt() {
        // Note:  this only works for positive lengths...
        // Timer only takes an int... cap the length at MAXINT
        if (length > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else { // small enough to safely cast.
            return (int) length;
        }
    }

    public void setLength(long p) {
        length = p;
    }

    public void setLength() {
        length = calcLength(this);
    }

    public static long calcLength(SoundBite s) {
        return calcLength(s.getBuffer());
    }

    public static long calcLength(AudioBuffer buf) {
        // Assumes later getBuffer() will find the buffer from AudioManager instead
        // of the current local reference... that's why I'm not directly using sound_buf here.

        // Required buffer functions not yet implemented
        long num_frames;
        int frequency;

        if (buf != null) {
            num_frames = buf.getLength();
            frequency = buf.getFrequency();
        } else {
            // No buffer attached!
            num_frames = 0;
            frequency = 0;
        }

        /*
         long num_frames = 1;
         long frequency = 125;
         */
        if (frequency <= 0) {
            // Protect against divide-by-zero errors
            return 0l;
        } else {
            return (1000 * num_frames) / frequency;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(SoundBite.class);
}
