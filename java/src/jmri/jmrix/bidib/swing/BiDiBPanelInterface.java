package jmri.jmrix.bidib.swing;

import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;

/**
 * JPanel interface to handle providing system connection information to a
 * panel.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public interface BiDiBPanelInterface {

    /**
     * 2nd stage of initialisation, invoked after the constructor is complete.
     * <p>
     * This needs to be connected to the initContext() method in implementing
     * classes.
     * @param memo SystemConnectionMemo for configured RFID system
     */
    public void initComponents(BiDiBSystemConnectionMemo memo);

}
