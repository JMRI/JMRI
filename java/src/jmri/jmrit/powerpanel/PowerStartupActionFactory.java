package jmri.jmrit.powerpanel;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for LCD Clock startup actions.
 * 
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class PowerStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(PowerButtonAction.class)) {
            return Bundle.getMessage(locale, "StartupPowerButtonAction");
        } else if (clazz.equals(PowerPanelAction.class)) {
            return Bundle.getMessage(locale, "StartupPowerPanelAction");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{PowerButtonAction.class, PowerPanelAction.class};
    }
    
}
