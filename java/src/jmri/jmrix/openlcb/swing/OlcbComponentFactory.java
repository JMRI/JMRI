// OlcbComponentFactory.java

package jmri.jmrix.openlcb.swing;

import jmri.jmrix.openlcb.OlcbSystemConnectionMemo;
import jmri.jmrix.openlcb.OpenLcbMenu;

/**
 * Provide access to Swing components for the LocoNet subsystem.
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision$
 * @since 2.9.4
 */
public class OlcbComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public OlcbComponentFactory(OlcbSystemConnectionMemo memo) {
        this.memo = memo;
    }
    
    OlcbSystemConnectionMemo memo;
    
    /**
     * Provide a menu with all items attached to this system connection
     */
    public javax.swing.JMenu getMenu() {
        return new OpenLcbMenu(memo);
    }
}


/* @(#)OlcbComponentFactory.java */
