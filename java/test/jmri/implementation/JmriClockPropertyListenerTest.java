package jmri.implementation;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JmriClockPropertyListenerTest {

    @Test
    public void testCTor() {
        JmriClockPropertyListener t = new JmriClockPropertyListener("foo",0,"bar",1,new DefaultConditional("foo"),0,0);
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

    // private final static Logger log = LoggerFactory.getLogger(JmriClockPropertyListenerTest.class);

}
