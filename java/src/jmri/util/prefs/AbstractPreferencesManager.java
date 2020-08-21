package jmri.util.prefs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
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
    private final HashMap<Profile, Boolean> initializing = new HashMap<>();
    private final HashMap<Profile, List<Exception>> exceptions = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInitialized(Profile profile) {
        return this.initialized.getOrDefault(profile, false)
                && this.exceptions.getOrDefault(profile, new ArrayList<>()).isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInitializedWithExceptions(Profile profile) {
        return this.initialized.getOrDefault(profile, false)
                && !this.exceptions.getOrDefault(profile, new ArrayList<>()).isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<Exception> getInitializationExceptions(Profile profile) {
        return new ArrayList<>(this.exceptions.getOrDefault(profile, new ArrayList<>()));
    }

    /**
     * Test if the manager is being initialized.
     *
     * @param profile the profile against which the manager is being initialized
     *                or null if being initialized for this user regardless of
     *                profile
     * @return true if being initialized; false otherwise
     */
    protected boolean isInitializing(Profile profile) {
        return !this.initialized.getOrDefault(profile, false) && this.initializing.getOrDefault(profile, false);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation includes a default dependency on the
     * {@link jmri.jmrix.ConnectionConfigManager}.
     *
     * @return An set of classes; if there are no dependencies, return an empty
     *         set instead of null; overriding implementations may add to this
     *         set directly
     */
    @Override
    @Nonnull
    public Set<Class<? extends PreferencesManager>> getRequires() {
        Set<Class<? extends PreferencesManager>> requires = new HashSet<>();
        requires.add(ConnectionConfigManager.class);
        return requires;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns the class of the object against which this
     * method is called.
     */
    @Override
    @Nonnull
    public Set<Class<?>> getProvides() {
        Set<Class<?>> provides = new HashSet<>();
        provides.add(this.getClass());
        return provides;
    }

    /**
     * Set the initialized state for the given profile. Sets
     * {@link #isInitializing(jmri.profile.Profile)} to false if setting
     * initialized to false.
     *
     * @param profile     the profile to set initialized against
     * @param initialized the initialized state to set
     */
    protected void setInitialized(Profile profile, boolean initialized) {
        this.initialized.put(profile, initialized);
        if (initialized) {
            this.setInitializing(profile, false);
        }
    }

    /**
     * Protect against circular attempts to initialize during initialization.
     *
     * @param profile      the profile for which initializing is ongoing
     * @param initializing the initializing state to set
     */
    protected void setInitializing(Profile profile, boolean initializing) {
        this.initializing.put(profile, initializing);
    }

    /**
     * Add an error to the list of exceptions.
     *
     * @param profile   the profile against which the manager is being
     *                  initialized
     * @param exception the exception to add
     */
    protected void addInitializationException(Profile profile, @Nonnull Exception exception) {
        if (this.exceptions.get(profile) == null) {
            this.exceptions.put(profile, new ArrayList<>());
        }
        this.exceptions.get(profile).add(exception);
    }

    /**
     * Require that instances of the specified classes have initialized
     * correctly. This method should only be called from within
     * {@link #initialize(jmri.profile.Profile)}, generally immediately after
     * the PreferencesManager verifies that it is not already initialized. If
     * this method is within a try-catch block, the exception it generates
     * should be re-thrown by initialize(profile).
     *
     * @param profile the profile against which the manager is being initialized
     * @param classes the manager classes for which all calling
     *                {@link #isInitialized(jmri.profile.Profile)} must return
     *                true against all instances of
     * @param message the localized message to display if an
     *                InitializationExcpetion is thrown
     * @throws InitializationException  if any instance of any class in classes
     *                                  returns false on isIntialized(profile)
     * @throws IllegalArgumentException if any member of classes is not also in
     *                                  the set of classes returned by
     *                                  {@link #getRequires()}
     */
    protected void requiresNoInitializedWithExceptions(Profile profile,
            @Nonnull Set<Class<? extends PreferencesManager>> classes, @Nonnull String message)
            throws InitializationException {
        classes.stream().filter((clazz) -> (!this.getRequires().contains(clazz))).forEach((clazz) -> {
            throw new IllegalArgumentException(
                    "Class " + clazz.getClass().getName() + " not marked as required by " + this.getClass().getName());
        });
        for (Class<? extends PreferencesManager> clazz : classes) {
            for (PreferencesManager instance : InstanceManager.getList(clazz)) {
                if (instance.isInitializedWithExceptions(profile)) {
                    InitializationException exception = new InitializationException("Refusing to initialize", message);
                    this.addInitializationException(profile, exception);
                    this.setInitialized(profile, true);
                    throw exception;
                }
            }
        }
    }

    /**
     * Require that instances of the specified classes have initialized
     * correctly. This method should only be called from within
     * {@link #initialize(jmri.profile.Profile)}, generally immediately after
     * the PreferencesManager verifies that it is not already initialized. If
     * this method is within a try-catch block, the exception it generates
     * should be re-thrown by initialize(profile). This calls
     * {@link #requiresNoInitializedWithExceptions(jmri.profile.Profile, java.util.Set, java.lang.String)}
     * with the result of {@link #getRequires()} as the set of classes to
     * require.
     *
     * @param profile the profile against which the manager is being initialized
     * @param message the localized message to display if an
     *                InitializationExcpetion is thrown
     * @throws InitializationException if any instance of any class in classes
     *                                 returns false on isIntialized(profile)
     */
    protected void requiresNoInitializedWithExceptions(Profile profile, @Nonnull String message)
            throws InitializationException {
        this.requiresNoInitializedWithExceptions(profile, this.getRequires(), message);
    }

    /**
     * Convenience method to allow a PreferencesManager to require all other
     * PreferencesManager in an attempt to be the last PreferencesManager
     * initialized. Use this method as the body of {@link #getRequires()}.
     * <p>
     * <strong>Note</strong> given a set of PreferencesManagers using this
     * method as the body of {@link #getRequires()}, the order in which those
     * PreferencesManagers are initialized is non-deterministic.
     *
     * @return a set of all PreferencesManagers registered with the
     *         InstanceManager except this one
     */
    @Nonnull
    protected Set<Class<? extends PreferencesManager>> requireAllOther() {
        return InstanceManager.getList(PreferencesManager.class).stream()
                .filter(pm -> !pm.equals(this))
                .map(pm -> pm.getClass())
                .collect(Collectors.toSet());
    }
}
