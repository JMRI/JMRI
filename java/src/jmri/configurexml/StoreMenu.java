package jmri.configurexml;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Save" menu item containing actions for storing various data
 * (subsets).
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class StoreMenu extends JMenu {

    public StoreMenu(String name) {
        this();
        setText(name);
    }

    public StoreMenu() {

        super();

        setText(Bundle.getMessage("MenuItemStore"));

        add(new jmri.configurexml.StoreXmlConfigAction(Bundle.getMessage("MenuItemStoreConfig")));
        add(new jmri.configurexml.StoreXmlUserAction(Bundle.getMessage("MenuItemStoreUser")));

    }
}
