package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * XNetExceptionTest.java
 *
 * Description: tests for the jmri.jmrix.lenz.XNetException class
 *
 * @author Paul Bender
 */
public class XNetExceptionTest {

    @Test
    public void testCtor() {

        XNetException c = new XNetException("Test Exception");
        Assert.assertNotNull(c);
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
