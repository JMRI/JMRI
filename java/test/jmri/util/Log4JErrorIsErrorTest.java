package jmri.util;

import org.junit.jupiter.api.*;

/**
 * Works with the JUnitAppender class to 
 * fail if any FATAL or ERROR messages have been
 * emitted (e.g. not expected)
 *
 * @author Bob Jacobsen Copyright 2016
 */
public class Log4JErrorIsErrorTest {

    @Test
    public void testNoLog4JMessages() {
        Assertions.assertFalse( jmri.util.JUnitAppender.unexpectedMessageSeen(org.slf4j.event.Level.ERROR),
            "Unexpected ERROR or FATAL messages emitted");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetWindows( false,false); //this shouldn't be necessary.
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Log4JErrorIsErrorTest.class);
}
