package jmri.jmrit.beantable.beanedit;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SensorEditActionTest {

    @Test
    public void testCTor() {
        SensorEditAction t = new SensorEditAction();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testDetects() {
        jmri.Sensor s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        s.setDetects(jmri.Sensor.Detects.BODY);
        SensorEditAction a = new SensorEditAction();
        a.setBean(s);
        a.basicDetails();
        a.resetBasicItems(null);
        Assert.assertEquals("shows the sensor's setting", jmri.Sensor.Detects.BODY, a.detects.getSelectedItem());

        a.detects.setSelectedItem(jmri.Sensor.Detects.TAG);
        a.saveBasicItems(null);
        Assert.assertEquals("saved to the sensor", jmri.Sensor.Detects.TAG, s.getDetects());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(SensorEditActionTest.class);

}
