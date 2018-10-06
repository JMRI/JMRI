package jmri.implementation;

import jmri.Conditional;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JmriMemoryPropertyListenerTest {

    @Test
    public void testCTor() {
        JmriMemoryPropertyListener t =
                new JmriMemoryPropertyListener("foo",0,"bar",
                        Conditional.Type.SENSOR_ACTIVE,new DefaultConditional("foo"),"test");
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

    // private final static Logger log = LoggerFactory.getLogger(JmriMemoryPropertyListenerTest.class);

}
