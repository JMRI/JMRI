/**
 * ToolsMenu.java
 */

package jmri.jmrit;

import javax.swing.*;
import java.util.*;

/**
 * Create a "Tools" menu containing the Jmri system-independent tools
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.18 $
 */
public class ToolsMenu extends JMenu {
    public ToolsMenu(String name) {
        this();
        setText(name);
    }

    public ToolsMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.JmritToolsBundle");

        setText(rb.getString("MenuTools"));
        
        JMenu programmerMenu = new JMenu(rb.getString("MenuProgrammers"));
        programmerMenu.add(new jmri.jmrit.simpleprog.SimpleProgAction(rb.getString("MenuItemSingleCVProgrammer")));
        programmerMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction(rb.getString("MenuItemDecoderProServiceProgrammer")));
        programmerMenu.add(new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction(rb.getString("MenuItemDecoderProOpsModeProgrammer")));
        programmerMenu.add(new jmri.jmrit.dualdecoder.DualDecoderToolAction());
        add(programmerMenu);
        
        JMenu tableMenu = new JMenu(rb.getString("MenuTables"));
        tableMenu.add(new jmri.jmrit.beantable.TurnoutTableAction(rb.getString("MenuItemTurnoutTable")));
        tableMenu.add(new jmri.jmrit.beantable.SensorTableAction(rb.getString("MenuItemSensorTable")));
        tableMenu.add(new jmri.jmrit.beantable.LightTableAction(rb.getString("MenuItemLightTable")));
        tableMenu.add(new jmri.jmrit.beantable.SignalHeadTableAction(rb.getString("MenuItemSignalTable")));
        tableMenu.add(new jmri.jmrit.beantable.ReporterTableAction(rb.getString("MenuItemReporterTable")));
        tableMenu.add(new jmri.jmrit.beantable.MemoryTableAction(rb.getString("MenuItemMemoryTable")));
        tableMenu.add(new jmri.jmrit.beantable.RouteTableAction(rb.getString("MenuItemRouteTable")));
        tableMenu.add(new jmri.jmrit.beantable.LogixTableAction(rb.getString("MenuItemLogixTable")));
        add(tableMenu);

        JMenu throttleMenu = new JMenu(rb.getString("MenuThrottles"));
        throttleMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(rb.getString("MenuItemNewThrottle" )));
        throttleMenu.add(new jmri.jmrit.throttle.StoreXmlThrottleAction(rb.getString("MenuItemSaveThrottleLayout" )));
        throttleMenu.add(new jmri.jmrit.throttle.LoadXmlThrottleAction(rb.getString("MenuItemLoadThrottleLayout")));
        throttleMenu.add(new jmri.jmrit.throttle.EditThrottlePreferencesAction(rb.getString("MenuItemEditThrottlePreferences")));
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
        clockMenu.add(new jmri.jmrit.analogclock.AnalogClockAction(rb.getString("MenuItemAnalogClock")));
	add(clockMenu);

        add(new JSeparator());
        
        add(new jmri.jmrit.powerpanel.PowerPanelAction(rb.getString("MenuItemPowerControl")));
        add(new jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlAction(rb.getString("MenuItemTurnoutControl")));
        add(new jmri.jmrit.blockboss.BlockBossAction(rb.getString("MenuItemSimpleSignal")));
        add(new jmri.jmrit.sensorgroup.SensorGroupAction(rb.getString("MenuItemSensorGroup")));
        add(new jmri.jmrit.speedometer.SpeedometerAction(rb.getString("MenuItemSpeedometer")));

        add(new JSeparator());

        add(new jmri.jmrit.sendpacket.SendPacketAction( rb.getString("MenuItemSendDCCPacket") ));

        add(new JSeparator());
        // US&S CTC subsystem tools
        add(new jmri.jmrit.ussctc.ToolsMenu());		
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ToolsMenu.class.getName());
}


