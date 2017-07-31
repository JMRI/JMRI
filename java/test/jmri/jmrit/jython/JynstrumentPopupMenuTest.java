package jmri.jmrit.jython;

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
public class JynstrumentPopupMenuTest {

    @Test
    public void testCTor() {
        Jynstrument j = new Jynstrument(){
           @Override
           public String getExpectedContextClassName(){
              return "Test Jynstrument";
           }

           @Override
           public void init(){
           }

           protected void quit(){
           }
        };
        JynstrumentPopupMenu t = new JynstrumentPopupMenu(j);
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(JynstrumentPopupMenuTest.class.getName());

}
