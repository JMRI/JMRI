package jmri.jmrix.lenz.li100;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * LI100AdapterTest.java
 *
 * Test for the jmri.jmrix.lenz.li100.LI100Adapter class
 *
 * @author Paul Bender
 */
public class LI100AdapterTest {

    @Test
    public void testCtor() {
        LI100Adapter a = new LI100Adapter();
        Assert.assertNotNull("exists", a);
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
