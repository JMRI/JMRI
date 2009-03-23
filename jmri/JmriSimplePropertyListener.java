package jmri;

import java.util.ArrayList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A class monitoring a bound value used to set the state of a variable used
 * in one or more Conditionals of a Logix.
 * The parent class for all the Conditional variable listeners.
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

public class JmriSimplePropertyListener implements PropertyChangeListener
{
    int _type;
    String _varName;
    int _varType;
    String _propertyName;
    ArrayList <Conditional> _clients;
    boolean _enabled;

    JmriSimplePropertyListener(String propName, int type, String varName, int varType, Conditional client)
    {
        _propertyName = propName;
        _type = type;
        _varName = varName;
        _varType = varType;
        _clients = new ArrayList<Conditional>();
        _clients.add(client);
        _enabled = true;
    }

    public int getType() {
        return _type;
    }

    public String getPropertyName() {
        return _propertyName;
    }
	
    public int getVarType() {
        return _varType;
    }
	
    public String getDevName() {
        return _varName;
    }
	
    public void addConditional(Conditional client) {
        _clients.add(client);
    }

    public void setEnabled(boolean state) {
        _enabled = state;
    }

    public void calculateClient(int idx, PropertyChangeEvent evt) {
        _clients.get(idx).calculate(_enabled, evt);
    }	
	
    public void propertyChange(PropertyChangeEvent evt) {
        //log.debug("\""+_varName+"\" sent PropertyChangeEvent "+evt.getPropertyName()+
        //    ", old value =\""+evt.getOldValue()+"\", new value =\""+evt.getNewValue()+
        //    ", enabled = "+_enabled);
        for (int i=0; i<_clients.size(); i++)  {
            _clients.get(i).calculate(_enabled, evt);
        }
    }

static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JmriSimplePropertyListener.class.getName());
}
