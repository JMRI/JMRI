package jmri.util;

import java.awt.GraphicsEnvironment;
import java.util.*;

import org.apache.log4j.*;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
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
        jmri.util.JUnitUtil.tearDown();
    }
}
