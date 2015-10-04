package jmri.spi;

import javax.annotation.Nonnull;
import jmri.profile.Profile;

/**
 * An API for Java Service Providers that manage preferences within JMRI. It is
 * strongly recommended that PreferencesProviders use
 * {@link jmri.util.prefs.JmriConfigurationProvider} or
 * {@link jmri.util.prefs.JmriPreferencesProvider} to store preferences.
 *
 * PreferencesProviders must provide a default public constructor, but must also
 * not perform any initialization until
 * {@link #initialize(jmri.profile.Profile)} is called as the
 * PreferencesProvider may be constructed before the
 * {@link jmri.profile.Profile} is known.
 *
 * @author Randall Wood 2015
 */
public interface PreferencesProvider {

    /**
     * Initialize the PreferencesProvider with preferences associated with the
     * provided Profile.
     *
     * @param profile
     */
    public void initialize(Profile profile);

    /**
     * Test if the PreferencesProvider is initialized for the provided Profile.
     *
     * @param profile
     * @return true if the provider is initialized.
     */
    public boolean isInitialized(Profile profile);

    /**
     * Get the set of PreferencesProviders that must be initialized prior to
     * initializing this PreferencesProvider. It is generally preferable to
     * require an Interface or an abstract Class instead of a concrete Class,
     * since that allows all (or any) concrete implementations of the required
     * class to be initialized to provide required services for the requiring
     * PreferencesProvider instance.
     *
     * @return An set or list of classes. If there are no dependencies, return
     *         an empty set instead of null.
     */
    public @Nonnull
    Iterable<Class<? extends PreferencesProvider>> getRequires();

    /**
     * Get the set of Classes that this PreferencesProvider can be registered as
     * a provider of in the {@link jmri.InstanceManager}.
     *
     * @return An set or list of classes. If this PreferencesProvider provides
     *         an instance of no other Interfaces or abstract Classes than
     *         PreferencesProvider, return an empty set instead of null.
     */
    public @Nonnull
    Iterable<Class<?>> getProvides();

    /**
     * Save the preferences that this provider manages for the provided Profile.
     *
     * @param profile
     */
    public void savePreferences(Profile profile);

}
