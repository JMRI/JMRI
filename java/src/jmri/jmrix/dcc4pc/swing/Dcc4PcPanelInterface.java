package jmri.jmrix.dcc4pc.swing;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;

/**
 * JPanel interface to handle providing system connection information to a
 * panel.
 *
 * @author Kevin Dickerson Copyright 2010
 * @since 2.11.3
 */
public interface Dcc4PcPanelInterface {

    /**
     * 2nd stage of initialization, invoked after the constructor is complete.
     * <p>
     * This needs to be connected to the initContext() method in implementing
     * classes.
     * @param memo system connection.
     */
    public void initComponents(Dcc4PcSystemConnectionMemo memo);

}
