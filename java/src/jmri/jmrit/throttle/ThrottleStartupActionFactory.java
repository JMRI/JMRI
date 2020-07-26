package jmri.jmrit.throttle;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for Throttle actions.
 * 
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class ThrottleStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(LoadDefaultXmlThrottlesLayoutAction.class)) {
            return Bundle.getMessage(locale, "StartupLoadDefaultXmlThrottlesLayoutAction");
        } else if (clazz.equals(ThrottleCreationAction.class)) {
            return Bundle.getMessage(locale, "MenuItemNewThrottle");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{LoadDefaultXmlThrottlesLayoutAction.class, ThrottleCreationAction.class};
    }
    
}
