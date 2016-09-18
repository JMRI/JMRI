package jmri.jmrit.roster.swing;

import apps.startup.AbstractStartupActionFactory;
import java.util.Locale;

/**
 * {@link apps.startup.StartupActionFactory} for the
 * {@link jmri.jmrit.roster.swing.RosterFrameAction}.
 *
 * @author Randall Wood Copyright (C) 2016
 */
public class RosterFrameStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (!clazz.equals(RosterFrameAction.class)) {
            throw new IllegalArgumentException();
        }
        return Bundle.getMessage(locale, "RosterFrameAction"); // NOI18N
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{RosterFrameAction.class};
    }

}
