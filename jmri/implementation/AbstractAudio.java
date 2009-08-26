// AbstractAudio.java

package jmri.implementation;

import jmri.Audio;

/**
 * Base implementation of the Audio class.
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
 * @version $Revision: 1.1 $
 */
public abstract class AbstractAudio extends AbstractNamedBean implements Audio {

    private int _state = STATE_INITIAL;

    /**
     * Abstract constructor for new Audio with system name
     * 
     * @param systemName Audio object system name (e.g. IAS1, IAB4)
     */
    public AbstractAudio(String systemName) {
        super(systemName);
    }

    /**
     * Abstract constructor for new Audio with system name and user name
     * 
     * @param systemName Audio object system name (e.g. IAS1, IAB4)
     * @param userName Audio object user name
     */
    public AbstractAudio(String systemName, String userName) {
        super(systemName, userName);
    }
    
    public int getState() {
        return this._state;
    }

    public void setState(int newState) {
        Object _old = this._state;
        this._state = newState;
        stateChanged();
        firePropertyChange("State", _old, _state);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " (" + this.getSystemName() + ")";
    }

    /**
     * Abstract method that concrete classes will implement to perform necessary
     * cleanup routines.
     */
    abstract protected void cleanUp();

}

/* $(#)AbstractAudio.java */