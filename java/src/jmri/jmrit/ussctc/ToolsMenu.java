/**
 * ToolsMenu.java
 */
package jmri.jmrit.ussctc;

import javax.swing.JMenu;

/**
 * Create a "Tools" menu containing the USS CTC tools
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version $Revision$
 */
public class ToolsMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = -6832144369646539440L;

    public ToolsMenu(String name) {
        this();
        setText(name);
    }

    public ToolsMenu() {

        super();

        if (rb == null) {
            rb = java.util.ResourceBundle.getBundle("jmri.jmrit.ussctc.UssCtcBundle");
        }
        setText(rb.getString("MenuTools"));

        add(new OsIndicatorAction(rb.getString("MenuItemOsIndicator")));
        add(new FollowerAction(rb.getString("MenuItemFollower")));
    }

    static java.util.ResourceBundle rb = null;
}
