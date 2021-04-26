package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * XNetMessageExceptionTest.java
 *
 * Test for the jmri.jmrix.lenz.XNetMessageException class
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
