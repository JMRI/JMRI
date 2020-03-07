package jmri.jmrix.lenz.lzv200;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * LZV200AdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.lzv200.LZV200Adapter class
 *
 * @author	Paul Bender
 */
public class LZV200AdapterTest {

    @Test
    public void testCtor() {
        LZV200Adapter a = new LZV200Adapter();
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
