package jmri.jmrix.bachrus;

import jmri.util.startup.AbstractStartupActionFactory;
import java.util.Locale;

/**
 * {@link apps.startup.StartupActionFactory} for the
 * {@link jmri.jmrix.bachrus.SpeedoConsoleAction}.
 * 
 * @author Todd Wegter Copyright (C) 2019
 */
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