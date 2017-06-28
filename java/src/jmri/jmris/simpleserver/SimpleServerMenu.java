package jmri.jmris.simpleserver;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "JMRI Simple Server" menu containing the Server interface to the
 * JMRI system-independent tools
 *
 * @author Paul Bender Copyright 2009
 */
public class SimpleServerMenu extends JMenu {

    public SimpleServerMenu(String name) {
        this();
        setText(name);
    }

    public SimpleServerMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.simpleserver.SimpleServerBundle");

        setText(rb.getString("MenuServer"));
        add(new jmri.jmris.simpleserver.SimpleServerAction(rb.getString("MenuItemStartServer")));

    }
}
