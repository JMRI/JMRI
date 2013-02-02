/**
 * ServerMenu.java
 */

package jmri.jmris;

import org.apache.log4j.Logger;
import javax.swing.*;
import java.util.*;

/**
 * Create a "Server" menu containing the Server interface to the JMRI 
 * system-independent tools
 *
 * @author	Paul Bender   Copyright 2010
 * @version     $Revision$
 */
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

    static Logger log = Logger.getLogger(ServerMenu.class.getName());
}


