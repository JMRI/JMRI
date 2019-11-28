package jmri.jmrix.loconet.swing;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * JPanel interface to handle providing system connection information to a
 * panel.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.4
 */
public interface LnPanelInterface {

    /**
     * 2nd stage of initialization, invoked after the constuctor is complete.
     * <p>
     * This needs to be connected to the initContext() method in implementing
     * classes.
     *
     * @param memo  a {@link jmri.jmrix.loconet.LocoNetSystemConnectionMemo} object
     */
    public void initComponents(LocoNetSystemConnectionMemo memo);

}
