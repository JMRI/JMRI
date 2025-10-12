package jmri.util;

import java.util.TreeSet;

import org.junit.jupiter.api.*;

import jmri.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NamedBeanUserNameComparatorTest {

    @Test
    public void testNonNullUserNameCases() {
        NamedBeanUserNameComparator<Turnout> t = new NamedBeanUserNameComparator<>();

        Turnout it1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        Turnout it10 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT10");
        Turnout it2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2");
        it1.setUserName(it1.getSystemName());
        it10.setUserName(it10.getSystemName());
        it2.setUserName(it2.getSystemName());

        assertEquals( 0, t.compare(it1, it1), "IT1 == IT1");

        assertEquals( -1, t.compare(it1, it2), "IT1 < IT2");
        assertEquals( +1, t.compare(it2, it1), "IT2 > IT1");

        assertEquals( +1, t.compare(it10, it2), "IT10 > IT2");
        assertEquals( -1, t.compare(it2, it10), "IT2 < IT10");

        TreeSet<Turnout> set = new TreeSet<>(t);
        set.addAll(InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet());
        assertArrayEquals(new Turnout[]{it1, it2, it10},
                set.toArray(Turnout[]::new));

        it1.setUserName("A");
        it10.setUserName("B");
        it2.setUserName("C");

        assertEquals( 0, t.compare(it1, it1), "A == A");

        assertEquals( -1, t.compare(it1, it2), "A < C");
        assertEquals( +1, t.compare(it2, it1), "C > A");

        assertEquals( -1, t.compare(it10, it2), "B < C");
        assertEquals( +1, t.compare(it2, it10), "C > B");

        set = new TreeSet<>(t);
        set.addAll(InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet());
        assertArrayEquals(new Turnout[]{it1, it10, it2},
                set.toArray(Turnout[]::new));
    }

    @Test
    public void testOneLetterCases() {
        NamedBeanUserNameComparator<Turnout> t = new NamedBeanUserNameComparator<>();

        Turnout it1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        Turnout it10 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT10");
        Turnout it2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2");

        assertEquals( 0, t.compare(it1, it1), "IT1 == IT1");

        assertEquals( -1, t.compare(it1, it2), "IT1 < IT2");
        assertEquals( +1, t.compare(it2, it1));

        assertEquals( +1, t.compare(it10, it2), "IT10 > IT2");
        assertEquals( -1, t.compare(it2, it10), "IT2 < IT10");

        TreeSet<Turnout> set = new TreeSet<>(t);
        set.addAll(InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet());
        assertArrayEquals(new Turnout[]{it1, it2, it10},
                set.toArray(Turnout[]::new));
    }

    @Test
    public void testTwoLetterCases() {
        NamedBeanUserNameComparator<Turnout> t = new NamedBeanUserNameComparator<>();

        Turnout i2t1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I2T1");
        Turnout i2t10 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I2T10");
        Turnout i2t2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I2T2");

        assertEquals( 0, t.compare(i2t1, i2t1), "I2T1 == I2T1");

        assertEquals( -1, t.compare(i2t1, i2t2), "I2T1 < I2T2");
        assertEquals( +1, t.compare(i2t2, i2t1), "I2T2 > I2T1");

        assertEquals( +1, t.compare(i2t10, i2t2), "I2T10 > I2T2");
        assertEquals( -1, t.compare(i2t2, i2t10), "I2T2 < I2T10");
    }

    @Test
    public void testThreeLetterCases() {
        NamedBeanUserNameComparator<Turnout> t = new NamedBeanUserNameComparator<>();

        Turnout i23t1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T1");
        Turnout i23t10 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T10");
        Turnout i23t2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T2");

        assertEquals( 0, t.compare(i23t1, i23t1), "I23T1 == I23T1");

        assertEquals( -1, t.compare(i23t1, i23t2), "I23T1 < I23T2");
        assertEquals( +1, t.compare(i23t2, i23t1), "I23T2 > I23T1");

        assertEquals( +1, t.compare(i23t10, i23t2), "I23T10 > I23T2");
        assertEquals( -1, t.compare(i23t2, i23t10), "I23T2 < I23T10");
    }

    @Test
    public void testForUniqueOrdering() {

        // check the ordering of mix of beans with and without user names
        //    IT3 FOO
        //    IT1 XYZ
        //    IT2
        //    IT4

        NamedBeanUserNameComparator<Turnout> t = new NamedBeanUserNameComparator<>();

        Turnout it1xyz = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        it1xyz.setUserName("XYZ");
        Turnout it2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2");
        Turnout it3foo = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT3");
        it3foo.setUserName("FOO");
        Turnout it4 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT4");

        assertEquals( -1, t.compare(it3foo, it1xyz), "IT3 < IT1");
        assertEquals( -1, t.compare(it3foo, it2), "IT3 < IT2");
        assertEquals( -1, t.compare(it3foo, it4), "IT3 < IT4");
        assertEquals( -1, t.compare(it1xyz, it2), "IT1 < IT2");
        assertEquals( -1, t.compare(it1xyz, it4), "IT1 < IT4");
        assertEquals( -1, t.compare(it2, it4), "IT2 < IT4");

        TreeSet<Turnout> set = new TreeSet<>(t);
        set.addAll(InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet());
        assertArrayEquals(new Turnout[]{it3foo, it1xyz, it2, it4},
                set.toArray(Turnout[]::new));
    }

    @Test
    public void testForUniqueOrderingWithLS() {
        ((jmri.managers.ProxySensorManager) InstanceManager.getDefault(SensorManager.class)).getDefaultManager();
        // add an LS manager
        var lsm = new jmri.jmrix.internal.InternalSensorManager(
                    new jmri.jmrix.internal.InternalSystemConnectionMemo("L", "LocoNet"));
        ((jmri.managers.ProxySensorManager) InstanceManager.getDefault(SensorManager.class)).addManager(lsm);

        // Check the ordering of mix of beans with and without user names
        // Expect:
        //  IS102    // due to prefer number comparator
        //  IS 101
        //  LS102
        //  LS 101
        //  ISCLOCKRUNNING


        NamedBeanUserNameComparator<Sensor> t = new NamedBeanUserNameComparator<>();

        Sensor is101 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS101");
        is101.setUserName("IS 101");
        Sensor is102 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS102");
        is102.setUserName("IS102");

        Sensor clock = InstanceManager.getDefault(SensorManager.class).provideSensor ("ISCLOCKRUNNING");

        Sensor ls101 = InstanceManager.getDefault(SensorManager.class).provideSensor("LS101");
        ls101.setUserName("LS 101");
        Sensor ls102 = InstanceManager.getDefault(SensorManager.class).provideSensor("LS102");
        ls102.setUserName("LS102");

        assertEquals("LS101", ls101.getSystemName());
        assertEquals("IS101", is101.getSystemName(), "checking that no prefixes were added");

        assertEquals( -1, t.compare(is102, is101), "IS102 < IS101");
        assertEquals( -1, t.compare(is101, clock), "IS101 < ISCLOCKRUNNING");

        TreeSet<Sensor> set = new TreeSet<>(t);
        set.addAll(InstanceManager.getDefault(SensorManager.class).getNamedBeanSet());
        assertArrayEquals(new Sensor[]{is102, is101, ls102, ls101, clock},  // wrong order - fail
            set.toArray(Sensor[]::new));
    }

    @Test
    public void testMixedUserNamesSystemNamesCase() {
        NamedBeanUserNameComparator<Turnout> c = new NamedBeanUserNameComparator<>();

        Turnout i23t1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T1");
        Turnout i23t10 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T10");
        Turnout i23t2 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T2");
        Turnout i23t3 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T3");
        Turnout i23t4 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T4");
        Turnout i23t5 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T5");
        Turnout i23t6 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T6");
        Turnout i23t7 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T7");
        Turnout i23t8 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T8");
        Turnout i23t9 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("I23T9");

        i23t3.setUserName("Name 4");
        i23t4.setUserName("Name 3");
        i23t5.setUserName("A name");

        // expected sort order:
        // i23t5 (A Name)
        // i23t4 (Name 3)
        // i23t3 (Name 4)
        // i23t1
        // i23t2
        // i23t6
        // i23t7
        // i23t8
        // i23t9
        // i23t10
        assertEquals( 0, c.compare(i23t1, i23t1), "I23T1 == I23T1");
        assertEquals( 0, c.compare(i23t2, i23t2), "I23T2 == I23T2");
        assertEquals( 0, c.compare(i23t3, i23t3), "I23T3 == I23T3");
        assertEquals( 0, c.compare(i23t4, i23t4), "I23T4 == I23T4");
        assertEquals( 0, c.compare(i23t5, i23t5), "I23T5 == I23T5");
        assertEquals( 0, c.compare(i23t6, i23t6), "I23T6 == I23T6");
        assertEquals( 0, c.compare(i23t7, i23t7), "I23T7 == I23T7");
        assertEquals( 0, c.compare(i23t8, i23t8), "I23T8 == I23T8");
        assertEquals( 0, c.compare(i23t9, i23t9), "I23T9 == I23T9");
        assertEquals( 0, c.compare(i23t10, i23t10), "I23T10 == I23T10");

        assertEquals( -1, c.compare(i23t1, i23t2), "I23T1 < I23T2");
        assertEquals( +1, c.compare(i23t2, i23t1), "I23T2 > I23T1");

        assertEquals( +1, c.compare(i23t10, i23t2), "I23T10 > I23T2");
        assertEquals( -1, c.compare(i23t2, i23t10), "I23T2 < I23T10");

        assertEquals( -1, c.compare(i23t4, i23t3), "I23T4 < I23T3");
        assertEquals( +1, c.compare(i23t3, i23t4), "I23T3 > I23T4");

        assertEquals( -1, c.compare(i23t5, i23t1), "I23T5 < I23T1");
        assertEquals( +1, c.compare(i23t1, i23t5), "I23T1 > I23T5");

        TreeSet<Turnout> set = new TreeSet<>(c);
        set.addAll(InstanceManager.getDefault(TurnoutManager.class).getNamedBeanSet());
        assertArrayEquals(new Turnout[]{i23t5, i23t4, i23t3, i23t1, i23t2, i23t6, i23t7, i23t8, i23t9, i23t10},
            set.toArray(Turnout[]::new));
    }

    private boolean hit = false;

    @Test
    public void testSystemSpecificCase() {
        NamedBeanUserNameComparator<Turnout> t = new NamedBeanUserNameComparator<>();

        // this just checks that the local sort is called
        Turnout it1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        Turnout it2 = new jmri.implementation.AbstractTurnout("IT2") {

            @Override
            protected void forwardCommandChangeToLayout(int s) {
            }

            @Override
            protected void turnoutPushbuttonLockout(boolean b) {
            }

            @Override
            public int compareSystemNameSuffix(String suffix1, String suffix2, jmri.NamedBean n) {
                hit = true;
                return super.compareSystemNameSuffix(suffix1, suffix2, n);
            }
        };

        hit = false;
        assertEquals( -1, t.compare(it1, it2), "IT1 < IT2");
        assertFalse(hit);

        hit = false;
        assertEquals( +1, t.compare(it2, it1), "IT2 < IT1");
        assertTrue(hit);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NamedBeanUserNameComparatorTest.class);
}
