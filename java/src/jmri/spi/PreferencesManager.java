package jmri.spi;

import javax.annotation.Nonnull;
import jmri.profile.Profile;
import jmri.util.prefs.InitializationException;

/**
 * An API for Java Service Providers that manage preferences within JMRI. It is
 * strongly recommended that PreferencesManagers use
 * {@link jmri.util.prefs.JmriConfigurationProvider} or
 * {@link jmri.util.prefs.JmriPreferencesProvider} to store preferences.
 *
 * PreferencesManagers must provide a default public constructor, but must also
 * not perform any initialization until
 * {@link #initialize(jmri.profile.Profile)} is called as the PreferencesManager
 * may be constructed before the {@link jmri.profile.Profile} is known.
 *
 * @see {@link jmri.util.prefs.AbstractPreferencesMananger} for an abstract
 * implementation that is ready to extend.
 * @author Randall Wood 2015
 */
public interface PreferencesManager extends JmriServiceProviderInterface {

    /**
     * Initialize the PreferencesManager with preferences associated with the
     * provided Profile.
     *
     * Implementing classes should throw an InitializationException with a user
     * readable localized message, since it most likely be displayed to the
     * user. Implementing classes will still want to ensure that isInitialized
     * returns true if throwing an InitializationException to ensure that the
     * provider is not repeatedly initialized.
     *
     * @param profile
     * @throws jmri.util.prefs.InitializationException
     */
    public void initialize(@Nonnull Profile profile) throws InitializationException;

    /**
     * Test if the PreferencesManager is initialized for the provided Profile.
     *
     * @param profile
     * @return true if the provider is initialized.
     */
    public boolean isInitialized(@Nonnull Profile profile);

    /**
     * Get the set of PreferencesProviders that must be initialized prior to
     * initializing this PreferencesManager. It is generally preferable to
     * require an Interface or an abstract Class instead of a concrete Class,
     * since that allows all (or any) concrete implementations of the required
     * class to be initialized to provide required services for the requiring
     * PreferencesManager instance.
     *
     * @return An set or list of classes. If there are no dependencies, return
     *         an empty set instead of null.
     */
    public @Nonnull
    Iterable<Class<? extends PreferencesManager>> getRequires();

    /**
     * Get the set of Classes that this PreferencesManager can be registered as
     * a provider of in the {@link jmri.InstanceManager}.
     *
     * @return An set or list of classes. If this PreferencesManager provides an
     *         instance of no other Interfaces or abstract Classes than
     *         PreferencesManager, return an empty set instead of null.
     */
    public @Nonnull
    Iterable<Class<?>> getProvides();

    /**
     * Save the preferences that this provider manages for the provided Profile.
     *
     * @param profile
     */
    public void savePreferences(@Nonnull Profile profile);

}
