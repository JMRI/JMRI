package jmri.jmrix.pricom.pockettester;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a menu or menu item containing the PRICOM Pocket Tester tools
 *
 * @author	Bob Jacobsen Copyright 2005
 */
public class PocketTesterMenu extends JMenu {

    public PocketTesterMenu(String name) {
        this();
        setText(name);
    }

    public PocketTesterMenu() {

        super();

        setText(Bundle.getMessage("MenuPocketTester"));

        add(new jmri.jmrix.pricom.pockettester.DataSourceAction());

        // rest need to be added dynamically after connection is made
        // add(new jmri.jmrix.pricom.pockettester.MonitorAction());
        // add(new jmri.jmrix.pricom.pockettester.PacketTableAction());
        // add(new jmri.jmrix.pricom.pockettester.StatusAction());
    }

}
