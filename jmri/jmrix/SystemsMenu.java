// SystemsMenu.java

package jmri.jmrix;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Provide a "Systems" menu containing the Jmri system-specific tools in submenus.
 * <P>
 * This contains all compiled systems, whether active or not.  For the
 * set of currently-active system-specific tools, see
 * {@link ActiveSystemsMenu}.
 *
 * @see ActiveSystemsMenu
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.7 $
 */
public class SystemsMenu extends JMenu {
    public SystemsMenu(String name) {
        this();
        setText(name);
    }

    public SystemsMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        setText(rb.getString("MenuSystems"));

        add(new jmri.jmrix.cmri.CMRIMenu());
        add(new jmri.jmrix.easydcc.EasyDCCMenu());
        add(new jmri.jmrix.loconet.LocoNetMenu());
        add(new jmri.jmrix.nce.NceMenu());
        add(new jmri.jmrix.sprog.SPROGMenu());
        add(new jmri.jmrix.wangrow.WangrowMenu());
        add(new jmri.jmrix.lenz.XNetMenu());
        add(new jmri.jmrix.zimo.Mx1Menu());

    }

}


