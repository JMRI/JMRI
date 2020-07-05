package jmri.jmris.srcp;

import java.util.ResourceBundle;
import javax.swing.JMenu;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Create a "JMRI SRCP Server" menu containing the Server interface to the JMRI
 * system-independent tools
 *
 * @author Paul Bender Copyright 2009
 */
@API(status = EXPERIMENTAL)
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
}
