package jmri.util.prefs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import jmri.beans.Bean;
import jmri.jmrix.ConnectionConfigManager;
import jmri.profile.Profile;
import jmri.spi.PreferencesManager;

/**
 * An abstract PreferencesManager that implements some of the boilerplate that
 * PreferencesManager implementations would otherwise require.
 *
 * @author Randall Wood (C) 2015
 */
public abstract class AbstractPreferencesManager extends Bean implements PreferencesManager {

    private final HashMap<Profile, Boolean> initialized = new HashMap<>();

    @Override
    public boolean isInitialized(@Nonnull Profile profile) {
        return this.initialized.getOrDefault(profile, false);
    }

    /**
     * Get the set of PreferencesProviders that must be initialized prior to
     * initializing this PreferencesManager. It is generally preferable to
     * require an Interface or an abstract Class instead of a concrete Class,
     * since that allows all (or any) concrete implementations of the required
     * class to be initialized to provide required services for the requiring
     * PreferencesManager instance.
     *
     * This implementation includes a default dependency on the
     * {@link jmri.jmrix.ConnectionConfigManager}.
     *
     * @return An set of classes. If there are no dependencies, return an empty
     *         set instead of null.
     */
    @Override
    @Nonnull
    public Set<Class<? extends PreferencesManager>> getRequires() {
        Set<Class<? extends PreferencesManager>> requires = new HashSet<>();
        requires.add(ConnectionConfigManager.class);
        return requires;
    }

    /**
     * Get the set of Classes that this PreferencesManager can be registered as
     * a provider of in the {@link jmri.InstanceManager}.
     *
     * This implementation returns the class of the object against which this
     * method is called.
     *
     * @return An set of classes. If this PreferencesManager provides an
     *         instance of no other Interfaces or abstract Classes than
     *         PreferencesManager, return an empty set instead of null.
     */
    @Override
    @Nonnull
    public Set<Class<?>> getProvides() {
        Set<Class<?>> provides = new HashSet<>();
        provides.add(this.getClass());
        return provides;
    }

    protected void setInitialized(@Nonnull Profile profile, boolean initialized) {
        this.initialized.put(profile, initialized);
    }
}
