package jmri.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import jmri.InstanceManager;
import jmri.PowerManager;

/**
 * Create a menu for selecting the Power Manager to use
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.5
 */
public abstract class PowerManagerMenu extends JMenu {

    JMenuItem allConnsItem = new JRadioButtonMenuItem(Bundle.getMessage("AllConnections"));
    private final List<JMenuItem> menuItems = new java.util.ArrayList<>();

    protected abstract void choiceChanged();

    /**
     * Create a PowerManager menu.
     */
    public PowerManagerMenu() {
        this(false, null);
    }

    /**
     * Create a PowerManager menu.
     * @param includeAllConns true to include all connections
     * @param defaultPwrMgr specify a PowerManager to be selected on Menu startup.
     */
    public PowerManagerMenu(boolean includeAllConns, PowerManager defaultPwrMgr) {
        super();

        ButtonGroup group = new ButtonGroup();

        // label this menu
        setText(Bundle.getMessage("MenuConnection")) ;

        if (includeAllConns) {
            add(allConnsItem);
            group.add(allConnsItem);
            menuItems.add(allConnsItem);
            allConnsItem.addActionListener((java.awt.event.ActionEvent e) -> 
                choiceChanged());
        }

        // now add an item for each available manager
        List<PowerManager> managers = InstanceManager.getList(PowerManager.class);
        for (PowerManager mgr : managers) {
            if (mgr != null) {
                JMenuItem item = new JRadioButtonMenuItem(getManagerNameIncludeIfDefault(mgr));
                item.setActionCommand(mgr.getUserName());
                add(item);
                group.add(item);
                menuItems.add(item);
                item.addActionListener((java.awt.event.ActionEvent e) -> 
                    choiceChanged());
            }
        }

        PowerManagerMenu.this.setManager(defaultPwrMgr);
    }

    public void setManager(@CheckForNull PowerManager suppliedMgr) {
        if (InstanceManager.getNullableDefault(jmri.PowerManager.class) == null) {
            return;
        }
        String searchMgr = ( suppliedMgr != null ? suppliedMgr.getUserName() : "");
        for (JMenuItem item : menuItems) {
            if (searchMgr.equals(item.getActionCommand())) {
                item.setSelected(true);
                return;
            }
        }
        allConnsItem.setSelected(true);
    }

    /**
     * Get the selected PowerManager.
     * @return null if All Connections option selected.
     */
    @CheckForNull
    public PowerManager getManager() {

        String name="";
        // find active name
        for (JMenuItem item : menuItems) {
            if (item.isSelected()) {
                name = item.getActionCommand();
                break;
            }
        }
        // find PowerManager and return
        List<PowerManager> managers = InstanceManager.getList(PowerManager.class);
        if (managers.size()==1){
            return managers.get(0);
        }
        for (PowerManager mgr : managers) {
            if (name.equals(mgr.getUserName())) {
                return mgr;
            }
        }
        return null;
    }

    @Nonnull
    public static String getManagerNameIncludeIfDefault(@Nonnull PowerManager mgr){
        String mgrName = mgr.getUserName();
        if ( mgr == InstanceManager.getDefault(PowerManager.class) ) {
            mgrName = Bundle.getMessage("DefaultConnection",mgr.getUserName());
        }
        return mgrName;
    }

}
