package jmri.jmrix.ztc.ztc611;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * ZTC611AdapterTest.java
 *
 * Test for the jmri.jmrix.lenz.ztc611.ZTC611Adapter class
 *
 * @author Paul Bender
 */
public class ZTC611AdapterTest {

    @Test
    public void testCtor() {
        ZTC611Adapter a = new ZTC611Adapter();
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
