package jmri;

import java.util.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the static methods of the interface.
 *
 * Detailed tests are in jmri.managers.AbstractManagerTestBase with even more
 * detailed tests (which require beans, etc) in type-specific subclasses
 *
 * @author Bob Jacobsen Copyright (C) 2017
 */
public class ManagerTest {

    @Test
    public void testGetSystemPrefixLengthOK() {
        assertEquals( 1, Manager.getSystemPrefixLength("LT1"), "LT1");
        assertEquals( 2, Manager.getSystemPrefixLength("L2T1"), "L2T1");
        assertEquals( 3, Manager.getSystemPrefixLength("L21T1"), "L21T1");
    }

    @Test
    public void testGetSystemPrefixLengthThrow1() {
        Exception ex = assertThrows( NamedBean.BadSystemNameException.class,
            () -> Manager.getSystemPrefixLength(".T1"),
            "Should have thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetSystemPrefixLengthThrow2() {
        Exception ex = assertThrows( NamedBean.BadSystemNameException.class,
            () -> Manager.getSystemPrefixLength("1T1"),
            "Should have thrown");
        assertNotNull(ex);
    }


    @Test
    public void testGetSystemPrefixLengthBad() {
        Exception ex = assertThrows( NamedBean.BadSystemNameException.class,
            () -> Manager.getSystemPrefixLength(""),
            "Should have thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetSystemPrefixOK() {
        assertEquals( "L", Manager.getSystemPrefix("LT1"), "LT1");
        assertEquals( "L2", Manager.getSystemPrefix("L2T1"), "L2T1");
        assertEquals( "L21", Manager.getSystemPrefix("L21T1"), "L21T1");
    }

    @Test
    public void testGetSystemSuffixOK() {
        assertEquals( "12", Manager.getSystemSuffix("LT12"), "LT12");
        assertEquals( "12", Manager.getSystemSuffix("L2T12"), "L2T12");
        assertEquals( "12", Manager.getSystemSuffix("L21T12"), "L21T12");
    }

    @Test
    public void testGetSystemPrefixBad() {
        Exception ex = assertThrows( NamedBean.BadSystemNameException.class,
            () -> Manager.getSystemPrefix(""),
            "Should have thrown");
        assertNotNull(ex);
    }

    // test proper coding of constants
    @Test
    public void checkAgainstSwingConstants() {
        assertEquals(javax.swing.event.ListDataEvent.CONTENTS_CHANGED, Manager.ManagerDataEvent.CONTENTS_CHANGED);
        assertEquals(javax.swing.event.ListDataEvent.INTERVAL_ADDED, Manager.ManagerDataEvent.INTERVAL_ADDED);
        assertEquals(javax.swing.event.ListDataEvent.INTERVAL_REMOVED, Manager.ManagerDataEvent.INTERVAL_REMOVED);
    }

    // test semantics of various collections
    @Test
    public void checkTreeMap() {
        NamedBean n1 = new BogusBean("BB1");
        NamedBean n2 = new BogusBean("BB2");
        NamedBean n3 = new BogusBean("BB3");
        NamedBean n4 = new BogusBean("BB4");
        NamedBean n5 = new BogusBean("BB5");
        new BogusBean("BB5");  // created, but not used directly

        TreeSet<NamedBean> set1 = new TreeSet<>();

        // create facades
        SortedSet<NamedBean> set1s = Collections.synchronizedSortedSet(set1);
        SortedSet<NamedBean> set1u = Collections.unmodifiableSortedSet(set1);

        // add _not_ in order
        set1.add(n3);
        set1.add(n2);
        set1.add(n5);

        // check order
        assertTrue(checkOrderNamedBeans(set1.iterator()));

        assertTrue(checkOrderNamedBeans(set1s.iterator()));

        assertTrue(checkOrderNamedBeans(set1u.iterator()));

        // to check for liveness, add a couple more and test length, first entry
        set1.add(n4);
        set1.add(n1);

        assertEquals(5, set1.size());
        assertEquals(5, set1.size());
        assertEquals(5, set1.size());

        assertEquals(n1, set1.iterator().next());
        assertEquals(n1, set1s.iterator().next());
        assertEquals(n1, set1u.iterator().next());

        // check order
        assertTrue(checkOrderNamedBeans(set1.iterator()));

        assertTrue(checkOrderNamedBeans(set1s.iterator()));

        assertTrue(checkOrderNamedBeans(set1u.iterator()));
    }

    /**
     * Check the test service routines
     */
    @Test
    public void checkCheckOrder() {
        ArrayList<NamedBean> test = new ArrayList<>();
        test.add(new BogusBean("BB2"));
        test.add(new BogusBean("BB3"));
        assertTrue(checkOrderNamedBeans(test.iterator()));

        test.add(new BogusBean("BB5"));
        test.add(new BogusBean("BB1"));
        assertFalse(checkOrderNamedBeans(test.iterator()));

        ArrayList<String> strings = new ArrayList<>();
        strings.add("2");
        strings.add("3");
        assertTrue(checkOrderStrings(strings.iterator()));

        strings.add("5");
        strings.add("1");
        assertFalse(checkOrderStrings(strings.iterator()));
    }

    /**
     * Service routine
     *
     * @param iter set of entries
     * @return true when all entries are in correct order, false otherwise
     */
    boolean checkOrderNamedBeans(Iterator<NamedBean> iter) {
        NamedBean first = iter.next();
        NamedBean next;
        jmri.util.NamedBeanComparator<NamedBean> comp = new jmri.util.NamedBeanComparator<>();
        while (iter.hasNext()) {
            next = iter.next();
            if (comp.compare(first, next) >= 0) return false;
            first = next;
        }
        return true;
    }

    /**
     * Service routine
     *
     * @param iter set of entries
     * @return true when all entries are in correct order, false otherwise
     */
    boolean checkOrderStrings(Iterator<String> iter) {
        String first = iter.next();
        String next;
        while (iter.hasNext()) {
            next = iter.next();
            if (first.compareTo(next) >= 0) return false;
            first = next;
        }
        return true;
    }

    static class BogusBean extends jmri.implementation.AbstractNamedBean {
        public BogusBean(String n) { super(n); }
        @Override
        public int getState() { return -1; }
        @Override
        public String getBeanType() { return "";}
        @Override
        public void setState(int i) {}
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();

        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ManagerTest.class);

}
