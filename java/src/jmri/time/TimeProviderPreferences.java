package jmri.time;

/**
 * TimeProvider preferences.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public interface TimeProviderPreferences {

    /**
     * Compare if the values are different from the other preferences.
     * @param prefs the other preferences to check
     * @return true if preferences differ, false otherwise
     */
    boolean compareValuesDifferent(TimeProviderPreferences prefs);

    /**
     * Apply other preferences to this class
     * @param prefs the other preferences
     */
    void apply(TimeProviderPreferences prefs);

    /**
     * Save the preferences
     */
    void save();

    /**
     * Set whenether TimeProvider should be enabled for the user.
     * TimeProvider is always used behind the scenes, but if it's enabled, more
     * features are enabled. The downside is that a tables and panels file with
     * TimeProvider enabled cannot be read by a JMRI version that doesn't
     * support TimeProvider.
     * @param value true if TimeProvider should be enabled, false otherwise
     */
    void setEnableTimeProvider(boolean value);

    /**
     * Get whenether TimeProvider should be enabled for the user.
     * TimeProvider is always used behind the scenes, but if it's enabled, more
     * features are enabled. The downside is that a tables and panels file with
     * TimeProvider enabled cannot be read by a JMRI version that doesn't
     * support TimeProvider.
     * @return true if TimeProvider should be enabled, false otherwise
     */
    boolean getEnableTimeProvider();

}
