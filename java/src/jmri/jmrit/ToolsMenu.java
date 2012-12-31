/**
 * ToolsMenu.java
 */

package jmri.jmrit;

import javax.swing.*;

import java.util.*;

/**
 * Create a "Tools" menu containing the Jmri system-independent tools
 *<P>
 * As a best practice, we are migrating the action names (constructor arguments)
 * out of this class and into the contructors themselves.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2008
 * @author      Matthew Harris copyright (c) 2009
 * @version     $Revision$
 */
public class ToolsMenu extends JMenu {
    public ToolsMenu(String name) {
        this();
        setText(name);
    }

    Action prefsAction;
    
    protected void doPreferences() {
            prefsAction.actionPerformed(null);
    }
    
    public ToolsMenu() {

        super();

        setText(Bundle.getString("MenuTools"));

        JMenu programmerMenu = new JMenu(Bundle.getString("MenuProgrammers"));
        programmerMenu.add(new jmri.jmrit.simpleprog.SimpleProgAction());
        programmerMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction(Bundle.getString("MenuItemDecoderProServiceProgrammer")));
        programmerMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction(Bundle.getString("MenuItemDecoderProOpsModeProgrammer")));
        programmerMenu.add(new jmri.jmrit.dualdecoder.DualDecoderToolAction());
        add(programmerMenu);

        // disable programmer menu if there's no programmer manager
        if (jmri.InstanceManager.programmerManagerInstance()==null){
        	programmerMenu.setEnabled(false);
        }
        
        JMenu tableMenu = new JMenu(Bundle.getString("MenuTables"));
        
        tableMenu.add(tableMenu);
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemTurnoutTable"), "jmri.jmrit.beantable.TurnoutTableTabAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemSensorTable"), "jmri.jmrit.beantable.SensorTableTabAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemLightTable"), "jmri.jmrit.beantable.LightTableTabAction"));
        
        JMenu signalMenu = new JMenu(Bundle.getString("MenuSignals"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemSignalTable"), "jmri.jmrit.beantable.SignalHeadTableAction"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemSignalMastTable"), "jmri.jmrit.beantable.SignalMastTableAction"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemSignalGroupTable"), "jmri.jmrit.beantable.SignalGroupTableAction"));
        signalMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemSignalMastLogicTable"), "jmri.jmrit.beantable.SignalMastLogicTableAction"));
        
        tableMenu.add(signalMenu);
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemReporterTable"), "jmri.jmrit.beantable.ReporterTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemMemoryTable"), "jmri.jmrit.beantable.MemoryTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemRouteTable"), "jmri.jmrit.beantable.RouteTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemLRouteTable"), "jmri.jmrit.beantable.LRouteTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemLogixTable"), "jmri.jmrit.beantable.LogixTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.OBlockTableAction(Bundle.getString("MenuItemOBlockTable")));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemBlockTable"), "jmri.jmrit.beantable.BlockTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemSectionTable"), "jmri.jmrit.beantable.SectionTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemTransitTable"), "jmri.jmrit.beantable.TransitTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemAudioTable"), "jmri.jmrit.beantable.AudioTableAction"));
        tableMenu.add(new jmri.jmrit.beantable.ListedTableAction(Bundle.getString("MenuItemIdTagTable"), "jmri.jmrit.beantable.IdTagTableAction"));
        add(tableMenu);

        JMenu throttleMenu = new JMenu(Bundle.getString("MenuThrottles"));
        throttleMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(Bundle.getString("MenuItemNewThrottle" )));
        throttleMenu.add(new jmri.jmrit.throttle.ThrottlesListAction(Bundle.getString("MenuItemThrottlesList" )));
        throttleMenu.addSeparator();
        throttleMenu.add(new jmri.jmrit.throttle.StoreXmlThrottlesLayoutAction(Bundle.getString("MenuItemSaveThrottleLayout" )));
        throttleMenu.add(new jmri.jmrit.throttle.LoadXmlThrottlesLayoutAction(Bundle.getString("MenuItemLoadThrottleLayout")));
        throttleMenu.addSeparator();
        throttleMenu.add(new jmri.jmrit.throttle.StoreDefaultXmlThrottlesLayoutAction(Bundle.getString("MenuItemSaveAsDefaultThrottleLayout" )));
        throttleMenu.add(new jmri.jmrit.throttle.LoadDefaultXmlThrottlesLayoutAction(Bundle.getString("MenuItemLoadDefaultThrottleLayout" )));
        //throttleMenu.addSeparator();
        //throttleMenu.add(new jmri.jmrit.throttle.ThrottlesPreferencesAction(Bundle.getString("MenuItemThrottlesPreferences"))); // now in tabbed preferences
        throttleMenu.add(new JSeparator());
        throttleMenu.add(new jmri.jmrit.withrottle.WiThrottleCreationAction(Bundle.getString("MenuItemStartWiThrottle")));
        add(throttleMenu);

	// disable the throttle menu if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance()==null) {
            throttleMenu.setEnabled(false);
        }

        AbstractAction consistAction= new jmri.jmrit.consisttool.ConsistToolAction(Bundle.getString("MenuItemConsistTool"));

        add(consistAction);

	// disable the consist tool if there is no consist Manager
        if (jmri.InstanceManager.consistManagerInstance()==null) {
            consistAction.setEnabled(false);
        }


        JMenu clockMenu = new JMenu(Bundle.getString("MenuClocks"));
        clockMenu.add(new jmri.jmrit.simpleclock.SimpleClockAction(Bundle.getString("MenuItemSetupClock")));
        clockMenu.add(new jmri.jmrit.nixieclock.NixieClockAction(Bundle.getString("MenuItemNixieClock")));
        clockMenu.add(new jmri.jmrit.lcdclock.LcdClockAction(Bundle.getString("MenuItemLcdClock")));
        clockMenu.add(new jmri.jmrit.analogclock.AnalogClockAction(Bundle.getString("MenuItemAnalogClock")));
	add(clockMenu);

        add(new JSeparator());

        add(new jmri.jmrit.powerpanel.PowerPanelAction(Bundle.getString("MenuItemPowerControl")));
        add(new jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlAction(Bundle.getString("MenuItemTurnoutControl")));
        add(new jmri.jmrit.blockboss.BlockBossAction(Bundle.getString("MenuItemSimpleSignal")));
        add(new jmri.jmrit.sensorgroup.SensorGroupAction(Bundle.getString("MenuItemSensorGroup")));
    	add(new jmri.jmrit.speedometer.SpeedometerAction(Bundle.getString("MenuItemSpeedometer")));
        add(new jmri.jmrit.simplelightctrl.SimpleLightCtrlAction(Bundle.getString("MenuItemLightControl")));
        add(new jmri.jmrit.dispatcher.DispatcherAction(Bundle.getString("MenuItemDispatcher")));

        add(new JSeparator());

        add(new jmri.jmrit.sendpacket.SendPacketAction( Bundle.getString("MenuItemSendDCCPacket") ));

        add(new JSeparator());
        // US&S CTC subsystem tools
        add(new jmri.jmrit.ussctc.ToolsMenu());

        add(new JSeparator());
        // operations menu
        add(new jmri.jmrit.operations.OperationsMenu());

        add(new JSeparator());
        // add start web server
        add(new jmri.web.server.WebServerAction());

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ToolsMenu.class.getName());
}


