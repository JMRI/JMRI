package jmri.configurexml;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for XML file loading startup actions.
 *
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class ConfigureXmlStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(LoadXmlConfigAction.class)) {
            return Bundle.getMessage(locale, "FileMenuItemLoad");  // NOI18N
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());  // NOI18N
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{LoadXmlConfigAction.class};
    }

}
