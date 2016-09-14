package jmri.jmrit;

import apps.startup.AbstractStartupActionFactory;
import java.util.Locale;

/**
 * {@link apps.startup.StartupActionFactory} implementation to allow
 * {@link jmri.jmrit.XmlFileValidateAction} to be included as a startup action.
 *
 * @author Randall Wood Copyright (C) 2016
 */
public class XmlFileValidateStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (!clazz.equals(XmlFileValidateAction.class)) {
            throw new IllegalArgumentException();
        }
        return Bundle.getMessage(locale, "XmlFileValidateAction"); // NOI18N
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{XmlFileValidateAction.class};
    }
    
}
