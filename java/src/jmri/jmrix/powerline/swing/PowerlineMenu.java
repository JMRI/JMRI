package jmri.jmrix.powerline.swing;

import javax.annotation.Nonnull;
import javax.swing.JMenu;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialSystemConnectionMemo.MenuItem;

/**
 * Create a "Systems" menu containing the JMRI Powerline-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003, 2010 Copied from NCE Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class PowerlineMenu extends JMenu {

    /**
     * Create a Powerline menu. And loads the SerialSystemConnectionMemo to the
     * various actions. Actions will open new windows.
     * @param memo Connection details memo
     */
    // Need to Sort out the Powerline server menu items;
    public PowerlineMenu(@Nonnull SerialSystemConnectionMemo memo) {
        super();

        setText(memo.getUserName());

        jmri.util.swing.WindowInterface wi = new jmri.util.swing.sdi.JmriJFrameInterface();

        // rely on list of menu items
        for (MenuItem item : memo.provideMenuItemList()) {
            if (item == null) {
                add(new javax.swing.JSeparator());
            } else {
                PowerlineNamedPaneAction a = new PowerlineNamedPaneAction(Bundle.getMessage(item.name), wi, item.load, memo);
                add(a);
                a.setEnabled(true);
            }
        }

        // do we have a TrafficController?
        setEnabled(memo.getTrafficController() != null); // disable menu, no connection, no tools!

        add(new javax.swing.JSeparator());
    }

}
