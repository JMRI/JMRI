package jmri.jmrix.internal.configurexml;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * InternalSensorManagerXmlTest.java
 *
 * Test for the InternalSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class InternalSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("InternalSensorManagerXml constructor",new InternalSensorManagerXml());
    }

    @Test
    public void testDetectsStoredAndLoaded() throws Exception {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        sm.provideSensor("IS1").setDetects(Sensor.Detects.BODY);
        sm.provideSensor("IS2").setDetects(Sensor.Detects.TAG);
        sm.provideSensor("IS3"); // left as Wheels
        Element stored = new InternalSensorManagerXml().store(sm);

        for (Element e : stored.getChildren("sensor")) {
            if ("IS3".equals(e.getChildText("systemName"))) {
                Assertions.assertNull(e.getChild("properties"), "nothing stored for the default");
            }
        }

        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        new InternalSensorManagerXml().load(stored, null);
        SensorManager loaded = InstanceManager.getDefault(SensorManager.class);
        Assertions.assertNotSame(sm, loaded, "a new manager");
        Assertions.assertEquals(Sensor.Detects.BODY, loaded.provideSensor("IS1").getDetects());
        Assertions.assertEquals(Sensor.Detects.TAG, loaded.provideSensor("IS2").getDetects());
        Assertions.assertEquals(Sensor.Detects.WHEELS, loaded.provideSensor("IS3").getDetects());
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

}

