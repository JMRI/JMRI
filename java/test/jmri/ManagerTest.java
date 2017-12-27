package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
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
