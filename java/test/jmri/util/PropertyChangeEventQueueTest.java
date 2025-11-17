package jmri.util;

import java.beans.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import jmri.*;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the jmri.util.PropertyChangeEventQueue class.
 *
 * @author Bob Jacobsen Copyright 2017
 */
public class PropertyChangeEventQueueTest {

    @Test
    public void testArraySingleListen() throws jmri.JmriException, InterruptedException {
        PropertyChangeEventQueue pq = new PropertyChangeEventQueue(new NamedBean[]{is1, is2});

        Assertions.assertNotNull(is1);
        is1.setState(Sensor.ACTIVE);

        // may have to wait, but done automatically
        PropertyChangeEvent e = pq.take();
        assertEquals(is1, e.getSource());
        assertEquals("KnownState", e.getPropertyName());
        assertEquals(Sensor.ACTIVE, (int)e.getNewValue());

        // test no more
        PropertyChangeEvent another = pq.poll(100, TimeUnit.MILLISECONDS);
        assertNull(another);
    }

    @Test
    public void testListSingleListen() throws jmri.JmriException, InterruptedException {
        PropertyChangeEventQueue pq = new PropertyChangeEventQueue(Arrays.asList(new NamedBean[]{is1, is2}));

        is1.setState(Sensor.ACTIVE);

        // may have to wait, but done automatically
        PropertyChangeEvent e = pq.take();
        Assertions.assertNotNull(is1);
        assertEquals(is1, e.getSource());
        assertEquals("KnownState", e.getPropertyName());
        assertEquals(Sensor.ACTIVE, (int)e.getNewValue());

        // test no more
        PropertyChangeEvent another = pq.poll(100, TimeUnit.MILLISECONDS);
        assertNull(another);
    }

    @Test
    public void testToString() throws jmri.JmriException {
        PropertyChangeEventQueue pq = new PropertyChangeEventQueue(new NamedBean[]{is1, is2});

        assertEquals("PropertyChangeEventQueue for (\"IS1\") (\"IS2\")", pq.toString());
    }

    @Test
    public void testDispose() throws jmri.JmriException, InterruptedException {
        PropertyChangeEventQueue pq = new PropertyChangeEventQueue(Arrays.asList(new NamedBean[]{is1, is2}));

        Assertions.assertNotNull(is1);
        int start = is1.getNumPropertyChangeListeners(); // there can be internal listeners
        assertTrue(start >= 1);

        pq.dispose();

        assertEquals(start - 1, is1.getNumPropertyChangeListeners());

        pq.dispose();  // check not an error

        assertEquals(start - 1, is1.getNumPropertyChangeListeners());
    }

    private Sensor is1 = null;
    private Sensor is2;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        is1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        is2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertyChangeEventQueueTest.class);

}
