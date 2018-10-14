package jmri.jmrix.tams.swing;

import jmri.jmrix.tams.TamsSystemConnectionMemo;

/**
 * JPanel interface to handle providing system connection information to a
 * panel.
 *
 * Based on work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public interface TamsPanelInterface {

    /**
     * 2nd stage of initialization, invoked after the constuctor is complete.
     * <p>
     * This needs to be connected to the initContext() method in implementing
     * classes.
     */
    public void initComponents(TamsSystemConnectionMemo memo);

}
