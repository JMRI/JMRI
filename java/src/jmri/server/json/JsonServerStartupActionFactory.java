package jmri.server.json;

import java.util.Locale;
import jmri.util.startup.AbstractStartupActionFactory;
import jmri.util.startup.StartupActionFactory;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StartupActionFactory.class)
public final class JsonServerStartupActionFactory extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) {
        if (clazz.equals(JsonServerAction.class)) {
            return Bundle.getMessage(locale, "StartJsonServerAction"); // NOI18N
        }
        throw new IllegalArgumentException(clazz.getName() + " is not supported by " + this.getClass().getName());
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
