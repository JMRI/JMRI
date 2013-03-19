// AbstractAudioSource.java

package jmri.jmrit.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Random;
import jmri.Audio;
import jmri.AudioManager;
import jmri.InstanceManager;
import javax.vecmath.Vector3f;
import jmri.implementation.AbstractAudio;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

/**
 * Base implementation of the AudioSource class.
 * <P>
 * Specific implementations will extend this base class.
 * <P>
 *
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
 * @author Matthew Harris  copyright (c) 2009
 * @version $Revision$
 */
public abstract class AbstractAudioSource extends AbstractAudio implements AudioSource {

    private Vector3f _position        = new Vector3f( 0.0f,  0.0f,  0.0f);
    private Vector3f _currentPosition = new Vector3f( 0.0f,  0.0f,  0.0f);
    private Vector3f _velocity        = new Vector3f( 0.0f,  0.0f,  0.0f);
    private float _gain               = 1.0f;
    private float _pitch              = 1.0f;
    private float _referenceDistance  = 1.0f;
    private float _maximumDistance    = Audio.MAX_DISTANCE;
    private float _rollOffFactor      = 1.0f;
    private int _minLoops             = LOOP_NONE;
    private int _maxLoops             = LOOP_NONE;
    private int _numLoops             = 0;
//    private int _minLoopDelay         = 0;
//    private int _maxLoopDelay         = 0;
//    private int _loopDelay            = 0;
    private int _fadeInTime           = 1000;
    private int _fadeOutTime          = 1000;
    private float _fadeGain           = 1.0f;
    private float _dopplerFactor      = 1.0f;
    private long _timeOfLastFadeCheck = 0;
    private long _timeOfLastPositionCheck = 0;
    private int _fading               = Audio.FADE_NONE;
    private boolean _bound            = false;
    private boolean _positionRelative = false;
    private boolean _queued           = false;
    private AudioBuffer _buffer;
//    private AudioSourceDelayThread asdt = null;
    private LinkedList<AudioBuffer> pendingBufferQueue = new LinkedList<AudioBuffer>();

    private static final AudioFactory activeAudioFactory = InstanceManager.audioManagerInstance().getActiveAudioFactory();

    private static float metersPerUnit = activeAudioFactory.getActiveAudioListener().getMetersPerUnit();

    /**
     * Abstract constructor for new AudioSource with system name
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     */
    public AbstractAudioSource(String systemName) {
        super(systemName);
    }

    /**
     * Abstract constructor for new AudioSource with system name and user name
     *
     * @param systemName AudioSource object system name (e.g. IAS1)
     * @param userName AudioSource object user name
     */
    public AbstractAudioSource(String systemName, String userName) {
        super(systemName, userName);
    }

    @Override
    public char getSubType() {
        return SOURCE;
    }

    @Override
    public boolean queueBuffers(Queue<AudioBuffer> audioBuffers) {
	// Note: Cannot queue buffers to a Source that has a bound buffer.
	if (!_bound) {
	    this.pendingBufferQueue = new LinkedList<AudioBuffer>(audioBuffers);
	    activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_QUEUE_BUFFERS));
	    activeAudioFactory.getCommandThread().interrupt();
	    if (log.isDebugEnabled())
		log.debug("Queued Buffer " + audioBuffers.peek().getSystemName() + " to Source " + this.getSystemName());
	    return(true);
	} else {
	    log.error("Attempted to queue buffers " + audioBuffers.peek().getSystemName() + " (etc) to Bound Source " + this.getSystemName());
	    return(false);
	}
    }

    @Override
    public boolean queueBuffer(AudioBuffer audioBuffer) {
	if (!_bound) {
	    //this.pendingBufferQueue.clear();
	    this.pendingBufferQueue.add(audioBuffer);
	    activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_QUEUE_BUFFERS));
	    activeAudioFactory.getCommandThread().interrupt();
	    if (log.isDebugEnabled())
		log.debug("Queued Buffer " + audioBuffer.getSystemName() + " to Source " + this.getSystemName());
	    return(true);
	} else {
	    log.error("Attempted to queue buffer " + audioBuffer.getSystemName() + " to Bound Source " + this.getSystemName());
	    return(false);
	}
    }

    @Override
    public boolean unqueueBuffers() {
	if (_bound) {
	    log.error("Attempted to unqueue buffers on Bound Source " + this.getSystemName());
	    return(false);
	} else if (_queued) {
	    activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_UNQUEUE_BUFFERS));
	    activeAudioFactory.getCommandThread().interrupt();
	    if (log.isDebugEnabled())
		log.debug("Unqueued Processed Buffers on Source " + this.getSystemName());
	    return(true);
	} else {
	    log.debug("Source neither queued nor bound. Not an error. " + this.getSystemName());
	    return(false);
	}
    }

    public Queue<AudioBuffer> getQueuedBuffers() {
	return(this.pendingBufferQueue);
    }

    @Override
    public void setAssignedBuffer(AudioBuffer audioBuffer) {
	if (!_queued) {
	    this._buffer = audioBuffer;
	    // Ensure that the source is stopped
	    this.stop(false);
	    activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_BIND_BUFFER));
	    activeAudioFactory.getCommandThread().interrupt();
	    if (log.isDebugEnabled())
		log.debug("Assigned Buffer " + audioBuffer.getSystemName() + " to Source " + this.getSystemName());
	} else {
	    log.error("Attempted to assign buffer " + audioBuffer.getSystemName() + " to Queued Source " + this.getSystemName());
	}
    }

    @Override
    public void setAssignedBuffer(String bufferSystemName) {
	if (!_queued) {
	    AudioManager am = InstanceManager.audioManagerInstance();
	    Audio a = am.getBySystemName(bufferSystemName);
	    if (a.getSubType()==Audio.BUFFER) {
		setAssignedBuffer((AudioBuffer) a);
	    }
	    else {
		log.warn("Attempt to assign incorrect object type to buffer - AudioBuffer expected.");
		this._buffer = null;
		this._bound = false;
	    }
	} else {
	    log.error("Attempted to assign buffer " + bufferSystemName + " to Queued Source " + this.getSystemName());
	}
    }
    
    @Override
    public AudioBuffer getAssignedBuffer() {
        return this._buffer;
    }

    @Override
    public String getAssignedBufferName() {
        return (_buffer!=null)?_buffer.getSystemName():"[none]";
    }

    @Override
    public void setPosition(Vector3f pos) {
        this._position = pos;
        this._currentPosition = pos;
        changePosition(pos);
        if (log.isDebugEnabled())
            log.debug("Set position of Source " + this.getSystemName() + " to " + pos);
    }

    @Override
    public void setPosition(float x, float y, float z) {
        this.setPosition(new Vector3f(x, y, z));
    }

    @Override
    public void setPosition(float x, float y) {
        this.setPosition(new Vector3f(x, y, 0.0f));
    }

    @Override
    public Vector3f getPosition() {
        return this._position;
    }

    @Override
    public Vector3f getCurrentPosition() {
        return this._currentPosition;
    }

    @Override
    public void setPositionRelative(boolean relative) {
        this._positionRelative = relative;
    }

    @Override
    public boolean isPositionRelative() {
        return this._positionRelative;
    }

    @Override
    public void setVelocity(Vector3f vel) {
        this._velocity = vel;
        if (log.isDebugEnabled())
            log.debug("Set velocity of Source " + this.getSystemName() + " to " + vel);
    }

    @Override
    public Vector3f getVelocity() {
        return this._velocity;
    }

    /**
     * Method to calculate current position based on velocity
     */
    protected void calculateCurrentPosition() {

        // Calculate how long it's been since we lasted checked position
        long currentTime = System.currentTimeMillis();
        float timePassed = (currentTime - this._timeOfLastPositionCheck);
        this._timeOfLastPositionCheck = currentTime;

        log.debug("timePassed = " + timePassed +
                  " metersPerUnit = " + metersPerUnit +
                  " source = " + this.getSystemName() +
                  " state = " + this.getState());
        if (this._velocity.length()!=0) {
            this._currentPosition.scaleAdd(
                    (timePassed/1000) * metersPerUnit,
                    this._velocity,
                    this._currentPosition);
            changePosition(this._currentPosition);
            if (log.isDebugEnabled())
                log.debug("Set current position of Source " + this.getSystemName() + " to " + this._currentPosition);
        }
    }

    @Override
    public void resetCurrentPosition() {
        activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_RESET_POSITION));
        activeAudioFactory.getCommandThread().interrupt();
    }

    /**
     * Method to reset the current position
     */
    protected void doResetCurrentPosition() {
        this._currentPosition = this._position;
    }

    /**
     * Method to change the current position of this source
     * @param pos new position
     */
    abstract protected void changePosition(Vector3f pos);

    @Override
    public void setGain(float gain) {
        this._gain = gain;
        if (log.isDebugEnabled())
            log.debug("Set gain of Source " + this.getSystemName() + " to " + gain);
    }

    @Override
    public float getGain() {
        return this._gain;
    }

    /**
     * Method to calculate the gain of this AudioSource based on
     * distance from listener and fade levels
     */
    abstract protected void calculateGain();

    @Override
    public void setPitch(float pitch) {
        if (pitch < 0.5f) {
            pitch = 0.5f;
        }
        if (pitch > 2.0f) {
            pitch = 2.0f;
        }
        this._pitch = pitch;
        if (log.isDebugEnabled())
            log.debug("Set pitch of Source " + this.getSystemName() + " to " + pitch);
    }

    @Override
    public float getPitch() {
        return this._pitch;
    }

    @Override
    public void setReferenceDistance(float referenceDistance) {
        if (referenceDistance < 0.0f) {
            referenceDistance = 0.0f;
        }
        this._referenceDistance = referenceDistance;
        if (log.isDebugEnabled())
            log.debug("Set reference distance of Source " + this.getSystemName() + " to " + referenceDistance);
    }

    @Override
    public float getReferenceDistance() {
        return this._referenceDistance;
    }

    @Override
    public void setMaximumDistance(float maximumDistance) {
        if (maximumDistance < 0.0f) {
            maximumDistance = 0.0f;
        }
        this._maximumDistance = maximumDistance;
        if (log.isDebugEnabled())
            log.debug("Set maximum distance of Source " + this.getSystemName() + " to " + maximumDistance);
    }

    @Override
    public float getMaximumDistance() {
        return this._maximumDistance;
    }

    @Override
    public void setRollOffFactor(float rollOffFactor) {
        this._rollOffFactor = rollOffFactor;
        if (log.isDebugEnabled())
            log.debug("Set roll-off factor of Source " + this.getSystemName() + " to " + rollOffFactor);
    }

    @Override
    public float getRollOffFactor() {
        return this._rollOffFactor;
    }

    @Override
    public void setLooped(boolean loop) {
        if (loop) {
            this._minLoops = LOOP_CONTINUOUS;
            this._maxLoops = LOOP_CONTINUOUS;
        } else {
            this._minLoops = LOOP_NONE;
            this._maxLoops = LOOP_NONE;
        }
        calculateLoops();
    }

    @Override
    public boolean isLooped() {
        return (this._minLoops != LOOP_NONE || this._maxLoops != LOOP_NONE);
    }

    @Override
    public void setMinLoops(int loops) {
        if (this._maxLoops < loops) {
            this._maxLoops = loops;
        }
        this._minLoops = loops;
        calculateLoops();
    }

    @Override
    public int getMinLoops() {
        return this._minLoops;
    }

    @Override
    public void setMaxLoops(int loops) {
        if (this._minLoops > loops) {
            this._minLoops = loops;
        }
        this._maxLoops = loops;
        calculateLoops();
    }

    /**
     * Method to calculate the number of times to loop playback of this sound
     */
    protected void calculateLoops() {
        if (this._minLoops != this._maxLoops) {
            Random r = new Random();
            this._numLoops = this._minLoops + r.nextInt(this._maxLoops-this._minLoops);
        } else {
            this._numLoops = this._minLoops;
        }
    }

    @Override
    public int getMaxLoops() {
        return this._maxLoops;
    }

    @Override
    public int getNumLoops() {
        // Call the calculate method each time so as to ensure
        // randomness when min and max are not equal
        calculateLoops();
        return this._numLoops;
    }

//    public void setMinLoopDelay(int loopDelay) {
//        if (this._maxLoopDelay < loopDelay) {
//            this._maxLoopDelay = loopDelay;
//        }
//        this._minLoopDelay = loopDelay;
//        calculateLoopDelay();
//    }
//
//    public int getMinLoopDelay() {
//        return this._minLoopDelay;
//    }
//
//    public void setMaxLoopDelay(int loopDelay) {
//        if (this._minLoopDelay > loopDelay) {
//            this._minLoopDelay = loopDelay;
//        }
//        this._maxLoopDelay = loopDelay;
//        calculateLoopDelay();
//    }
//
//    public int getMaxLoopDelay() {
//        return this._maxLoopDelay;
//    }
//
//    public int getLoopDelay() {
//        // Call the calculate method each time so as to ensure
//        // randomness when min and max are not equal
//        calculateLoopDelay();
//        return this._loopDelay;
//    }
//
//    /**
//     * Method to calculate the delay between subsequent loops of this source
//     */
//    protected void calculateLoopDelay() {
//        if (this._minLoopDelay != this._maxLoopDelay) {
//            Random r = new Random();
//            this._loopDelay = this._minLoopDelay + r.nextInt(this._maxLoopDelay-this._minLoopDelay);
//        } else {
//            this._loopDelay = this._minLoopDelay;
//        }
//    }

    @Override
    public void setFadeIn(int fadeInTime) {
        this._fadeInTime = fadeInTime;
    }

    @Override
    public int getFadeIn() {
        return this._fadeInTime;
    }

    @Override
    public void setFadeOut(int fadeOutTime) {
        this._fadeOutTime = fadeOutTime;
    }

    @Override
    public int getFadeOut() {
        return this._fadeOutTime;
    }

    @Override
    public void setDopplerFactor(float dopplerFactor) {
        this._dopplerFactor = dopplerFactor;
    }

    @Override
    public float getDopplerFactor() {
        return this._dopplerFactor;
    }

    /**
     * Used to return the current calculated fade gain for this AudioSource
     *
     * @return current fade gain
     */
    protected float getFadeGain() {
        return this._fadeGain;
    }

    /**
     * Method used to calculate the fade gains
     */
    protected void calculateFades() {

        // Calculate how long it's been since we lasted checked fade gains
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - this._timeOfLastFadeCheck;
        this._timeOfLastFadeCheck = currentTime;

        switch (this._fading) {
            case Audio.FADE_NONE:
                // Reset fade gain
                this._fadeGain = 1.0f;
                break;
            case Audio.FADE_OUT:
                // Calculate fade-out gain
                this._fadeGain -= roundDecimal(timePassed) / (this.getFadeOut());

                // Ensure that fade-out gain is not less than 0.0f
                if (this._fadeGain < 0.0f) {
                    this._fadeGain = 0.0f;

                    // If so, we're done fading
                    this._fading = Audio.FADE_NONE;
                }
                if (log.isDebugEnabled())
                    log.debug("Set fade out gain of AudioSource "
                            + this.getSystemName() + " to " + this._fadeGain);
                break;
            case Audio.FADE_IN:
                // Calculate fade-in gain
                this._fadeGain += roundDecimal(timePassed) / (this.getFadeIn());

                // Ensure that fade-in gain is not greater than 1.0f
                if (this._fadeGain >= 1.0f) {
                    this._fadeGain = 1.0f;

                    // If so, we're done fading
                    this._fading = Audio.FADE_NONE;
                }
                if (log.isDebugEnabled())
                    log.debug("Set fade in gain of AudioSource "
                            + this.getSystemName() + " to " + this._fadeGain);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    // Probably aught to be abstract, but I don't want to force the non-JOAL Source
    // types to implement this (yet).  So default to failing.
    public boolean queueAudioBuffers(Queue<AudioBuffer> audioBuffers) {
	log.debug("Abstract queueAudioBuffers() called.");
	return(false);
    }

    // Probably aught to be abstract, but I don't want to force the non-JOAL Source
    // types to implement this (yet).  So default to failing.
    public boolean queueAudioBuffer(AudioBuffer audioBuffer) {
	return(false);
    }

    public boolean unqueueAudioBuffers() {
	return(false);
    }

    // Probably aught to be abstract, but I don't want to force the non-JOAL Source
    // types to implement this (yet).  So default to failing.
    @Override
    public int numQueuedBuffers() { return(0); }

    // Probably aught to be abstract, but I don't want to force the non-JOAL Source
    // types to implement this (yet).  So default to failing.
    public int numProcessedBuffers() { return(0); }

    /**
     * Binds this AudioSource with the specified AudioBuffer
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Source
     * </ul>
     * @param buffer The AudioBuffer to bind to this AudioSource
     * @return true if successful
     */
    abstract boolean bindAudioBuffer(AudioBuffer buffer);

    /**
     * Method to define if this AudioSource has been bound to an AudioBuffer
     *
     * @param bound True if bound to an AudioBufferr
     */
    protected void setBound(boolean bound) {
        this._bound = bound;
    }

    protected void setQueued(boolean queued) {
	this._queued = queued;
    }

    @Override
    public boolean isBound() {
        return this._bound;
    }

    @Override
    public boolean isQueued() {
	return this._queued;
    }

    @Override
    public void stateChanged(int oldState) {
        // Get the current state
        int i = this.getState();

        // Check if the current state has changed to playing
        if (i!=oldState && i==STATE_PLAYING) {
            // We've changed to playing so start the move thread
            this._timeOfLastPositionCheck = System.currentTimeMillis();
            AudioSourceMoveThread asmt = new AudioSourceMoveThread(this);
            asmt.start();
        }

//        // Check if the current state has changed to stopped
//        if (i!=oldState && i==STATE_STOPPED) {
//            // We've changed to stopped so determine if we need to start the
//            // loop delay thread
//            if (isLooped() && getMinLoops()!=LOOP_CONTINUOUS) {
//                // Yes, we need to
//                if (asdt!=null) {
//                    asdt.cleanup();
//                    asdt = null;
//                }
//                asdt = new AudioSourceDelayThread(this);
//                asdt.start();
//            }
//        }
    }

    @Override
    public void play() {
        this._fading = Audio.FADE_NONE;
//        if (asdt!=null) {
//            asdt.interrupt();
//        }
        if (this.getState()!=STATE_PLAYING) {
            this.setState(STATE_PLAYING);
            activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_PLAY));
            activeAudioFactory.getCommandThread().interrupt();
        }
    }

    /**
     * Play the clip from the beginning.
     * If looped, start looping
     */
    abstract protected void doPlay();

    @Override
    public void stop() {
        stop(true);
    }

    private void stop(boolean interruptThread) {
        this._fading = Audio.FADE_NONE;
        this.setState(STATE_STOPPED);
        activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_STOP));
        if (interruptThread) {
            activeAudioFactory.getCommandThread().interrupt();
        }
    }

    /**
     * Stop playing the clip and rewind to the beginning
     */
    abstract protected void doStop();

    @Override
    public void togglePlay() {
        this._fading = Audio.FADE_NONE;
        activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_PLAY_TOGGLE));
        activeAudioFactory.getCommandThread().interrupt();
    }

    /**
     * Toggle the current playing status.
     * Will always start at/return to the beginning of the sample
     */
    protected void doTogglePlay() {
        if (this.getState()==STATE_PLAYING) {
            stop();
        } else {
            play();
        }
    }

    @Override
    public void pause() {
        this._fading = Audio.FADE_NONE;
        this.setState(STATE_STOPPED);
        activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_PAUSE));
        activeAudioFactory.getCommandThread().interrupt();
    }

    /**
     * Stop playing the clip but retain the current position
     */
    abstract protected void doPause();

    @Override
    public void resume() {
        this._fading = Audio.FADE_NONE;
        this.setState(STATE_PLAYING);
        activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_RESUME));
        activeAudioFactory.getCommandThread().interrupt();
    }

    /**
     * Play the clip from the current position
     */
    abstract protected void doResume();

    @Override
    public void togglePause() {
        this._fading = Audio.FADE_NONE;
        activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_PAUSE_TOGGLE));
        activeAudioFactory.getCommandThread().interrupt();
    }

    /**
     * Toggle the current playing status.
     * Will retain the playback position of the sample
     */
    protected void doTogglePause() {
        if (this.getState()==STATE_PLAYING) {
            pause();
        } else {
            resume();
        }
    }

    @Override
    public void rewind() {
        this._fading = Audio.FADE_NONE;
        this.setState(STATE_STOPPED);
        activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_REWIND));
        activeAudioFactory.getCommandThread().interrupt();
    }

    /**
     * Rewind clip to the beginning
     */
    abstract protected void doRewind();

    @Override
    public void fadeIn() {
        if (this.getState()!=STATE_PLAYING && this._fading!=Audio.FADE_IN) {
            this._fading = Audio.FADE_IN;
            this._fadeGain = 0.0f;
            this._timeOfLastFadeCheck = System.currentTimeMillis();
            this.setState(STATE_PLAYING);
            activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_FADE_IN));
            activeAudioFactory.getCommandThread().interrupt();
        }
    }

    /**
     * Fade in then play this AudioSource
     */
    abstract protected void doFadeIn();

    @Override
    public void fadeOut() {
        if (this.getState()==STATE_PLAYING && this._fading!=Audio.FADE_OUT) {
            this._fading = Audio.FADE_OUT;
            this._fadeGain = 1.0f;
            this._timeOfLastFadeCheck = System.currentTimeMillis();
            this.setState(STATE_PLAYING);
            activeAudioFactory.audioCommandQueue(new AudioCommand(this, Audio.CMD_FADE_OUT));
            activeAudioFactory.getCommandThread().interrupt();
        }
    }

    /**
     * Fade out then stop this AudioSource
     */
    abstract protected void doFadeOut();

    /**
     * Return the current fading status
     *
     * @return fading status
     */
    protected int getFading() {
        return this._fading;
    }

    @Override
    public String toString() {
        return "Pos: " + this.getPosition().toString()
                + ", bound to: " + this.getAssignedBufferName()
                + ", loops: "
                    + ((this.getMinLoops()==LOOP_CONTINUOUS)?"infinite":
                        ((!this.isLooped())?"none":
                            "(min=" + this.getMinLoops() + " max=" + this.getMaxLoops() + ")"));
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractAudioSource.class.getName());

    /**
     * An internal class used to create a new thread to monitor and maintain
     * fade in and fade out levels.
     * <p>
     * Will exist only as long as this source is
     * in the process of fading in or out.
     */
    protected static class AudioSourceFadeThread extends AbstractAudioThread {

        /**
         * Reference to the AudioSource object being monitored
         */
        private AbstractAudioSource audioSource;

        /**
         * Internal variable to hold the fade direction
         */
        private int fadeDirection;

        /**
         * Constructor that takes handle to looping AudioSource to monitor
         *
         * @param audioSource looping AudioSource to monitor
         */
        AudioSourceFadeThread(AbstractAudioSource audioSource) {
            super();
            this.setName("fadesrc-"+super.getName());
            this.audioSource = audioSource;
            this.fadeDirection = audioSource.getFading();
            if (log.isDebugEnabled()) log.debug("Created AudioSourceFadeThread for AudioSource " + audioSource.getSystemName());
        }

        /**
         * Main processing loop
         */
        @Override
        public void run() {

            while (!dying()) {

                // Recalculate the fade levels
                audioSource.calculateFades();
                
                // Recalculate the gain levels
                audioSource.calculateGain();

                // Check if we've done fading
                if (audioSource.getFading()==Audio.FADE_NONE) {
                    die();
                }

                // sleep for a while so as not to overload CPU
                snooze(20);
            }

            // Reset fades
            audioSource.calculateFades();

            // Check if we were fading out and, if so, stop.
            // Otherwise reset gain
            if (this.fadeDirection==Audio.FADE_OUT) {
                audioSource.doStop();
            } else {
                audioSource.calculateGain();
            }

            // Finish up
            if (log.isDebugEnabled()) log.debug("Clean up thread " + this.getName());
            cleanup();
        }

        /**
         * Shuts this thread down and clears references to created objects
         */
        @Override
        protected void cleanup() {
            // Thread is to shutdown
            die();

            // Clear references to objects
            this.audioSource = null;

            // Finalise cleanup in super-class
            super.cleanup();
        }
    }

    /**
     * An internal class used to create a new thread to monitor and maintain
     * current source position with respect to velocity.
     */
    protected static class AudioSourceMoveThread extends AbstractAudioThread {

        /**
         * Reference to the AudioSource object being monitored
         */
        private AbstractAudioSource audioSource;

        /**
         * Constructor that takes handle to looping AudioSource to monitor
         *
         * @param audioSource looping AudioSource to monitor
         */
        AudioSourceMoveThread(AbstractAudioSource audioSource) {
            super();
            this.setName("movesrc-" + super.getName());
            this.audioSource = audioSource;
            if (log.isDebugEnabled()) log.debug("Created AudioSourceMoveThread for AudioSource " + audioSource.getSystemName());
        }

        /**
         * Main processing loop
         */
        @Override
        public void run() {

            while (!dying()) {

                // Recalculate the position
                audioSource.calculateCurrentPosition();

                // Check state and die if not playing
                if (audioSource.getState()!=STATE_PLAYING) {
                    die();
                }

                // sleep for a while so as not to overload CPU
                snooze(100);
            }

//            // Reset the current position
//            audioSource.resetCurrentPosition();

            // Finish up
            if (log.isDebugEnabled()) log.debug("Clean up thread " + this.getName());
            cleanup();
        }

        /**
         * Shuts this thread down and clears references to created objects
         */
        @Override
        protected void cleanup() {
            // Thread is to shutdown
            die();

            // Clear references to objects
            this.audioSource = null;

            // Finalise cleanup in super-class
            super.cleanup();
        }
    }

//    /**
//     * An internal class used to create a new thread to delay subsequent
//     * playbacks of a non-continuous looped source.
//     */
//    private class AudioSourceDelayThread extends Thread {
//
//        /**
//         * Reference to the AudioSource object being monitored
//         */
//        private AbstractAudioSource audioSource;
//
//        /**
//         * Constructor that takes handle to looping AudioSource to monitor
//         *
//         * @param audioSource looping AudioSource to monitor
//         */
//        AudioSourceDelayThread(AbstractAudioSource audioSource) {
//            super();
//            this.setName("delaysrc-"+super.getName());
//            this.audioSource = audioSource;
//            if (log.isDebugEnabled()) log.debug("Created AudioSourceDelayThread for AudioSource " + audioSource.getSystemName());
//        }
//
//        /**
//         * Main processing loop
//         */
//        @Override
//        public void run() {
//
//            // Sleep for the required period of time
//            try {
//                Thread.sleep(audioSource.getLoopDelay());
//            } catch (InterruptedException ex) {}
//
//            // Restart playing this AudioSource
//            this.audioSource.play();
//
//            // Finish up
//            if (log.isDebugEnabled()) log.debug("Clean up thread " + this.getName());
//            cleanup();
//        }
//
//        /**
//         * Shuts this thread down and clears references to created objects
//         */
//        protected void cleanup() {
//            // Clear references to objects
//            this.audioSource = null;
//        }
//    }
}

/* $(#)AbstractAudioSource.java */
