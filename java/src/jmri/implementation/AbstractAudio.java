package jmri.implementation;

import jmri.Audio;

/**
 * Base implementation of the Audio class.
 * <P>
 * Specific implementations will extend this base class.
 *
 * <p>
 * Audio bean system names are always upper case.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Matthew Harris copyright (c) 2009
 */
public abstract class AbstractAudio extends AbstractNamedBean implements Audio {

    private int _state = STATE_INITIAL;

    private static final int INT_PRECISION = (int) Math.pow(10, DECIMAL_PLACES);

    /**
     * Abstract constructor for new Audio with system name
     *
     * @param systemName Audio object system name (e.g. IAS1, IAB4)
     */
    public AbstractAudio(String systemName) {
        super(systemName.toUpperCase());
    }

    /**
     * Abstract constructor for new Audio with system name and user name
     *
     * @param systemName Audio object system name (e.g. IAS1, IAB4)
     * @param userName   Audio object user name
     */
    public AbstractAudio(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
    }

    @Override
    public int getState() {
        return this._state;
    }

    @Override
    public void setState(int newState) {
        Object _old = this._state;
        this._state = newState;
        stateChanged((Integer) _old);
        firePropertyChange("State", _old, _state); //NOI18N
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " (" + this.getSystemName() + ")"; //NOI18N
    }

    /**
     * Abstract method that concrete classes will implement to perform necessary
     * cleanup routines.
     */
    abstract protected void cleanUp();

    /**
     * Static method to round a float value to the specified number of decimal
     * places
     *
     * @param value  float value to round
     * @param places number of decimal places to round to
     * @return float value rounded to specified number of decimal places
     */
    public static float roundDecimal(float value, double places) {
        double multiplier = Math.pow(10, places);
        value *= multiplier;
        return (float) (Math.round(value) / multiplier);
    }

    /**
     * Static method to round a float value to the number of decimal places
     * defined by DECIMAL_PLACES.
     *
     * @param value float value to round
     * @return float value rounded to DECIMAL_PLACES decimal places
     */
    public static float roundDecimal(float value) {
        return roundDecimal(value, Math.log10(INT_PRECISION));
    }

    public String getBeanType() {
        return Bundle.getMessage("BeanNameAudio");
    }

}
