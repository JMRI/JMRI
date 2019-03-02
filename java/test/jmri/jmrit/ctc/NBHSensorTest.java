package jmri.jmrit.ctc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.ctc.setup.CreateTestObjects;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.*;

/*
* Tests for the NBHSensor Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class NBHSensorTest {

    private PropertyChangeListener _testListener1 = null;
    private PropertyChangeListener _testListener2 = null;

    @Test
    public void testGetsAndSets() {
        CreateTestObjects.createSensor("IS91", "IS 91");
        CreateTestObjects.createSensor("IS92", "IS 92");
        CreateTestObjects.createSensor("IS93", "IS 93");

        // Use NB constructor
        Sensor sensor91 = InstanceManager.getDefault(SensorManager.class).getSensor("IS91");
        Assert.assertNotNull(sensor91);
        NamedBeanHandle<Sensor> nbSensor91 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(sensor91.getUserName(), sensor91);
        Assert.assertNotNull(nbSensor91);
        NBHSensor sensor = new NBHSensor(nbSensor91);
        Assert.assertNotNull(sensor);

        // Use regular constructor with optional true
        NBHSensor sensor92 = new NBHSensor("Module", "UserId", "Parameter", "IS92", true);
        Assert.assertNotNull(sensor92);

        // Use regular constructor with optional false
        NBHSensor sensor93 = new NBHSensor("Module", "UserId", "Parameter", "IS93", true);
        Assert.assertNotNull(sensor93);
        realBean(sensor93);

        // Use regular constructor with optional false and invalid
        NBHSensor sensor94 = new NBHSensor("Module", "UserId", "Parameter", "IS94", true);
        Assert.assertNotNull(sensor94);
        nullBean(sensor94);

        JUnitAppender.suppressErrorMessage("Module, UserIdParameter, Sensor does not exist: IS94");
    }

    public void nullBean(NBHSensor sensor) {
        Sensor sbean = (Sensor) sensor.getBean();
        Assert.assertNull(sbean);
        boolean match = sensor.matchSensor(sbean);
        Assert.assertFalse(match);

        sensor.setKnownState(Sensor.ACTIVE);
        int known = sensor.getKnownState();
        Assert.assertEquals(Sensor.INACTIVE, known);

        sensor.requestUpdateFromLayout();

        sensor.setInverted(true);
        boolean inverted = sensor.getInverted();
        Assert.assertFalse(inverted);
        boolean canInvert = sensor.canInvert();
        Assert.assertFalse(canInvert);

        int rawState = sensor.getRawState();
        Assert.assertEquals(Sensor.INACTIVE, rawState);

        sensor.setSensorDebounceGoingActiveTimer(100);
        long goActive = sensor.getSensorDebounceGoingActiveTimer();
        Assert.assertEquals(0, goActive);

        sensor.setSensorDebounceGoingInActiveTimer(100);
        long goInActive = sensor.getSensorDebounceGoingInActiveTimer();
        Assert.assertEquals(0, goInActive);

        sensor.setUseDefaultTimerSettings(true);
        boolean useDefault = sensor.getUseDefaultTimerSettings();
        Assert.assertFalse(useDefault);

        jmri.Reporter reporter = sensor.getReporter();
        Assert.assertNull(reporter);
        sensor.setReporter(reporter);

        jmri.Sensor.PullResistance pull = sensor.getPullResistance();
        Assert.assertNull(pull);
        sensor.setPullResistance(pull);

        String userName = sensor.getUserName();
        Assert.assertEquals("UNKNOWN", userName);
        sensor.setUserName(userName);

        String systemName = sensor.getSystemName();
        Assert.assertEquals("UNKNOWN", systemName);

        String displayName = sensor.getDisplayName();
        Assert.assertEquals("UNKNOWN", displayName);

        String fullName = sensor.getFullyFormattedDisplayName();
        Assert.assertEquals("UNKNOWN", fullName);

        String comment = sensor.getComment();
        Assert.assertEquals("UNKNOWN", comment);
        sensor.setComment(comment);

        sensor.addPropertyChangeListener(_testListener1 = (PropertyChangeEvent e) -> {}, "Name", "Ref");
        sensor.addPropertyChangeListener(_testListener2 = (PropertyChangeEvent e) -> {});
        sensor.removePropertyChangeListener(_testListener2);
        sensor.updateListenerRef(_testListener1, "newRef");

        String ref = sensor.getListenerRef(_testListener1);
        Assert.assertEquals("UNKNOWN", ref);

        java.util.ArrayList<String> refs = sensor.getListenerRefs();
        Assert.assertEquals(0, refs.size());

        int num = sensor.getNumPropertyChangeListeners();
        Assert.assertEquals(0, num);

        PropertyChangeListener[] numrefs = sensor.getPropertyChangeListenersByReference("newRef");
        Assert.assertEquals(0, numrefs.length);

        sensor.vetoableChange(null);

        int state = sensor.getState();
        Assert.assertEquals(4, state);
        String stateName = sensor.describeState(state);
        Assert.assertEquals("UNKNOWN", stateName);
        sensor.setState(state);

        sensor.setProperty("Test", "Value");
        String property = (String) sensor.getProperty("Test");
        Assert.assertNull(property);
        sensor.removeProperty("Test");
        java.util.Set keys = sensor.getPropertyKeys();
        Assert.assertEquals(0, keys.size());

        String type = sensor.getBeanType();
        Assert.assertEquals("UNKNOWN", type);

        sensor.compareSystemNameSuffix("", "", null);
        sensor.dispose();
    }

    public void realBean(NBHSensor sensor) {
        Sensor sbean = (Sensor) sensor.getBean();
        Assert.assertNotNull(sbean);
        boolean match = sensor.matchSensor(sbean);
        Assert.assertTrue(match);

        sensor.setKnownState(Sensor.ACTIVE);
        int known = sensor.getKnownState();
        Assert.assertEquals(Sensor.ACTIVE, known);

        sensor.requestUpdateFromLayout();

        sensor.setInverted(true);
        boolean inverted = sensor.getInverted();
        Assert.assertTrue(inverted);
        boolean canInvert = sensor.canInvert();
        Assert.assertTrue(canInvert);

        int rawState = sensor.getRawState();
        Assert.assertEquals(Sensor.INACTIVE, rawState);

        sensor.setSensorDebounceGoingActiveTimer(100);
        long goActive = sensor.getSensorDebounceGoingActiveTimer();
        Assert.assertEquals(100, goActive);

        sensor.setSensorDebounceGoingInActiveTimer(100);
        long goInActive = sensor.getSensorDebounceGoingInActiveTimer();
        Assert.assertEquals(100, goInActive);

        sensor.setUseDefaultTimerSettings(true);
        boolean useDefault = sensor.getUseDefaultTimerSettings();
        Assert.assertTrue(useDefault);

        jmri.Reporter reporter = sensor.getReporter();
        Assert.assertNull(reporter);
        sensor.setReporter(reporter);

        jmri.Sensor.PullResistance pull = sensor.getPullResistance();
        Assert.assertNotNull(pull);
        sensor.setPullResistance(pull);

        String userName = sensor.getUserName();
        Assert.assertEquals("IS 93", userName);
        sensor.setUserName(userName);

        String systemName = sensor.getSystemName();
        Assert.assertEquals("IS93", systemName);

        String displayName = sensor.getDisplayName();
        Assert.assertEquals("IS 93", displayName);

        String fullName = sensor.getFullyFormattedDisplayName();
        Assert.assertEquals("IS93(IS 93)", fullName);

        String comment = sensor.getComment();
        Assert.assertNull(comment);
        sensor.setComment(comment);

        sensor.addPropertyChangeListener(_testListener1 = (PropertyChangeEvent e) -> {}, "Name", "Ref");
        sensor.addPropertyChangeListener(_testListener2 = (PropertyChangeEvent e) -> {});
        sensor.removePropertyChangeListener(_testListener2);
        sensor.updateListenerRef(_testListener1, "newRef");

        String ref = sensor.getListenerRef(_testListener1);
        Assert.assertEquals("newRef", ref);

        java.util.ArrayList<String> refs = sensor.getListenerRefs();
        Assert.assertEquals(2, refs.size());

        int num = sensor.getNumPropertyChangeListeners();
        Assert.assertEquals(2, num);

        PropertyChangeListener[] numrefs = sensor.getPropertyChangeListenersByReference("newRef");
        Assert.assertEquals(0, numrefs.length);

        sensor.vetoableChange(null);

        int state = sensor.getState();
        Assert.assertEquals(4, state);
        String stateName = sensor.describeState(state);
        Assert.assertEquals("Inactive", stateName);
        sensor.setState(state);

        sensor.setProperty("Test", "Value");
        String property = (String) sensor.getProperty("Test");
        Assert.assertEquals("Value", property);
        sensor.removeProperty("Test");
        java.util.Set keys = sensor.getPropertyKeys();
        Assert.assertEquals(0, keys.size());

        String type = sensor.getBeanType();
        Assert.assertEquals("Sensor", type);

        sensor.compareSystemNameSuffix("", "", null);
        sensor.dispose();
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NBHSensorTest.class);
}