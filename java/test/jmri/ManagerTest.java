package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the static methods of the interface
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
    public void testIsLegacySystemPrefix() {
        Assert.assertTrue(Manager.isLegacySystemPrefix("DX"));
        Assert.assertTrue(Manager.isLegacySystemPrefix("DCCPP"));
        Assert.assertTrue(Manager.isLegacySystemPrefix("DP"));
        Assert.assertTrue(Manager.isLegacySystemPrefix("json"));
        
        Assert.assertFalse(Manager.isLegacySystemPrefix("C"));
        Assert.assertFalse(Manager.isLegacySystemPrefix("C2"));
        Assert.assertFalse(Manager.isLegacySystemPrefix("D"));
        
        for (String s : Manager.legacyPrefixes.toArray(new String[0])) {
            Assert.assertTrue(Manager.isLegacySystemPrefix(s));
        }
    }
    
    // Test legacy prefixes
    @Deprecated
    public void testLegacyPrefixes() {
        // catch if this is changed, so we remember to change
        // rest of tests
        Assert.assertEquals("length of legacy set", 8, Manager.legacyPrefixes.toArray().length);
    }

    // Test legacy prefixes
    @Deprecated
    public void startsWithLegacySystemPrefix() {
        Assert.assertEquals(2, Manager.startsWithLegacySystemPrefix("DXS1"));
        Assert.assertEquals(5, Manager.startsWithLegacySystemPrefix("DCCPPT4"));
        Assert.assertEquals(2, Manager.startsWithLegacySystemPrefix("DPS12"));
        Assert.assertEquals(4, Manager.startsWithLegacySystemPrefix("jsonL1"));
        
        Assert.assertEquals(-1, Manager.startsWithLegacySystemPrefix("CT1"));
        Assert.assertEquals(-1, Manager.startsWithLegacySystemPrefix("C2T12"));
        Assert.assertEquals(-1, Manager.startsWithLegacySystemPrefix("DT12132"));
        
        for (String s : Manager.legacyPrefixes.toArray(new String[0])) {
            Assert.assertEquals(s.length()+3, Manager.startsWithLegacySystemPrefix(s+"T12"));
        }
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
