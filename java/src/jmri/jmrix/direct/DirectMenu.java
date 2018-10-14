package jmri.jmrix.direct;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Create a "Systems" menu containing the Jmri direct-drive-specific tools (none at present).
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class DirectMenu extends JMenu {

    public DirectMenu(String name, DirectSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public DirectMenu(DirectSystemConnectionMemo memo) {
        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText(Bundle.getMessage("MenuItemDirect"));
        }

        // no items
        if (memo != null) {
            // do we have a TrafficController?
            setEnabled(false); // memo.getTrafficController() != null); // disable menu, no connection, no tools!
            add(new JMenuItem(Bundle.getMessage("MenuNoOptions")));
        }
    }

}
