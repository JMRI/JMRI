// PanelMenu.java

package jmri.jmrit.display;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.1 $
 */
public class PanelMenu extends JMenu {
    public PanelMenu() {

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

        this.setText(rb.getString("MenuPanels"));

        add(new jmri.jmrit.display.PanelEditorAction(rb.getString("MenuItemNew")));
        add(new jmri.configurexml.LoadXmlConfigAction(rb.getString("MenuItemLoad")));
        add(new jmri.configurexml.StoreXmlConfigAction(rb.getString("MenuItemStore")));

    }
}


