/**
 * SimpleServerMenu.java
 */

package jmri.jmris.simpleserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.util.*;

/**
 * Create a "JMRI Simple Server" menu containing the Server interface to the JMRI 
 * system-independent tools
 *
 * @author	Paul Bender   Copyright 2009
 * @version     $Revision$
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

    static Logger log = LoggerFactory.getLogger(SimpleServerMenu.class.getName());
}


