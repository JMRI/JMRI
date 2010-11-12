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
 * @version     $Revision: 1.43 $
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

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.JmritToolsBundle");

        setText(rb.getString("MenuTools"));

        JMenu programmerMenu = new JMenu(rb.getString("MenuProgrammers"));
        programmerMenu.add(new jmri.jmrit.simpleprog.SimpleProgAction());
        programmerMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction(rb.getString("MenuItemDecoderProServiceProgrammer")));
        programmerMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction(rb.getString("MenuItemDecoderProOpsModeProgrammer")));
        programmerMenu.add(new jmri.jmrit.dualdecoder.DualDecoderToolAction());
        add(programmerMenu);

        // disable programmer menu if there's no programmer manager
        if (jmri.InstanceManager.programmerManagerInstance()==null){
        	programmerMenu.setEnabled(false);
        }
        
        JMenu tableMenu = new JMenu(rb.getString("MenuTables"));
        
        tableMenu.add(tableMenu);
        tableMenu.add(new jmri.jmrit.beantable.TurnoutTableAction(rb.getString("MenuItemTurnoutTable")));
        tableMenu.add(new jmri.jmrit.beantable.SensorTableAction(rb.getString("MenuItemSensorTable")));
        tableMenu.add(new jmri.jmrit.beantable.LightTableAction(rb.getString("MenuItemLightTable")));
        tableMenu.add(new jmri.jmrit.beantable.SignalHeadTableAction(rb.getString("MenuItemSignalTable")));
        tableMenu.add(new jmri.jmrit.beantable.SignalMastTableAction(rb.getString("MenuItemSignalMastTable")));
        tableMenu.add(new jmri.jmrit.beantable.SignalGroupTableAction(rb.getString("MenuItemSignalGroupTable")));
        tableMenu.add(new jmri.jmrit.beantable.ReporterTableAction(rb.getString("MenuItemReporterTable")));
        tableMenu.add(new jmri.jmrit.beantable.MemoryTableAction(rb.getString("MenuItemMemoryTable")));
        tableMenu.add(new jmri.jmrit.beantable.RouteTableAction(rb.getString("MenuItemRouteTable")));
        tableMenu.add(new jmri.jmrit.beantable.LRouteTableAction(rb.getString("MenuItemLRouteTable")));
        tableMenu.add(new jmri.jmrit.beantable.LogixTableAction(rb.getString("MenuItemLogixTable")));
        tableMenu.add(new jmri.jmrit.beantable.OBlockTableAction(rb.getString("MenuItemOBlockTable")));
        tableMenu.add(new jmri.jmrit.beantable.BlockTableAction(rb.getString("MenuItemBlockTable")));
        tableMenu.add(new jmri.jmrit.beantable.SectionTableAction(rb.getString("MenuItemSectionTable")));
        tableMenu.add(new jmri.jmrit.beantable.TransitTableAction(rb.getString("MenuItemTransitTable")));
        tableMenu.add(new jmri.jmrit.beantable.AudioTableAction(rb.getString("MenuItemAudioTable")));
        add(tableMenu);

        JMenu throttleMenu = new JMenu(rb.getString("MenuThrottles"));
        throttleMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(rb.getString("MenuItemNewThrottle" )));
        throttleMenu.add(new jmri.jmrit.throttle.ThrottlesListAction(rb.getString("MenuItemThrottlesList" )));
        throttleMenu.addSeparator();
        throttleMenu.add(new jmri.jmrit.throttle.StoreXmlThrottlesLayoutAction(rb.getString("MenuItemSaveThrottleLayout" )));
        throttleMenu.add(new jmri.jmrit.throttle.LoadXmlThrottlesLayoutAction(rb.getString("MenuItemLoadThrottleLayout")));
        throttleMenu.addSeparator();
        throttleMenu.add(new jmri.jmrit.throttle.StoreDefaultXmlThrottlesLayoutAction(rb.getString("MenuItemSaveAsDefaultThrottleLayout" )));
        throttleMenu.add(new jmri.jmrit.throttle.LoadDefaultXmlThrottlesLayoutAction(rb.getString("MenuItemLoadDefaultThrottleLayout" )));
        //throttleMenu.addSeparator();
        //throttleMenu.add(new jmri.jmrit.throttle.ThrottlesPreferencesAction(rb.getString("MenuItemThrottlesPreferences"))); // now in tabbed preferences
        throttleMenu.add(new JSeparator());
        throttleMenu.add(new jmri.jmrit.withrottle.WiThrottleCreationAction(rb.getString("MenuItemStartWiThrottle")));
        add(throttleMenu);

	// disable the throttle menu if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance()==null) {
            throttleMenu.setEnabled(false);
        }

        AbstractAction consistAction= new jmri.jmrit.consisttool.ConsistToolAction(rb.getString("MenuItemConsistTool"));

        add(consistAction);

	// disable the consist tool if there is no consist Manager
        if (jmri.InstanceManager.consistManagerInstance()==null) {
            consistAction.setEnabled(false);
        }


        JMenu clockMenu = new JMenu(rb.getString("MenuClocks"));
        clockMenu.add(new jmri.jmrit.simpleclock.SimpleClockAction(rb.getString("MenuItemSetupClock")));
        clockMenu.add(new jmri.jmrit.nixieclock.NixieClockAction(rb.getString("MenuItemNixieClock")));
        clockMenu.add(new jmri.jmrit.lcdclock.LcdClockAction(rb.getString("MenuItemLcdClock")));
        clockMenu.add(new jmri.jmrit.analogclock.AnalogClockAction(rb.getString("MenuItemAnalogClock")));
	add(clockMenu);

        add(new JSeparator());

        add(new jmri.jmrit.powerpanel.PowerPanelAction(rb.getString("MenuItemPowerControl")));
        add(new jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlAction(rb.getString("MenuItemTurnoutControl")));
        add(new jmri.jmrit.blockboss.BlockBossAction(rb.getString("MenuItemSimpleSignal")));
        add(new jmri.jmrit.sensorgroup.SensorGroupAction(rb.getString("MenuItemSensorGroup")));
    	add(new jmri.jmrit.speedometer.SpeedometerAction(rb.getString("MenuItemSpeedometer")));
        add(new jmri.jmrit.simplelightctrl.SimpleLightCtrlAction(rb.getString("MenuItemLightControl")));
        add(new jmri.jmrit.dispatcher.DispatcherAction(rb.getString("MenuItemDispatcher")));

        add(new JSeparator());

        add(new jmri.jmrit.sendpacket.SendPacketAction( rb.getString("MenuItemSendDCCPacket") ));

        add(new JSeparator());
        // US&S CTC subsystem tools
        add(new jmri.jmrit.ussctc.ToolsMenu());

        add(new JSeparator());
        // operations menu
        add(new jmri.jmrit.operations.OperationsMenu());
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ToolsMenu.class.getName());
}


