package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.roco.z21.z21Adapter class
 *
 * @author Paul Bender
 */
public class Z21AdapterTest {

    @Test
    public void testCtor() {
        Z21Adapter a = new Z21Adapter();
        Assert.assertNotNull(a);
        a.dispose();
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
