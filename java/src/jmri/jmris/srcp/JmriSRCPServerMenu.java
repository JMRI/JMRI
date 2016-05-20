/**
 * JmriSRCPServerMenu.java
 */

package jmri.jmris.srcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.util.*;

/**
 * Create a "JMRI SRCP Server" menu containing the Server interface to the JMRI 
 * system-independent tools
 *
 * @author	Paul Bender   Copyright 2009
 * @version     $Revision$
 */
public class JmriSRCPServerMenu extends JMenu {
    public JmriSRCPServerMenu(String name) {
        this();
        setText(name);
    }

    public JmriSRCPServerMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.srcp.JmriSRCPServerBundle");

        setText(rb.getString("MenuServer"));
	add(new jmri.jmris.srcp.JmriSRCPServerAction(rb.getString("MenuItemStartServer")));

    }

    static Logger log = LoggerFactory.getLogger(JmriSRCPServerMenu.class.getName());
}


