package jmri.jmrix.lenz.liusbethernet;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * LIUSBEthernetAdapterTest.java
 *
 * Test for the jmri.jmrix.lenz.liusbethernet.LIUSBEthernetAdapter
 * class
 *
 * @author Paul Bender
 */
public class LIUSBEthernetAdapterTest {

    @Test
    public void testCtor() {
        LIUSBEthernetAdapter a = new LIUSBEthernetAdapter();
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
