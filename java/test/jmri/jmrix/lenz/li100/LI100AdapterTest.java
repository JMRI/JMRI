package jmri.jmrix.lenz.li100;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * LI100AdapterTest.java
 *
 * Description: tests for the jmri.jmrix.lenz.li100.LI100Adapter class
 *
 * @author Paul Bender
 */
public class LI100AdapterTest {

    @Test
    public void testCtor() {
        LI100Adapter a = new LI100Adapter();
        Assert.assertNotNull("exists", a);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
