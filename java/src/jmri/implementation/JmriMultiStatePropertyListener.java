package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import jmri.Conditional;
import jmri.NamedBeanHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service base class for monitoring a bound property in one of the JMRI Named
 * beans (Turnout, Sensor, etc). For use with properties that may have more than
 * two states where the states are represented by numbers than can be cast to
 * integers.
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
public class JmriMultiStatePropertyListener extends JmriSimplePropertyListener {

    ArrayList<Integer> _states;

    JmriMultiStatePropertyListener(String propName, int type, String name, Conditional.Type varType,
            Conditional client, int state) {
        super(propName, type, name, varType, client);
        _states = new ArrayList<>();
        _states.add(Integer.valueOf(state));
    }

    JmriMultiStatePropertyListener(String propName, int type, NamedBeanHandle<?> namedBean, Conditional.Type varType,
            Conditional client, int state) {
        super(propName, type, namedBean, varType, client);
        _states = new ArrayList<>();
        _states.add(Integer.valueOf(state));
    }

    public void setState(int state) {
        _states.add(Integer.valueOf(state));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        log.debug("\"" + _varName + "\" sent PropertyChangeEvent " + evt.getPropertyName()
                + ", old value =\"" + evt.getOldValue() + "\", new value =\"" + evt.getNewValue()
                + ", enabled = " + _enabled);
        if (getPropertyName().equals(evt.getPropertyName())) {
            int newState = ((Number) evt.getNewValue()).intValue();
            int oldState = ((Number) evt.getOldValue()).intValue();
            if (newState != oldState) {
                for (int i = 0; i < _states.size(); i++) {
                    int state = _states.get(i).intValue();
                    if (oldState == state || newState == state) {
                        calculateClient(i, evt);
                    }
                }
            }
        }
    }
    private final static Logger log = LoggerFactory.getLogger(JmriMultiStatePropertyListener.class);
}
