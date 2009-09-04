// AbstractAudioListener.java

package jmri.jmrit.audio;

import javax.vecmath.Vector3f;
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
 * @version $Revision: 1.4 $
 */
public abstract class AbstractAudioListener extends AbstractAudio implements AudioListener {

    private Vector3f _position      = new Vector3f( 0.0f,  0.0f,  0.0f);
    private Vector3f _velocity      = new Vector3f( 0.0f,  0.0f,  0.0f);
    private Vector3f _orientationAt = new Vector3f( 0.0f,  0.0f, -1.0f);
    private Vector3f _orientationUp = new Vector3f( 0.0f,  1.0f,  0.0f);
    private float _gain             = 1.0f;
    private float _metersPerUnit    = 1.0f;

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

    public char getSubType() {
        return LISTENER;
    }

    public void setPosition(Vector3f pos) {
        this._position = pos;
        if (log.isDebugEnabled())
            log.debug("Set position of Listener " + this.getSystemName() + " to " + pos);
    }

    public void setPosition(float x, float y, float z) {
        this.setPosition(new Vector3f(x, y, z));
    }

    public void setPosition(float x, float z) {
        this.setPosition(new Vector3f(x, 0.0f, z));
    }

    public Vector3f getPosition() {
        return this._position;
    }

    public void setVelocity(Vector3f vel) {
        this._velocity = vel;
        if (log.isDebugEnabled())
            log.debug("Set velocity of Listener " + this.getSystemName() + " to " + vel);
    }

    public Vector3f getVelocity() {
        return this._velocity;
    }

    public void setOrientation(Vector3f at, Vector3f up) {
        this._orientationAt = at;
        this._orientationUp = up;
        if (log.isDebugEnabled())
            log.debug("Set orientation of Listener " + this.getSystemName() + " to (at) " + at + " (up) " + up);
    }

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
        }
        return _orientation;
    }

    public void setGain(float gain) {
        this._gain = gain;
        if (log.isDebugEnabled())
            log.debug("Set gain of Listener " + this.getSystemName() + " to " + gain);
    }

    public float getGain() {
        return this._gain;
    }

    public void setMetersPerUnit(float metersPerUnit) {
        this._metersPerUnit = metersPerUnit;
        if (log.isDebugEnabled())
            log.debug("Set Meters per unit of Listener " + this.getSystemName() + " to " + metersPerUnit);
    }

    public float getMetersPerUnit() {
        return this._metersPerUnit;
    }

    public void stateChanged() {
        // Move along... nothing to see here...
    }

    @Override
    public String toString() {
        return "Pos: " + this.getPosition().toString()
                + ", gain=" + this.getGain()
                + ", meters/unit=" + this.getMetersPerUnit();
    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractAudioListener.class.getName());
}

/* $(#)AbstractAudioListener.java */