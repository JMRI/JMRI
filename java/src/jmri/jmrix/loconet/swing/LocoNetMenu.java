package jmri.jmrix.loconet.swing;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JSeparator;

import java.util.ArrayList;

import jmri.jmrix.loconet.locomon.LocoMonPane;
import jmri.jmrix.loconet.slotmon.SlotMonPane;
import jmri.jmrix.loconet.clockmon.ClockMonPane;
import jmri.jmrix.loconet.locostats.swing.LocoStatsPanel;
import jmri.jmrix.loconet.bdl16.BDL16Panel;
import jmri.jmrix.loconet.pm4.PM4Panel;
import jmri.jmrix.loconet.se8.SE8Panel;
import jmri.jmrix.loconet.ds64.Ds64TabbedPanel;
import jmri.jmrix.loconet.cmdstnconfig.CmdStnConfigPane;
import jmri.jmrix.loconet.locoid.LocoIdPanel;
import jmri.jmrix.loconet.duplexgroup.swing.DuplexGroupTabbedPanel;
import jmri.jmrix.loconet.swing.throttlemsg.MessagePanel;
import jmri.jmrix.loconet.locogen.LocoGenPanel;
import jmri.jmrix.loconet.swing.lncvprog.LncvProgPane;
import jmri.jmrix.loconet.swing.lnsv1prog.Lnsv1ProgPane;
import jmri.jmrix.loconet.pr3.swing.Pr3SelectPane;
import jmri.jmrix.loconet.soundloader.LoaderPane;
import jmri.jmrix.loconet.soundloader.EditorPane;
import jmri.jmrix.loconet.loconetovertcp.LnTcpServerAction;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.swing.menuitemspi.MenuItemsService;

import jmri.util.swing.WindowInterface;
import jmri.util.swing.sdi.JmriJFrameInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Create a "Systems" menu containing the Jmri LocoNet-specific tools.
 *
 * @author Bob Jacobsen Copyright 2003, 2010
 * @author B. Milhaupt Copyright 2021, 2022
 */
public class LocoNetMenu extends JMenu {
    private boolean lastWasSeparator;



    /**
     * Create a LocoNet menu.
     * <br>
     * Adds menu items for JMRI code's LocoNet menu items, as defined in
     * the initialization of <code>panelItems</code> here, and appends those
     * menu items from SPI extensions which implement
     * {@link jmri.jmrix.loconet.swing.menuitemspi.spi.MenuItemsInterface}
     * to report additional menu items for inclusion on the LocoNet menu.
     * <br>
     * This method pre-loads the TrafficController to certain actions.
     * <br>
     * Actions will open new windows.
     *<br>
     * @param memo      {@link jmri.jmrix.loconet.LocoNetSystemConnectionMemo} to
     *                  be used by this object
     * @see jmri.jmrix.loconet.swing.menuitemspi.spi.MenuItemsInterface
     * @see jmri.jmrix.loconet.swing.menuitemspi.MenuItemsService
     */
    public LocoNetMenu(LocoNetSystemConnectionMemo memo) {
        super();
        ArrayList<LocoNetMenuItem> panelItems;
        panelItems = new ArrayList<>();

        // Define the common allExtensionItems in the LocoNet menu.  Note that
        // LnMessageServer and LnTcpServer are special-cased because they have no
        // GUI interface and are handled slightly differently by processItems().

        panelItems.add(new LocoNetMenuItem("MenuItemLocoNetMonitor", LocoMonPane.class, false, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemSlotMonitor", SlotMonPane.class, false, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemClockMon", ClockMonPane.class, true, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemLocoStats", LocoStatsPanel.class, false, true)); // NOI18N
        panelItems.add(null); // direct Ln CS/hardware tools
        panelItems.add(new LocoNetMenuItem("MenuItemBDL16Programmer", BDL16Panel.class, true, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemPM4Programmer", PM4Panel.class, true, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemSE8cProgrammer", SE8Panel.class, true, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemDS64Programmer", Ds64TabbedPanel.class, true, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemCmdStnConfig", CmdStnConfigPane.class,true, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemSetID", LocoIdPanel.class, true, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemDuplex", DuplexGroupTabbedPanel.class, true, true)); // NOI18N
        panelItems.add(null); // listing panes for Roster programming
        panelItems.add(new LocoNetMenuItem("MenuItemLnsv1Prog", Lnsv1ProgPane.class, true, true)); // NOI18N
        // Lnsv2?
        panelItems.add(new LocoNetMenuItem("MenuItemLncvProg", LncvProgPane.class, true, true)); // NOI18N
        panelItems.add(null); // message/packet tools
        panelItems.add(new LocoNetMenuItem("MenuItemThrottleMessages", MessagePanel.class, true, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemSendPacket", LocoGenPanel.class, false, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemPr3ModeSelect", Pr3SelectPane.class, false, true)); // NOI18N
        panelItems.add(null); // upload/download tools
        panelItems.add(new LocoNetMenuItem("MenuItemDownload", jmri.jmrix.loconet.downloader.LoaderPane.class, false, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemSoundload", LoaderPane.class, false, true)); // NOI18N
        panelItems.add(new LocoNetMenuItem("MenuItemSoundEditor", EditorPane.class, false, true)); // NOI18N
        panelItems.add(null); // servers
        panelItems.add(new LocoNetMenuItem("MenuItemLocoNetOverTCPServer", LnTcpServerAction.class, false, false));

        LnCommandStationType cmdStation = null;
        if (memo != null) {
            setText(memo.getUserName());
            cmdStation = memo.getSlotManager().getCommandStationType();
        } else {
            setText(Bundle.getMessage("MenuLocoNet"));
        }

        WindowInterface wi = new JmriJFrameInterface();

        boolean isLocoNetInterface;
        isLocoNetInterface = (cmdStation == null) ||
                (!cmdStation.equals(LnCommandStationType.COMMAND_STATION_PR2_ALONE) &&
                !cmdStation.equals(LnCommandStationType.COMMAND_STATION_PR3_ALONE) &&
                !cmdStation.equals(LnCommandStationType.COMMAND_STATION_PR4_ALONE) &&
                !cmdStation.equals(LnCommandStationType.COMMAND_STATION_USB_DCS240_ALONE) &&
                !cmdStation.equals(LnCommandStationType.COMMAND_STATION_USB_DCS52_ALONE));

        JMenu hostMenu = this;
        lastWasSeparator = true;  // to prevent an initial separator in menu
        processItems(hostMenu, panelItems, isLocoNetInterface, wi, memo);

        lastWasSeparator = false;
        add(hostMenu);
        panelItems.clear();
        // Deal with menu item tasks from SPI extensions
        ArrayList<JMenu> extensionMenus  = getExtensionMenuItems(isLocoNetInterface,
             wi,  memo);
        log.trace("number of extension items is {}.", panelItems.size());
        while ((!extensionMenus.isEmpty()) && (extensionMenus.get(extensionMenus.size()-1) == null)) {
            // remove any dangling separators at end of list of menu allExtensionItems
            extensionMenus.remove(panelItems.size()-1);
        }
        if (!extensionMenus.isEmpty()) {
            add(new JSeparator());  // ensure placement of a horizontal bar above
                                    // extension menu
            log.debug("number of items {}", panelItems.size());
            while (!extensionMenus.isEmpty()) {
                JMenu menu = extensionMenus.get(0);
                add(menu);
                log.trace("Added extension menu {}", menu.getName());
                extensionMenus.remove(0);
            }
        }
    }

    /**
     * Create an Action suitable for inclusion as a menu item on a LocoNet menu.
     *
     * @param item a LocoetMenuItem object which defines the menu item's
     *      characteristics, and which will be the basis for the returned Action
     *      object.
     * @param isLocoNetInterface is true if the LocoNet connection has a physical
     *      interface to LocoNet, else false.
     * @param wi the WindowInterface associated with the JMRI instance and LocoNetMenu.
     * @param memo the LocoNetSystemConnectionMemo associated with the LocoNet
     *          connection.
     * @return an Action which may be added to a local JMenu for inclusion in a
     * LocoNet connection's menu; the action's object may make use of the LocoNet
     * memo and associate its GUI objects with the JMRI WindowInterface.  If the
     * item requires a physical LocoNet interface but the connection does not have
     * such an interface, then null is returned.
     */
    public Action processExternalItem(LocoNetMenuItem item, boolean isLocoNetInterface,
            WindowInterface wi, LocoNetSystemConnectionMemo memo) {
        if (item == null) {
            return null;
        }
        if (item.hasGui()  ) {
            if (isLocoNetInterface || (!item.isInterfaceOnly())) {
                log.trace("created GUI menu item {}.", item.getName());
                return createGuiAction(item, wi, memo);
            } else {
                log.trace("not displaying item {} ({}) account requires "
                        + "interface which is not present in current "
                        + "configuration.",
                        item.getName(), item.getClassToLoad().getCanonicalName()
                        );
                return null;
            }
        } else {
            log.trace("created non-GUI menu item {}.", item.getName());
            return createNonGuiAction(item);
        }
    }

    /**
     * Create an Action object from a LocoNetMenuItem, linked to the appropriate
     * WindowInterface, for use as a menu item on a LocoNet menu.
     *
     * Depending on whether the item needs a gui and/or a physical LocoNet
     * interface, this method returns null or an Action which is suitable for
     * use as a menu item on a LocoNet menu.
     *<br>
     * If the item's name is found as a key the Bundle associated with this object,
     * then the I18N'd string will be used as the Action's text.
     *
     * @param item LocoNetMenuItem which defines the menu item's requirements.
     * @param wi WindowInterface to which the item's GUI object will be linked.
     * @param memo LocoNetSystemConnectionMemo with which the item will be linked.
     * @return null if the item's requirements are not met by the current
     *      connection, or an Action which may be used as a JMenuItem.
     */
    public Action createGuiAction(LocoNetMenuItem item, WindowInterface wi,
            LocoNetSystemConnectionMemo memo) {
        String translatedMenuItemName;
        try {
            translatedMenuItemName = Bundle.getMessage(item.getName());
        } catch (java.util.MissingResourceException e) {
            // skip internationalization if name is not present as a "key"
            translatedMenuItemName = item.getName();
        }

        return new LnNamedPaneAction(translatedMenuItemName, wi,
                item.getClassToLoad().getCanonicalName(), memo);
    }

    /**
     * Create an Action object from a LocoNetMenuItem, for use as a menu item on
     * a LocoNet menu, without linkage to the WindowInterface associated with the
     * LocoNet menu.
     *
     * This method returns an Action which is suitable for use as a menu item on
     * a LocoNet menu.
     *<br>
     * If the item's name is found as a key the Bundle associated with this object,
     * then the I18N'd string will be used as the Action's text.
     *
     * @param item LocoNetMenuItem which defines the menu item's requirements.
     * @return an Action which may be used as a JMenuItem.
     */
    public Action createNonGuiAction(LocoNetMenuItem item) {
        Action menuItem = null;
        try {
            menuItem = (Action) item.getClassToLoad()
                            .getDeclaredConstructor().newInstance();
            menuItem.putValue("NAME", item.getName()); // set the menu item name // NOI18N
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | java.lang.reflect.InvocationTargetException ex ) {
            log.warn("could not load menu item {} ({})",
                    item.getName(), item.getClassToLoad().getCanonicalName(), ex);
        }
        return menuItem;
    }

    /**
     * Get an ArrayList of JMenu objects as provided via the SPI "extension"
     * mechanisms.
     * @param isConnectionWithHardwareInterface informs whether the connection
     *      has actual hardware
     * @param wi allows the extension menu items to be associated with the
     *      JAVA WindowInterface which relates to the connection's menu
     * @param memo the LocoNetSystemConnectionMemo associated with the menu to
     *      which the extension's MenuItem(s) are to be attached.
     * @return an ArrayList of JMenu objects, as populated from the menu items
     *      reported by any available SPI extensions.  May be an empty ArrayList
     *      if none of the SPI extensions provide menu items for this menu.
     * <br>
     * @see jmri.jmrix.loconet.swing.menuitemspi.spi.MenuItemsInterface
     * @see jmri.jmrix.loconet.swing.menuitemspi.MenuItemsService
     */
    public final java.util.ArrayList<JMenu> getExtensionMenuItems(
            boolean isConnectionWithHardwareInterface, WindowInterface wi,
            LocoNetSystemConnectionMemo memo) {
        ArrayList<JMenu> locoNetMenuItems = new ArrayList<>();
        log.trace("searching for extensions for the canonical name {}",
                this.getClass().getCanonicalName());
        MenuItemsService lnMenuItemServiceInstance;
        lnMenuItemServiceInstance = MenuItemsService.getInstance();
        locoNetMenuItems.addAll(
                lnMenuItemServiceInstance.getMenuExtensionsItems(
                        isConnectionWithHardwareInterface, wi, memo));
        log.trace("LocoNetItems size is {}", locoNetMenuItems.size());
        return locoNetMenuItems;
    }

    private void processItems(JMenu menu, ArrayList<LocoNetMenuItem> items, boolean isLocoNetInterface,
            WindowInterface wi, LocoNetSystemConnectionMemo memo) {
        items.forEach(item -> {
            processAnItem(menu, item, isLocoNetInterface, wi, memo);
        });
    }

    private void processAnItem(JMenu menu, LocoNetMenuItem item, boolean isLocoNetInterface,
            WindowInterface wi, LocoNetSystemConnectionMemo memo) {
        if (item == null) {
            if (!lastWasSeparator) {
                menu.add(new JSeparator());
                log.trace("Added new JSeparator");

                lastWasSeparator = true;
            }
        } else if (item.hasGui()  ) {
            if (isLocoNetInterface || (!item.isInterfaceOnly())) {
                addGuiItem(menu, item, wi, memo);
                log.trace("added GUI item {}", item.getName());
            } else {
                log.trace("not displaying item {} ({}) account requires "
                        + "interface which is not present in current "
                        + "configuration.",
                        item.getName(), item.getClassToLoad().getCanonicalName());
            }
        } else {
            addNonGuiItem(menu, item);
                log.trace("added non-GUI item {}", item.getName());
        }
    }

    private void addNonGuiItem(JMenu menu, LocoNetMenuItem item) {
        if (item != null) {
            Action menuItem = createNonGuiAction(item);
            menu.add(menuItem);
            log.debug("Adding (non-gui) item {} ({})",
                    item.getName(), item.getClassToLoad());
            lastWasSeparator = false;
        } else {
            menu.add(new JSeparator());
            lastWasSeparator = true;
            log.trace("adding a JSeparator account null item");
        }
    }
    private void addGuiItem(JMenu menu, LocoNetMenuItem item, WindowInterface wi,
            LocoNetSystemConnectionMemo memo) {
        Action a = createGuiAction(item, wi, memo);
        menu.add(a);
        lastWasSeparator = false;
        log.debug("Added new GUI-based item for {} ({}).",
                item.getName(), item.getClassToLoad().getCanonicalName());
    }

    private static final Logger log = LoggerFactory.getLogger(LocoNetMenu.class);

}
