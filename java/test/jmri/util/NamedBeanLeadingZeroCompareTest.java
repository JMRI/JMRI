package jmri.util;

import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.*;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that user names which differ only by leading zeros are kept apart
 * when sorting beans, and that one comparator can be shared by threads.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class NamedBeanLeadingZeroCompareTest {

    @Test
    public void testUserNamesWithLeadingZerosAreDistinct() {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Sensor s1 = sm.newSensor("IS1", "Siding 7");
        Sensor s2 = sm.newSensor("IS2", "Siding 07");
        Sensor s3 = sm.newSensor("IS3", "Platform 1 East");
        Sensor s4 = sm.newSensor("IS4", "Platform 01 East");

        NamedBeanUserNameComparator<Sensor> c = new NamedBeanUserNameComparator<>();
        assertTrue(c.compare(s1, s2) < 0, "Siding 7 (IS1) before Siding 07 (IS2), by system name");
        assertTrue(c.compare(s2, s1) > 0, "Siding 07 (IS2) after Siding 7 (IS1), by system name");
        assertEquals(0, c.compare(s1, s1), "Siding 7 equals itself");

        TreeSet<Sensor> set = new TreeSet<>(c);
        set.add(s1);
        set.add(s2);
        set.add(s3);
        set.add(s4);
        assertEquals(4, set.size(), "all four sensors kept in the sorted set");
        // alphanumeric order of user names, system name order on a tie
        StringBuilder order = new StringBuilder();
        set.forEach(s -> order.append(s.getSystemName()).append(' '));
        assertEquals("IS3 IS4 IS1 IS2 ", order.toString());
    }

    @Test
    public void testSameUserNameOnDifferentBeansIsDistinct() {
        Sensor s = InstanceManager.getDefault(SensorManager.class).newSensor("IS5", "Yard");
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT5", "Yard");

        NamedBeanUserNameComparator<NamedBean> c = new NamedBeanUserNameComparator<>();
        assertTrue(c.compare(s, t) != 0, "beans with the same user name differ");
        assertEquals(-Integer.signum(c.compare(s, t)), Integer.signum(c.compare(t, s)),
                "order is consistent both ways");

        TreeSet<NamedBean> set = new TreeSet<>(c);
        set.add(s);
        set.add(t);
        assertEquals(2, set.size(), "both beans kept in the sorted set");
    }

    private static boolean lessThan(AlphanumComparator c, String s1, String s2) {
        try {
            return c.compare(s1, s2) < 0;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Test
    public void testSharedComparatorIsThreadSafe() throws InterruptedException {
        AlphanumComparator shared = new AlphanumComparator();
        AtomicInteger wrong = new AtomicInteger();
        Runnable r1 = () -> {
            for (int i = 0; i < 100000 && wrong.get() == 0; i++) {
                if (!lessThan(shared, "A2", "A10")) {
                    wrong.incrementAndGet();
                }
            }
        };
        Runnable r2 = () -> {
            for (int i = 0; i < 100000 && wrong.get() == 0; i++) {
                if (!lessThan(shared, "Platform 123456 East", "Platform 123456 West")) {
                    wrong.incrementAndGet();
                }
            }
        };
        Thread t1 = new Thread(r1, "compare 1");
        Thread t2 = new Thread(r2, "compare 2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        assertEquals(0, wrong.get(), "wrong results from a shared comparator");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
