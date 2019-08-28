package jmri.jmrit.logixng.ztest;

import apps.startup.AbstractStartupActionFactory;
import apps.startup.StartupActionFactory;
import java.util.Locale;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link apps.startup.StartupActionFactory} for the
 * {@link jmri.jmrit.timetable.swing.TimeTableAction}.
 *
 * @author Dave Sand Copyright (C) 2018
 */
@ServiceProvider(service = StartupActionFactory.class)
public class LogixNG_Startup extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (!clazz.equals(LogixNG_StartupAction.class)) {
            throw new IllegalArgumentException();
        }
        return Bundle.getMessage(locale, "LogixNG_TestAction"); // NOI18N
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{LogixNG_StartupAction.class};
    }

}
