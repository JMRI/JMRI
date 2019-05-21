package jmri.jmrix.nce.swing;

import jmri.jmrix.nce.NceSystemConnectionMemo;

/**
 * JPanel interface to handle providing system connection information to a
 * panel.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.11.1
 * @author kcameron 2010
 */
public interface NcePanelInterface {

    /**
     * 2nd stage of initialization, invoked after the constuctor is complete.
     * <p>
     * This needs to be connected to the initContext() method in implementing
     * classes.
     *
     * @param memo the system connection memo for this connection
     */
    public void initComponents(NceSystemConnectionMemo memo);

}
