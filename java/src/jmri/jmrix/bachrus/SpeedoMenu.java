/**
 * SpeedoMenu.java
 */
package jmri.jmrix.bachrus;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the bachrus-specific tools
 *
 * @author	Andrew Crosland Copyright 2010
 * @version $Revision$
 */
public class SpeedoMenu extends JMenu {

    /**
     *
     */
    private static final long serialVersionUID = -2390789739386732815L;

    public SpeedoMenu(String name) {
        this();
        setText(name);
    }

    public SpeedoMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText("Speedo");

        add(new jmri.jmrix.bachrus.SpeedoConsoleAction(rb.getString("MenuItemSpeedo")));
    }

}
