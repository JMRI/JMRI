package jmri.jmrit.beantable.beanedit;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SensorPullUpEditActionTest {

    @Test
    public void testCTor() {
        SensorPullUpEditAction t = new SensorPullUpEditAction();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSet() {
        SensorPullUpEditAction t = new SensorPullUpEditAction();

        InstanceManager.sensorManagerInstance().provideSensor("IS1");

        t.setBean(InstanceManager.sensorManagerInstance().getBySystemName("IS1"));
        
        t.sensorPullUp(null);
        
        t.savePullUpItems(null);
    }

    @Test
    public void testResetAndEnable() {
        SensorPullUpEditAction t = new SensorPullUpEditAction();

        InstanceManager.sensorManagerInstance().provideSensor("IS1");

        t.setBean(InstanceManager.sensorManagerInstance().getBySystemName("IS1"));
        
        t.sensorPullUp(null);
        
        t.enabled(false);
        t.resetPullUpItems(null);
        t.enabled(true);
    }


    @Test
    public void testServiceCalls() {
        SensorPullUpEditAction t = new SensorPullUpEditAction();

        Assert.assertEquals("package.jmri.jmrit.beantable.SensorAddEdit", t.helpTarget());
        
        t.initPanels();
        
        Sensor is1 = InstanceManager.sensorManagerInstance().newSensor("IS1", "user1");
        Assert.assertEquals(is1, t.getByUserName("user1"));
        
        // to ensure t.bean.getBeanType() equals removed method t.getBeanType()
        t.setBean(is1);
        Assert.assertEquals("Sensor", t.bean.getBeanType());
        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SensorPullUpEditActionTest.class);

}
