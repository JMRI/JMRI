package jmri.jmrit.audio;

import javax.vecmath.Vector3f;
import jmri.Audio;

/**
 * Represent an AudioListener, a place to store or control sound information.
 * <p>
 * The AbstractAudio class contains a basic implementation of the state and
 * messaging code, and forms a useful start for a system-specific
 * implementation. Specific implementations in the jmrix package, e.g. for
 * LocoNet and NCE, will convert to and from the layout commands.
 * <p>
 * The states and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <p>
 * Each AudioListener object has a two names. The "user" name is entirely free
 * form, and can be used for any purpose. The "system" name is provided by the
 * system-specific implementations, and provides a unique mapping to the layout
 * control system (for example LocoNet or NCE) and address within that system.
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
public interface AudioListener extends Audio {

    /**
     * Sets the position of this AudioListener object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @param pos 3d position vector
     */
    public void setPosition(Vector3f pos);

    /**
     * Sets the position of this AudioListener object in x, y and z planes
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @param z z-coordinate
     */
    public void setPosition(float x, float y, float z);

    /**
     * Sets the position of this AudioListener object in x and y planes with z
     * plane position fixed at zero
     * <p>
     * Equivalent to setPosition(x, y, 0.0f)
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void setPosition(float x, float y);

    /**
     * Returns the position of this AudioListener object as a 3-dimensional
     * vector.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @return 3d position vector
     */
    public Vector3f getPosition();

    /**
     * Returns the current position of this AudioListener object as a
     * 3-dimensional vector.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @return 3d position vector
     */
    public Vector3f getCurrentPosition();

    /**
     * Method to reset the current position of this AudioListener object to the
     * initial position as defined by setPosition.
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     */
    public void resetCurrentPosition();

    /**
     * Sets the velocity of this AudioListener object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @param vel 3d velocity vector
     */
    public void setVelocity(Vector3f vel);

    /**
     * Returns the velocity of this AudioListener object
     *
     * Applies only to sub-types: - Listener - Source
     *
     * @return 3d velocity vector
     */
    public Vector3f getVelocity();

    /**
     * Set the orientation of this AudioListener object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * </ul>
     *
     * @param at 3d vector representing the position
     * @param up 3d vector representing the look-at point
     */
    public void setOrientation(Vector3f at, Vector3f up);

    /**
     * Return the orientation of this AudioListener object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * </ul>
     *
     * @param which the orientation vector to return: == AT - position; == UP -
     *              look-at point
     * @return vector representing the chosen orientation vector
     */
    public Vector3f getOrientation(int which);

    /**
     * Return the current orientation of this AudioListener object
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * </ul>
     *
     * @param which the orientation vector to return: == AT - position; == UP -
     *              look-at point
     * @return vector representing the chosen orientation vector
     */
    public Vector3f getCurrentOrientation(int which);

    /**
     * Return the current gain setting
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @return float gain
     */
    public float getGain();

    /**
     * Set the gain of this AudioListener object
     * <p>
     * Applicable values 0.0f to 1.0f
     * <p>
     * When applied to Listeners, has the effect of altering the master gain (or
     * volume)
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * <li>Source
     * </ul>
     *
     * @param gain new gain value - range 0.0f to 1.0f
     */
    public void setGain(float gain);

    /**
     * Method to set the Meters per unit ratio for all distance calculations.
     * <p>
     * Default value = 1.0f (i.e. 1 unit == 1 metre)
     * <p>
     * Typical alternative values:
     * <ul>
     * <li>0.3048f (i.e. 1 unit == 1 foot)
     * <li>0.9144f (i.e. 1 unit == 1 yard)
     * </ul>
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * </ul>
     *
     * @param metersPerUnit Meters per unit ratio
     */
    public void setMetersPerUnit(float metersPerUnit);

    /**
     * Retrieve the current Meters per unit ratio to use for all distance
     * calculations.
     * <p>
     * Default value = 1.0f (i.e. 1 unit == 1 metre)
     * <p>
     * Typical alternative values:
     * <ul>
     * <li>0.3048f (i.e. 1 unit == 1 foot)
     * <li>0.9144f (i.e. 1 unit == 1 yard)
     * </ul>
     * <p>
     * Applies only to sub-types:
     * <ul>
     * <li>Listener
     * </ul>
     *
     * @return Meters per unit ratio
     */
    public float getMetersPerUnit();

}
