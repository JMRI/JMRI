package jmri.jmrit.ampmeter;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AmpMeterActionTest {

    @Test
    public void testCTor() {
        AmpMeterAction t = new AmpMeterAction();
        Assert.assertNotNull("exists",t);
        // there is no Meter registered, make sure the action is
	// disabled.
	Assert.assertFalse(t.isEnabled());
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

    // private final static Logger log = LoggerFactory.getLogger(AmpMeterActionTest.class);

}
