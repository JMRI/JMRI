package jmri.implementation;

import jmri.Conditional;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmriSimplePropertyListenerTest {

    @Test
    public void testCTor() {
        JmriSimplePropertyListener t =
                new JmriSimplePropertyListener("foo",0,"bar",
                        Conditional.Type.SENSOR_ACTIVE,new DefaultConditional("foo"));
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriSimplePropertyListenerTest.class);

}
