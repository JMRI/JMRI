package jmri.jmrix.powerline.swing;

import jmri.jmrix.powerline.SerialSystemConnectionMemo;

/**
 * JPanel interface to handle providing system connection information to a
 * panel.
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.11.1 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public interface PowerlinePanelInterface {

    /**
     * 2nd stage of initialization, invoked after the constuctor is complete.
     * <p>
     * This needs to be connected to the initContext() method in implementing
     * classes.
     * @param memo Connection details memo
     */
    public void initComponents(SerialSystemConnectionMemo memo);

}
