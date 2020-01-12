package jmri.jmrix.lenz.ztc640;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * ZTC640AdapterTest.java
 *
 * Description: tests for the jmri.jmrix.lenz.ztc640.ZTC640Adapter class
 *
 * @author Paul Bender
 */
public class ZTC640AdapterTest {

    @Test
    public void testCtor() {
        ZTC640Adapter a = new ZTC640Adapter();
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
