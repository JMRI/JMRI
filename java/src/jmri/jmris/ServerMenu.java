package jmri.jmris;

import java.util.ResourceBundle;
import javax.swing.JMenu;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Create a "Server" menu containing the Server interface to the JMRI
 * system-independent tools
 *
 * @author Paul Bender Copyright 2010
 */
@API(status = EXPERIMENTAL)
public class ServerMenu extends JMenu {

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
}
