package jmri.jmrit.ussctc;

import javax.swing.JMenu;

/**
 * Create a "Tools" menu containing the USS CTC tools
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class ToolsMenu extends JMenu {

    public ToolsMenu(String name) {
        this();
        setText(name);
    }

    public ToolsMenu() {

        super();

        setText(rb.getString("MenuCtcTools"));

        add(new OsIndicatorAction(rb.getString("MenuItemOsIndicator")));
        add(new FollowerAction(rb.getString("MenuItemFollower")));
    }

    static java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.ussctc.UssCtcBundle");
}
