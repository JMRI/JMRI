// PanelMenu.java

package jmri.jmrit.display;

import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JSeparator;

/**
 * Create the default "Panels" menu for use in a menubar.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2004
 * @version     $Revision: 1.7 $
 */
public class PanelMenu extends JMenu {
    public PanelMenu() {

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

        this.setText(rb.getString("MenuPanels"));

        add(new jmri.jmrit.display.PanelEditorAction(rb.getString("MenuItemNew")));
        add(new jmri.configurexml.LoadXmlUserAction(rb.getString("MenuItemLoad")));
        add(new jmri.configurexml.StoreXmlUserAction(rb.getString("MenuItemStore")));
        add(new JSeparator());
        add(new jmri.jmrit.jython.RunJythonScript(rb.getString("MenuItemScript")));
        add(new jmri.jmrit.automat.monitor.AutomatTableAction(rb.getString("MenuItemMonitor")));
        add(new jmri.jmrit.jython.JythonWindow(rb.getString("MenuItemScriptLog")));
        add(new jmri.jmrit.jython.InputWindowAction(rb.getString("MenuItemScriptInput")));

    }
}


