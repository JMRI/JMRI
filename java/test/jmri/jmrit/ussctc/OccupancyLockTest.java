package jmri.jmrit.ussctc;

import org.junit.*;

import jmri.*;

import java.util.*;

/**
 * Tests for OccupancyLock classes in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
  */
public class OccupancyLockTest {

    @Test
    public void testEmpty() {
        ArrayList<NamedBeanHandle<Sensor>> list = new ArrayList<>();
        
        OccupancyLock lock = new OccupancyLock(list);
        
        Assert.assertTrue(lock.isLockClear());
    }

    @Test
    public void testOneInListPass() throws JmriException {
        ArrayList<NamedBeanHandle<Sensor>> list = new ArrayList<>();
        
        Sensor s = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        NamedBeanHandle<Sensor> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s);
        
        list.add(h);
        s.setState(Sensor.INACTIVE);

        OccupancyLock lock = new OccupancyLock(list);
        
        Assert.assertTrue(lock.isLockClear());
    }

    @Test
    public void testOneFailActive() throws JmriException {
        ArrayList<NamedBeanHandle<Sensor>> list = new ArrayList<>();
        
        Sensor s = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        NamedBeanHandle<Sensor> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s);
        
        list.add(h);
        s.setState(Sensor.ACTIVE);

        OccupancyLock lock = new OccupancyLock(list);
        
        Assert.assertTrue( ! lock.isLockClear());
    }

    @Test
    public void testOneFailStringArrayCtor() throws JmriException {
        ArrayList<NamedBeanHandle<Sensor>> list = new ArrayList<>();
        
        Sensor s = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        NamedBeanHandle<Sensor> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s);
        
        OccupancyLock lock = new OccupancyLock(new String[]{"IS1"});
        
        Assert.assertTrue( ! lock.isLockClear());
    }

    @Test
    public void testOneFailSingleStringCtor() throws JmriException {
        ArrayList<NamedBeanHandle<Sensor>> list = new ArrayList<>();
        
        Sensor s = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        NamedBeanHandle<Sensor> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s);
        
        OccupancyLock lock = new OccupancyLock("IS1");
        
        Assert.assertTrue( ! lock.isLockClear());
    }

    @Test
    public void testSecondFailActive() throws JmriException {
        ArrayList<NamedBeanHandle<Sensor>> list = new ArrayList<>();
        
        Sensor s = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        NamedBeanHandle<Sensor> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s);
        
        list.add(h);
        s.setState(Sensor.INACTIVE);

        s = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS2");
        h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS2", s);

        list.add(h);
        s.setState(Sensor.ACTIVE);

        OccupancyLock lock = new OccupancyLock(list);
        
        Assert.assertTrue( ! lock.isLockClear());
    }

    @Test
    public void testOneFailInconsistent() throws JmriException {
        ArrayList<NamedBeanHandle<Sensor>> list = new ArrayList<>();
        
        Sensor s = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        NamedBeanHandle<Sensor> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s);
        
        list.add(h);
        s.setState(Sensor.INCONSISTENT);

        OccupancyLock lock = new OccupancyLock(list);
        
        Assert.assertTrue( ! lock.isLockClear());
    }

    @Test
    public void testOneFailUnknown() throws JmriException {
        ArrayList<NamedBeanHandle<Sensor>> list = new ArrayList<>();
        
        Sensor s = InstanceManager.getDefault(jmri.SensorManager.class).provideSensor("IS1");
        NamedBeanHandle<Sensor> h = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle("IS1", s);
        
        list.add(h);
        s.setState(Sensor.UNKNOWN);

        OccupancyLock lock = new OccupancyLock(list);
        
        Assert.assertTrue( ! lock.isLockClear());
    }
       
    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
