package jmri.managers;

import java.util.Arrays;
import java.util.Set;
import jmri.UserPreferencesManager;
import jmri.implementation.AbstractInstanceInitializer;

/**
 * Factory for the default instance of the {@link UserPreferencesManager} and
 * {@link JmriUserPreferencesManager}.
 *
 * @author Randall Wood Copyright 2017
 */
public class DefaultUserPreferencesManagerFactory extends AbstractInstanceInitializer {

    @Override
    public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
        if (type == UserPreferencesManager.class) {
            JmriUserPreferencesManager manager = new JmriUserPreferencesManager();
            manager.readUserPreferences();
            return manager;
        }

        return super.getDefault(type);
    }

    @Override
    public Set<Class<?>> getInitalizes() {
        Set<Class<?>> set = super.getInitalizes();
        set.addAll(Arrays.asList(UserPreferencesManager.class));
        return set;
    }

}
