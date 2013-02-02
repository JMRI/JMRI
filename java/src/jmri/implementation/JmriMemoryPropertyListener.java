package jmri.implementation;

import org.apache.log4j.Logger;
import java.beans.PropertyChangeEvent;
import jmri.*;

/**
 * A service class for monitoring a bound property
 * in one of the JMRI Named beans
 * For use with properties having two states which are determined
 * by equality to a String value (e.g. Internal Memory).
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
 * @since           2.5.1
 */

public class JmriMemoryPropertyListener extends JmriSimplePropertyListener
{
    String _data;

    JmriMemoryPropertyListener(String propName, int type, String name, int varType, 
                                Conditional client, String data) {
        super(propName, type, name, varType, client);
        _data = data;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        log.debug("\""+_varName+"\" sent PropertyChangeEvent "+evt.getPropertyName()+
            ", old value =\""+evt.getOldValue()+"\", new value =\""+evt.getNewValue()+
            ", enabled = "+_enabled);
        if ( getPropertyName().equals(evt.getPropertyName()) ) {
            String newValue = (String) evt.getNewValue();
            String oldValue = (String) evt.getOldValue();
            if (newValue == null) return;
            if (oldValue == null) return;
            if ( newValue.equals(_data) || oldValue.equals(_data) ) {
                // property has changed to/from the watched state, calculate
                super.propertyChange(evt);
            }
        }
    }
static final Logger log = Logger.getLogger(JmriMemoryPropertyListener.class.getName());
}

