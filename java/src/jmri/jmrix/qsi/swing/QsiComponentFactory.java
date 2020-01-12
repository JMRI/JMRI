package jmri.jmrix.qsi.swing;

import jmri.jmrix.qsi.QSIMenu;
import jmri.jmrix.qsi.QsiSystemConnectionMemo;

/**
 * Provide access to Swing components for the Qsi subsystem.
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @author	Paul Bender Copyright (C) 2010,2016
 * @since 4.5.1
 */
public class QsiComponentFactory extends jmri.jmrix.swing.ComponentFactory {

    public QsiComponentFactory(QsiSystemConnectionMemo memo) {
        this.memo = memo;
    }

    QsiSystemConnectionMemo memo;

    /**
     * Provide a menu with all items attached to this system connection.
     */
    @Override
    public javax.swing.JMenu getMenu() {
        if (memo.getDisabled()) {
            return null;
        }
        return new QSIMenu(memo);
    }

}
