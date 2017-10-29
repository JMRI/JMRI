package jmri.jmrix.roco.z21.swing;

import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;

/**
 * Provide access to Swing components for the Roco Z21 subsystem.
 *
 * @author	Paul Bender Copyright (C) 2014
 */
public class Z21ComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public Z21ComponentFactory(Z21SystemConnectionMemo memo) {
        this.memo = memo;
    }

    Z21SystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new Z21Menu(memo);
    }

}
