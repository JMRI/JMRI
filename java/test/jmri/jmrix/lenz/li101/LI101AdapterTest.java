package jmri.jmrix.lenz.li101;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LI101AdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.li101.LI101Adapter class
 *
 * @author	Paul Bender
 */
public class LI101AdapterTest {

    @Test
    public void testCtor() {
        LI101Adapter a = new LI101Adapter();
        Assert.assertNotNull(a);
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

}
