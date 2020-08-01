package jmri.jmrit;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class ToolsStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(MemoryFrameAction.class)) {
            return Bundle.getMessage(locale, "StartMemoryFrameAction");
        } else if (clazz.equals(XmlFileValidateAction.class)) {
            return Bundle.getMessage(locale, "XmlFileValidateAction");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{MemoryFrameAction.class, XmlFileValidateAction.class};
    }
    
}
