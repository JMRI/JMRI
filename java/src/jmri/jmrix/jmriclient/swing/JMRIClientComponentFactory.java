package jmri.jmrix.jmriclient.swing;

import jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo;

/**
 * Provide access to Swing components for the JMRI Network Client.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Paul Bender Copyright (C) 2010
 * @since 2.11.1
 */
public class JMRIClientComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public JMRIClientComponentFactory(JMRIClientSystemConnectionMemo memo) {
        this.memo = memo;
    }

    JMRIClientSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new JMRIClientMenu(memo);
    }
}



