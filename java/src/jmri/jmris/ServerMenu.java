/**
 * ServerMenu.java
 */
package jmri.jmris;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a "Server" menu containing the Server interface to the JMRI
 * system-independent tools
 *
 * @author	Paul Bender Copyright 2010
 * @version $Revision$
 */
public class ServerMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = 7699901823839772206L;

    public ServerMenu(String name) {
        this();
        setText(name);
    }

    public ServerMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.JmriServerBundle");

        setText(rb.getString("MenuServer"));
        // This first menu item is for connection testing only.  
        // It provides no parsing.
        //add(new jmri.jmris.JmriServerAction(rb.getString("MenuItemStartServer")));
        add(new jmri.jmris.simpleserver.SimpleServerMenu());
        add(new jmri.jmris.srcp.JmriSRCPServerMenu());

    }

    private final static Logger log = LoggerFactory.getLogger(ServerMenu.class.getName());
}
