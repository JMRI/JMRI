package jmri.jmrix.ecos.swing;

import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import javax.swing.JMenu;
/**
 * Provide access to Swing components for the Ecos subsystem.
 *
 * @author Kevin Dickerson 2010
 */
public class EcosComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public EcosComponentFactory(EcosSystemConnectionMemo memo) {
        this.memo = memo;
    }

    EcosSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */

    //JMenu currentMenu;

    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) return null;
        return new EcosMenu(memo);
    }
}

// EcosComponentFactory.java