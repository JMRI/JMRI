package apps;

import apps.startup.AbstractStartupActionFactory;
import java.util.Locale;

/**
 * Factory to register Restart action as a startup action.
 *
 * @author Randall Wood Copyright (C) 2016
 */
public class RestartStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (!clazz.equals(RestartAction.class)) {
            throw new IllegalArgumentException();
        }
        return Bundle.getMessage(locale, "RestartAction"); // NOI18N
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{RestartAction.class};
    }

}
