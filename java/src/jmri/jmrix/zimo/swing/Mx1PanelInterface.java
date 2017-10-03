package jmri.jmrix.zimo.swing;

import jmri.jmrix.zimo.Mx1SystemConnectionMemo;

/**
 * JPanel interface to handle providing system connection information to a
 * panel.
 *
 * @author Bob Jacobsen Copyright 2010 Copied from nce.swing
 * @author Ken Cameron 2014
 * @author Kevin Dickerson 2014
 */
public interface Mx1PanelInterface {

    /**
     * 2nd stage of initialization, invoked after the constructor is complete.
     * <p>
     * This needs to be connected to the initContext() method in implementing
     * classes.
     *
     * @param memo the memo to initialize components with
     */
    public void initComponents(Mx1SystemConnectionMemo memo);

}
