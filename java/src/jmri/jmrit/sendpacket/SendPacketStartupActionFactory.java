package jmri.jmrit.sendpacket;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for roster startup actions.
 * 
 * @author Randall Wood Copyright 2020
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class SendPacketStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(SendPacketAction.class)) {
            return Bundle.getMessage(locale, "StartupSendPacketAction");
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{SendPacketAction.class};
    }
    
}
