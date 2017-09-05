package jmri.jmrix;

import java.util.ResourceBundle;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import jmri.jmrix.swing.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a "Systems" menu containing as submenus the JMRI system-specific menus
 * for available systems.
 * <P>
 * Also provides a static member for adding these items to an existing menu.
 *
 * @see SystemsMenu
 *
 * @author Bob Jacobsen Copyright 2003
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

    /**
     * Add menus for active systems to the menu bar
     */
    static public void addItems(JMenuBar m) {

        // get ComponentFactory objects and create menus
        java.util.List<ComponentFactory> list
                = jmri.InstanceManager.getList(ComponentFactory.class);

        for (ComponentFactory memo : list) {
            JMenu menu = memo.getMenu();
            if (menu != null) {
                m.add(menu);
            }
        }

        if (jmri.jmrix.easydcc.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.easydcc.EasyDCCMenu"));
        }

        if (jmri.jmrix.grapevine.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.grapevine.GrapevineMenu"));
        }

        if (jmri.jmrix.oaktree.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.oaktree.OakTreeMenu"));
        }

        if (jmri.jmrix.rps.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.rps.RpsMenu"));
        }

        if (jmri.jmrix.secsi.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.secsi.SecsiMenu"));
        }

        if (jmri.jmrix.tmcc.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.tmcc.TMCCMenu"));
        }

        if (jmri.jmrix.direct.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.direct.DirectMenu"));
        }

        if (jmri.jmrix.maple.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.maple.MapleMenu"));
        }

    }

    /**
     * Add active systems as submenus inside a single menu entry. Only used in
     * JmriDemo, which has a huge number of menus
     */
    static public void addItems(JMenu m) {
        //ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // get ComponentFactory objects and create menus
        java.util.List<ComponentFactory> list
                = jmri.InstanceManager.getList(ComponentFactory.class);

        for (ComponentFactory memo : list) {
            JMenu menu = memo.getMenu();
            if (menu != null) {
                m.add(menu);
            }
        }


        if (jmri.jmrix.easydcc.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.easydcc.EasyDCCMenu"));
        }
        if (jmri.jmrix.grapevine.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.grapevine.GrapevineMenu"));
        }
        if (jmri.jmrix.oaktree.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.oaktree.OakTreeMenu"));
        }
        if (jmri.jmrix.rps.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.rps.RpsMenu"));
        }
        if (jmri.jmrix.secsi.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.secsi.SecsiMenu"));
        }
        if (jmri.jmrix.tmcc.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.tmcc.TMCCMenu"));
        }
        m.add(new javax.swing.JSeparator());

        if (jmri.jmrix.direct.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.direct.DirectMenu"));
        }

        if (jmri.jmrix.maple.ActiveFlag.isActive()) {
            m.add(getMenu("jmri.jmrix.maple.MapleMenu"));
        }
    }

    static JMenu getMenu(String className) {
        try {
            return (JMenu) Class.forName(className).newInstance();
        } catch (Exception e) {
            log.error("Could not load class " + className, e);
            return null;
        }
    }
    private final static Logger log = LoggerFactory.getLogger(ActiveSystemsMenu.class);

}
