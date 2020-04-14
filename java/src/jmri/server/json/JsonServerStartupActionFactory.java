package jmri.server.json;

import java.util.Locale;

import apps.startup.AbstractStartupActionFactory;

public class JsonServerStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) {
        if (!clazz.equals(JsonServerAction.class)) {
            throw new IllegalArgumentException();
        }
        return Bundle.getMessage(locale, "StartJsonServerAction"); // NOI18N
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class<?>[]{JsonServerAction.class};
    }

    @Override
    public String[] getOverriddenClasses(Class<?> clazz) {
        return new String[]{"jmri.jmris.json.JsonServerAction"};
    }
}
