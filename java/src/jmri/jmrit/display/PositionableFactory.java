package jmri.jmrit.display;

import javax.annotation.Nonnull;

/**
 * A factory for Positionables.
 * The purpose of this interface is to allow Positionables to be loaded
 * conditionally, for example if a particular connection type is available,
 * and to have the code for that Positionable in a package related to that
 * connection type.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public interface PositionableFactory {

    /**
     * Get an unique identifier for this factory.
     * It's recommended that identifiers for connection specific factories
     * begins with the connection type. Example: DCC-EX-VirtualDisplay.
     *
     * @return an unique identifier for this factory
     */
    @Nonnull
    public String getIdentifier();

    /**
     * Get the description for this factory.
     *
     * @return the description
     */
    @Nonnull
    public String getDescription();

    /**
     * Determines whenever this factory enabled.
     * This method can for example check if a particular connection type is
     * available. This is useful for positionables like DCC-VirtualLCD which
     * are useful only for particular connections.
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Add a positionable to the panel.
     * The method might show a dialog to let the user configure the positionable.
     * @param editor   the editor to which the new positionable should be added
     * @param doAfter  an optional task to run after the positionable is added
     *                 to the editor, or null if no task is needed
     */
    void addPositionable(@Nonnull Editor editor, DoAfter doAfter);


    /**
     * A task to do after the positionable is added to the editor.
     */
    interface DoAfter {

        /**
         * The task to do.
         * @param p the positionable
         */
        void doAfter(Positionable p);
    }

}
