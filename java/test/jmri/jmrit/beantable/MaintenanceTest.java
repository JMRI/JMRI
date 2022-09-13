package jmri.jmrit.beantable;

import jmri.*;
import jmri.util.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(10)
public class MaintenanceTest {

    @Test
    public void testCtor() {
        Maintenance t = new Maintenance();
        Assert.assertNotNull("exists", t);
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
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testDeviceReportPressed() throws InterruptedException {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Maintenance.rbm.getString("CrossReferenceTitle"));
            JButtonOperator jbo = new JButtonOperator(jdo,"OK");
            ThreadingUtil.runOnGUI(() -> jbo.push() );
            jdo.waitClosed();
        });
        t.setName("Cross Reference Dialog Close Thread");
        t.start();
        JmriJFrame parent = new JmriJFrame("DeviceReportParent");
        ThreadingUtil.runOnGUI(() -> {
            Maintenance.deviceReportPressed("IS1", parent);
        });
        JUnitUtil.waitFor(() -> !t.isAlive(), "Thread did not complete "+t.getName());
        JUnitUtil.dispose(parent);
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testFindOrphansPressed() throws InterruptedException {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Maintenance.rbm.getString("OrphanTitle"));
            JButtonOperator jbo = new JButtonOperator(jdo,"OK");
            ThreadingUtil.runOnGUI(() -> jbo.push() );
            jdo.waitClosed();
        });
        t.setName("Find Orphan Dialog Close Thread");
        t.start();
        JmriJFrame parent = new JmriJFrame("FindOrphansParent");
        ThreadingUtil.runOnGUI(() -> {
            Maintenance.findOrphansPressed(parent);
        });
        JUnitUtil.waitFor(() -> !t.isAlive(), "Thread did not complete "+t.getName());
        JUnitUtil.dispose(parent);
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testFindEmptyPressed() throws InterruptedException {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(Maintenance.rbm.getString("EmptyConditionalTitle"));
            JButtonOperator jbo = new JButtonOperator(jdo,"OK");
            ThreadingUtil.runOnGUI(() -> jbo.push() );
            jdo.waitClosed();
        });
        t.setName("Find Empty Dialog Close Thread");
        t.start();
        JmriJFrame parent = new JmriJFrame("FindEmptyParent");
        ThreadingUtil.runOnGUI(() -> {
            Maintenance.findEmptyPressed(parent);
        });

        JUnitUtil.waitFor(() -> !t.isAlive(), "Thread did not complete "+t.getName());
        JUnitUtil.dispose(parent);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MaintenanceTest.class);
}
