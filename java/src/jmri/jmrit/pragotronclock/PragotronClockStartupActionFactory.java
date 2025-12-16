package jmri.jmrit.pragotronclock;

import java.util.Locale;

import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;

import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for analog clock startup actions.
 * 
 * @author Petr Sidlo Copyright (C) 2025
 *
 * Based on Nixie clock by Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class PragotronClockStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(PragotronClockAction.class)) {
            return Bundle.getMessage(locale, "StartupPragotronClockAction");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{PragotronClockAction.class};
    }
    
}
