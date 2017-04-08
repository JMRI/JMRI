package jmri.jmrix.marklin.swing;

import jmri.jmrix.marklin.MarklinSystemConnectionMemo;

/**
 * Provide access to Swing components for the Marklin subsystem.
 *
 * @author Kevin Dickerson 2010
 */
public class MarklinComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public MarklinComponentFactory(MarklinSystemConnectionMemo memo) {
        this.memo = memo;
    }
    MarklinSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    //JMenu currentMenu;
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new MarklinMenu(memo);
    }
}
