package jmri.jmrix.lenz.liusb;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * LIUSBAdapterTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.liusb.LIUSBAdapter class
 *
 * @author	Paul Bender
 */
public class LIUSBAdapterTest {

    @Test
    public void testCtor() {
        LIUSBAdapter a = new LIUSBAdapter();
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
