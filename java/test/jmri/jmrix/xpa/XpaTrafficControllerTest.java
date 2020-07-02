package jmri.jmrix.xpa;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * XpaTrafficControllerTest.java
 *
 * Test for the jmri.jmrix.xpa.XpaTrafficController class
 *
 * @author Paul Bender
 */
public class XpaTrafficControllerTest {

    @Test
    public void testCtor() {
        XpaTrafficController t = new XpaTrafficController();
        Assert.assertNotNull(t);
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
