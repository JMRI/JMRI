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
 * @see SystemsMenu
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.5 $
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

        if (jmri.jmrix.cmri.serial.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.cmri.CMRIMenu"));
        if (jmri.jmrix.easydcc.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.easydcc.EasyDCCMenu"));
        if (jmri.jmrix.loconet.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.loconet.LocoNetMenu"));
        if (jmri.jmrix.nce.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.nce.NceMenu"));
        if (jmri.jmrix.sprog.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.sprog.SPROGMenu"));
        if (jmri.jmrix.wangrow.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.wangrow.WangrowMenu"));
        if (jmri.jmrix.lenz.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.lenz.XNetMenu"));
        if (jmri.jmrix.zimo.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.zimo.Mx1Menu"));
    }

    static public void addItems(JMenuBar m) {
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // the following is somewhat brute-force!

        if (jmri.jmrix.cmri.serial.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.cmri.CMRIMenu"));

        if (jmri.jmrix.easydcc.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.easydcc.EasyDCCMenu"));

        if (jmri.jmrix.loconet.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.loconet.LocoNetMenu"));

        if (jmri.jmrix.nce.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.nce.NceMenu"));

        if (jmri.jmrix.sprog.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.sprog.SPROGMenu"));

        if (jmri.jmrix.wangrow.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.wangrow.WangrowMenu"));

        if (jmri.jmrix.lenz.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.lenz.XNetMenu"));

        if (jmri.jmrix.zimo.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.zimo.Mx1Menu"));
    }

    static JMenu getMenu(String className) {
        try {
            return (JMenu) Class.forName(className).newInstance();
        } catch (Exception e) {
            log.error("Could not load class "+className+"; "+e);
            return null;
        }
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ActiveSystemsMenu.class.getName());

}
