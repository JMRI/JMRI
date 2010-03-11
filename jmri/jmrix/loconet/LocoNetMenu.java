// LocoNetMenu.java

package jmri.jmrix.loconet;

/**
 * Migration class for compatibility after LocoNetMenu class
 * was moved to {@link jmri.jmrix.loconet.swing}.
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.22 $
 * @deprecated 2.9.5
 */
@Deprecated
public class LocoNetMenu extends jmri.jmrix.loconet.swing.LocoNetMenu {
    public LocoNetMenu(LocoNetSystemConnectionMemo memo) {
        super(memo);
    }
}


