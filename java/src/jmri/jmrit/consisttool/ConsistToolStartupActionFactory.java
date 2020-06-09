package jmri.jmrit.consisttool;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for consist tool startup actions.
 * 
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class ConsistToolStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(ConsistToolAction.class)) {
            return Bundle.getMessage(locale, "StartupConsistToolAction");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{ConsistToolAction.class};
    }
    
}
