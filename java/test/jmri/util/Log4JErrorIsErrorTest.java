package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Works with the JUnitAppender class to 
 * fail if any FATAL or ERROR messages have been
 * emitted (e.g. not expected)
 *
 * @author	Bob Jacobsen Copyright 2016
 */
public class Log4JErrorIsErrorTest {

    @Test
    public void testNoLog4JMessages() {
        Assert.assertFalse("Unexpected ERROR or FATAL messages emitted", jmri.util.JUnitAppender.unexpectedMessageSeen(org.apache.log4j.Level.ERROR));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows( false,false); //this shouldn't be necessary.
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Log4JErrorIsErrorTest.class);
}
