package jmri.jmrit.voltmeter;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * copied from ampmeter
 * @author Andrew Crosland Copyright (C) 2020
 */
public class VoltMeterActionTest {

    @Test
    public void testCTor() {
        VoltMeterAction t = new VoltMeterAction();
        Assert.assertNotNull("exists",t);
        // there is no Meter registered, make sure the action is
        // disabled.
        Assert.assertFalse(t.isEnabled());
    }

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
