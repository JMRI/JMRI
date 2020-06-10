package apps;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Startup action factory for application actions.
 * 
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class AppsStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(CheckForUpdateAction.class)) {
            return Bundle.getMessage(locale, "StartupCheckForUpdateAction"); // NOI18N
        } else if (clazz.equals(RestartAction.class)) {
            return Bundle.getMessage(locale, "RestartAction"); // NOI18N
        } else if (clazz.equals(SystemConsoleAction.class)) {
            return Bundle.getMessage(locale, "StartupSystemConsoleAction");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{CheckForUpdateAction.class, RestartAction.class, SystemConsoleAction.class};
    }
    
}
