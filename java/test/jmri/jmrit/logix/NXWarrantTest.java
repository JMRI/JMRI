package jmri.jmrit.logix;

import java.io.File;
import java.util.List;
import java.util.Locale;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import junit.extensions.jfcunit.TestHelper;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.ComponentFinder;
import junit.extensions.jfcunit.finder.DialogFinder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Warrant creation
 *
 * @author  Pete Cressman 2015
 * @version $Revision: 00000 $
 * 
 * todo - test error conditions
 */
public class NXWarrantTest extends jmri.util.SwingTestCase {    

    OBlockManager _OBlockMgr;
    PortalManager _portalMgr;
    SensorManager _sensorMgr;
    TurnoutManager _turnoutMgr;
    
    @SuppressWarnings("unchecked") // For types from DialogFinder().findAll(..)
    public void testNXWarrant() throws Exception {

        // load and display
        File f = new File("java/test/jmri/jmrit/logix/valid/NXWarrantTest.xml");
        InstanceManager.getDefault(ConfigureManager.class).load(f);
        _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        _sensorMgr = InstanceManager.getDefault(SensorManager.class);

        NXFrame nxFrame = NXFrame.getInstance();
        Assert.assertNotNull("NXFrame", nxFrame);
        nxFrame.init();
        nxFrame.setVisible(true);
        nxFrame.setRampIncrement(0.075f);
        nxFrame.setTimeInterval(1000);
        pressButton(nxFrame, Bundle.getMessage("ButtonCancel"));
        
        nxFrame = NXFrame.getInstance();
        nxFrame.init();
        nxFrame.setVisible(true);
        nxFrame._maxSpeedBox.setText("0.30");
        
        nxFrame._origin.blockBox.setText("OB0");
        nxFrame._destination.blockBox.setText("OB10");
        
        pressButton(nxFrame, Bundle.getMessage("ButtonRunNX"));
        confirmJOptionPane(null, Bundle.getMessage("WarningTitle"), Bundle.getMessage("NoLoco"), "OK");
        nxFrame.setAddress("666");
        nxFrame.setTrainName("Nick");
        pressButton(nxFrame, Bundle.getMessage("ButtonRunNX"));

        DialogFinder finder = new DialogFinder(Bundle.getMessage("DialogTitle"));
        java.awt.Container pickDia = (java.awt.Container)finder.find();
        Assert.assertNotNull("PickRoute Dialog not found", pickDia);               
        pressButton(pickDia, Bundle.getMessage("ButtonReview"));
        confirmJOptionPane(null, Bundle.getMessage("WarningTitle"), Bundle.getMessage("SelectRoute"), "OK");

        List<JRadioButton> list = getRadioButtons(pickDia);
        Assert.assertNotNull("Route RadioButtons not found", list);
        Assert.assertEquals("Number of RadioButton Routes", 4, list.size());
        getHelper().enterClickAndLeave(new MouseEventData(this, list.get(3)));
        pressButton(pickDia, Bundle.getMessage("ButtonReview"));
        getHelper().enterClickAndLeave(new MouseEventData(this, list.get(1)));
        pressButton(pickDia, Bundle.getMessage("ButtonReview"));

        nxFrame.setRampIncrement(0.05f);
        pressButton(pickDia, Bundle.getMessage("ButtonSelect"));
        WarrantTableFrame tableFrame = WarrantTableFrame.getInstance();
        Warrant warrant = tableFrame.getModel().getWarrantAt(0);
        OBlock block = _OBlockMgr.getBySystemName("OB0");
        Assert.assertEquals("Waiting message", warrant.getRunningMessage(), 
                Bundle.getMessage("waitForDelayStart", warrant.getTrainName(), block.getDisplayName()));
        
        Sensor sensor = _sensorMgr.getBySystemName("IS0");
        Assert.assertNotNull("Senor IS0 not found", sensor);               
        sensor.setState(Sensor.ACTIVE);
        Assert.assertEquals("Halted/Resume message", warrant.getRunningMessage(), 
                Bundle.getMessage("Halted", block.getDisplayName(), "0"));
        warrant.controlRunTrain(Warrant.RESUME);
        String[] route = {"IS1", "IS2", "IS3", "IS7", "IS5", "IS10"};
        sensor = _sensorMgr.getBySystemName("IS10");
        Assert.assertEquals("Train in last block", sensor, runtimes(sensor, route));
        
        flushAWT();
        flushAWT();   // let calm down before running abort
                
        warrant.controlRunTrain(Warrant.ABORT);
        flushAWT();
        
        // passed test - cleanup.  Do it here so failure leaves traces.
        TestHelper.disposeWindow(tableFrame, this);
        ControlPanelEditor panel = (ControlPanelEditor)jmri.util.JmriJFrame.getFrame("NXWarrantTest");
        TestHelper.disposeWindow(panel, this);

        // Dialog has popped up, so handle that. First, locate it.
        List<JDialog> dialogList = new DialogFinder(null).findAll(panel);
        TestHelper.disposeWindow(dialogList.get(0), this);
        
        // confirm one message logged
        jmri.util.JUnitAppender.assertWarnMessage("RosterSpeedProfile not found. Using default ThrottleFactor 0.75");
    }
    
    private javax.swing.AbstractButton pressButton(java.awt.Container frame, String text) {
        AbstractButtonFinder buttonFinder = new AbstractButtonFinder(text);
        javax.swing.AbstractButton button = (javax.swing.AbstractButton) buttonFinder.find(frame, 0);
        Assert.assertNotNull(text+" Button not found", button);
        getHelper().enterClickAndLeave(new MouseEventData(this, button));
        return button;
    }

    @SuppressWarnings("unchecked") // For types from DialogFinder().findAll(..)
    private void confirmJOptionPane(java.awt.Container frame, String title, String message, String buttonLabel) {
        ComponentFinder finder = new ComponentFinder(JOptionPane.class);
        JOptionPane pane;
        if (frame==null) {
            pane = (JOptionPane)finder.find();
            Assert.assertNotNull(title+" JOptionPane not found", pane);            
        } else {
            List<JOptionPane> list = finder.findAll(frame);
            Assert.assertNotNull(title+" JOptionPane not found", list);
            Assert.assertTrue(title+" JOptionPane not found", list.size()==1);
//          java.util.Iterator iter = list.iterator();
            pane = list.get(0);
        }
        if (message!=null) {
            Assert.assertEquals(title+" JOptionPane message", message, pane.getMessage());            
        }
        pressButton(pane, buttonLabel);
    }
        
    @SuppressWarnings("unchecked") // For types from DialogFinder().findAll(..)
    private static List<JRadioButton> getRadioButtons(java.awt.Container frame) {
        ComponentFinder finder = new ComponentFinder(JRadioButton.class);
        List<JRadioButton> list = finder.findAll(frame);
        Assert.assertNotNull("JRadioButton list not found", list);
        return list;
    }

    /**
     * @param sensor - active start sensor
     * @return - active end sensor
     * @throws Exception
     */
    private Sensor runtimes(Sensor sensor, String[] sensors) throws Exception {
        flushAWT();
        for (int i=0; i<sensors.length; i++) {
            flushAWT();
            Sensor sensorNext = _sensorMgr.getSensor(sensors[i]);
            sensorNext.setState(Sensor.ACTIVE);
            flushAWT();           
            sensor.setState(Sensor.INACTIVE);
            sensor = sensorNext;
        }
        return sensor;
    }

    // from here down is testing infrastructure
    public NXWarrantTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        String[] testCaseName = {"-noloading", NXWarrantTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(NXWarrantTest.class);
    }

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
