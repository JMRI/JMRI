package jmri.jmrit.swing.meter;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * copied from ampmeter
 * @author Andrew Crosland Copyright (C) 2020
 */
public class MeterActionTest {

    @Test
    public void testCTor() {
        MeterAction t = new MeterAction();
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
