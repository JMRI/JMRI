package jmri.managers;

import java.util.Arrays;
import java.util.Set;
import jmri.AnalogIO;
import jmri.AnalogIOManager;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide the default implementation for the AnalogIOManager.
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
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2014
 * @author Daniel Bergqvist 2019
 * @since 3.15.5
 */
@ServiceProvider(service = InstanceInitializer.class)
public class AnalogIOManagerInstanceInitializer extends AbstractInstanceInitializer {

    @Override
    public <T> Object getDefault(Class<T> type) {

        if (type == AnalogIOManager.class) {
            return getAnalogIOManager();
        }

        return super.getDefault(type);
    }

    @Override
    public Set<Class<?>> getInitalizes() {
        Set<Class<?>> set = super.getInitalizes();
        set.addAll(Arrays.asList(
                AnalogIOManager.class
        ));
        return set;
    }

    private AnalogIOManager getAnalogIOManager() {
        
        String beanNameAnalogIO = Bundle.getMessage("BeanNameAnalogIO");
        DefaultAnalogIOManager m =
                new DefaultAnalogIOManager(beanNameAnalogIO, Manager.DIGITALIO);

        ConnectionConfigManager ccm = InstanceManager.getDefault(ConnectionConfigManager.class);
        for (ConnectionConfig connection : ccm.getConnections()) {
            m.addManager(new AbstractManager<AnalogIO>() {

                @Override
                public String getSystemPrefix() {
                    return connection.getConnectionName();
                }

                @Override
                public char typeLetter() {
                    return 'A';
                }

                @Override
                public int getXMLOrder() {
                    return Manager.ANALOGIO;
                }

                @Override
                public String getBeanTypeHandled() {
                    return beanNameAnalogIO;
                }
            });
        }

        // Add the internal manager?
        // jmri.jmrix.internal.InternalAnalogIOManager
        // See: jmri.jmrix.internal.InternalLightManager

        // The following are examples from DigitalIOManagerInstanceInitializer
        // and are kept as examples if managers are created for types that
        // extends AnalogIO.

        // Add turnout manager
//        Manager<jmri.Turnout> mt = InstanceManager.getDefault(TurnoutManager.class);
//        m.addManager(new AdapterManager<>(mt));

        // Add sensor manager
//        Manager<jmri.Sensor> ms = InstanceManager.getDefault(SensorManager.class);
//        m.addManager(new AdapterManager<>(ms));

        // Add light manager
//        Manager<jmri.Light> ml = InstanceManager.getDefault(LightManager.class);
//        m.addManager(new AdapterManager<>(ml));

        return m;
    }

}
