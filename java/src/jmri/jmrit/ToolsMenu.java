package jmri.jmrit;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import javax.annotation.CheckForNull;

import jmri.InstanceManager;
import jmri.jmrit.throttle.ThrottleCreationAction;
import jmri.jmrit.z21server.Z21serverCreationAction;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.AddressedProgrammerManager;
import jmri.GlobalProgrammerManager;
import jmri.jmrit.swing.ToolsMenuAction;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;

/**
 * Create a "Tools" menu containing the Jmri system-independent tools
 * <p>
 * As a best practice, we are migrating the action names (constructor arguments)
 * out of this class and into the contructors themselves.
 *
 * @author Bob Jacobsen Copyright 2003, 2008
 * @author Matthew Harris copyright (c) 2009
 */
public class ToolsMenu extends JMenu {

    ConnectionConfig serModeProCon = null;
    ConnectionConfig opsModeProCon = null;
    
    AbstractAction serviceAction = new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction(Bundle.getMessage("MenuItemDecoderProServiceProgrammer"));
    AbstractAction opsAction = new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction(Bundle.getMessage("MenuItemDecoderProOpsModeProgrammer"));
        
    public ToolsMenu(String name) {
        this();
        setText(name);
    }

    public ToolsMenu() {

        super();

        setText(Bundle.getMessage("MenuTools"));
        
        JMenu programmerMenu = new JMenu(Bundle.getMessage("MenuProgrammers"));
        programmerMenu.add(new jmri.jmrit.simpleprog.SimpleProgAction());
        programmerMenu.add(serviceAction);
        programmerMenu.add(opsAction);
        programmerMenu.add(new jmri.jmrit.dualdecoder.DualDecoderToolAction());
        add(programmerMenu);

        // disable programmer menu if there's no programmer manager
        if (InstanceManager.getNullableDefault(jmri.AddressedProgrammerManager.class) == null
                && InstanceManager.getNullableDefault(jmri.GlobalProgrammerManager.class) == null) {
            programmerMenu.setEnabled(false);
        }

        JMenu tableMenu = new JMenu(Bundle.getMessage("MenuTables"));

        ///tableMenu.add(tableMenu);    /// <=== WHY?
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemTurnoutTable"), "jmri.jmrit.beantable.TurnoutTableTabAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSensorTable"), "jmri.jmrit.beantable.SensorTableTabAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLightTable"), "jmri.jmrit.beantable.LightTableTabAction"));

        JMenu signalMenu = new JMenu(Bundle.getMessage("MenuSignals"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSignalTable"), "jmri.jmrit.beantable.SignalHeadTableAction"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSignalMastTable"), "jmri.jmrit.beantable.SignalMastTableAction"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSignalGroupTable"), "jmri.jmrit.beantable.SignalGroupTableAction"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSignalMastLogicTable"), "jmri.jmrit.beantable.SignalMastLogicTableAction"));
        tableMenu.add(signalMenu);

        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemReporterTable"), "jmri.jmrit.beantable.ReporterTableTabAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemMemoryTable"), "jmri.jmrit.beantable.MemoryTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemRouteTable"), "jmri.jmrit.beantable.RouteTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLRouteTable"), "jmri.jmrit.beantable.LRouteTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLogixTable"), "jmri.jmrit.beantable.LogixTableAction"));

        JMenu logixNG_Menu = new JMenu(Bundle.getMessage("MenuLogixNG"));
        logixNG_Menu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLogixNGTable"), "jmri.jmrit.beantable.LogixNGTableAction"));
        logixNG_Menu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLogixNGModuleTable"), "jmri.jmrit.beantable.LogixNGModuleTableAction"));
        logixNG_Menu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLogixNGTableTable"), "jmri.jmrit.beantable.LogixNGTableTableAction"));
        logixNG_Menu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemLogixNGGlobalVariableTableAction"), "jmri.jmrit.beantable.LogixNGGlobalVariableTableAction"));
        tableMenu.add(logixNG_Menu);

        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemBlockTable"), "jmri.jmrit.beantable.BlockTableAction"));
        if (InstanceManager.getDefault(GuiLafPreferencesManager.class).isOblockEditTabbed()) { // turn on or off in prefs
            tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemOBlockTable"), "jmri.jmrit.beantable.OBlockTableAction"));
        } else {
            tableMenu.add(new jmri.jmrit.beantable.OBlockTableAction(Bundle.getMessage("MenuItemOBlockTable")));
        }
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSectionTable"), "jmri.jmrit.beantable.SectionTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemTransitTable"), "jmri.jmrit.beantable.TransitTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemAudioTable"), "jmri.jmrit.beantable.AudioTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemIdTagTable"), "jmri.jmrit.beantable.IdTagTableTabAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemRailComTable"), "jmri.jmrit.beantable.RailComTableAction"));
        add(tableMenu);

        JMenu throttleMenu = new JMenu(Bundle.getMessage("MenuThrottles"));
        ThrottleCreationAction.addNewThrottleItemsToThrottleMenu(throttleMenu);

        throttleMenu.add(new jmri.jmrit.throttle.ThrottlesListAction(Bundle.getMessage("MenuItemThrottlesList")));
        throttleMenu.addSeparator();
        throttleMenu.add(new jmri.jmrit.throttle.StoreXmlThrottlesLayoutAction(Bundle.getMessage("MenuItemSaveThrottleLayout")));
        throttleMenu.add(new jmri.jmrit.throttle.LoadXmlThrottlesLayoutAction(Bundle.getMessage("MenuItemLoadThrottleLayout")));
        throttleMenu.addSeparator();
        throttleMenu.add(new jmri.jmrit.throttle.StoreDefaultXmlThrottlesLayoutAction(Bundle.getMessage("MenuItemSaveAsDefaultThrottleLayout")));
        throttleMenu.add(new jmri.jmrit.throttle.LoadDefaultXmlThrottlesLayoutAction(Bundle.getMessage("MenuItemLoadDefaultThrottleLayout")));
        //throttleMenu.addSeparator();
        //throttleMenu.add(new jmri.jmrit.throttle.ThrottlesPreferencesAction(Bundle.getMessage("MenuItemThrottlesPreferences"))); // now in tabbed preferences
        throttleMenu.add(new JSeparator());
        throttleMenu.add(new jmri.jmrit.withrottle.WiThrottleCreationAction(Bundle.getMessage("MenuItemStartWiThrottle")));
        add(throttleMenu);

        // disable the throttle menu if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            throttleMenu.setEnabled(false);
        }

        AbstractAction consistAction = new jmri.jmrit.consisttool.ConsistToolAction(Bundle.getMessage("MenuItemConsistTool"));

        add(consistAction);

        // disable the consist tool if there is no consist Manager
        jmri.ConsistManager consistManager = jmri.InstanceManager.getNullableDefault(jmri.ConsistManager.class);
        if (consistManager == null) {
            consistAction.setEnabled(false);
        } else if (consistManager.canBeDisabled()) {
            consistManager.registerEnableListener((value) -> {
                consistAction.setEnabled(value);
            });
            consistAction.setEnabled(consistManager.isEnabled());
        }

        JMenu clockMenu = new JMenu(Bundle.getMessage("MenuClocks"));
        clockMenu.add(new jmri.jmrit.simpleclock.SimpleClockAction(Bundle.getMessage("MenuItemSetupClock")));
        clockMenu.add(new jmri.jmrit.nixieclock.NixieClockAction(Bundle.getMessage("MenuItemNixieClock")));
        clockMenu.add(new jmri.jmrit.lcdclock.LcdClockAction(Bundle.getMessage("MenuItemLcdClock")));
        clockMenu.add(new jmri.jmrit.analogclock.AnalogClockAction(Bundle.getMessage("MenuItemAnalogClock")));
        clockMenu.add(new jmri.jmrit.pragotronclock.PragotronClockAction(Bundle.getMessage("MenuItemPragotronClock")));
        add(clockMenu);

        add(new JSeparator());
        // single-pane tools
        add(new jmri.jmrit.powerpanel.PowerPanelAction(Bundle.getMessage("MenuItemPowerControl")));
        add(new jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlAction(Bundle.getMessage("MenuItemTurnoutControl")));
        add(new jmri.jmrit.simplelightctrl.SimpleLightCtrlAction(Bundle.getMessage("MenuItemLightControl")));
        add(new jmri.jmrit.speedometer.SpeedometerAction(Bundle.getMessage("MenuItemSpeedometer")));
        add(new jmri.jmrit.swing.meter.MeterAction(Bundle.getMessage("MenuItemMeter")));
        add(new jmri.jmrit.sensorgroup.SensorGroupAction(Bundle.getMessage("MenuItemSensorGroup")));
        add(new jmri.jmrit.blockboss.BlockBossAction(Bundle.getMessage("MenuItemSimpleSignal")));
        add(new jmri.jmrit.sendpacket.SendPacketAction(Bundle.getMessage("MenuItemSendDCCPacket")));

        add(new JSeparator());
        // more complex multi-window tools
        add(new jmri.jmrit.operations.OperationsMenu());
        add(new jmri.jmrit.dispatcher.DispatcherAction(Bundle.getMessage("MenuItemDispatcher")));
        add(new jmri.jmrit.timetable.swing.TimeTableAction(Bundle.getMessage("MenuItemTimeTable")));
        add(new jmri.jmrit.whereused.WhereUsedAction(Bundle.getMessage("MenuItemWhereUsed")));
        // CTC menu item with submenus
        JMenu ctcMenu = new JMenu(Bundle.getMessage("MenuCTC"));
        ctcMenu.add(new jmri.jmrit.ctc.editor.CtcEditorAction(Bundle.getMessage("MenuItemCTCEditor")));
        ctcMenu.add(new jmri.jmrit.ctc.CtcRunAction(Bundle.getMessage("MenuItemCTCMain")));
        add(ctcMenu);
        // US&S CTC subsystem tools
        add(new jmri.jmrit.ussctc.ToolsMenu());
        // add cab signals
        add(new jmri.jmrit.cabsignals.CabSignalAction());

        add(new JSeparator());
        JMenu serverMenu = new JMenu(Bundle.getMessage("MenuServers"));
        serverMenu.add(new jmri.jmrit.withrottle.WiThrottleCreationAction());
        serverMenu.add(new Z21serverCreationAction());
        serverMenu.add(new jmri.web.server.WebServerAction());
        serverMenu.add(new JSeparator());
        serverMenu.add(new jmri.jmris.srcp.JmriSRCPServerAction());
        serverMenu.add(new jmri.jmris.simpleserver.SimpleServerAction());
        add(serverMenu);

        add(new JSeparator());
        JMenu vsdMenu = new JMenu(Bundle.getMessage("MenuItemVSDecoder"));
        vsdMenu.add(new jmri.jmrit.vsdecoder.VSDecoderCreationAction(Bundle.getMessage("MenuItemVSDecoderManager")));
        vsdMenu.add(new jmri.jmrit.vsdecoder.swing.ManageLocationsAction(Bundle.getMessage("MenuItemVSDecoderLocationManager")));
        vsdMenu.add(new jmri.jmrit.vsdecoder.swing.VSDPreferencesAction(Bundle.getMessage("MenuItemVSDecoderPreferences")));
        add(vsdMenu);

        add(new JSeparator());
        // LogixNG menu
        add(new jmri.jmrit.logixng.tools.swing.LogixNGMenu());

        // Enable or disable the service mode programmer menu items for the types of programmer available.
        updateProgrammerStatus(null);
        ConnectionStatus.instance().addPropertyChangeListener((PropertyChangeEvent e) -> {
            if ((e.getPropertyName().equals("change")) || (e.getPropertyName().equals("add"))) {
                log.debug("Received property {} with value {} ", e.getPropertyName(), e.getNewValue());
                updateProgrammerStatus(e);
            }
        });
        InstanceManager.addPropertyChangeListener(InstanceManager.getListPropertyName(AddressedProgrammerManager.class),
                evt -> {
                    AddressedProgrammerManager m = (AddressedProgrammerManager) evt.getNewValue();
                    if (m != null) {
                        m.addPropertyChangeListener(this::updateProgrammerStatus);
                    }
                    updateProgrammerStatus(evt);
                });
        InstanceManager.getList(AddressedProgrammerManager.class).forEach(m -> m.addPropertyChangeListener(this::updateProgrammerStatus));
        InstanceManager.addPropertyChangeListener(InstanceManager.getListPropertyName(GlobalProgrammerManager.class),
                evt -> {
                    GlobalProgrammerManager m = (GlobalProgrammerManager) evt.getNewValue();
                    if (m != null) {
                        m.addPropertyChangeListener(this::updateProgrammerStatus);
                    }
                    updateProgrammerStatus(evt);
                });
        InstanceManager.getList(GlobalProgrammerManager.class).forEach(m -> m.addPropertyChangeListener(this::updateProgrammerStatus));

        // add items given by ToolsMenuItem service provider
        var newItemList = new ArrayList<ToolsMenuAction>();
        java.util.ServiceLoader.load(jmri.jmrit.swing.ToolsMenuAction.class).forEach((toolsMenuAction) -> {
            newItemList.add(toolsMenuAction);
        });
        if (!newItemList.isEmpty()) {
            add(new JSeparator());
            newItemList.forEach((item) -> {
                log.info("Adding Plug In \'{}\' to Tools Menu", item);
                add(item);
            });
        }

    }

    /**
     * Enable or disable the service mode programmer menu items for the types of programmer
     * available.
     *
     * Adapted from similar named function in @link jmri.jmrit.roster.swing.RosterFrame.java
     * 
     * @param evt the triggering event; if not null and if a removal of a
     *            ProgrammerManager, care will be taken not to trigger the
     *            automatic creation of a new ProgrammerManager
     */
    protected void updateProgrammerStatus(@CheckForNull PropertyChangeEvent evt) {
        log.debug("Updating Programmer Status for property {}", (evt != null) ? evt.getPropertyName() : "null");
        ConnectionConfig oldServMode = serModeProCon;
        ConnectionConfig oldOpsMode = opsModeProCon;
        GlobalProgrammerManager gpm = null;
        AddressedProgrammerManager apm = null;

        // Find the connection that goes with the global programmer
        // test that IM has a default GPM, or that event is not the removal of a GPM
        if (InstanceManager.containsDefault(GlobalProgrammerManager.class)
                || (evt != null
                && evt.getPropertyName().equals(InstanceManager.getDefaultsPropertyName(GlobalProgrammerManager.class))
                && evt.getNewValue() == null)) {
            gpm = InstanceManager.getNullableDefault(GlobalProgrammerManager.class);
            log.trace("found global programming manager {}", gpm);
        }
        if (gpm != null) {
            String serviceModeProgrammerName = gpm.getUserName();
            log.debug("GlobalProgrammerManager found of class {} name {} ", gpm.getClass(), serviceModeProgrammerName);
            InstanceManager.getOptionalDefault(ConnectionConfigManager.class).ifPresent((ccm) -> {
                for (ConnectionConfig connection : ccm) {
                    log.debug("Checking connection name {}", connection.getConnectionName());
                    if (connection.getConnectionName() != null && connection.getConnectionName().equals(serviceModeProgrammerName)) {
                        log.debug("Connection found for GlobalProgrammermanager");
                        serModeProCon = connection;
                    }
                }
            });
        }

        // Find the connection that goes with the addressed programmer
        // test that IM has a default APM, or that event is not the removal of an APM
        if (InstanceManager.containsDefault(AddressedProgrammerManager.class)
                || (evt != null
                && evt.getPropertyName().equals(InstanceManager.getDefaultsPropertyName(AddressedProgrammerManager.class))
                && evt.getNewValue() == null)) {
            apm = InstanceManager.getNullableDefault(AddressedProgrammerManager.class);
            log.trace("found addressed programming manager {}", gpm);
        }
        if (apm != null) {
            String opsModeProgrammerName = apm.getUserName();
            log.debug("AddressedProgrammerManager found of class {} name {} ", apm.getClass(), opsModeProgrammerName);
            InstanceManager.getOptionalDefault(ConnectionConfigManager.class).ifPresent((ccm) -> {
                for (ConnectionConfig connection : ccm) {
                    log.debug("Checking connection name {}", connection.getConnectionName());
                    if (connection.getConnectionName() != null && connection.getConnectionName().equals(opsModeProgrammerName)) {
                        log.debug("Connection found for AddressedProgrammermanager");
                        opsModeProCon = connection;
                    }
                }
            });
        }

        log.trace("start global check with {}, {}, {}", serModeProCon, gpm, (gpm != null ? gpm.isGlobalProgrammerAvailable() : "<none>"));
        if (gpm != null && gpm.isGlobalProgrammerAvailable()) {
            log.debug("service mode available");
            if (oldServMode == null) {
                serviceAction.setEnabled(true);
                firePropertyChange("setprogservice", "setEnabled", true);
            }
        } else {
            // No service programmer available, disable menu
            log.debug("no service programmer");
            if (oldServMode != null) {
                serviceAction.setEnabled(false);
                firePropertyChange("setprogservice", "setEnabled", false);
            }
            serModeProCon = null;
        }

        if (apm != null && apm.isAddressedModePossible()) {
            log.debug("ops mode available");
            if (oldOpsMode == null) {
                opsAction.setEnabled(true);
                firePropertyChange("setprogops", "setEnabled", true);
            }
        } else {
            // No ops mode programmer available, disable interface sections not available
            log.debug("no ops mode programmer");
            if (oldOpsMode != null) {
                opsAction.setEnabled(false);
                firePropertyChange("setprogops", "setEnabled", false);
            }
            opsModeProCon = null;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(jmri.jmrit.ToolsMenu.class);
    
}
