package jmri.jmrix.dcc4pc.swing;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;

/**
 * Provide access to Swing components for the Ecos subsystem.
 *
 * @author Kevin Dickerson 2010
 */
public class Dcc4PcComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public Dcc4PcComponentFactory(Dcc4PcSystemConnectionMemo memo) {
        this.memo = memo;
    }
    Dcc4PcSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new Dcc4PcMenu(memo);
    }
}
