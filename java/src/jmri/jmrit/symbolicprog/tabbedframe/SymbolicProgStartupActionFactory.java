package jmri.jmrit.symbolicprog.tabbedframe;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for symbolic programmer startup actions.
 * 
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class SymbolicProgStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(PaneOpsProgAction.class)) {
            return Bundle.getMessage(locale, "StartupPaneOpsProgAction");
        } else if (clazz.equals(PaneProgAction.class)) {
            return Bundle.getMessage(locale, "StartupPaneProgAction");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{PaneOpsProgAction.class, PaneProgAction.class};
    }
    
}
