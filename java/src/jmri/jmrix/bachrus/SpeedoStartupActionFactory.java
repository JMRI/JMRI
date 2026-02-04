package jmri.jmrix.bachrus;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link jmri.util.startup.StartupActionFactory} for the
 * {@link jmri.jmrix.bachrus.SpeedoConsoleAction}.
 * 
 * @author Todd Wegter Copyright (C) 2022
 */
@ServiceProvider(service = StartupActionFactory.class)
public class SpeedoStartupActionFactory extends AbstractStartupActionFactory{

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (!clazz.equals(SpeedoConsoleAction.class)) {
            throw new IllegalArgumentException();
        }
        return "Open Bachrus Speedometer";
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class<?>[]{SpeedoConsoleAction.class};
    }
}
