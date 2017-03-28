package jmri.jmrit.beantable;

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
public class SetPhysicalLocationActionTest {

    @Test
    public void testCTor() {
        jmri.implementation.AbstractReporter r = new jmri.implementation.AbstractReporter("foo","bar") {
           @Override
           public int getState() {
              return state;
           }

           @Override
           public void setState(int s) {
              state = s;
           }
           int state = 0;
        };

        SetPhysicalLocationAction t = new SetPhysicalLocationAction("Test",r);
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

    private final static Logger log = LoggerFactory.getLogger(SetPhysicalLocationActionTest.class.getName());

}
