package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * XNetMessageExceptionTest.java
 *
 * Description: tests for the jmri.jmrix.lenz.XNetMessageException class
 *
 * @author Paul Bender
 */
public class XNetMessageExceptionTest {

    @Test
    public void testCtor() {

        XNetMessageException c = new XNetMessageException();
        Assert.assertNotNull(c);
    }

    @Test
    public void testStringCtor() {

        XNetMessageException c = new XNetMessageException("Test Exception");
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
