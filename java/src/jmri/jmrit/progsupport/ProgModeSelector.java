package jmri.jmrit.progsupport;

import jmri.Programmer;

/**
 * Provide a JPanel to configure the programming mode.
 * <p>
 * The using code should get a configured programmer with getProgrammer.
 * <p>
 * This pane will only display ops mode options if ops mode is available, as
 * evidenced by an attempt to get an ops mode programmer at startup time.
 * <p>
 * For service mode, you can get the programmer either from here or direct from
 * the instance manager. For ops mode, you have to get it from here.
 * <p>
 * Note that you should call the dispose() method when you're really done, so
 * that a ProgModeSelector object can disconnect its listeners.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public abstract class ProgModeSelector extends javax.swing.JPanel {

    /**
     * Get the configured programmer
     */
    abstract public Programmer getProgrammer();

    /**
     * Does this object have sufficient selection information to provide a
     * programmer?
     *
     * @return true if a programmer is available
     */
    abstract public boolean isSelected();

    /**
     * Enable/Disable the selection aspect of whatever GUI is presented
     * <p>
     * Default beavior is to do nothing.
     *
     * @param enabled false disables GUI user changes
     */
    @Override
    public void setEnabled(boolean enabled) {
    }

    /**
     * Clean up when done. Required.
     */
    abstract public void dispose();

}
