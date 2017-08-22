package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.apache.log4j.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.util.*;
import java.awt.GraphicsEnvironment;
import apps.tests.Log4JFixture;

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
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
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
