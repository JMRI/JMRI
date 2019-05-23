package jmri.jmrit;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import jmri.InstanceManager;

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

    public ToolsMenu(String name) {
        this();
        setText(name);
    }

    public ToolsMenu() {

        super();

        setText(Bundle.getMessage("MenuTools"));

        JMenu programmerMenu = new JMenu(Bundle.getMessage("MenuProgrammers"));
        programmerMenu.add(new jmri.jmrit.simpleprog.SimpleProgAction());
        programmerMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction(Bundle.getMessage("MenuItemDecoderProServiceProgrammer")));
        programmerMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction(Bundle.getMessage("MenuItemDecoderProOpsModeProgrammer")));
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
        tableMenu.add(new jmri.jmrit.beantable.OBlockTableAction(Bundle.getMessage("MenuItemOBlockTable")));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemBlockTable"), "jmri.jmrit.beantable.BlockTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemSectionTable"), "jmri.jmrit.beantable.SectionTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemTransitTable"), "jmri.jmrit.beantable.TransitTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemAudioTable"), "jmri.jmrit.beantable.AudioTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemIdTagTable"), "jmri.jmrit.beantable.IdTagTableTabAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getMessage("MenuItemRailComTable"), "jmri.jmrit.beantable.RailComTableAction"));
        add(tableMenu);

        JMenu throttleMenu = new JMenu(Bundle.getMessage("MenuThrottles"));
        throttleMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(Bundle.getMessage("MenuItemNewThrottle")));
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
        if (jmri.InstanceManager.getNullableDefault(jmri.ConsistManager.class) == null) {
            consistAction.setEnabled(false);
        }

        JMenu clockMenu = new JMenu(Bundle.getMessage("MenuClocks"));
        clockMenu.add(new jmri.jmrit.simpleclock.SimpleClockAction(Bundle.getMessage("MenuItemSetupClock")));
        clockMenu.add(new jmri.jmrit.nixieclock.NixieClockAction(Bundle.getMessage("MenuItemNixieClock")));
        clockMenu.add(new jmri.jmrit.lcdclock.LcdClockAction(Bundle.getMessage("MenuItemLcdClock")));
        clockMenu.add(new jmri.jmrit.analogclock.AnalogClockAction(Bundle.getMessage("MenuItemAnalogClock")));
        add(clockMenu);

        add(new JSeparator());
        // single-pane tools
        add(new jmri.jmrit.powerpanel.PowerPanelAction(Bundle.getMessage("MenuItemPowerControl")));
        add(new jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlAction(Bundle.getMessage("MenuItemTurnoutControl")));
        add(new jmri.jmrit.simplelightctrl.SimpleLightCtrlAction(Bundle.getMessage("MenuItemLightControl")));
        add(new jmri.jmrit.speedometer.SpeedometerAction(Bundle.getMessage("MenuItemSpeedometer")));
        add(new jmri.jmrit.ampmeter.AmpMeterAction(Bundle.getMessage("MenuItemAmpMeter")));
        add(new jmri.jmrit.sensorgroup.SensorGroupAction(Bundle.getMessage("MenuItemSensorGroup")));
        add(new jmri.jmrit.blockboss.BlockBossAction(Bundle.getMessage("MenuItemSimpleSignal")));
        add(new jmri.jmrit.sendpacket.SendPacketAction(Bundle.getMessage("MenuItemSendDCCPacket")));

        add(new JSeparator());
        // more complex multi-window tools
        add(new jmri.jmrit.operations.OperationsMenu());
        add(new jmri.jmrit.dispatcher.DispatcherAction(Bundle.getMessage("MenuItemDispatcher")));
        add(new jmri.jmrit.timetable.swing.TimeTableAction(Bundle.getMessage("MenuItemTimeTable")));
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
        // add start web server menu item (immediate action)
        add(new jmri.web.server.WebServerAction());
    }

}
