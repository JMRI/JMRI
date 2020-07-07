package jmri.jmrit.blockboss;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for block boss startup actions.
 * 
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class BlockBossStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) {
        if (clazz.equals(BlockBossAction.class)) {
            return Bundle.getMessage(locale, "StartupBlockBossAction");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{BlockBossAction.class};
    }
    
}
