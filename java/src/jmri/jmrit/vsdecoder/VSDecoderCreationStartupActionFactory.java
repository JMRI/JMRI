package jmri.jmrit.vsdecoder;

import apps.startup.AbstractStartupActionFactory;
import java.util.Locale;
import jmri.jmrit.vsdecoder.VSDecoderCreationAction;
import jmri.jmrit.vsdecoder.Bundle;

/**
 * {@link apps.startup.StartupActionFactory} for the
 * {@link jmri.jmrit.roster.swing.RosterFrameAction}.
 *
 * @author Randall Wood Copyright (C) 2016
 */
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
