package jmri.jmrix.lenz.ztc640;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * ZTC640AdapterTest.java
 *
 * Test for the jmri.jmrix.lenz.ztc640.ZTC640Adapter class
 *
 * @author Paul Bender
 */
public class ZTC640AdapterTest {

    @Test
    public void testCtor() {
        ZTC640Adapter a = new ZTC640Adapter();
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
