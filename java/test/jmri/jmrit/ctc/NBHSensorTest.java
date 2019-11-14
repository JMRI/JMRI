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

    private PropertyChangeListener _testListener = null;

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
        Sensor sbean = sensor.getBean();
        Assert.assertNull(sbean);
        boolean match = sensor.matchSensor(sbean);
        Assert.assertFalse(match);

        sensor.setKnownState(Sensor.ACTIVE);
        int known = sensor.getKnownState();
        Assert.assertEquals(Sensor.INACTIVE, known);

        sensor.addPropertyChangeListener(_testListener = (PropertyChangeEvent e) -> {});
        sensor.removePropertyChangeListener(_testListener);
    }

    public void realBean(NBHSensor sensor) {
        Sensor sbean = sensor.getBean();
        Assert.assertNotNull(sbean);
        boolean match = sensor.matchSensor(sbean);
        Assert.assertTrue(match);

        sensor.setKnownState(Sensor.ACTIVE);
        int known = sensor.getKnownState();
        Assert.assertEquals(Sensor.ACTIVE, known);

        sensor.addPropertyChangeListener(_testListener = (PropertyChangeEvent e) -> {});
        sensor.removePropertyChangeListener(_testListener);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        // stop any BlockBossLogic threads created
        JUnitUtil.clearBlockBossLogic();

        jmri.util.JUnitUtil.tearDown();
    }
}