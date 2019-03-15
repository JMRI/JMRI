package jmri.jmrix;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import jmri.jmrix.swing.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a "Systems" menu containing as submenus the JMRI system-specific menus
 * for available systems.
 * <p>
 * Also provides a static member for adding these items to an existing menu.
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
        setText(Bundle.getMessage("MenuSystems"));
        addItems(this);
    }

    /**
     * Add menus for active systems to the menu bar.
     */
    static public void addItems(JMenuBar m) {

        // get ComponentFactory objects and create menus
        java.util.List<ComponentFactory> list
                = jmri.InstanceManager.getList(ComponentFactory.class);

        for (ComponentFactory memo : list) {
            try {
                JMenu menu = memo.getMenu();
                if (menu != null) {
                    m.add(menu);
                }
            } catch (RuntimeException ex) {
                log.error("Proceeding after error while trying to create menu for {}", memo.getClass(), ex);
            }
        }
    }

    /**
     * Add active systems as submenus inside a single menu entry.
     */
    static public void addItems(JMenu m) {

        // get ComponentFactory objects and create menus
        java.util.List<ComponentFactory> list
                = jmri.InstanceManager.getList(ComponentFactory.class);

        for (ComponentFactory memo : list) {
            JMenu menu = memo.getMenu();
            if (menu != null) {
                m.add(menu);
            }
        }
    }

    static JMenu getMenu(String className) {
        try {
            return (JMenu) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
            log.error("Could not load class {}", className, e);
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ActiveSystemsMenu.class);

}
