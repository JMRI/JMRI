package jmri.jmrit.log;

import java.util.*;
import jmri.util.JTextPaneAppender;
import jmri.util.JUnitUtil;
import org.apache.log4j.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LogOutputWindowActionTest {

    @Test
    public void testCTor() {
        LogOutputWindowAction t = new LogOutputWindowAction("Test");
        Assert.assertNotNull("exists",t);
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
        
        JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

}
