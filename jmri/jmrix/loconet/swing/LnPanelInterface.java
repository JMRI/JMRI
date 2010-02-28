// LnPanelInterface.java

package jmri.jmrix.loconet.swing;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

import jmri.util.swing.WindowInterface;

/**
 * JPanel interface to handle providing 
 * system connection information to a panel.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.9.4
 * @version $Revision: 1.1 $
 */

public interface LnPanelInterface  {

    /**
     * 2nd stage of initialization, invoked after
     * the constuctor is complete.
     */
    public void initComponents(LocoNetSystemConnectionMemo memo) throws Exception;
}