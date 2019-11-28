package apps.startup;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.JmriException;

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
    public void setName(@CheckForNull String name);

    /**
     * Test is model is a valid model. Invalid models will not be shown or saved
     * by {@link apps.startup.StartupActionsPreferencesPanel}.
     *
     * @return true if valid; false otherwise
     */
    public boolean isValid();

    /**
     * Perform the startup action.
     *
     * @throws jmri.JmriException if there is an exception thrown initializing
     *                            the startup item
     */
    public void performAction() throws JmriException;

    /**
     * Get the exceptions thrown by the startup model.
     *
     * @return the list of exceptions thrown during startup in order or an empty
     *         list if no exceptions were thrown
     */
    @Nonnull
    public List<Exception> getExceptions();

    /**
     * Add an exception to the list of exceptions thrown when loading the model
     * or performing the action.
     *
     * @param exception the exception to retain with the model
     */
    public void addException(@Nonnull Exception exception);
}
