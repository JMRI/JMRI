package jmri.configurexml;

import javax.swing.JMenu;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Create a "Save" menu item containing actions for storing various data
 * (subsets).
 *
 * @author Bob Jacobsen Copyright 2005
 */
@API(status = EXPERIMENTAL)
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
