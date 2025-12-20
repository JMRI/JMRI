package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.roco.z21.z21Adapter class
 *
 * @author Paul Bender
 */
class Z21AdapterTest {

    @Test
    void testCtor() {
        Z21Adapter a = new Z21Adapter();
        Assert.assertNotNull(a);
        a.dispose();
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
