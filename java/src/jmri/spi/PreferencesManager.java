package jmri.spi;

import java.util.List;
import javax.annotation.Nonnull;
import jmri.profile.Profile;
import jmri.util.prefs.InitializationException;

/**
 * An API for Java Service Providers that manage preferences within JMRI. It is
 * strongly recommended that PreferencesManagers use
 * {@link jmri.util.prefs.JmriConfigurationProvider} or
 * {@link jmri.util.prefs.JmriPreferencesProvider} to store preferences.
 * <p>
 * PreferencesManagers must provide a default public constructor, but must also
 * not perform any initialization until
 * {@link #initialize(jmri.profile.Profile)} is called as the PreferencesManager
 * may be constructed before the {@link jmri.profile.Profile} is known.
 * <p>
 * {@link jmri.util.prefs.AbstractPreferencesManager} provides an abstract
 * implementation that is ready to extend.
 *
 * @see jmri.util.prefs.AbstractPreferencesManager
 * @author Randall Wood 2015, 2019
 */
public interface PreferencesManager extends JmriServiceProviderInterface {

    /**
     * Initialize the PreferencesManager with preferences associated with the
     * provided Profile.
     * <p>
     * Implementing classes should throw an InitializationException with a user
     * readable localized message, since it most likely be displayed to the
     * user. Implementing classes will still want to ensure that
     * {@link #isInitialized(jmri.profile.Profile)} or
     * {@link #isInitializedWithExceptions(jmri.profile.Profile)} return true if
     * throwing an InitializationException to ensure that the provider is not
     * repeatedly initialized.
     *
     * @param profile the configuration profile used for this initialization;
     *                    may be null to initialize for this user regardless of
     *                    profile
     * @throws jmri.util.prefs.InitializationException if the user needs to be
     *                                                     notified of an issue
     *                                                     that prevents regular
     *                                                     use of the
     *                                                     application
     */
    public void initialize(Profile profile) throws InitializationException;

    /**
     * Test if the PreferencesManager is initialized without errors for the
     * provided Profile. Note that although both this method and
     * {@link #isInitializedWithExceptions(jmri.profile.Profile)} can be false,
     * if isInitializedWithExceptions(Profile) returns true, this method must
     * return false.
     *
     * @param profile the configuration profile to test against; may be null to
     *                    test for exceptions thrown when initializing for this
     *                    user regardless of profile
     * @return true if the provider is initialized without exceptions
     */
    public boolean isInitialized(Profile profile);

    /**
     * Test if the PreferencesManager is initialized, but threw an
     * {@link InitializationException} during initialization, for the provided
     * Profile. Note that although both this method and
     * {@link #isInitialized(jmri.profile.Profile)} can be false, if
     * isInitialized(Profile) returns true, this method must return false.
     *
     * @param profile the configuration profile to test against; may be null to
     *                    test for exceptions thrown when initializing for this
     *                    user regardless of profile
     * @return true if the provide is initialized with exceptions
     */
    public boolean isInitializedWithExceptions(Profile profile);

    /**
     * Get the set of exceptions thrown during initialization for the provided
     * Profile.
     *
     * @param profile the configuration profile to test against; may be null to
     *                    test for exceptions thrown when initializing for this
     *                    user regardless of profile
     * @return A list of exceptions. If there are no exceptions, return an empty
     *         set instead of null.
     */
    @Nonnull
    List<Exception> getInitializationExceptions(Profile profile);

    /**
     * Get the set of PreferencesProviders that must be initialized prior to
     * initializing this PreferencesManager. It is generally preferable to
     * require an Interface or an abstract Class instead of a concrete Class,
     * since that allows all (or any) concrete implementations of the required
     * class to be initialized to provide required services for the requiring
     * PreferencesManager instance.
     *
     * @return A set or list of classes. If there are no dependencies, return an
     *         empty set instead of null.
     */
    @Nonnull
    public Iterable<Class<? extends PreferencesManager>> getRequires();

    /**
     * Get the set of Classes that this PreferencesManager can be registered as
     * a provider of in the {@link jmri.InstanceManager}.
     *
     * @return A set or list of classes. If this PreferencesManager provides an
     *         instance of no other Interfaces or abstract Classes than
     *         PreferencesManager, return an empty set instead of null.
     */
    @Nonnull
    public Iterable<Class<?>> getProvides();

    /**
     * Save the preferences that this provider manages for the provided Profile.
     *
     * @param profile the profile associated with the preferences to save; may
     *                    be null to save preferences that apply to the current
     *                    user regardless of profile
     */
    public void savePreferences(Profile profile);

}
