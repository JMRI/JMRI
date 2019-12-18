package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import jmri.Conditional;
import jmri.NamedBean;
import jmri.NamedBeanHandle;

/**
 * A service base class for monitoring a bound property in one of the JMRI Named
 * beans (Turnout, Sensor, etc).
 * <p>
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
 * @author Pete Cressman Copyright (C) 2009
 * @since 2.5.1
 */
public class JmriSimplePropertyListener implements PropertyChangeListener {

    int _type;
    String _varName;
    Conditional.Type _varType;
    String _propertyName;
    ArrayList<Conditional> _clients;
    boolean _enabled;
    NamedBeanHandle<?> _namedBean;

    JmriSimplePropertyListener(String propName, int type, String varName, Conditional.Type varType, Conditional client) {
        _propertyName = propName;
        _type = type;
        _varName = varName;
        _varType = varType;
        _clients = new ArrayList<Conditional>();
        _clients.add(client);
        _enabled = true;
    }

    JmriSimplePropertyListener(String propName, int type, NamedBeanHandle<?> namedBean, Conditional.Type varType, Conditional client) {
        _propertyName = propName;
        _type = type;
        _namedBean = namedBean;
        _varType = varType;
        _clients = new ArrayList<Conditional>();
        _clients.add(client);
        _enabled = true;
    }

    NamedBeanHandle<?> getNamedBean() {
        return _namedBean;
    }

    public NamedBean getBean() {
        if (_namedBean != null) {
            return  _namedBean.getBean();
        }
        return null;
    }

    public int getType() {
        return _type;
    }

    public String getPropertyName() {
        return _propertyName;
    }

    public Conditional.Type getVarType() {
        return _varType;
    }

    public String getDevName() {
        if (_namedBean != null) {
            return _namedBean.getName();
        }
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

    /**
     * When _enabled is false, Conditional.calculate will compute the state of
     * the conditional, but will not trigger its actions. When _enabled is true,
     * Conditional.calculates its state and trigger its actions if its state has
     * changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        //log.debug("\""+_varName+"\" sent PropertyChangeEvent "+evt.getPropertyName()+
        //    ", old value =\""+evt.getOldValue()+"\", new value =\""+evt.getNewValue()+
        //    ", enabled = "+_enabled);
        Object newValue = evt.getNewValue();
        if (newValue != null && newValue.equals(evt.getOldValue())) {
            return;
        }
        for (int i = 0; i < _clients.size(); i++) {
            _clients.get(i).calculate(_enabled, evt);
        }
    }
}
