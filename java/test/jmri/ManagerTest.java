package jmri;

import java.util.*;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the static methods of the interface.
 * 
 * Detailed tests are in jmri.managers.AbstractManagerTestBase with even more
 * detailed tests (which require beans, etc) in jmri.managers.InternalSensorManagerTest
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

    // Test legacy prefixes
    @Deprecated
    @Test
    public void testGetSystemPrefixLengthLegacyPrefixes() {
        Assert.assertEquals("DCCPPT12", 5, Manager.getSystemPrefixLength("DCCPPT12"));
        Assert.assertEquals("MRT13", 2, Manager.getSystemPrefixLength("MRT13"));
        Assert.assertEquals("DXT512", 2, Manager.getSystemPrefixLength("DXT512"));
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

    // Test legacy prefixes
    @Deprecated
    @Test
    public void testGetSystemPrefixLegacyPrefixes() {
        Assert.assertEquals("DCCPPT12", "DCCPP", Manager.getSystemPrefix("DCCPPT12"));
        Assert.assertEquals("MRT13", "MR", Manager.getSystemPrefix("MRT13"));
        Assert.assertEquals("DXT512", "DX", Manager.getSystemPrefix("DXT512"));
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
    
    // Test legacy prefixes
    @Deprecated
    @Test
    public void testIsLegacySystemPrefix() {
        Assert.assertTrue(Manager.isLegacySystemPrefix("DX"));
        Assert.assertTrue(Manager.isLegacySystemPrefix("DCCPP"));
        Assert.assertTrue(Manager.isLegacySystemPrefix("DP"));
        
        Assert.assertFalse(Manager.isLegacySystemPrefix("C"));
        Assert.assertFalse(Manager.isLegacySystemPrefix("C2"));
        Assert.assertFalse(Manager.isLegacySystemPrefix("D"));
        
        for (String s : Manager.LEGACY_PREFIXES.toArray(new String[0])) {
            Assert.assertTrue(Manager.isLegacySystemPrefix(s));
        }
    }
    
    // Test legacy prefixes
    @Deprecated
    @Test
    public void testLegacyPrefixes() {
        // catch if this is changed, so we remember to change
        // rest of tests
        Assert.assertEquals("length of legacy set", 7, Manager.LEGACY_PREFIXES.toArray().length);
    }

    // Test legacy prefixes
    @Deprecated
    @Test
    public void startsWithLegacySystemPrefix() {
        Assert.assertEquals(2, Manager.startsWithLegacySystemPrefix("DXS1"));
        Assert.assertEquals(5, Manager.startsWithLegacySystemPrefix("DCCPPT4"));
        Assert.assertEquals(2, Manager.startsWithLegacySystemPrefix("DPS12"));
        
        Assert.assertEquals(-1, Manager.startsWithLegacySystemPrefix("CT1"));
        Assert.assertEquals(-1, Manager.startsWithLegacySystemPrefix("C2T12"));
        Assert.assertEquals(-1, Manager.startsWithLegacySystemPrefix("DT12132"));

        Assert.assertEquals(-1, Manager.startsWithLegacySystemPrefix(""));
        Assert.assertEquals(-1, Manager.startsWithLegacySystemPrefix("X"));
        Assert.assertEquals(-1, Manager.startsWithLegacySystemPrefix("-"));
        
        for (String s : Manager.LEGACY_PREFIXES.toArray(new String[0])) {
            Assert.assertEquals("Length test of \""+s+"\"",s.length(), Manager.startsWithLegacySystemPrefix(s+"T12"));
        }
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
     * @return true when all entries are in correct order, false otherwise
     */
    boolean checkOrderNamedBeans(Iterator<NamedBean> iter) {
        NamedBean first = iter.next();
        NamedBean next;
        jmri.util.NamedBeanComparator comp = new jmri.util.NamedBeanComparator();
        while (iter.hasNext()) {
            next = iter.next();
            if (comp.compare(first, next) >= 0) return false;
            first = next;
        }
        return true;
    }
    
    /**
     * Service routine
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
        public int getState() { return -1; }
        public String getBeanType() { return "";}
        public void setState(int i) {}
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ManagerTest.class);

}
