package jmri.jmrix.xpa;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * XpaTrafficControllerTest.java
 *
 * Description: tests for the jmri.jmrix.xpa.XpaTrafficController class
 *
 * @author Paul Bender
 */
public class XpaTrafficControllerTest {

    @Test
    public void testCtor() {
        XpaTrafficController t = new XpaTrafficController();
        Assert.assertNotNull(t);
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
