package jmri.jmrix.lenz.li101;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * LI101AdapterTest.java
 *
 * Test for the jmri.jmrix.lenz.li101.LI101Adapter class
 *
 * @author Paul Bender
 */
public class LI101AdapterTest {

    @Test
    public void testCtor() {
        LI101Adapter a = new LI101Adapter();
        Assert.assertNotNull(a);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
