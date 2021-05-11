package jmri.jmrit.logixng.implementation;

import java.util.Arrays;
import java.util.Set;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.jmrit.logixng.*;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide the usual default implementations for the
 * {@link jmri.InstanceManager}.
 * <P>
 * Not all {@link jmri.InstanceManager} related classes are provided by this
 * class. See the discussion in {@link jmri.InstanceManager} of initialization
 * methods.
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
 * @since 2.9.4
 */
@ServiceProvider(service = InstanceInitializer.class)
public class LogixNG_InstanceInitializer extends AbstractInstanceInitializer {

    @Override
    public <T> Object getDefault(Class<T> type) {
        
        // In order for getDefault() to be called for a particular manager,
        // the manager also needs to be added to the method getInitalizes()
        // below.

        if (type == ConditionalNG_Manager.class) {
            return new DefaultConditionalNGManager();
        }

        if (type == FemaleSocketManager.class) {
            return new DefaultFemaleSocketManager();
        }

        if (type == LogixNG_InitializationManager.class) {
            return new DefaultLogixNGInitializationManager();
        }

        if (type == LogixNG_Manager.class) {
            return new DefaultLogixNGManager();
        }

        if (type == LogixNGPreferences.class) {
            return new DefaultLogixNGPreferences();
        }

        if (type == ModuleManager.class) {
            return new DefaultModuleManager();
        }

        if (type == NamedTableManager.class) {
            return new DefaultNamedTableManager();
        }

        return super.getDefault(type);
    }

    @Override
    public Set<Class<?>> getInitalizes() {
        Set<Class<?>> set = super.getInitalizes();
        set.addAll(Arrays.asList(
                ConditionalNG_Manager.class,
                FemaleSocketManager.class,
                LogixNG_InitializationManager.class,
                LogixNG_Manager.class,
                LogixNGPreferences.class,
                ModuleManager.class,
                NamedTableManager.class
        ));
        return set;
    }

}
