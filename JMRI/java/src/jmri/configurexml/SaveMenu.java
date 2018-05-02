package jmri.configurexml;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Save" menu item containing actions for storing various data
 * (subsets).
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class SaveMenu extends JMenu {

    public SaveMenu(String name) {
        this();
        setText(name);
    }

    public SaveMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.configurexml.SaveMenuBundle");

        setText(rb.getString("MenuItemSave"));

        add(new jmri.configurexml.StoreXmlConfigAction(rb.getString("MenuItemStoreConfig")));
        add(new jmri.configurexml.StoreXmlUserAction(rb.getString("MenuItemStoreUser")));

    }
}
