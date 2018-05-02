package jmri.jmrix.mrc.swing;

import jmri.jmrix.mrc.MrcSystemConnectionMemo;

/**
 * Provide access to Swing components for the Mrc subsystem.
 *
 * @author Bob Jacobsen Copyright (C) 2010 Copied from nce.swing
 * @author Ken Cameron 2014
 * @author Kevin Dickerson 2014
 */
public class MrcComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public MrcComponentFactory(MrcSystemConnectionMemo memo) {
        this.memo = memo;
    }

    MrcSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new MrcMenu(memo);
    }
}



