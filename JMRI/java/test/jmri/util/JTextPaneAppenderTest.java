package jmri.util;

import apps.tests.Log4JFixture;
import java.util.*;
import org.apache.log4j.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        JUnitUtil.setUp();
    }

    @After
    @SuppressWarnings("unchecked") // cast required by logging APIs
    public void tearDown() {        
        // remove any JTextPaneAppender objects that 
        // have been added to logging
        Enumeration<Object> en = LogManager.getCurrentLoggers();

        while (en.hasMoreElements()) {
            Object o = en.nextElement();

            if (o instanceof Logger) {
                Logger logger = (Logger) o;
                Enumeration<Appender> appenders = logger.getAllAppenders();
                while (appenders.hasMoreElements()) {
                    Appender a = appenders.nextElement();
                    if (a instanceof JTextPaneAppender) {
                        logger.removeAppender(a);
                    }                        
                }
            } // if o instanceof Logger

        } // while ( en )

        Enumeration<Appender> appenders = LogManager.getRootLogger().getAllAppenders();
        while (appenders.hasMoreElements()) {
            Appender a = appenders.nextElement();
            if (a instanceof JTextPaneAppender) {
                LogManager.getRootLogger().removeAppender(a);
            }                        
        }
        
        jmri.util.JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }

}
