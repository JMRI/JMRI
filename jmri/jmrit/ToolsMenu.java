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
 * @version     $Revision: 1.3 $
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
        add(new jmri.jmrit.beantable.SignalHeadTableAction(rb.getString("MenuItemSignalTable")));
        add(new jmri.jmrit.blockboss.BlockBossAction(rb.getString("MenuItemSimpleSignal")));
        add(new JSeparator());

        JMenu throttleMenu = new JMenu(rb.getString("MenuThrottles"));
        throttleMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(rb.getString("MenuItemNewThrottle" )));
        throttleMenu.add(new jmri.jmrit.throttle.StoreXmlThrottleAction(rb.getString("MenuItemSaveThrottleLayout" )));
        throttleMenu.add(new jmri.jmrit.throttle.LoadXmlThrottleAction(rb.getString("MenuItemLoadThrottleLayout")));
        throttleMenu.add(new jmri.jmrit.throttle.EditThrottlePreferencesAction(rb.getString("MenuItemEditThrottlePreferences")));
        add(throttleMenu);

        add(new jmri.jmrit.sendpacket.SendPacketAction( rb.getString("MenuItemSendDCCPacket") ));

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ToolsMenu.class.getName());
}


