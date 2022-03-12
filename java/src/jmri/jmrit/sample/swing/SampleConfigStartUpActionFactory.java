package jmri.jmrit.sample.swing;

import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import java.util.Locale;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link jmri.util.startup.StartupActionFactory} for the
 * {@link jmri.jmrit.roster.swing.RosterFrameAction}.
 *
 * @author Randall Wood Copyright (C) 2016
 */
@ServiceProvider(service = StartupActionFactory.class)
public class SampleConfigStartUpActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (clazz.equals(SampleConfigPane.Default.class)) {
            return "Open Sample Config"; // Bundle.getMessage(locale, "RosterFrameAction"); // NOI18N
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{SampleConfigPane.Default.class};
    }

}
