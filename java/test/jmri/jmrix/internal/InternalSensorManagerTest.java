package jmri.jmrix.internal;

import java.beans.PropertyChangeListener;
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
public class InternalSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase implements Manager.ManagerDataListener<Sensor>, PropertyChangeListener {

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

    public void testSensorNameCase() {
        Assert.assertEquals(0, l.getObjectCount());
        // create
        Sensor t = l.provideSensor("IS:XYZ");
        t = l.provideSensor("IS:xyz");  // upper canse and lower case are the same object
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("IS:XYZ", t.getSystemName());  // we force upper
        Assert.assertTrue("system name correct ", t == l.getBySystemName("IS:XYZ"));
        Assert.assertEquals(1, l.getObjectCount());

        t = l.provideSensor("IS:XYZ");
        Assert.assertEquals(1, l.getObjectCount());
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
        // listen to explicitly selected property changes
        l.addPropertyChangeListener("length", this);
        l.addPropertyChangeListener("DisplayListName", this);

        // add an item
        Sensor s2 = l.provideSensor("IS2");

        // property listener should have been immediately invoked
        Assert.assertEquals("propertyListenerCount", 1, propertyListenerCount);
        Assert.assertEquals("last call", "length", propertyListenerLast);

        s2.setUserName("Sensor 2");

        Assert.assertEquals("propertyListenerCount", 2, propertyListenerCount);
        Assert.assertEquals("last call", "DisplayListName", propertyListenerLast);

        // data listener should have been immediately invoked
        Assert.assertEquals("events", 1, events);
        Assert.assertEquals("last call 1", "Added", lastCall);
        Assert.assertEquals("type 1", Manager.ManagerDataEvent.INTERVAL_ADDED, lastType);
        Assert.assertEquals("start == end 1", lastEvent0, lastEvent1);
        Assert.assertEquals("index 1", 1, lastEvent0);

        // add an item
        l.newSensor("IS3", "Sensor 3");

        // property listener should have been immediately invoked
        Assert.assertEquals("propertyListenerCount", 3, propertyListenerCount);
        Assert.assertEquals("last call", "length", propertyListenerLast);

        // listener should have been immediately invoked
        Assert.assertEquals("events", 2, events);
        Assert.assertEquals("last call 2", "Added", lastCall);
        Assert.assertEquals("type 2", Manager.ManagerDataEvent.INTERVAL_ADDED, lastType);
        Assert.assertEquals("start == end 2", lastEvent0, lastEvent1);
        Assert.assertEquals("index 2", 2, lastEvent0);
    }

    @Test
    public void testRenoveTracking() {
        
        Sensor s1 = l.provideSensor("IS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = l.provideSensor("IS2");
        l.provideSensor("IS3");
        
        l.addDataListener(this);

        l.deregister(s2);
    
        // listener should have been immediately invoked
        Assert.assertEquals("events", 1, events);
        Assert.assertEquals("last call", "Removed", lastCall);
        Assert.assertEquals("type", Manager.ManagerDataEvent.INTERVAL_REMOVED, lastType);
        Assert.assertEquals("start == end 2", lastEvent0, lastEvent1);
        Assert.assertEquals("index", 1, lastEvent0);
    }

    @Test
    public void testOrderVsSorted() {
        Sensor s4 = l.provideSensor("IS4");
        Sensor s2 = l.provideSensor("IS2");
        
        List<String> sortedList = l.getSystemNameList();
        SortedSet<Sensor> beanSet = l.getNamedBeanSet();
        String[] sortedArray = l.getSystemNameArray();  // deprecated, but we test until removed
        jmri.util.JUnitAppender.suppressWarnMessage("Manager#getSystemNameArray() is deprecated");
        
        Assert.assertEquals("sorted list length", 2, sortedList.size());
        Assert.assertEquals("sorted list 1st", "IS2", sortedList.get(0));
        Assert.assertEquals("sorted list 2nd", "IS4", sortedList.get(1));

        Assert.assertEquals("bean set length", 2, beanSet.size());
        Iterator<Sensor> iter = beanSet.iterator();
        Assert.assertEquals("bean set 1st", s2, iter.next());
        Assert.assertEquals("bean set 2nd", s4, iter.next());

        Assert.assertEquals("sorted array length", 2, sortedArray.length);
        Assert.assertEquals("sorted array 1st", "IS2", sortedArray[0]);
        Assert.assertEquals("sorted array 2nd", "IS4", sortedArray[1]);
        
        // add and test (non) liveness
        Sensor s3 = l.provideSensor("IS3");
        Sensor s1 = l.provideSensor("IS1");

        Assert.assertEquals("sorted list length", 2, sortedList.size());
        Assert.assertEquals("sorted list 1st", "IS2", sortedList.get(0));
        Assert.assertEquals("sorted list 2nd", "IS4", sortedList.get(1));

        Assert.assertEquals("bean set length", 4, beanSet.size());
        iter = beanSet.iterator();
        Assert.assertEquals("bean set 1st", s1, iter.next());
        Assert.assertEquals("bean set 2nd", s2, iter.next());
        Assert.assertEquals("bean set 3rd", s3, iter.next());
        Assert.assertEquals("bean set 4th", s4, iter.next());

        Assert.assertEquals("sorted array length", 2, sortedArray.length);
        Assert.assertEquals("sorted array 1st", "IS2", sortedArray[0]);
        Assert.assertEquals("sorted array 2nd", "IS4", sortedArray[1]);
        
        // update and test update
        sortedList = l.getSystemNameList();
        beanSet = l.getNamedBeanSet();
        sortedArray = l.getSystemNameArray();
        
        Assert.assertEquals("sorted list length", 4, sortedList.size());
        Assert.assertEquals("sorted list 1st", "IS1", sortedList.get(0));
        Assert.assertEquals("sorted list 2nd", "IS2", sortedList.get(1));
        Assert.assertEquals("sorted list 3rd", "IS3", sortedList.get(2));
        Assert.assertEquals("sorted list 4th", "IS4", sortedList.get(3));

        Assert.assertEquals("bean set length", 4, beanSet.size());
        iter = beanSet.iterator();
        Assert.assertEquals("bean set 1st", s1, iter.next());
        Assert.assertEquals("bean set 2nd", s2, iter.next());
        Assert.assertEquals("bean set 3rd", s3, iter.next());
        Assert.assertEquals("bean set 4th", s4, iter.next());

        Assert.assertEquals("sorted array length", 4, sortedArray.length);
        Assert.assertEquals("sorted array 1st", "IS1", sortedArray[0]);
        Assert.assertEquals("sorted array 2nd", "IS2", sortedArray[1]);
        Assert.assertEquals("sorted array 3rd", "IS3", sortedArray[2]);
        Assert.assertEquals("sorted array 4th", "IS4", sortedArray[3]);

    }

    @Test
    public void testUnmodifiable() {
        l.provideSensor("IS1");
        l.provideSensor("IS2");
        
        List<String> nameList = l.getSystemNameList();

        try {
            nameList.add("Foo");
            Assert.fail("Should have thrown");
        } catch (UnsupportedOperationException e) { /* this is OK */}

    }

    // from here down is testing infrastructure

    // Property listen & audit methods
    static protected int propertyListenerCount = 0;
    static protected String propertyListenerLast = null;

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        propertyListenerCount++;
        propertyListenerLast = e.getPropertyName();
    }

    // Data listen & audit methods
    int events;
    int lastEvent0;
    int lastEvent1;
    int lastType;
    String lastCall;
    
    @Override
    public void intervalAdded(Manager.ManagerDataEvent<Sensor> e) {
        events++;
        lastEvent0 = e.getIndex0();
        lastEvent1 = e.getIndex1();
        lastType = e.getType();
        lastCall = "Added";
    }
    @Override
    public void intervalRemoved(Manager.ManagerDataEvent<Sensor> e) {
        events++;
        lastEvent0 = e.getIndex0();
        lastEvent1 = e.getIndex1();
        lastType = e.getType();
        lastCall = "Removed";
    }
    @Override
    public void contentsChanged(Manager.ManagerDataEvent<Sensor> e) {
        events++;
        lastEvent0 = e.getIndex0();
        lastEvent1 = e.getIndex1();
        lastType = e.getType();
        lastCall = "Changed";
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager object
        jmri.util.JUnitUtil.resetInstanceManager();
        
        l = new InternalSensorManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));

        propertyListenerCount = 0;
        propertyListenerLast = null;

        events = 0;
        lastEvent0 = -1;
        lastEvent1 = -1;
        lastType = -1;
        lastCall = null;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    private final static Logger log = LoggerFactory.getLogger(InternalSensorManagerTest.class);

}
