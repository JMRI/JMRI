package jmri;

import java.util.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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
        Assert.assertEquals("LT1", 1, Manager.getSystemPrefixLength("LT1"));
        Assert.assertEquals("L2T1", 2, Manager.getSystemPrefixLength("L2T1"));
        Assert.assertEquals("L21T1", 3, Manager.getSystemPrefixLength("L21T1"));
    }

    @Test
    public void testGetSystemPrefixLengthThrow1() {
        try {
            Manager.getSystemPrefixLength(".T1");
        } catch (NamedBean.BadSystemNameException e) { 
            return; // OK
        }
        Assert.fail("Should have thrown");
    }
    
    @Test
    public void testGetSystemPrefixLengthThrow2() {
        try {
            Manager.getSystemPrefixLength("1T1");
        } catch (NamedBean.BadSystemNameException e) { 
            return; // OK
        }
        Assert.fail("Should have thrown");
    }


    @Test
    public void testGetSystemPrefixLengthBad() {
        try {
            Assert.assertEquals("LT1", 0, Manager.getSystemPrefixLength(""));
        } catch (NamedBean.BadSystemNameException e) {
            return; // OK
        }
        Assert.fail("should have thrown");
    }

    @Test
    public void testGetSystemPrefixOK() {
        Assert.assertEquals("LT1", "L", Manager.getSystemPrefix("LT1"));
        Assert.assertEquals("L2T1", "L2", Manager.getSystemPrefix("L2T1"));
        Assert.assertEquals("L21T1", "L21", Manager.getSystemPrefix("L21T1"));
    }


    @Test
    public void testGetSystemPrefixBad() {
        try {
            Assert.assertEquals("LT1", "L", Manager.getSystemPrefix(""));
        } catch (NamedBean.BadSystemNameException e) {
            return; // OK
        }
        Assert.fail("should have thrown");
    }
    
    // test proper coding of constants
    @Test
    public void checkAgainstSwingConstants() {
        Assert.assertEquals(javax.swing.event.ListDataEvent.CONTENTS_CHANGED, Manager.ManagerDataEvent.CONTENTS_CHANGED);
        Assert.assertEquals(javax.swing.event.ListDataEvent.INTERVAL_ADDED, Manager.ManagerDataEvent.INTERVAL_ADDED);
        Assert.assertEquals(javax.swing.event.ListDataEvent.INTERVAL_REMOVED, Manager.ManagerDataEvent.INTERVAL_REMOVED);
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
        Assert.assertTrue(checkOrderNamedBeans(set1.iterator()));

        Assert.assertTrue(checkOrderNamedBeans(set1s.iterator()));

        Assert.assertTrue(checkOrderNamedBeans(set1u.iterator()));
        
        // to check for liveness, add a couple more and test length, first entry
        set1.add(n4);
        set1.add(n1);
        
        Assert.assertEquals(5, set1.size());
        Assert.assertEquals(5, set1.size());
        Assert.assertEquals(5, set1.size());
        
        Assert.assertEquals(n1, set1.iterator().next());
        Assert.assertEquals(n1, set1s.iterator().next());
        Assert.assertEquals(n1, set1u.iterator().next());
        
        // check order
        Assert.assertTrue(checkOrderNamedBeans(set1.iterator()));

        Assert.assertTrue(checkOrderNamedBeans(set1s.iterator()));

        Assert.assertTrue(checkOrderNamedBeans(set1u.iterator()));        
    }

    /**
     * Check the test service routines
     */
    @Test
    public void checkCheckOrder() {
        ArrayList<NamedBean> test = new ArrayList<>();
        test.add(new BogusBean("BB2"));
        test.add(new BogusBean("BB3"));
        Assert.assertTrue(checkOrderNamedBeans(test.iterator()));

        test.add(new BogusBean("BB5"));
        test.add(new BogusBean("BB1"));
        Assert.assertFalse(checkOrderNamedBeans(test.iterator()));

        ArrayList<String> strings = new ArrayList<>();
        strings.add("2");
        strings.add("3");
        Assert.assertTrue(checkOrderStrings(strings.iterator()));

        strings.add("5");
        strings.add("1");
        Assert.assertFalse(checkOrderStrings(strings.iterator()));
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
