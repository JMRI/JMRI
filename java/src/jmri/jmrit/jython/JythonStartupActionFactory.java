package jmri.jmrit.jython;

import jmri.script.swing.InputWindowAction;

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
public final class JythonStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(InputWindowAction.class)) {
            return Bundle.getMessage(locale, "StartupInputWindowAction");
        } else if (clazz.equals(JythonWindow.class)) {
            return Bundle.getMessage(locale, "StartupJythonWindow");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{InputWindowAction.class, JythonWindow.class};
    }
    
}
