package jmri.jmrit.simpleprog;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
@API(status = MAINTAINED)
public final class SimpleProgStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(SimpleProgAction.class)) {
            return Bundle.getMessage(locale, "StartupSimpleProgAction");
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{SimpleProgAction.class};
    }
    
}
