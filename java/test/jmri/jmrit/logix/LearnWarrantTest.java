// WarrantTest.java
package jmri.jmrit.logix;

import java.io.File;
import java.util.List;
import java.util.Locale;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;
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
 * @version $Revision: 00000 $
 * 
 * todo - test error conditions
 */
public class LearnWarrantTest extends jmri.util.SwingTestCase {

    OBlockManager _OBlockMgr;
    PortalManager _portalMgr;
    SensorManager _sensorMgr;
    TurnoutManager _turnoutMgr;
    
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
        
        frame._dccNumBox.setText("99");
        pressButton(frame, Bundle.getMessage("Start"));
        // dismiss warning "starting block not occupied
        confirmJOptionPane(frame, Bundle.getMessage("WarningTitle"), "OK");
        // occupy starting block
        Sensor sensor = _sensorMgr.getBySystemName("IS1");
        sensor.setState(Sensor.ACTIVE);
        connectThrottle();
        pressButton(frame, Bundle.getMessage("Start"));
        sensor = runtimes(sensor);
        pressButton(frame, Bundle.getMessage("Stop"));
        
        frame._dccNumBox.setText("111");
        sensor.setState(Sensor.INACTIVE);
        sensor = _sensorMgr.getBySystemName("IS1");
        sensor.setState(Sensor.ACTIVE);
        pressButton(frame, Bundle.getMessage("ARun"));
        sensor = runtimes(sensor);
        String msg = w.getRunModeMessage();
        Assert.assertEquals("run finished", Bundle.getMessage("NotRunning", w.getDisplayName()), msg);
//        sensor.setState(Sensor.INACTIVE);
        pressButton(frame, Bundle.getMessage("ButtonSave"));
        List<ThrottleSetting> commands = w.getThrottleCommands();
        Assert.assertEquals("5 ThrottleCommands", 5, commands.size());
        
    }
    
    private void pressButton(java.awt.Container frame, String text) {
        AbstractButtonFinder buttonFinder = new AbstractButtonFinder(text);
        javax.swing.JButton button = (javax.swing.JButton) buttonFinder.find(frame, 0);
        Assert.assertNotNull("Button not found", button);
//        Assert.assertNotNull("getHelper() not found", getHelper());
        getHelper().enterClickAndLeave(new MouseEventData(this, button));        
    }
    
    private void pressRadioButton(java.awt.Container frame, String text) {
        AbstractButtonFinder buttonFinder = new AbstractButtonFinder(text);
        javax.swing.JRadioButton button = (javax.swing.JRadioButton) buttonFinder.find(frame, 0);
        Assert.assertNotNull("Button not found", button);
        getHelper().enterClickAndLeave(new MouseEventData(this, button));        
    }
    
    private void confirmJOptionPane(java.awt.Container frame, String title, String text) {
        DialogFinder finder = new DialogFinder(title);
        java.awt.Container pane = (java.awt.Container)finder.find();
        Assert.assertNotNull("JOptionPane not found", pane);
        AbstractButtonFinder buttonFinder = new AbstractButtonFinder(text);
        javax.swing.JButton button = (javax.swing.JButton)buttonFinder.find(pane, 0);
        Assert.assertNotNull("Button not found", button);
        getHelper().enterClickAndLeave(new MouseEventData(this, button));                
    }
    
    private static void connectThrottle() {
        jmri.jmrix.nce.simulator.SimulatorAdapter nceSimulator = new jmri.jmrix.nce.simulator.SimulatorAdapter();
        Assert.assertNotNull("Nce SimulatorAdapter", nceSimulator);
        jmri.jmrix.nce.NceSystemConnectionMemo memo = nceSimulator.getSystemConnectionMemo();
        nceSimulator.openPort("(None Selected)", "JMRI test");
        nceSimulator.configure();
        Assert.assertNotNull("NceSystemConnectionMemo", memo);        
    }

    /**
     * @param sensor - active start sensor
     * @return - active end sensor
     * @throws Exception
     */
    private Sensor runtimes(Sensor sensor) throws Exception {
        for (int i=2; i<=5; i++) {
            Thread.sleep(200);            
            Sensor sensorNext = _sensorMgr.getBySystemName("IS"+i);
            sensorNext.setState(Sensor.ACTIVE);
            Thread.sleep(200);            
            sensor.setState(Sensor.INACTIVE);
            sensor = sensorNext;
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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(LearnWarrantTest.class);
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        // set the locale to US English
        Locale.setDefault(Locale.ENGLISH);
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
        JUnitUtil.initWarrantManager();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
