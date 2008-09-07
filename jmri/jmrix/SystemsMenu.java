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
 * @version     $Revision: 1.22 $
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

        addMenu("jmri.jmrix.acela.AcelaMenu");
        addMenu("jmri.jmrix.cmri.CMRIMenu");
        addMenu("jmri.jmrix.easydcc.EasyDCCMenu");
        addMenu("jmri.jmrix.grapevine.GrapevineMenu");
        addMenu("jmri.jmrix.loconet.LocoNetMenu");
        addMenu("jmri.jmrix.nce.NceMenu");
        addMenu("jmri.jmrix.oaktree.OakTreeMenu");
        addMenu("jmri.jmrix.powerline.SystemMenu");
        addMenu("jmri.jmrix.pricom.PricomMenu");
        addMenu("jmri.jmrix.qsi.QSIMenu");
        addMenu("jmri.jmrix.rps.RpsMenu");
        addMenu("jmri.jmrix.secsi.SecsiMenu");
        addMenu("jmri.jmrix.sprog.SPROGMenu");
        addMenu("jmri.jmrix.srcp.SystemMenu");
        addMenu("jmri.jmrix.tchtech.TCHTechMenu");    
        addMenu("jmri.jmrix.tmcc.TMCCMenu");
        addMenu("jmri.jmrix.wangrow.WangrowMenu");
        addMenu("jmri.jmrix.lenz.XNetMenu");
        addMenu("jmri.jmrix.xpa.XpaMenu");
        addMenu("jmri.jmrix.zimo.Mx1Menu");
        add(new javax.swing.JSeparator());
        addMenu("jmri.jmrix.direct.DirectMenu");

        addMenu("jmri.jmrix.can.CanMenu");
        addMenu("jmri.jmrix.ecos.Menu");
    }

    void addMenu(String className) {
        JMenu j = null;
        try {
            j = (JMenu) Class.forName(className).newInstance();
        } catch (Exception e) {
            log.warn("Could not make menu from class "+className+"; "+e);
        }
        if (j!=null) add(j);
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SystemsMenu.class.getName());
}


