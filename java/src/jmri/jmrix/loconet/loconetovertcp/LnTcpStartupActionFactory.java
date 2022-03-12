package jmri.jmrix.loconet.loconetovertcp;

import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import java.util.Locale;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link jmri.util.startup.StartupActionFactory} for the
 * {@link jmri.jmrix.loconet.loconetovertcp.LnTcpServerAction}.
 *
 * @author Randall Wood Copyright (C) 2017
 */
@ServiceProvider(service = StartupActionFactory.class)
public final class LnTcpStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(LnTcpServerAction.class)) {
            return Bundle.getMessage(locale, "StartServerAction"); // NOI18N
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class<?>[]{LnTcpServerAction.class};
    }

}

