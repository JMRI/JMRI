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
public class AbstractFrameActionTest {

    @Test
    public void testCtor() {
        AbstractFrameAction t = new AbstractFrameAction("TestAction","jmri.util.JmriJFrame"){
        };
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testAction() {
        AbstractFrameAction t = new AbstractFrameAction("TestAction","jmri.util.JmriJFrame"){
        };
        t.actionPerformed(new java.awt.event.ActionEvent(this,1,"test action event")); // set up verifies this does not generate an error.
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
