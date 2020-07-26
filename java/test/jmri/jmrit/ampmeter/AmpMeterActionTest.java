package jmri.jmrit.ampmeter;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AmpMeterActionTest.class);

}
