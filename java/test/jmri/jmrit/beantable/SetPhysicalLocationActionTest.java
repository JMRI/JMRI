package jmri.jmrit.beantable;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SetPhysicalLocationActionTest.class);

}
