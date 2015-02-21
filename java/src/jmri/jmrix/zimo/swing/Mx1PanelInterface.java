// MrcPanelInterface.java
package jmri.jmrix.zimo.swing;

import jmri.jmrix.zimo.Mx1SystemConnectionMemo;

/**
 * JPanel interface to handle providing system connection information to a
 * panel.
 *
 * @author Bob Jacobsen Copyright 2010 Copied from nce.swing
 * @author Ken Cameron 2014
 * @author Kevin Dickerson 2014
 * @version $Revision: 17977 $
 */
public interface Mx1PanelInterface {

    /**
     * 2nd stage of initialization, invoked after the constuctor is complete.
     * <p>
     * This needs to be connected to the initContext() method in implementing
     * classes.
     */
    public void initComponents(Mx1SystemConnectionMemo memo) throws Exception;

}
