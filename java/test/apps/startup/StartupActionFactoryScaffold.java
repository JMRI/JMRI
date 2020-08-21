package apps.startup;

import java.util.Locale;
import org.openide.util.lookup.ServiceProvider;

/**
 * The purpose of this class is to test that a class that extends
 * apps.startup.AbstractStartupActionFactory can use the service
 * provider apps.startup.StartupActionFactory.
 * 
 * This class will be removed when apps.startup.StartupActionFactory
 * and apps.startup.AbstractStartupActionFactory is removed.
 * 
 * @author Daniel Bergqvist Copyright (C) 2020
 */
@SuppressWarnings("deprecation")
@ServiceProvider(service = StartupActionFactory.class)
public class StartupActionFactoryScaffold extends AbstractStartupActionFactory {

    @Override
    public String getTitle(Class<?> clazz, Locale locale) throws IllegalArgumentException {
        if (!clazz.equals(String.class)) {
            throw new IllegalArgumentException();
        }
        return "String"; // NOI18N
    }

    @Override
    public Class<?>[] getActionClasses() {
        return new Class[]{String.class};
    }

}

