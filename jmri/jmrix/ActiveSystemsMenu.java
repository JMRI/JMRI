/**
 * ActiveSystemsMenu.java
 */

package jmri.jmrix;

import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

/**
 * Create a "Systems" menu containing as submenus the
 * JMRI system-specific menus for available systems.
 * <P>
 * Also provides a static member for adding these items to an
 * existing menu.
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.1 $
 */
public class ActiveSystemsMenu extends JMenu {
    public ActiveSystemsMenu(String name) {
        this();
        setText(name);

        addItems(this);
    }

    public ActiveSystemsMenu() {
        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");
        setText(rb.getString("MenuSystems"));

        addItems(this);
    }

    static public void addItems(JMenu m) {
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // the following is somewhat brute-force!

        if (jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter.hasInstance())
            m.add(new jmri.jmrix.cmri.CMRIMenu());
        if (jmri.jmrix.easydcc.serialdriver.SerialDriverAdapter.hasInstance())
            m.add(new jmri.jmrix.easydcc.EasyDCCMenu());
        if (jmri.jmrix.loconet.LnTrafficController.hasInstance())
            m.add(new jmri.jmrix.loconet.LocoNetMenu());
        if (jmri.jmrix.nce.serialdriver.SerialDriverAdapter.hasInstance())
            m.add(new jmri.jmrix.nce.NceMenu());
        if (jmri.jmrix.lenz.li100.LI100Adapter.hasInstance())
            m.add(new jmri.jmrix.lenz.XNetMenu());
        if (jmri.jmrix.sprog.serialdriver.SerialDriverAdapter.hasInstance())
            m.add(new jmri.jmrix.sprog.SPROGMenu());
    }

    static public void addItems(JMenuBar m) {
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // the following is somewhat brute-force!

        if (jmri.jmrix.cmri.serial.serialdriver.SerialDriverAdapter.hasInstance())
            m.add(new jmri.jmrix.cmri.CMRIMenu());
        if (jmri.jmrix.easydcc.serialdriver.SerialDriverAdapter.hasInstance())
            m.add(new jmri.jmrix.easydcc.EasyDCCMenu());
        if (jmri.jmrix.loconet.LnTrafficController.hasInstance())
            m.add(new jmri.jmrix.loconet.LocoNetMenu());
        if (jmri.jmrix.nce.serialdriver.SerialDriverAdapter.hasInstance())
            m.add(new jmri.jmrix.nce.NceMenu());
        if (jmri.jmrix.lenz.li100.LI100Adapter.hasInstance())
            m.add(new jmri.jmrix.lenz.XNetMenu());
        if (jmri.jmrix.sprog.serialdriver.SerialDriverAdapter.hasInstance())
            m.add(new jmri.jmrix.sprog.SPROGMenu());
    }
}
