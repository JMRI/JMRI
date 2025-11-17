package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test the ProxySensorManager
 *
 * @author Bob Jacobsen 2003, 2006, 2008, 2014
 */
public class ProxySensorManagerTest extends AbstractProxyManagerTestBase<ProxySensorManager,Sensor>
    implements Manager.ManagerDataListener<Sensor>, PropertyChangeListener {

    @Test
    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
    }

    @Test
    public void testPutGetJ() {
        // create
        Sensor tj = l.newSensor("JS1", "mine");
        // check
        assertNotNull( tj, "real object returned ");
        assertSame( tj, l.getByUserName("mine"), "user name correct ");
        assertSame( tj, l.getBySystemName("JS1"), "system name correct ");
    }

    @Test
    public void testSensorNameCase() {
        assertEquals(0, l.getObjectCount());
        // create
        Sensor t = l.provideSensor("IS:XYZ");
        assertNotEquals(t, l.provideSensor("IS:xyz"));  // upper case and lower case are different objects
        // check
        assertNotNull( t, "real object returned ");
        assertEquals("IS:XYZ", t.getSystemName());  // we force upper
        assertSame( t, l.getBySystemName("IS:XYZ"), "system name correct ");
        assertEquals(2, l.getObjectCount());
        assertEquals(2, l.getNamedBeanSet().size());
        // test providing same name as existing sensor does not create new sensor
        l.provideSensor("IS:XYZ");
        assertEquals(2, l.getObjectCount());
        assertEquals(2, l.getNamedBeanSet().size());
    }

    @Test
    public void testPutGetI() {
        // create
        Sensor ti = l.newSensor("IS1", "mine");
        // check
        assertNotNull( ti, "real object returned " );
        assertSame( ti, l.getByUserName("mine"), "user name correct ");
        assertSame( ti, l.getBySystemName("IS1"), "system name correct ");
    }

    @Test
    public void testPutGetK() {
        // create
        Sensor tk = l.newSensor("KS1", "mine");
        // check
        assertNotNull( tk, "real object returned " );
        assertSame( tk, l.getByUserName("mine"), "user name correct ");
        assertSame( tk, l.getBySystemName("KS1"), "system name correct ");
    }

    @Test
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("9");
        // check
        assertNotNull( t, "real object returned");
        assertEquals( "JS9", t.getSystemName(), "system name correct");
        assertEquals( t, l.getBySystemName("JS9"), "can find by name");
    }

    @Test
    public void testProvideFailure() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> l.provideSensor(""));
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("Invalid system name for Sensor: System name must start with \"" + l.getSystemNamePrefix() + "\".");
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        Sensor t1 = l.newSensor("JS1", "mine");
        assertNotNull( t1, "t1 real object returned ");
        assertEquals( t1, l.getByUserName("mine"), "same by user ");
        assertEquals( t1, l.getBySystemName("JS1"), "same by system ");

        Sensor t2 = l.newSensor("JS1", "mine");
        assertNotNull( t2, "t2 real object returned ");
        // check
        assertSame( t1, t2, "same new ");
    }

    @Test
    public void testMisses() {
        // try to get nonexistant objects
        assertNull( l.getByUserName("foo"));
        assertNull( l.getBySystemName("bar"));
    }

    @Test
    public void testUpperLower() {  // verify that names are case sensitive
        Sensor t = l.provideSensor("JS1ABC");  // internal will always accept that name
        String name = t.getSystemName();

        int prefixLength = l.getSystemPrefix().length()+1;     // 1 for type letter
        String lowerName = name.substring(0,prefixLength)+name.substring(prefixLength, name.length()).toLowerCase();

        assertNotEquals(t, l.getSensor(lowerName));
    }

    @Test
    public void testRename() {
        // get
        Sensor t1 = l.newSensor("JS1", "before");
        assertNotNull( t1, "t1 real object ");
        t1.setUserName("after");
        Sensor t2 = l.getByUserName("after");
        assertEquals( t1, t2, "same object");
        assertNull( l.getByUserName("before"), "no old object");
    }

    @Test
    public void testTwoNames() {
        Sensor jl212 = l.provideSensor("JS212");
        Sensor jl211 = l.provideSensor("JS211");

        assertNotNull(jl212);
        assertNotNull(jl211);
        assertNotSame(jl212, jl211);
    }

    @Test
    public void testDefaultNotInternal() {
        Sensor lut = l.provideSensor("211");

        assertNotNull(lut);
        assertEquals("JS211", lut.getSystemName());
    }

    @Test
    public void testProvideUser() {
        Sensor l1 = l.provideSensor("211");
        l1.setUserName("user 1");
        Sensor l2 = l.provideSensor("user 1");
        Sensor l3 = l.getSensor("user 1");

        assertNotNull(l1);
        assertNotNull(l2);
        assertNotNull(l3);
        assertEquals(l1, l2);
        assertEquals(l3, l2);
        assertEquals(l1, l3);

        Sensor l4 = l.getSensor("JLuser 1");
        assertNull(l4);
    }

    // the following methods test code in Manager and AbstractManager,
    // but they need a concrete implementation to do it, hence are here.

    @Test
    public void testAddTracking() {
        Sensor s1 = l.provideSensor("IS1");
        s1.setUserName("Sensor 1");

        l.addDataListener(this);
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
        assertEquals( Manager.ManagerDataEvent.INTERVAL_ADDED, lastType,
            "type 2");
        assertEquals( lastEvent0, lastEvent1, "start == end 2");
        assertEquals( 2, lastEvent0, "index 2");

        // can add a manager and still get notifications
        l.addManager(new InternalSensorManager(new InternalSystemConnectionMemo("Z", "Zulu")));
        l.provideSensor("ZS2");

        // property listener should have been immediately invoked
        assertEquals( 5, propertyListenerCount, "propertyListenerCount");
        assertEquals( "length", propertyListenerLast, "last call");

        // listener should have been immediately invoked
        assertEquals( 3, events, "events");
        assertEquals( "Added", lastCall, "last call 2");
        assertEquals( Manager.ManagerDataEvent.INTERVAL_ADDED, lastType, "type 2");
        assertEquals( lastEvent0, lastEvent1, "start == end 2");
        assertEquals( 3, lastEvent0, "index 3");

        JUnitAppender.suppressWarnMessageStartsWith("getNamedBeanList");
    }

    @Test
    public void testRemoveTrackingI() {

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

        JUnitAppender.suppressWarnMessageStartsWith("getNamedBeanList");
    }

    @Test
    public void testRemoveTrackingJ() {

        l.provideSensor("IS10");
        l.provideSensor("IS11");

        Sensor s1 = l.provideSensor("JS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = l.provideSensor("JS2");
        l.provideSensor("JS3");

        l.addDataListener(this);

        l.deregister(s2);

        // listener should have been immediately invoked
        assertEquals( 1, events, "events");
        assertEquals( "Removed", lastCall, "last call");
        assertEquals( Manager.ManagerDataEvent.INTERVAL_REMOVED, lastType, "type");
        assertEquals( lastEvent0, lastEvent1, "start == end 2");
        assertEquals( 3, lastEvent0, "index");

        JUnitAppender.suppressWarnMessageStartsWith("getNamedBeanList");
    }

    @Test
    public void testGetObjectCount() {
        assertEquals(0, l.getObjectCount());

        l.provideSensor("IS10");
        assertEquals(1, l.getObjectCount());

        l.provideSensor("JS1");
        assertEquals(2, l.getObjectCount());

        l.provideSensor("IS11");
        assertEquals(3, l.getObjectCount());

        Sensor s2 = l.provideSensor("JS2");
        l.provideSensor("JS3");
        assertEquals(5, l.getObjectCount());

        l.deregister(s2);
        assertEquals(4, l.getObjectCount());
    }

    @Test
    public void testRemoveTrackingJMute() {

        l.setDataListenerMute(true);

        l.provideSensor("IS10");
        l.provideSensor("IS11");

        Sensor s1 = l.provideSensor("JS1");
        s1.setUserName("Sensor 1");
        Sensor s2 = l.provideSensor("JS2");
        l.provideSensor("JS3");

        l.addDataListener(this);

        l.deregister(s2);

        // listener should have not been invoked
        assertEquals( 0, events, "events");

        // unmute and get notification
        l.setDataListenerMute(false);
        assertEquals( 1, events, "events");
        assertEquals( "Changed", lastCall, "last call");
        assertEquals( Manager.ManagerDataEvent.CONTENTS_CHANGED, lastType, "type");
        assertEquals( 0, lastEvent0, "index0");
        assertEquals( 3, lastEvent1, "index1"); // originally five items, deleted 1, so 4, and last index is then 3
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
        Sensor s1 = l.provideSensor("IS1");
        l.provideSensor("IS2");

        SortedSet<Sensor> beanSet = l.getNamedBeanSet();

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class,
            () -> beanSet.add(s1), "beanSet should have thrown");
        assertNotNull(ex);

    }

    // check how proxy is integrated with defaults
    @Test
    public void testInstanceManagerIntegration() {
        JUnitUtil.resetInstanceManager();
        assertNotNull(InstanceManager.getDefault(SensorManager.class));

        JUnitUtil.initInternalSensorManager();

        assertInstanceOf( ProxySensorManager.class,
            InstanceManager.getDefault(SensorManager.class));

        assertNotNull(InstanceManager.getDefault(SensorManager.class));
        assertNotNull(InstanceManager.getDefault(SensorManager.class).provideSensor("IS1"));

        InternalSensorManager m = new InternalSensorManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setSensorManager(m);

        assertNotNull(InstanceManager.getDefault(SensorManager.class).provideSensor("JS1"));
        assertNotNull(InstanceManager.getDefault(SensorManager.class).provideSensor("IS2"));
    }

    // Property listen & audit methods
    private int propertyListenerCount = 0;
    private String propertyListenerLast = null;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager object
        l = new ProxySensorManager();
        // initially has three systems: IS, JS, KS
        l.addManager(new InternalSensorManager(new InternalSystemConnectionMemo("J", "Juliet")));
        l.addManager(new InternalSensorManager(new InternalSystemConnectionMemo("I", "India"))); // not in alpha order to make it exciting
        l.addManager(new InternalSensorManager(new InternalSystemConnectionMemo("K", "Kilo")));

        InstanceManager.setSensorManager(l);

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

}
