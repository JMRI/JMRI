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
 * @version     $Revision: 1.12 $
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

        add(new jmri.jmrit.simpleprog.SimpleProgAction(rb.getString("MenuItemSingleCVProgrammer")));
        add(new jmri.jmrit.symbolicprog.tabbedframe.PaneProgAction(rb.getString("MenuItemDecoderProServiceProgrammer")));
        add(new jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction(rb.getString("MenuItemDecoderProOpsModeProgrammer")));
        add(new jmri.jmrit.dualdecoder.DualDecoderToolAction());


        add(new JSeparator());
        add(new jmri.jmrit.powerpanel.PowerPanelAction(rb.getString("MenuItemPowerControl")));
        add(new jmri.jmrit.speedometer.SpeedometerAction(rb.getString("MenuItemSpeedometer")));
        add(new jmri.jmrit.simpleturnoutctrl.SimpleTurnoutCtrlAction(rb.getString("MenuItemTurnoutControl")));
        add(new jmri.jmrit.beantable.TurnoutTableAction(rb.getString("MenuItemTurnoutTable")));
        add(new jmri.jmrit.beantable.SensorTableAction(rb.getString("MenuItemSensorTable")));
        add(new jmri.jmrit.beantable.LightTableAction(rb.getString("MenuItemLightTable")));
        add(new jmri.jmrit.beantable.SignalHeadTableAction(rb.getString("MenuItemSignalTable")));
        add(new jmri.jmrit.beantable.ReporterTableAction(rb.getString("MenuItemReporterTable")));
        add(new jmri.jmrit.beantable.MemoryTableAction(rb.getString("MenuItemMemoryTable")));
        add(new jmri.jmrit.beantable.RouteTableAction(rb.getString("MenuItemRouteTable")));
        add(new jmri.jmrit.blockboss.BlockBossAction(rb.getString("MenuItemSimpleSignal")));
        add(new JSeparator());

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

        add(new jmri.jmrit.consisttool.ConsistToolAction(rb.getString("MenuItemConsistTool")));
        add(new jmri.jmrit.sendpacket.SendPacketAction( rb.getString("MenuItemSendDCCPacket") ));
        add(new JSeparator());

        JMenu clockMenu = new JMenu(rb.getString("MenuClocks"));
        clockMenu.add(new jmri.jmrit.simpleclock.SimpleClockAction(rb.getString("MenuItemSetupClock")));
        clockMenu.add(new jmri.jmrit.nixieclock.NixieClockAction(rb.getString("MenuItemNixieClock")));
		add(clockMenu);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ToolsMenu.class.getName());
}


