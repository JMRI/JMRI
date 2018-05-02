package jmri.util;

import apps.tests.Log4JFixture;
import java.awt.GraphicsEnvironment;
import java.util.*;
import org.apache.log4j.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JLogoutputFrameTest {

    @Test
    public void testGetInstance() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JLogoutputFrame t = JLogoutputFrame.getInstance();
        Assert.assertNotNull("exists", t);
        t.getMainFrame().dispose();
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
                Enumeration<Appender> apps = logger.getAllAppenders();
                while (apps.hasMoreElements()) {
                    Appender a = apps.nextElement();
                    if (a instanceof JTextPaneAppender) {
                        logger.removeAppender(a);
                    }
                }
            } // if o instanceof Logger

        } // while ( en )

        Enumeration<Appender> apps = LogManager.getRootLogger().getAllAppenders();
        while (apps.hasMoreElements()) {
            Appender a = apps.nextElement();
            if (a instanceof JTextPaneAppender) {
                LogManager.getRootLogger().removeAppender(a);
            }
        }

        jmri.util.JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
