package jmri.jmrix.lenz.liusbserver;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.lenz.liusbserver.LIUSBServerAdapter
 * class.
 *
 * @author Paul Bender
 */
public class LIUSBServerAdapterTest {

    @Test
    public void testCtor() {
        LIUSBServerAdapter a = new LIUSBServerAdapter();
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
