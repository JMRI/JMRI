package jmri.jmrit.z21server;


import jmri.jmrit.z21server.Bundle;
import jmri.jmrit.z21server.z21serverCreationAction;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

import java.util.Locale;

/**
 * Factory for roster startup actions.
 *
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class z21serverStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(z21serverCreationAction.class)) {
            return Bundle.getMessage(locale, "MenuStartServer");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{z21serverCreationAction.class};
    }

}
