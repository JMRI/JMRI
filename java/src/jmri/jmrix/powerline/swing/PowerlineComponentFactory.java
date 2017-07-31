package jmri.jmrix.powerline.swing;

import jmri.jmrix.powerline.SerialSystemConnectionMemo;

/**
 * Provide access to Swing components for the Powerline subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010 coverted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class PowerlineComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public PowerlineComponentFactory(SerialSystemConnectionMemo memo) {
        this.memo = memo;
    }

    SerialSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new PowerlineMenu(memo);
    }
}



