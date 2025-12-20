package jmri.jmrit.z21server;


import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

import java.util.Locale;

/**
 * Factory for startup actions.
 *
 * @author Randall Wood Copyright 2020
 * @author Jean-Yves Roda (C) 2023
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class Z21serverStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(Z21serverCreationAction.class)) {
            return Bundle.getMessage(locale, "MenuStartServer");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{Z21serverCreationAction.class};
    }

}
