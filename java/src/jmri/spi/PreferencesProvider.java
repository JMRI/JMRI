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
     * initializing this PreferencesProvider.
     *
     * @return An set or list of class names. If there are no dependencies,
     *         return an empty set instead of null.
     */
    public @Nonnull
    Iterable<String> getRequires();

    /**
     * Save the preferences that this provider manages for the provided Profile.
     *
     * @param profile
     */
    public void savePreferences(Profile profile);

}
