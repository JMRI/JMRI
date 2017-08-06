package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JTextPaneAppenderTest {

    @Test
    public void testCtor() {
        JTextPaneAppender t = new JTextPaneAppender();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testAppend() {
        org.apache.log4j.Layout myLayout = new org.apache.log4j.PatternLayout("%d{HH:mm:ss.SSS} (%6r) %-5p [%-7t] %F:%L %x - %m%n");

        JTextPaneAppender t = new JTextPaneAppender(myLayout, "name", null, new javax.swing.JTextPane());
        
        t.append(new org.apache.log4j.spi.LoggingEvent(
                "jmri.util.JTextPaneAppenderTest",
                org.apache.log4j.Logger.getLogger("jmri.util.JTextPaneAppenderTest"),
                org.apache.log4j.Priority.DEBUG,
                "Test message", 
                new Exception("Test exception")
            ));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(JTextPaneAppenderTest.class.getName());

}
