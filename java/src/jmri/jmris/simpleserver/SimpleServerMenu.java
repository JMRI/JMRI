/**
 * SimpleServerMenu.java
 */
package jmri.jmris.simpleserver;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a "JMRI Simple Server" menu containing the Server interface to the
 * JMRI system-independent tools
 *
 * @author	Paul Bender Copyright 2009
 * @version $Revision$
 */
public class SimpleServerMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = -1335915650658048826L;

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

    private final static Logger log = LoggerFactory.getLogger(SimpleServerMenu.class.getName());
}
