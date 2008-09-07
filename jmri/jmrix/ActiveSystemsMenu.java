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
 * @version     $Revision: 1.21 $
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

        if (jmri.jmrix.acela.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.acela.AcelaMenu"));
        if (jmri.jmrix.cmri.serial.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.cmri.CMRIMenu"));
        if (jmri.jmrix.easydcc.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.easydcc.EasyDCCMenu"));
        if (jmri.jmrix.grapevine.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.grapevine.GrapevineMenu"));
        if (jmri.jmrix.loconet.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.loconet.LocoNetMenu"));
        if (jmri.jmrix.nce.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.nce.NceMenu"));
        if (jmri.jmrix.oaktree.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.oaktree.OakTreeMenu"));
        if (jmri.jmrix.powerline.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.powerline.SystemMenu"));
        if (jmri.jmrix.pricom.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.pricom.PricomMenu"));
        if (jmri.jmrix.qsi.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.qsi.QSIMenu"));
        if (jmri.jmrix.rps.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.rps.RpsMenu"));
        if (jmri.jmrix.secsi.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.secsi.SecsiMenu"));
        if (jmri.jmrix.sprog.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.sprog.SPROGMenu"));
        if (jmri.jmrix.sprog.ActiveFlagCS.isActive())
            m.add(getMenu("jmri.jmrix.sprog.SPROGCSMenu"));
        if (jmri.jmrix.srcp.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.srcp.SystemMenu"));
        if (jmri.jmrix.tchtech.serial.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.tchtech.TCHTechMenu"));
        if (jmri.jmrix.tmcc.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.tmcc.TMCCMenu"));
        if (jmri.jmrix.wangrow.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.wangrow.WangrowMenu"));
        if (jmri.jmrix.lenz.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.lenz.XNetMenu"));
        if (jmri.jmrix.xpa.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.xpa.XpaMenu"));
        if (jmri.jmrix.zimo.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.zimo.Mx1Menu"));
        
        m.add(new javax.swing.JSeparator());
        
        if (jmri.jmrix.can.adapters.gridconnect.canrs.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.can.CanMenu"));
        if (jmri.jmrix.can.adapters.gridconnect.canusb.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.can.CanMenu"));
        if (jmri.jmrix.can.adapters.lawicell.canusb.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.can.CanMenu"));
        if (jmri.jmrix.can.adapters.loopback.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.can.CanMenu"));

        m.add(new javax.swing.JSeparator());

        if (jmri.jmrix.direct.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.direct.DirectMenu"));

        if (jmri.jmrix.ecos.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.ecos.Menu"));
    }

    static public void addItems(JMenuBar m) {
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // the following is somewhat brute-force!

        if (jmri.jmrix.acela.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.acela.AcelaMenu"));

        if (jmri.jmrix.cmri.serial.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.cmri.CMRIMenu"));

        if (jmri.jmrix.easydcc.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.easydcc.EasyDCCMenu"));

        if (jmri.jmrix.grapevine.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.grapevine.GrapevineMenu"));

        if (jmri.jmrix.loconet.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.loconet.LocoNetMenu"));

        if (jmri.jmrix.nce.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.nce.NceMenu"));

        if (jmri.jmrix.oaktree.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.oaktree.OakTreeMenu"));

        if (jmri.jmrix.powerline.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.powerline.SystemMenu"));

        if (jmri.jmrix.pricom.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.pricom.PricomMenu"));

        if (jmri.jmrix.qsi.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.qsi.QSIMenu"));

        if (jmri.jmrix.rps.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.rps.RpsMenu"));

        if (jmri.jmrix.secsi.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.secsi.SecsiMenu"));

        if (jmri.jmrix.sprog.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.sprog.SPROGMenu"));

        if (jmri.jmrix.sprog.ActiveFlagCS.isActive())
              m.add(getMenu("jmri.jmrix.sprog.SPROGCSMenu"));

        if (jmri.jmrix.srcp.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.srcp.SystemMenu"));

        if (jmri.jmrix.tchtech.serial.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.tchtech.TCHTechMenu"));

        if (jmri.jmrix.tmcc.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.tmcc.TMCCMenu"));

        if (jmri.jmrix.wangrow.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.wangrow.WangrowMenu"));

        if (jmri.jmrix.lenz.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.lenz.XNetMenu"));

        if (jmri.jmrix.xpa.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.xpa.XpaMenu"));

        if (jmri.jmrix.zimo.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.zimo.Mx1Menu"));

        if (jmri.jmrix.direct.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.direct.DirectMenu"));

        if (jmri.jmrix.can.adapters.gridconnect.canrs.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.can.CanMenu"));

        if (jmri.jmrix.can.adapters.gridconnect.canusb.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.can.CanMenu"));

        if (jmri.jmrix.can.adapters.lawicell.canusb.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.can.CanMenu"));

        if (jmri.jmrix.can.adapters.loopback.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.can.CanMenu"));

        if (jmri.jmrix.ecos.ActiveFlag.isActive())
            m.add(getMenu("jmri.jmrix.ecos.Menu"));
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
