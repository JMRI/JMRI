package jmri.jmrit.timetable.swing;

import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import java.util.Locale;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link jmri.util.startup.StartupActionFactory} for the
 * {@link jmri.jmrit.timetable.swing.TimeTableAction}.
 *
 * @author Dave Sand Copyright (C) 2018
 */
@ServiceProvider(service = StartupActionFactory.class)
public class TimeTableStartup extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) {
        if (!clazz.equals(TimeTableAction.class)) {
            throw new IllegalArgumentException();
        }
        return Bundle.getMessage(locale, "TimeTableAction"); // NOI18N
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{TimeTableAction.class};
    }

}
