package jmri.jmrix.can.swing;

import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * JPanel interface to handle providing system connection information to a
 * panel.
 *
 * @author Kevin Dickerson Copyright 2012
 * @since 2.99.2
 */
public interface CanPanelInterface {

    /**
     * 2nd stage of initialization, invoked after the Constructor is complete.
     * <p>
     * This needs to be connected to the initContext() method in implementing
     * classes.
     * @param memo system connection.
     */
    public void initComponents(CanSystemConnectionMemo memo);

}
