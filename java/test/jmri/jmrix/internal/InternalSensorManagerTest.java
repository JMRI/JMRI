package jmri.jmrix.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.managers.InternalSensorManager class.
 *
 * @author Bob Jacobsen Copyright 2016
 */
public class InternalSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase implements Manager.ManagerDataListener<Sensor>, PropertyChangeListener {

    /** {@inheritDoc} */
    @Override
    public String getSystemName(int i) {
        return "IS" + i;
    }

    @Override
    protected String getASystemNameWithNoPrefix() {
        return "My Sensor 6";
    }

    @Test
    public void testAsAbstractFactory() {

        // ask for a Sensor, and check type
        Sensor tl = l.newSensor("IS21", "my name");

        assertNotNull(tl);

        // make sure loaded into tables
        assertNotNull( l.getBySystemName("IS21"));
        assertNotNull( l.getByUserName("my name"));

    }

    @Test
    @ToDo("investigate returning lower case system name as non-same object")
    public void testSensorNameCase() {
        assertEquals(0, l.getObjectCount());
        // create
        Sensor ta = l.provideSensor("IS:XYZ");
        Sensor tb = l.provideSensor("IS:xyz");  // upper canse and lower case are the same object
        // check
        assertNotNull( ta, "real object returned " );
        assertEquals("IS:XYZ", ta.getSystemName());  // we force upper
        assertEquals( ta, l.getBySystemName("IS:XYZ"),"system name correct ");
        
        assertNotNull( tb, "real object returned ");

        // 5.13.4+ returns IS:xyz
        // assertEquals("IS:XYZ", tb.getSystemName());  // we force upper
        // assertTrue( tb == l.getBySystemName("IS:XYZ"), "system name correct ");
        
        // assertEquals(1, l.getObjectCount());

        Sensor t = l.provideSensor("IS:XYZ");
        assertNotNull(t);
        // assertEquals(1, l.getObjectCount());
    }

    @Test
    public void testSetGetDefaultState() {

        // confirm default
        assertEquals( Sensor.UNKNOWN, InternalSensorManager.getDefaultStateForNewSensors(), "starting mode");

        // set and retrieve
        InternalSensorManager.setDefaultStateForNewSensors(Sensor.INACTIVE);
        assertEquals( Sensor.INACTIVE, InternalSensorManager.getDefaultStateForNewSensors(), "updated mode");

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
        assertEquals( 1, propertyListenerCount, "propertyListenerCount");
        assertEquals( "length", propertyListenerLast, "last call");

        s2.setUserName("Sensor 2");

        assertEquals( 2, propertyListenerCount, "propertyListenerCount");
        assertEquals( "DisplayListName", propertyListenerLast, "last call");

        // data listener should have been immediately invoked
        assertEquals( 1, events, "events");
        assertEquals( "Added", lastCall, "last call 1");
        assertEquals( Manager.ManagerDataEvent.INTERVAL_ADDED, lastType, "type 1");
        assertEquals( lastEvent0, lastEvent1, "start == end 1");
        assertEquals( 1, lastEvent0, "index 1");

        // add an item
        l.newSensor("IS3", "Sensor 3");

        // property listener should have been immediately invoked
        assertEquals( 3, propertyListenerCount, "propertyListenerCount");
        assertEquals( "length", propertyListenerLast, "last call");

        // listener should have been immediately invoked
        assertEquals( 2, events, "events");
        assertEquals( "Added", lastCall, "last call 2");
        assertEquals( Manager.ManagerDataEvent.INTERVAL_ADDED, lastType, "type 2");
        assertEquals( lastEvent0, lastEvent1, "start == end 2");
        assertEquals( 2, lastEvent0, "index 2");
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
        assertEquals( 1, events, "events");
        assertEquals( "Removed", lastCall, "last call");
        assertEquals( Manager.ManagerDataEvent.INTERVAL_REMOVED, lastType, "type");
        assertEquals( lastEvent0, lastEvent1, "start == end 2");
        assertEquals( 1, lastEvent0, "index");
    }

    @Test
    public void testOrderVsSorted() {
        Sensor s4 = l.provideSensor("IS4");
        Sensor s2 = l.provideSensor("IS2");

        SortedSet<Sensor> beanSet = l.getNamedBeanSet();

        assertEquals( 2, beanSet.size(), "bean set length");
        Iterator<Sensor> iter = beanSet.iterator();
        assertEquals( s2, iter.next(), "bean set 1st");
        assertEquals( s4, iter.next(), "bean set 2nd");

        // add and test (non) liveness
        Sensor s3 = l.provideSensor("IS3");
        Sensor s1 = l.provideSensor("IS1");

        assertEquals( 4, beanSet.size(), "bean set length");
        iter = beanSet.iterator();
        assertEquals( s1, iter.next(), "bean set 1st");
        assertEquals( s2, iter.next(), "bean set 2nd");
        assertEquals( s3, iter.next(), "bean set 3rd");
        assertEquals( s4, iter.next(), "bean set 4th");

        // update and test update
        beanSet = l.getNamedBeanSet();

        assertEquals( 4, beanSet.size(), "bean set length");
        iter = beanSet.iterator();
        assertEquals( s1, iter.next(), "bean set 1st");
        assertEquals( s2, iter.next(), "bean set 2nd");
        assertEquals( s3, iter.next(), "bean set 3rd");
        assertEquals( s4, iter.next(), "bean set 4th");

    }

    @Test
    public void testUnmodifiable() {
        l.provideSensor("IS1");
        l.provideSensor("IS2");

        java.util.SortedSet<Sensor> set = l.getNamedBeanSet();
        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class,
            () -> set.add(null));
        assertNotNull(ex);

    }

    // from here down is testing infrastructure

    // Property listen & audit methods
    protected int propertyListenerCount = 0;
    protected String propertyListenerLast = null;

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        propertyListenerCount++;
        propertyListenerLast = e.getPropertyName();
    }

    // Data listen & audit methods
    private int events;
    private int lastEvent0;
    private int lastEvent1;
    private int lastType;
    private String lastCall;

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

    @Test
    public void testBeansAreSilenceable() {
        CountingPropertyChangeListener pcl = new CountingPropertyChangeListener();
        l.addPropertyChangeListener("beans", pcl);
        assertEquals( 0, pcl.count);
        assertEquals( l.getNamedBeanSet().size(), pcl.count);
        l.provide("IS1");
        assertEquals( 1, pcl.count);
        assertEquals( l.getNamedBeanSet().size(), pcl.count);
        l.setPropertyChangesSilenced("beans", true);
        l.provide("IS2");
        assertEquals( 1, pcl.count);
        assertNotEquals( l.getNamedBeanSet().size(), pcl.count);
        l.setPropertyChangesSilenced("beans", false);
        assertEquals( 2, pcl.count);
        // this is true only if 1 item is added while silenced
        assertEquals( l.getNamedBeanSet().size(), pcl.count);
        l.provide("IS3");
        assertEquals( 3, pcl.count);
        assertEquals( l.getNamedBeanSet().size(), pcl.count);
    }

    @Test
    public void testFooIsNotSilenceable() {
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> l.setPropertyChangesSilenced("foo", true));
        assertEquals("Property foo cannot be silenced.", ex.getMessage());
    }

    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}

    @Test
    @Override
    @Disabled("No manager-specific system name validation at present")
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    private static class CountingPropertyChangeListener implements PropertyChangeListener {

        int count = 0;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            count++;
        }

    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager object

        l = new InternalSensorManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));

        propertyListenerCount = 0;
        propertyListenerLast = null;

        events = 0;
        lastEvent0 = -1;
        lastEvent1 = -1;
        lastType = -1;
        lastCall = null;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InternalSensorManagerTest.class);

}
