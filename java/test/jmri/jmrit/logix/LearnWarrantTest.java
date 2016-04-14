package jmri.jmrit.logix;

import java.io.File;
import java.util.List;
import java.util.Locale;
import javax.swing.JDialog;
import jmri.ConfigureManager;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Warrant creation
 *
 * @author  Pete Cressman 2015
 * 
 * todo - test error conditions
 */
public class LearnWarrantTest extends jmri.util.SwingTestCase {

    OBlockManager _OBlockMgr;
    PortalManager _portalMgr;
    SensorManager _sensorMgr;
    TurnoutManager _turnoutMgr;
    
    @SuppressWarnings("unchecked") // For types from DialogFinder().findAll(..)
    public void testLearnWarrant() throws Exception {
        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/LearnWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
//        ControlPanelEditor panel = (ControlPanelEditor)null;
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);

        Warrant w = new Warrant("IW00", "Learning");
        WarrantFrame frame = new WarrantFrame(w, true);
        
        frame._origin.blockBox.setText("OB1");
        frame._destination.blockBox.setText("OB5");
        pressButton(frame, Bundle.getMessage("Calculate"));
        List<BlockOrder> orders = frame.getOrders();
        Assert.assertEquals("5 BlockOrders", 5, orders.size());
        
        frame.setAddress("99");
        pressButton(frame, Bundle.getMessage("Start"));
        // dismiss warning "starting block not occupied
        confirmJOptionPane(frame, Bundle.getMessage("WarningTitle"), "OK");
        
        // occupy starting block
        Sensor sensor = _sensorMgr.getBySystemName("IS1");
        sensor.setState(Sensor.ACTIVE);
        pressButton(frame, Bundle.getMessage("Start"));

        Assert.assertNotNull("Throttle not found", frame._learnThrottle.getThrottle());
        sensor = runtimes(sensor, frame._learnThrottle.getThrottle());
        pressButton(frame, Bundle.getMessage("Stop"));
        
        frame.setAddress("111");
        sensor.setState(Sensor.INACTIVE);
        sensor = _sensorMgr.getBySystemName("IS1");
        sensor.setState(Sensor.ACTIVE);
        pressButton(frame, Bundle.getMessage("ARun"));
        sensor = runtimes(sensor, null);
        while (w.getThrottle() != null) {
            // Sometimes the engineer is blocked
            flushAWT();          
        }        
        String msg = w.getRunModeMessage();
        Assert.assertEquals("run finished", Bundle.getMessage("NotRunning", w.getDisplayName()), msg);
//        sensor.setState(Sensor.INACTIVE);
        pressButton(frame, Bundle.getMessage("ButtonSave"));
        w = InstanceManager.getDefault(WarrantManager.class).getWarrant("Learning");
        List<ThrottleSetting> commands = w.getThrottleCommands();
        Assert.assertEquals("9 ThrottleCommands", 9, commands.size());
        WarrantTableFrame tableFrame = WarrantTableFrame.getInstance();
//        WarrantTableFrame tableFrame = (WarrantTableFrame)jmri.util.JmriJFrame.getFrame(Bundle.getMessage("WarrantTable"));
        Assert.assertNotNull("Warrant Table save", tableFrame);

        // passed test - cleanup.  Do it here so failure leaves traces.
        TestHelper.disposeWindow(tableFrame, this);
        ControlPanelEditor panel = (ControlPanelEditor)jmri.util.JmriJFrame.getFrame("LearnWarrantTest");
        TestHelper.disposeWindow(panel, this);

        // Dialog has popped up, so handle that. First, locate it.
        List<JDialog> dialogList = new DialogFinder(null).findAll(panel);
        TestHelper.disposeWindow(dialogList.get(0), this);

        flushAWT();
        // confirm one message logged
        jmri.util.JUnitAppender.assertWarnMessage("RosterSpeedProfile not found. Using default ThrottleFactor 0.75");
    }
    
    private javax.swing.AbstractButton pressButton(java.awt.Container frame, String text) {
        AbstractButtonFinder buttonFinder = new AbstractButtonFinder(text);
        javax.swing.AbstractButton button = (javax.swing.AbstractButton) buttonFinder.find(frame, 0);
        Assert.assertNotNull(text+" Button not found", button);
        getHelper().enterClickAndLeave(new MouseEventData(this, button));
        flushAWT();
        return button;
    }

    private void confirmJOptionPane(java.awt.Container frame, String title, String text) {
        DialogFinder finder = new DialogFinder(title);
        java.awt.Container pane = (java.awt.Container)finder.find();
        Assert.assertNotNull(title+" JOptionPane not found", pane);
        pressButton(pane, text);
    }
    
    /**
     * @param sensor - active start sensor
     * @return - active end sensor
     * @throws Exception
     */
    private Sensor runtimes(Sensor sensor, DccThrottle throttle) throws Exception {
        flushAWT();
        if (throttle!=null) {
            throttle.setSpeedSetting(0.5f);
        }
        for (int i=2; i<=5; i++) {
            flushAWT();
            Sensor sensorNext = _sensorMgr.getBySystemName("IS"+i);
            sensorNext.setState(Sensor.ACTIVE);
            flushAWT();          
            sensor.setState(Sensor.INACTIVE);
            sensor = sensorNext;
        }
        if (throttle!=null) {
            // leaving script with non-zero speed adds 2 more speed commands (-0.5f & 0.0f)
            throttle.setSpeedSetting(0.2f);
        }
        return sensor;
    }

    // from here down is testing infrastructure
    public LearnWarrantTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LearnWarrantTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(LearnWarrantTest.class);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp(); 
        super.setUp();
         // set the locale to US English
        Locale.setDefault(Locale.ENGLISH);
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
        JUnitUtil.initWarrantManager();
    }

    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
    }

}
