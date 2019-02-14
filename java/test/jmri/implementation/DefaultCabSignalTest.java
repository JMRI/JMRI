package jmri.implementation;

import jmri.util.JUnitUtil;
import jmri.DccLocoAddress;
import org.junit.*;

/**
 * Tests for DefaultCabSignal
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class DefaultCabSignalTest {

    @Test
    public void testCTor() {
        DefaultCabSignal cs = new DefaultCabSignal(new DccLocoAddress(1234,true));
        Assert.assertNotNull("exists",cs);
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

}
