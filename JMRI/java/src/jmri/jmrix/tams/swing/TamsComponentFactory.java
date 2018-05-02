package jmri.jmrix.tams.swing;

import jmri.jmrix.tams.TamsSystemConnectionMemo;

/**
 * Provide access to Swing components for the Tams subsystem.
 *
 * @author Kevin Dickerson 2010
 */
public class TamsComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public TamsComponentFactory(TamsSystemConnectionMemo memo) {
        this.memo = memo;
    }
    TamsSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new TamsMenu(memo);
    }
}
