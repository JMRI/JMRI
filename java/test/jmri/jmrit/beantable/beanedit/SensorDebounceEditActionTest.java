package jmri.jmrit.beantable.beanedit;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SensorDebounceEditActionTest {

    @Test
    public void testCTor() {
        SensorDebounceEditAction t = new SensorDebounceEditAction();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSet() {
        SensorDebounceEditAction t = new SensorDebounceEditAction();

        InstanceManager.sensorManagerInstance().provideSensor("IS1");

        t.setBean(InstanceManager.sensorManagerInstance().getBySystemName("IS1"));
        
        t.sensorDebounce(null);
        
        t.saveDebounceItems(null);
    }

    @Test
    public void testResetAndEnable() {
        SensorDebounceEditAction t = new SensorDebounceEditAction();

        InstanceManager.sensorManagerInstance().provideSensor("IS1");

        t.setBean(InstanceManager.sensorManagerInstance().getBySystemName("IS1"));
        
        t.sensorDebounce(null);
        
        t.enabled(false);
        t.resetDebounceItems(null);
        t.enabled(true);
    }

    @Test
    public void testServiceCalls() {
        SensorDebounceEditAction t = new SensorDebounceEditAction();

        Assert.assertEquals("package.jmri.jmrit.beantable.SensorAddEdit", t.helpTarget());
        Assert.assertEquals("Sensor", t.getBeanType());
        
        t.initPanels();
        
        Sensor is1 = InstanceManager.sensorManagerInstance().newSensor("IS1", "user1");
        Assert.assertEquals(is1, t.getByUserName("user1"));
        
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SensorDebounceEditActionTest.class);

}
