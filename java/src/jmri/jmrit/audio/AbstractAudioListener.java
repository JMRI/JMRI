package jmri.jmrit.audio;

import javax.vecmath.Vector3f;
import jmri.Audio;
import jmri.InstanceManager;
import jmri.implementation.AbstractAudio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of the AudioListener class.
 * <p>
 * Specific implementations will extend this base class.
 * <br>
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris copyright (c) 2009
 */
public abstract class AbstractAudioListener extends AbstractAudio implements AudioListener {

    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f currentPosition = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f velocity = new Vector3f(0.0f, 0.0f, 0.0f);
    private Vector3f orientationAt = new Vector3f(0.0f, 1.0f, 0.0f);
    private Vector3f orientationUp = new Vector3f(0.0f, 0.0f, 1.0f);
    private Vector3f currentOriAt = new Vector3f(0.0f, 1.0f, 0.0f);
    private Vector3f currentOriUp = new Vector3f(0.0f, 0.0f, 1.0f);
    private float gain = 1.0f;
    private float metersPerUnit = 1.0f;
    private long timeOfLastPositionCheck = 0;

    private static final AudioFactory activeAudioFactory = InstanceManager.getDefault(jmri.AudioManager.class).getActiveAudioFactory();

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
     * @param userName   AudioListener object user name
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
        this.position = pos;
        changePosition(pos);
        if (log.isDebugEnabled()) {
            log.debug("Set position of Listener " + this.getSystemName() + " to " + pos);
        }
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
        return this.position;
    }

    @Override
    public Vector3f getCurrentPosition() {
        return this.currentPosition;
    }

    @Override
    public void setVelocity(Vector3f vel) {
        this.velocity = vel;
        this.setState(vel.length() != 0 ? STATE_MOVING : STATE_POSITIONED);
        if (log.isDebugEnabled()) {
            log.debug("Set velocity of Listener " + this.getSystemName() + " to " + vel);
        }
    }

    @Override
    public Vector3f getVelocity() {
        return this.velocity;
    }

    /**
     * Method to calculate current position based on velocity
     */
    protected void calculateCurrentPosition() {

        // Calculate how long it's been since we lasted checked position
        long currentTime = System.currentTimeMillis();
        long timePassed = (currentTime - this.timeOfLastPositionCheck) / 1000;
        this.timeOfLastPositionCheck = currentTime;

        if (this.velocity.length() != 0) {
            this.currentPosition.scaleAdd(timePassed * this.metersPerUnit,
                    this.velocity,
                    this.currentPosition);
            this.currentOriAt.scaleAdd(timePassed * this.metersPerUnit,
                    this.velocity,
                    this.currentOriAt);
            this.currentOriUp.scaleAdd(timePassed * this.metersPerUnit,
                    this.velocity,
                    this.currentOriUp);
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
        this.currentPosition = this.position;
    }

    /**
     * Method to change the current position of this source
     *
     * @param pos new position
     */
    abstract protected void changePosition(Vector3f pos);

    @Override
    public void setOrientation(Vector3f at, Vector3f up) {
        this.orientationAt = at;
        this.orientationUp = up;
        if (log.isDebugEnabled()) {
            log.debug("Set orientation of Listener " + this.getSystemName() + " to (at) " + at + " (up) " + up);
        }
    }

    @Override
    public Vector3f getOrientation(int which) {
        Vector3f orientation = null;
        switch (which) {
            case AT: {
                orientation = this.orientationAt;
                break;
            }
            case UP: {
                orientation = this.orientationUp;
                break;
            }
            default:
                throw new IllegalArgumentException();
        }
        return orientation;
    }

    @Override
    public Vector3f getCurrentOrientation(int which) {
        Vector3f orientation = null;
        switch (which) {
            case AT: {
                orientation = this.currentOriAt;
                break;
            }
            case UP: {
                orientation = this.currentOriUp;
                break;
            }
            default:
                throw new IllegalArgumentException();
        }
        return orientation;
    }

    @Override
    public void setGain(float gain) {
        this.gain = gain;
        if (log.isDebugEnabled()) {
            log.debug("Set gain of Listener " + this.getSystemName() + " to " + gain);
        }
    }

    @Override
    public float getGain() {
        return this.gain;
    }

    @Override
    public void setMetersPerUnit(float metersPerUnit) {
        this.metersPerUnit = metersPerUnit;
        if (log.isDebugEnabled()) {
            log.debug("Set Meters per unit of Listener " + this.getSystemName() + " to " + metersPerUnit);
        }
    }

    @Override
    public float getMetersPerUnit() {
        return this.metersPerUnit;
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

    private static final Logger log = LoggerFactory.getLogger(AbstractAudioListener.class);

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
            if (log.isDebugEnabled()) {
                log.debug("Created AudioListenerMoveThread for AudioListener " + audioListener.getSystemName());
            }
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
                if (audioListener.getState() != STATE_MOVING) {
                    die();
                }

                // sleep for a while so as not to overload CPU
                snooze(20);
            }

//            // Reset the current position
//            audioListener.resetCurrentPosition();
            // Finish up
            if (log.isDebugEnabled()) {
                log.debug("Clean up thread " + this.getName());
            }
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
