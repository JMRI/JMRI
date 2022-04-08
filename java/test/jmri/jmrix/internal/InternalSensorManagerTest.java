package jmri.jmrix.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

        SortedSet<Sensor> beanSet = l.getNamedBeanSet();

        Assert.assertEquals("bean set length", 2, beanSet.size());
        Iterator<Sensor> iter = beanSet.iterator();
        Assert.assertEquals("bean set 1st", s2, iter.next());
        Assert.assertEquals("bean set 2nd", s4, iter.next());

        // add and test (non) liveness
        Sensor s3 = l.provideSensor("IS3");
        Sensor s1 = l.provideSensor("IS1");

        Assert.assertEquals("bean set length", 4, beanSet.size());
        iter = beanSet.iterator();
        Assert.assertEquals("bean set 1st", s1, iter.next());
        Assert.assertEquals("bean set 2nd", s2, iter.next());
        Assert.assertEquals("bean set 3rd", s3, iter.next());
        Assert.assertEquals("bean set 4th", s4, iter.next());

        // update and test update
        beanSet = l.getNamedBeanSet();

        Assert.assertEquals("bean set length", 4, beanSet.size());
        iter = beanSet.iterator();
        Assert.assertEquals("bean set 1st", s1, iter.next());
        Assert.assertEquals("bean set 2nd", s2, iter.next());
        Assert.assertEquals("bean set 3rd", s3, iter.next());
        Assert.assertEquals("bean set 4th", s4, iter.next());

    }

    @Test
    public void testUnmodifiable() {
        l.provideSensor("IS1");
        l.provideSensor("IS2");

        java.util.SortedSet<Sensor> set = l.getNamedBeanSet();
        Assert.assertThrows(UnsupportedOperationException.class, () -> set.add(null));

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

    @Test
    public void testBeansAreSilenceable() {
        CountingPropertyChangeListener pcl = new CountingPropertyChangeListener();
        l.addPropertyChangeListener("beans", pcl);
        assertThat(pcl.count).isEqualTo(0);
        assertThat(pcl.count).isEqualTo(l.getNamedBeanSet().size());
        l.provide("IS1");
        assertThat(pcl.count).isEqualTo(1);
        assertThat(pcl.count).isEqualTo(l.getNamedBeanSet().size());
        l.setPropertyChangesSilenced("beans", true);
        l.provide("IS2");
        assertThat(pcl.count).isEqualTo(1);
        assertThat(pcl.count).isNotEqualTo(l.getNamedBeanSet().size());
        l.setPropertyChangesSilenced("beans", false);
        assertThat(pcl.count).isEqualTo(2);
        // this is true only if 1 item is added while silenced
        assertThat(pcl.count).isEqualTo(l.getNamedBeanSet().size());
        l.provide("IS3");
        assertThat(pcl.count).isEqualTo(3);
        assertThat(pcl.count).isEqualTo(l.getNamedBeanSet().size());
    }

    @Test
    public void testFooIsNotSilenceable() {
        assertThatThrownBy(() -> l.setPropertyChangesSilenced("foo", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Property foo cannot be silenced.");
    }

    // No manager-specific system name validation at present
    @Test
    @Override
    public void testMakeSystemNameWithNoPrefixNotASystemName() {}

    // No manager-specific system name validation at present
    @Test
    @Override
    public void testMakeSystemNameWithPrefixNotASystemName() {}

    // No manager-specific system name validation at present
    @Test
    @Override
    public void testIncorrectGetNextValidAddress() {}

    @Test
    public void testDeprecatedGetNextValidAddress() throws JmriException {

        Assert.assertEquals("2", "My Sensor 2", l.getNextValidAddress("My Sensor 2", l.getSystemPrefix(), false));

    }

    @Test
    public void testgetNextValidAddressMaxedOut() throws JmriException {

        Assert.assertNotNull("Created S1", l.provide("My Sensor 1"));
        Assert.assertEquals("2 false", "My Sensor 2", l.getNextValidAddress("My Sensor 1", l.getSystemPrefix(),false));
        Assert.assertEquals("2 true", "My Sensor 2", l.getNextValidAddress("My Sensor 1", l.getSystemPrefix(),true));


        Assert.assertNotNull("Created S2", l.provide("My Sensor 2"));
        Assert.assertNotNull("Created S3", l.provide("My Sensor 3"));
        Assert.assertEquals("2", "My Sensor 4", l.getNextValidAddress("My Sensor 1", l.getSystemPrefix(),false));

        Assert.assertNotNull("Created S4", l.provide("My Sensor 4"));
        Assert.assertNotNull("Created S5", l.provide("My Sensor 5"));
        Assert.assertNotNull("Created S6", l.provide("My Sensor 6"));
        Assert.assertNotNull("Created S7", l.provide("My Sensor 7"));
        Assert.assertNotNull("Created S8", l.provide("My Sensor 8"));
        Assert.assertEquals("9", "My Sensor 9", l.getNextValidAddress("My Sensor 1", l.getSystemPrefix(),false));

        Assert.assertNotNull("Created S9", l.provide("My Sensor 9"));
        Assert.assertEquals("10", "My Sensor 10", l.getNextValidAddress("My Sensor 1", l.getSystemPrefix(),false));

        Assert.assertNotNull("Created S10", l.provide("My Sensor 10"));
        Assert.assertEquals("11", "My Sensor 11", l.getNextValidAddress("My Sensor 1", l.getSystemPrefix(),false));

        Assert.assertNotNull("Created S11", l.provide("My Sensor 11"));

        Assert.assertThrows(JmriException.class, () -> l.getNextValidAddress("My Sensor 1",l.getSystemPrefix(),false));

        Assert.assertEquals("12", "My Sensor 12", l.getNextValidAddress("My Sensor 2", l.getSystemPrefix(),false));

        Assert.assertEquals("99 true", "My Sensor 100", l.getNextValidAddress("My Sensor 99", l.getSystemPrefix(),true));

    }

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

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(InternalSensorManagerTest.class);

}
