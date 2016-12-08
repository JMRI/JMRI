package apps.startup;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Startup object models all need to implement this interface. This allows the
 * {@link apps.StartupActionsManager} to handle lists of different model
 * classes.
 *
 * @author Randall Wood (C) 2015
 */
public interface StartupModel {

    /**
     * Return the name of of the model or its controlled object.
     *
     * @return the name, an empty string, or null
     */
    @CheckForNull
    public String getName();

    /**
     * Set the name of the model.
     *
     * @param name the name, an empty string, or null
     */
    public void setName(@Nullable String name);

    /**
     * Test is model is a valid model. Invalid models will not be shown or saved
     * by {@link apps.startup.StartupActionsPreferencesPanel}.
     *
     * @return true if valid; false otherwise
     */
    public boolean isValid();
}
