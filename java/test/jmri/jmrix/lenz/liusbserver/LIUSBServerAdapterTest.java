package jmri.jmrix.lenz.liusbserver;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.lenz.liusbserver.LIUSBServerAdapter
 * class.
 *
 * @author	Paul Bender
 */
public class LIUSBServerAdapterTest {

    @Test
    public void testCtor() {
        LIUSBServerAdapter a = new LIUSBServerAdapter();
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
