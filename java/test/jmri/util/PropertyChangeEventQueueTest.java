package jmri.util;

import java.beans.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import jmri.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the jmri.util.PropertyChangeEventQueue class.
 *
 * @author	Bob Jacobsen Copyright 2017
 */
public class PropertyChangeEventQueueTest extends TestCase {

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

    public void testToString() throws jmri.JmriException {
        PropertyChangeEventQueue pq = new PropertyChangeEventQueue(new NamedBean[]{is1, is2});

        Assert.assertEquals("PropertyChangeEventQueue for (\"IS1\") (\"IS2\")", pq.toString());
    }

    public void testDispose() throws jmri.JmriException, InterruptedException {
        PropertyChangeEventQueue pq = new PropertyChangeEventQueue(Arrays.asList(new NamedBean[]{is1, is2}));

        int start = is1.getNumPropertyChangeListeners(); // there can be internal listeners
        Assert.assertTrue(start >= 1);

        pq.dispose();

        Assert.assertEquals(start - 1, is1.getNumPropertyChangeListeners());

        pq.dispose();  // check not an error

        Assert.assertEquals(start - 1, is1.getNumPropertyChangeListeners());
    }

    // from here down is testing infrastructure
    public PropertyChangeEventQueueTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PropertyChangeEventQueueTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PropertyChangeEventQueueTest.class);
        return suite;
    }

    Sensor is1;
    Sensor is2;
    volatile boolean flag1;

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        is1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        is2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        flag1 = false;
    }

    @Override
    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertyChangeEventQueueTest.class);

}
