package jmri.jmrit.nixieclock;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for Jython startup actions.
 * 
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class NixieClockStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(NixieClockAction.class)) {
            return Bundle.getMessage(locale, "StartupNixieClockAction");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{NixieClockAction.class};
    }
    
}
