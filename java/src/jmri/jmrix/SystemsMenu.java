// SystemsMenu.java

package jmri.jmrix;

import org.apache.log4j.Logger;
import java.util.ResourceBundle;

import javax.swing.JMenu;

import jmri.jmrix.swing.ComponentFactory;

/**
 * Provide a "Systems" menu containing the Jmri system-specific tools in submenus.
 * <P>
 * This contains all compiled systems, whether active or not.  For the
 * set of currently-active system-specific tools, see
 * {@link ActiveSystemsMenu}.
 *
 * @see ActiveSystemsMenu
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision$
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

        // Put configured menus at top
        // get ComponentFactory object(s) and create menus
        java.util.List<Object> list 
                = jmri.InstanceManager.getList(ComponentFactory.class);
        if (list != null) {
            for (Object memo : list) {
                JMenu menu = ((ComponentFactory)memo).getMenu();
                if (menu != null) add(menu);
            }
            add(new javax.swing.JSeparator());
        }

        addMenu("jmri.jmrix.acela.AcelaMenu");
        addMenu("jmri.jmrix.bachrus.SpeedoMenu");
        // CAN is migrated
        add( new jmri.jmrix.can.swing.CanMenu(null));
        
        // Merg CBUS is migrated
        add( new jmri.jmrix.can.cbus.swing.CbusMenu(null));

        addMenu("jmri.jmrix.cmri.CMRIMenu");
        addMenu("jmri.jmrix.easydcc.EasyDCCMenu");
        addMenu("jmri.jmrix.grapevine.GrapevineMenu");
        
        // LocoNet is migrated
        add(new jmri.jmrix.loconet.swing.LocoNetMenu(null));
        //addMenu("jmri.jmrix.loconet.swing.LocoNetMenu");
        // NCE is migrated
        add( new jmri.jmrix.nce.swing.NceMenu(null));

        // OpenLCB is migrated
        add(new jmri.jmrix.openlcb.OpenLcbMenu(null));

        addMenu("jmri.jmrix.oaktree.OakTreeMenu");
        // Powerline is migrated
        add(new jmri.jmrix.powerline.swing.PowerlineMenu(null));
        addMenu("jmri.jmrix.pricom.PricomMenu");
        addMenu("jmri.jmrix.qsi.QSIMenu");
        addMenu("jmri.jmrix.rps.RpsMenu");
        addMenu("jmri.jmrix.secsi.SecsiMenu");
        addMenu("jmri.jmrix.sprog.SPROGMenu");
        addMenu("jmri.jmrix.srcp.SystemMenu");
        addMenu("jmri.jmrix.tmcc.TMCCMenu");
        addMenu("jmri.jmrix.wangrow.WangrowMenu");
        // XPressNet Allows Multiple Connections now
        add( new jmri.jmrix.lenz.swing.XNetMenu(null));
        addMenu("jmri.jmrix.xpa.XpaMenu");
        addMenu("jmri.jmrix.zimo.Mx1Menu");
        add(new javax.swing.JSeparator());
        addMenu("jmri.jmrix.direct.DirectMenu");

        // nmranet is migrated
        add(new jmri.jmrix.can.nmranet.swing.NmraNetMenu(null));
        
        // Ecos is migrated
        add( new jmri.jmrix.ecos.swing.EcosMenu(null));
        
        addMenu("jmri.jmrix.maple.MapleMenu");
        // The JMRI Network ClientAllows Multiple Connections
        add( new jmri.jmrix.jmriclient.swing.JMRIClientMenu(null));
        
        add(new jmri.jmrix.rfid.swing.RfidMenu(null));
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
    
    static Logger log = Logger.getLogger(SystemsMenu.class.getName());
}


