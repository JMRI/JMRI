package jmri.jmrix.openlcb;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the OlcbSignalMast implementation
 *
 * @author	Bob Jacobsen Copyright (C) 2013, 2017, 2018
 * updated to JUnit4 2016
 */
public class OlcbSignalMastTest {

    @Test
    public void testCtor1() {
        OlcbSignalMast s = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");

        Assert.assertEquals("system name", "MF$olm:AAR-1946:PL-1-high-abs(1)", s.getSystemName());
    }

    @Test
    public void testStopAspect() {
        OlcbSignalMast s = new OlcbSignalMast("MF$olm:AAR-1946:PL-1-high-abs(1)");
        s.setOutputForAppearance("Stop", "1.2.3.4.5.6.7.8");

        Assert.assertEquals("Stop aspect event", "x0102030405060708", s.getOutputForAppearance("Stop"));
    }

    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        
        new OlcbSystemConnectionMemo(); // this self-registers as 'M'
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
