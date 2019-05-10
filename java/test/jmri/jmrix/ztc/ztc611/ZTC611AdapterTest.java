package jmri.jmrix.ztc.ztc611;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * ZTC611AdapterTest.java
 *
 * Description: tests for the jmri.jmrix.lenz.ztc611.ZTC611Adapter class
 *
 * @author Paul Bender
 */
public class ZTC611AdapterTest {

    @Test
    public void testCtor() {
        ZTC611Adapter a = new ZTC611Adapter();
        Assert.assertNotNull(a);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
