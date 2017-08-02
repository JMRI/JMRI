package jmri.jmrit.vsdecoder;

import apps.startup.AbstractStartupActionFactory;
<<<<<<< HEAD
import java.util.Locale;
import jmri.jmrit.vsdecoder.VSDecoderCreationAction;
import jmri.jmrit.vsdecoder.Bundle;
=======
import apps.startup.StartupActionFactory;
import java.util.Locale;
import org.openide.util.lookup.ServiceProvider;
>>>>>>> JMRI/master

/**
 * {@link apps.startup.StartupActionFactory} for the
 * {@link jmri.jmrit.roster.swing.RosterFrameAction}.
 *
 * @author Randall Wood Copyright (C) 2016
 */
<<<<<<< HEAD
=======
@ServiceProvider(service = StartupActionFactory.class)
>>>>>>> JMRI/master
public class VSDecoderCreationStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (!clazz.equals(VSDecoderCreationAction.class)) {
            throw new IllegalArgumentException();
        }
        return Bundle.getMessage(locale, "VSDStartupActionText"); // NOI18N
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{VSDecoderCreationAction.class};
    }

}
