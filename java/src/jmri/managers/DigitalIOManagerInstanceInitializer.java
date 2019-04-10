package jmri.managers;

import java.util.Arrays;
import java.util.Set;
import jmri.DigitalIO;
import jmri.DigitalIOManager;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.Manager;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide the default implementation for the DigitalIOManager.
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
 * @since 4.15.5
 */
@ServiceProvider(service = InstanceInitializer.class)
public class DigitalIOManagerInstanceInitializer extends AbstractInstanceInitializer {

    @Override
    public <T> Object getDefault(Class<T> type) {

        if (type == DigitalIOManager.class) {
            return getDigitalIOManager();
        }

        return super.getDefault(type);
    }

    @Override
    public Set<Class<?>> getInitalizes() {
        Set<Class<?>> set = super.getInitalizes();
        set.addAll(Arrays.asList(
                DigitalIOManager.class
        ));
        return set;
    }

    private DigitalIOManager getDigitalIOManager() {
        
        String beanNameDigitalIO = Bundle.getMessage("BeanNameDigitalIO");
        DefaultDigitalIOManager m =
                new DefaultDigitalIOManager(beanNameDigitalIO, Manager.DIGITALIO);

        ConnectionConfigManager ccm = InstanceManager.getDefault(ConnectionConfigManager.class);
        for (ConnectionConfig connection : ccm.getConnections()) {
            m.addManager(new AbstractManager<DigitalIO>() {

                @Override
                public String getSystemPrefix() {
                    return connection.getConnectionName();
                }

                @Override
                public char typeLetter() {
                    return 'I';
                }

                @Override
                public int getXMLOrder() {
                    return Manager.DIGITALIO;
                }

                @Override
                public String getBeanTypeHandled() {
                    return beanNameDigitalIO;
                }
            });
        }

        // Add the internal manager?
        // jmri.jmrix.internal.InternalDigitalIOManager
        // See: jmri.jmrix.internal.InternalLightManager

        // Add turnout manager
        Manager<jmri.Turnout> mt = InstanceManager.getDefault(TurnoutManager.class);
        m.addManager(new IOManagerProxy<>(mt));

        // Add sensor manager
        Manager<jmri.Sensor> ms = InstanceManager.getDefault(SensorManager.class);
        m.addManager(new IOManagerProxy<>(ms));

        // Add light manager
        Manager<jmri.Light> ml = InstanceManager.getDefault(LightManager.class);
        m.addManager(new IOManagerProxy<>(ml));

        return m;
    }
    
}
