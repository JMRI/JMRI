package jmri.jmrix.lenz.lzv200;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * LZV200AdapterTest.java
 *
 * Test for the jmri.jmrix.lenz.lzv200.LZV200Adapter class
 *
 * @author Paul Bender
 */
public class LZV200AdapterTest {

    @Test
    public void testCtor() {
        LZV200Adapter a = new LZV200Adapter();
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
