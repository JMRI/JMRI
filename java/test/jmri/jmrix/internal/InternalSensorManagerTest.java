package jmri.jmrix.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.util.JUnitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Assert;
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

        Assert.assertNotNull(tl);

        // make sure loaded into tables
        Assert.assertNotNull( l.getBySystemName("IS21"));
        Assert.assertNotNull( l.getByUserName("my name"));

    }

    public void testSensorNameCase() {
        Assert.assertEquals(0, l.getObjectCount());
        // create
        Sensor ta = l.provideSensor("IS:XYZ");
        Sensor tb = l.provideSensor("IS:xyz");  // upper canse and lower case are the same object
        // check
        Assert.assertNotNull("real object returned ", ta );
        Assert.assertEquals("IS:XYZ", ta.getSystemName());  // we force upper
        Assert.assertTrue("system name correct ", ta == l.getBySystemName("IS:XYZ"));
        
        Assert.assertNotNull("real object returned ", tb );
        Assert.assertEquals("IS:XYZ", tb.getSystemName());  // we force upper
        Assert.assertTrue("system name correct ", tb == l.getBySystemName("IS:XYZ"));
        
        Assert.assertEquals(1, l.getObjectCount());

        Sensor t = l.provideSensor("IS:XYZ");
        Assert.assertNotNull(t);
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
        assertThatThrownBy(() -> set.add(null))
            .isInstanceOf(UnsupportedOperationException.class);

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
