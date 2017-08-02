package jmri.jmrix.zimo.swing;

import jmri.jmrix.zimo.Mx1SystemConnectionMemo;

/**
 * Provide access to Swing components for the Mrc subsystem.
 *
 * @author	Bob Jacobsen Copyright (C) 2010 Copied from nce.swing
 * @author Ken Cameron 2014
 * @author Kevin Dickerson 2014
 */
public class Mx1ComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public Mx1ComponentFactory(Mx1SystemConnectionMemo memo) {
        this.memo = memo;
    }

    Mx1SystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new Mx1Menu(memo);
    }
}



