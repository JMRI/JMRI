package jmri.managers;

import java.util.Arrays;
import java.util.Set;
import jmri.StringIO;
import jmri.StringIOManager;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide the default implementation for the StringIOManager.
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
public class StringIOManagerInstanceInitializer extends AbstractInstanceInitializer {

    @Override
    public <T> Object getDefault(Class<T> type) {

        if (type == StringIOManager.class) {
            return getStringIOManager();
        }

        return super.getDefault(type);
    }

    @Override
    public Set<Class<?>> getInitalizes() {
        Set<Class<?>> set = super.getInitalizes();
        set.addAll(Arrays.asList(
                StringIOManager.class
        ));
        return set;
    }

    private StringIOManager getStringIOManager() {
        
        String beanNameStringIO = Bundle.getMessage("BeanNameStringIO");
        DefaultStringIOManager m =
                new DefaultStringIOManager(beanNameStringIO, Manager.STRINGIO);

        ConnectionConfigManager ccm = InstanceManager.getDefault(ConnectionConfigManager.class);
        for (ConnectionConfig connection : ccm.getConnections()) {
            m.addManager(new AbstractManager<StringIO>() {

                @Override
                public String getSystemPrefix() {
                    return connection.getConnectionName();
                }

                @Override
                public char typeLetter() {
                    return 'Z';
                }

                @Override
                public int getXMLOrder() {
                    return Manager.STRINGIO;
                }

                @Override
                public String getBeanTypeHandled() {
                    return beanNameStringIO;
                }
            });
        }

        // Add the internal manager?
        // jmri.jmrix.internal.InternalStringIOManager
        // See: jmri.jmrix.internal.InternalLightManager

        // The following are examples from DigitalIOManagerInstanceInitializer
        // and are kept as examples if managers are created for types that
        // extends StringIO.

        // Add turnout manager
//        Manager<jmri.Turnout> mt = InstanceManager.getDefault(TurnoutManager.class);
//        m.addManager(new IOManagerProxy<>(mt));

        // Add sensor manager
//        Manager<jmri.Sensor> ms = InstanceManager.getDefault(SensorManager.class);
//        m.addManager(new IOManagerProxy<>(ms));

        // Add light manager
//        Manager<jmri.Light> ml = InstanceManager.getDefault(LightManager.class);
//        m.addManager(new IOManagerProxy<>(ml));

        return m;
    }

}
