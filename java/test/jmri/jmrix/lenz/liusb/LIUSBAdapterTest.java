package jmri.jmrix.lenz.liusb;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * LIUSBAdapterTest.java
 *
 * Test for the jmri.jmrix.lenz.liusb.LIUSBAdapter class
 *
 * @author Paul Bender
 */
public class LIUSBAdapterTest {

    @Test
    public void testCtor() {
        LIUSBAdapter a = new LIUSBAdapter();
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
