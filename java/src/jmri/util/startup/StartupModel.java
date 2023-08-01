package jmri.util.startup;

import java.util.List;
import javax.annotation.Nonnull;

import jmri.JmriException;

import javax.annotation.CheckForNull;

/**
 * Startup object models all need to implement this interface. This allows the
 * {@link StartupActionsManager} to handle lists of different model
 * classes.
 *
 * @author Randall Wood Copyright 2015, 2020
 */
public interface StartupModel {

    /**
     * Return the name of of the model or its controlled object.
     *
     * @return the name, an empty string, or null
     */
    @CheckForNull
    String getName();

    /**
     * Set the name of the model.
     *
     * @param name the name, an empty string, or null
     */
    void setName(@CheckForNull String name);

    /**
     * Test is model is a valid model. Invalid models will not be shown or saved
     * by the Startup Actions Preferences panel.
     *
     * @return true if valid; false otherwise
     */
    boolean isValid();

    /**
     * Perform the startup action.
     *
     * @throws JmriException if there is an exception thrown initializing the
     *                       startup item; the original exception should be
     *                       available as {@link Exception#getCause()}
     */
    void performAction() throws JmriException;

    /**
     * Get the exceptions thrown by the startup model.
     *
     * @return the list of exceptions thrown during startup in order or an empty
     *         list if no exceptions were thrown
     */
    @Nonnull
    List<Exception> getExceptions();

    /**
     * Add an exception to the list of exceptions thrown when loading the model
     * or performing the action.
     *
     * @param exception the exception to retain with the model
     */
    void addException(@Nonnull Exception exception);
}
