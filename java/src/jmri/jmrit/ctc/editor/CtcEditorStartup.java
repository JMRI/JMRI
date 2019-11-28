package jmri.jmrit.ctc.editor;

import apps.startup.AbstractStartupActionFactory;
import apps.startup.StartupActionFactory;
import java.util.Locale;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link apps.startup.StartupActionFactory} for the
 * {@link jmri.jmrit.ctc.editor.CtcEditorAction}.
 *
 * @author Dave Sand Copyright (C) 2018
 */
@ServiceProvider(service = StartupActionFactory.class)
public class CtcEditorStartup extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (!clazz.equals(CtcEditorAction.class)) {
            throw new IllegalArgumentException();
        }
        return Bundle.getMessage(locale, "CtcEditorAction"); // NOI18N
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{CtcEditorAction.class};
    }

}
