package jmri.jmrix.can.cbus.swing.power;

import jmri.InstanceManager;
import jmri.PowerManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusPowerManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test CBUS programming track power panel
 * 
 * @author Andrew Crosland Copyright (C) 2022
 */
public class CbusProgPowerPaneTest extends jmri.util.swing.JmriPanelTest {
    
    // setup a default PowerManager interface
    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
//        JUnitUtil.initDebugPowerManager();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        InstanceManager.setDefault(PowerManager.class, new CbusPowerManager(memo));
        panel = new CbusProgPowerPane();
        helpTarget="package.jmri.jmrix.can.cbus.swing.power.ProgPowerPanelFrame";
        title=jmri.jmrix.can.cbus.swing.power.Bundle.getMessage("MenuItemProgTrackPower");
    }
    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

//    // test on button routine
//    @org.junit.jupiter.api.Test
//    public void testPushOn() {
//        ((CbusProgPowerPane) panel).onButtonPushed();
//        Assert.assertEquals("Testing shown on/off", "On", ((CbusProgPowerPane) panel).onOffStatus.getText());
//    }
//
//    // test off button routine
//    @org.junit.jupiter.api.Test
//    public void testPushOff() {
//        ((CbusProgPowerPane) panel).offButtonPushed();
//        Assert.assertEquals("Testing shown on/off", "Off", ((CbusProgPowerPane) panel).onOffStatus.getText());
//    }
//
//    // click on button
//    @org.junit.jupiter.api.Test
//    public void testOnClicked() {
//        ((CbusProgPowerPane) panel).onButton.doClick();
//        Assert.assertEquals("Testing shown on/off", "On", ((CbusProgPowerPane) panel).onOffStatus.getText());
//    }
//
//    // click off button
//    @org.junit.jupiter.api.Test
//    public void testOffClicked() {
//        ((CbusProgPowerPane) panel).offButton.doClick();
//        Assert.assertEquals("Testing shown on/off", "Off", ((CbusProgPowerPane) panel).onOffStatus.getText());
//    }    
}
