package jmri.util;

import java.beans.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import jmri.*;
import org.junit.*;

/**
 * Tests for the jmri.util.PropertyChangeEventQueue class.
 *
 * @author	Bob Jacobsen Copyright 2017
 */
public class PropertyChangeEventQueueTest {

    @Test
    public void testArraySingleListen() throws jmri.JmriException, InterruptedException {
        PropertyChangeEventQueue pq = new PropertyChangeEventQueue(new NamedBean[]{is1, is2});

        is1.setState(Sensor.ACTIVE);

        // may have to wait, but done automatically
        PropertyChangeEvent e = pq.take();
        Assert.assertEquals(is1, e.getSource());
        Assert.assertEquals("KnownState", e.getPropertyName());
        Assert.assertEquals(Sensor.ACTIVE, e.getNewValue());

        // test no more
        PropertyChangeEvent another = pq.poll(100, TimeUnit.MILLISECONDS);
        Assert.assertTrue(another == null);
    }

    @Test
    public void testListSingleListen() throws jmri.JmriException, InterruptedException {
        PropertyChangeEventQueue pq = new PropertyChangeEventQueue(Arrays.asList(new NamedBean[]{is1, is2}));

        is1.setState(Sensor.ACTIVE);

        // may have to wait, but done automatically
        PropertyChangeEvent e = pq.take();
        Assert.assertEquals(is1, e.getSource());
        Assert.assertEquals("KnownState", e.getPropertyName());
        Assert.assertEquals(Sensor.ACTIVE, e.getNewValue());

        // test no more
        PropertyChangeEvent another = pq.poll(100, TimeUnit.MILLISECONDS);
        Assert.assertTrue(another == null);
    }

    @Test
    public void testToString() throws jmri.JmriException {
        PropertyChangeEventQueue pq = new PropertyChangeEventQueue(new NamedBean[]{is1, is2});

        Assert.assertEquals("PropertyChangeEventQueue for (\"IS1\") (\"IS2\")", pq.toString());
    }

    @Test
    public void testDispose() throws jmri.JmriException, InterruptedException {
        PropertyChangeEventQueue pq = new PropertyChangeEventQueue(Arrays.asList(new NamedBean[]{is1, is2}));

        int start = is1.getNumPropertyChangeListeners(); // there can be internal listeners
        Assert.assertTrue(start >= 1);

        pq.dispose();

        Assert.assertEquals(start - 1, is1.getNumPropertyChangeListeners());

        pq.dispose();  // check not an error

        Assert.assertEquals(start - 1, is1.getNumPropertyChangeListeners());
    }

    Sensor is1;
    Sensor is2;
    volatile boolean flag1;

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        is1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        is2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        flag1 = false;
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertyChangeEventQueueTest.class);

}
