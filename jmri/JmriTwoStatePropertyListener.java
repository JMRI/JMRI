package jmri;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A class monitoring a bound value used to set the state of a variable used
 * in one or more Conditionals of a Logix.
 * For use where only two states are possible, ACTIVE/INACTIVE, THROWN/CLOSED etc.
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

public class JmriTwoStatePropertyListener extends JmriSimplePropertyListener
{
    JmriTwoStatePropertyListener(String propName, int type, String name, int varType, 
                              Conditional client) {
        super(propName, type, name, varType, client);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        log.debug("\""+_varName+"\" sent PropertyChangeEvent "+evt.getPropertyName()+
            ", old value =\""+evt.getOldValue()+"\", new value =\""+evt.getNewValue()+
            ", enabled = "+_enabled);
        if ( getPropertyName().equals(evt.getPropertyName()) ) {
            super.propertyChange(evt);
        }
        /*
        int newState = ((Number) evt.getNewValue()).intValue();
        int oldState = ((Number) evt.getOldValue()).intValue();
        if (newState != oldState)  {
            // property has changed to/from the watched state, calculate
            super.propertyChange(evt);
        }
        */
    }

static final org.apache.log4j.Category 
log = org.apache.log4j.Category.getInstance(JmriTwoStatePropertyListener.class.getName());
}


