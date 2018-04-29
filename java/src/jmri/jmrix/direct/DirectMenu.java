package jmri.jmrix.direct;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri direct-drive-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class DirectMenu extends JMenu {

    public DirectMenu(String name,DirectSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public DirectMenu(DirectSystemConnectionMemo memo) {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuItemDirect"));

    }

}
