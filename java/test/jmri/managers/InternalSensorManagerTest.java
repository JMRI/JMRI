package jmri.managers;

import java.util.*;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests for the jmri.managers.InternalSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2016
 */
public class InternalSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase implements Manager.ManagerDataListener {

    /** {@inheritDoc} */
    @Override
    public String getSystemName(int i) {
        return "IS" + i;
    }

    @Test
    public void testAsAbstractFactory() {

        // ask for a Sensor, and check type
        Sensor tl = l.newSensor("IS21", "my name");

        log.debug("received sensor value {}", tl);

        Assert.assertTrue(null != tl);

        // make sure loaded into tables
        Assert.assertTrue(null != l.getBySystemName("IS21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    @Test
    public void testSetGetDefaultState() {

        // confirm default
        Assert.assertEquals("starting mode", Sensor.UNKNOWN, InternalSensorManager.getDefaultStateForNewSensors() );
        
        // set and retrieve
        InternalSensorManager.setDefaultStateForNewSensors(Sensor.INACTIVE);
        Assert.assertEquals("updated mode", Sensor.INACTIVE, InternalSensorManager.getDefaultStateForNewSensors() );
               
    }

    // the following methods test code in Manager and AbstractManager,
    // but they need a concrete implementation to do it, hence are here.
    
    @Test
    public void testAddTracking() {
        Sensor s1 = l.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        
        l.addDataListener(this);

        // add an item
        Sensor s2 = l.provideSensor("IS2");

        // listener should have been immediately invoked
        Assert.assertEquals("events", 1, events);
        Assert.assertEquals("last call 1", "Added", lastCall);
        Assert.assertEquals("type 1", Manager.ManagerDataEvent.INTERVAL_ADDED, lastType);
        Assert.assertEquals("start == end 1", lastEvent0, lastEvent1);
        Assert.assertEquals("right index 1", s2, l.getNamedBeanList().get(lastEvent0));

        // add an item
        Sensor s3 = l.provideSensor("IS3");
        s3.setUserName("Sensor 3");

        // listener should have been immediately invoked
        Assert.assertEquals("events", 2, events);
        Assert.assertEquals("last call 2", "Added", lastCall);
        Assert.assertEquals("type 2", Manager.ManagerDataEvent.INTERVAL_ADDED, lastType);
        Assert.assertEquals("start == end 2", lastEvent0, lastEvent1);
        Assert.assertEquals("right index 2", s3, l.getNamedBeanList().get(lastEvent0));
    }

    @Test
    public void testRenoveTracking() {
        
        Sensor s1 = l.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = l.provideSensor("IS2");
        Sensor s3 = l.provideSensor("IS3");
        
        l.addDataListener(this);
        List<Sensor> tlist = l.getNamedBeanList();

        l.deregister(s2);
    
        // listener should have been immediately invoked
        Assert.assertEquals("events", 1, events);
        Assert.assertEquals("last call", "Removed", lastCall);
        Assert.assertEquals("type", Manager.ManagerDataEvent.INTERVAL_REMOVED, lastType);
        Assert.assertEquals("start == end 2", lastEvent0, lastEvent1);
        Assert.assertEquals("right index", s2, tlist.get(lastEvent0));
        
    }

    // a listen & audit methods
    int events;
    int lastEvent0;
    int lastEvent1;
    int lastType;
    String lastCall;
    
    @Override
    public void intervalAdded(Manager.ManagerDataEvent e) {
        events++;
        lastEvent0 = e.getIndex0();
        lastEvent1 = e.getIndex1();
        lastType = e.getType();
        lastCall = "Added";
    }
    @Override
    public void intervalRemoved(Manager.ManagerDataEvent e) {
        events++;
        lastEvent0 = e.getIndex0();
        lastEvent1 = e.getIndex1();
        lastType = e.getType();
        lastCall = "Removed";
    }
    @Override
    public void contentsChanged(Manager.ManagerDataEvent e) {
        events++;
        lastEvent0 = e.getIndex0();
        lastEvent1 = e.getIndex1();
        lastType = e.getType();
        lastCall = "Changed";
    }

    // from here down is testing infrastructure
    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        // create and register the manager object
        jmri.util.JUnitUtil.resetInstanceManager();
        
        l = new InternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    private final static Logger log = LoggerFactory.getLogger(InternalSensorManagerTest.class);

}
