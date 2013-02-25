// AbstractAudioListener.java

package jmri.jmrit.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.vecmath.Vector3f;
import jmri.Audio;
import jmri.InstanceManager;
import jmri.implementation.AbstractAudio;

/**
 * Base implementation of the AudioListener class.
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
public abstract class AbstractAudioListener extends AbstractAudio implements AudioListener {

    private Vector3f _position      = new Vector3f( 0.0f,  0.0f,  0.0f);
    private Vector3f _currentPosition = new Vector3f( 0.0f,  0.0f,  0.0f);
    private Vector3f _velocity      = new Vector3f( 0.0f,  0.0f,  0.0f);
    private Vector3f _orientationAt = new Vector3f( 0.0f,  1.0f,  0.0f);
    private Vector3f _orientationUp = new Vector3f( 0.0f,  0.0f,  1.0f);
    private Vector3f _currentOriAt  = new Vector3f( 0.0f,  1.0f,  0.0f);
    private Vector3f _currentOriUp  = new Vector3f( 0.0f,  0.0f,  1.0f);
    private float _gain             = 1.0f;
    private float _metersPerUnit    = 1.0f;
    private long _timeOfLastPositionCheck = 0;

    private static final AudioFactory activeAudioFactory = InstanceManager.audioManagerInstance().getActiveAudioFactory();

    /**
     * Abstract constructor for new AudioListener with system name
     *
     * @param systemName AudioListener object system name (e.g. IAL)
     */
    public AbstractAudioListener(String systemName) {
        super(systemName);
        this.setState(STATE_POSITIONED);
    }

    /**
     * Abstract constructor for new AudioListener with system name and user name
     *
     * @param systemName AudioListener object system name (e.g. IAL)
     * @param userName AudioListener object user name
     */
    public AbstractAudioListener(String systemName, String userName) {
        super(systemName, userName);
        this.setState(STATE_POSITIONED);
    }

    @Override
    public char getSubType() {
        return LISTENER;
    }

    @Override
    public void setPosition(Vector3f pos) {
        this._position = pos;
        changePosition(pos);
        if (log.isDebugEnabled())
            log.debug("Set position of Listener " + this.getSystemName() + " to " + pos);
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
    public void setVelocity(Vector3f vel) {
        this._velocity = vel;
        this.setState(vel.length()!=0?STATE_MOVING:STATE_POSITIONED);
        if (log.isDebugEnabled())
            log.debug("Set velocity of Listener " + this.getSystemName() + " to " + vel);
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
        long timePassed = (currentTime - this._timeOfLastPositionCheck) / 1000;
        this._timeOfLastPositionCheck = currentTime;

        if (this._velocity.length()!=0) {
            this._currentPosition.scaleAdd(
                    timePassed * this._metersPerUnit,
                    this._velocity,
                    this._currentPosition);
            this._currentOriAt.scaleAdd(
                    timePassed * this._metersPerUnit,
                    this._velocity,
                    this._currentOriAt);
            this._currentOriUp.scaleAdd(
                    timePassed * this._metersPerUnit,
                    this._velocity,
                    this._currentOriUp);
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
    public void setOrientation(Vector3f at, Vector3f up) {
        this._orientationAt = at;
        this._orientationUp = up;
        if (log.isDebugEnabled())
            log.debug("Set orientation of Listener " + this.getSystemName() + " to (at) " + at + " (up) " + up);
    }

    @Override
    public Vector3f getOrientation(int which) {
        Vector3f _orientation = null;
        switch (which) {
            case AT: {
                _orientation = this._orientationAt;
                break;
            }
            case UP: {
                _orientation = this._orientationUp;
                break;
            }
            default:
                throw new IllegalArgumentException();
        }
        return _orientation;
    }

    @Override
    public Vector3f getCurrentOrientation(int which) {
        Vector3f _orientation = null;
        switch (which) {
            case AT: {
                _orientation = this._currentOriAt;
                break;
            }
            case UP: {
                _orientation = this._currentOriUp;
                break;
            }
            default:
                throw new IllegalArgumentException();
        }
        return _orientation;
    }

    @Override
    public void setGain(float gain) {
        this._gain = gain;
        if (log.isDebugEnabled())
            log.debug("Set gain of Listener " + this.getSystemName() + " to " + gain);
    }

    @Override
    public float getGain() {
        return this._gain;
    }

    @Override
    public void setMetersPerUnit(float metersPerUnit) {
        this._metersPerUnit = metersPerUnit;
        if (log.isDebugEnabled())
            log.debug("Set Meters per unit of Listener " + this.getSystemName() + " to " + metersPerUnit);
    }

    @Override
    public float getMetersPerUnit() {
        return this._metersPerUnit;
    }

    @Override
    public void stateChanged(int oldState) {
        // Move along... nothing to see here...
    }

    @Override
    public String toString() {
        return "Pos: " + this.getPosition().toString()
                + ", gain=" + this.getGain()
                + ", meters/unit=" + this.getMetersPerUnit();
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractAudioListener.class.getName());

    /**
     * An internal class used to create a new thread to monitor and maintain
     * current listener position with respect to velocity.
     */
    protected static class AudioListenerMoveThread extends AbstractAudioThread {

        /**
         * Reference to the AudioListener object being monitored
         */
        private AbstractAudioListener audioListener;

        /**
         * Constructor that takes handle to AudioListener to monitor
         *
         * @param audioListener AudioListener to monitor
         */
        AudioListenerMoveThread(AbstractAudioListener audioListener) {
            super();
            this.setName("movelis-" + super.getName());
            this.audioListener = audioListener;
            if (log.isDebugEnabled()) log.debug("Created AudioListenerMoveThread for AudioListener " + audioListener.getSystemName());
        }
        /**
         * Main processing loop
         */
        @Override
        public void run() {

            while (!dying()) {

                // Recalculate the position
                audioListener.calculateCurrentPosition();

                // Check state and die if not playing
                if (audioListener.getState()!=STATE_MOVING) {
                    die();
                }

                // sleep for a while so as not to overload CPU
                snooze(20);
            }

//            // Reset the current position
//            audioListener.resetCurrentPosition();

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
            this.audioListener = null;

            // Finalise cleanup in super-class
            super.cleanup();
        }
    }
}

/* $(#)AbstractAudioListener.java */
