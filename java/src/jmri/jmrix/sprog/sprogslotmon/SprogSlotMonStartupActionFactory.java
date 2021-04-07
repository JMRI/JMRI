package jmri.jmrix.sprog.sprogslotmon;

import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;

import java.util.Locale;

import org.openide.util.lookup.ServiceProvider;

/**
 * {@link jmri.util.startup.StartupActionFactory} for the
 * {@link jmri.jmrix.sprog.sprogslotmon.SprogSlotMonAction}.
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class SprogSlotMonStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(SprogSlotMonAction.class)) {
            return Bundle.getMessage(locale, "StartSlotMonAction"); // NOI18N
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class<?>[]{SprogSlotMonAction.class};
    }

}

