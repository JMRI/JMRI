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

        setText(Bundle.getMessage("MenuCtcTools"));   // NOI18N

        add(new OsIndicatorAction(Bundle.getMessage("MenuItemOsIndicator")));   // NOI18N
        add(new FollowerAction(Bundle.getMessage("MenuItemFollower")));    // NOI18N
    }
}
