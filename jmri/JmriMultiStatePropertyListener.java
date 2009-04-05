package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A class monitoring a bound value used to set the state of a variable used
 * in one or more Conditionals of a Logix.
 * For use with Conditional variables that may have more than two states
 * where the states are represented by numbers than can be cast to integers.
 * <P>
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
 * @author			Pete Cressman Copyright (C) 2009
 * @version			$Revision 1.0 $
 */

public class JmriMultiStatePropertyListener extends JmriSimplePropertyListener
{
    static int SIZE = 6;
    int numStates = 0;
    int[] _states = new int[SIZE];

    JmriMultiStatePropertyListener(String propName, int type, String name, int varType, 
                              Conditional client, int state) {
        super(propName, type, name, varType, client);
        _states[0] = state;
        numStates = 1;
    }

    public void setState(int state) {
        if (numStates >= _states.length)
        {
            int[] temp = new int[numStates+SIZE];
            System.arraycopy(_states, 0, temp, 0, _states.length);
            _states = temp;
        }
        _states[numStates] = state;
        numStates++;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        log.debug("\""+_varName+"\" sent PropertyChangeEvent "+evt.getPropertyName()+
            ", old value =\""+evt.getOldValue()+"\", new value =\""+evt.getNewValue()+
            ", enabled = "+_enabled);
        if ( getPropertyName().equals(evt.getPropertyName()) ) {
            int newState = ((Number) evt.getNewValue()).intValue();
            int oldState = ((Number) evt.getOldValue()).intValue();
            if (newState != oldState)  {
                for (int i=0; i<numStates; i++)
                {
                    if (oldState == _states[i] || newState == _states[i]) {
                        calculateClient(i, evt);
                    }
                }
            }
        }
    }

static final org.apache.log4j.Logger
log = org.apache.log4j.Logger.getLogger(JmriMultiStatePropertyListener.class.getName());
}


