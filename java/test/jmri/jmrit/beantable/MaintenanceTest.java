package jmri.jmrit.beantable;

import jmri.*;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;

import java.awt.GraphicsEnvironment;

import org.junit.*;
import org.junit.rules.Timeout;

import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MaintenanceTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);

    @Rule
    public RetryRule retryRule = new RetryRule(1); // allow 1 retry

    @Test
    public void testCTor() {
        Maintenance t = new Maintenance();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetTypeAndNames() {
        String[] result;
        // nothing present
        result = Maintenance.getTypeAndNames("foo");
        checkReturnString(result, "", "foo", "foo", "0");
        
        // hit on sensor via system name
        InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        result = Maintenance.getTypeAndNames("IS1");
        checkReturnString(result, "Sensor", null, "IS1", "1");  //num listeners is empirical

        // hit on sensor via system name with user name
        InstanceManager.getDefault(SensorManager.class).provideSensor("IS2").setUserName("foo");
        result = Maintenance.getTypeAndNames("IS2");
        checkReturnString(result, "Sensor", "foo", "IS2", "1");  //num listeners is empirical

        // hit on sensor via user name
        InstanceManager.getDefault(SensorManager.class).provideSensor("IS3").setUserName("bar");
        result = Maintenance.getTypeAndNames("bar");
        checkReturnString(result, "Sensor", "bar", "IS3", "1");  //num listeners is empirical

        // hit on turnout via system name
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        result = Maintenance.getTypeAndNames("IT1");
        checkReturnString(result, "Turnout", null, "IT1", "1");  //num listeners is empirical

        // hit on sensor via system name with user name
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT3").setUserName("bar");
        result = Maintenance.getTypeAndNames("IT3");
        checkReturnString(result, "Turnout", "bar", "IT3", "1");  //num listeners is empirical

        // hit sensors before turnouts with same user name
        result = Maintenance.getTypeAndNames("bar");
        checkReturnString(result, "Sensor", "bar", "IS3", "1");  //num listeners is empirical   
    }

    @Test
    public void testGetTypeAndNamesObsoleteCase() {
        // This is checking the obsolete cases where UPPER CASE names are forced
        
        String[] result;
        
        // hit on sensor via to-capital system name
        InstanceManager.getDefault(SensorManager.class).provideSensor("is1");
        result = Maintenance.getTypeAndNames("IS1");
        checkReturnString(result, "", "IS1", "IS1", "0"); // Sensor "IS1" not found
        result = Maintenance.getTypeAndNames("ISis1"); // because "is" is invalid prefix, system name is "ISis1"
        checkReturnString(result, "Sensor", null, "ISis1", "1"); // num listeners is empirical
    }

    void checkReturnString(String[] result, String compare, String username, String systemname, String listeners) {
        Assert.assertNotNull(result);
        Assert.assertEquals("Type", compare, result[0]);
        Assert.assertEquals("UserName", username, result[1]);
        Assert.assertEquals("SystemName", systemname, result[2]);
        Assert.assertEquals("Listeners", listeners, result[3]);
    }
   
    @Test
    public void testDeviceReportPressed() throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Maintenance.rbm.getString("CrossReferenceTitle"));
            jdo.close();
	    });
        t.setName("Cross Reference Dialog Close Thread");
        t.start();
        JmriJFrame parent = new jmri.util.JmriJFrame("DeviceReportParent");
        ThreadingUtil.runOnGUI(() -> {
            Maintenance.deviceReportPressed("IS1",parent);
        });
        t.join(); // only proceed when all done
        JUnitUtil.dispose(parent);
    }

    @Test
    public void testFindOrphansPressed() throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Maintenance.rbm.getString("OrphanTitle"));
            jdo.close();
	    });
        t.setName("Find Orphan Dialog Close Thread");
        t.start();
        JmriJFrame parent = new jmri.util.JmriJFrame("FindOrphansParent");
        ThreadingUtil.runOnGUI(() -> {
            Maintenance.findOrphansPressed(parent);
        });
        t.join(); // only proceed when all done
        JUnitUtil.dispose(parent);
    }

    //@Test
    public void testFindEmptyPressed() throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Maintenance.rbm.getString("EmptyConditionalTitle"));
            jdo.close();
	    });
        t.setName("Find Empty Dialog Close Thread");
        t.start();
        JmriJFrame parent = new jmri.util.JmriJFrame("FindEmptyParent");
        ThreadingUtil.runOnGUI(() -> {
            Maintenance.findEmptyPressed(parent);
        });
        t.join(); // only proceed when all done
        JUnitUtil.dispose(parent);
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MaintenanceTest.class);

}
