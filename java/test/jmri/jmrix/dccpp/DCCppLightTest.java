package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the {@link jmri.jmrix.dccpp.DCCppLight} class.
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 */
public class DCCppLightTest extends jmri.implementation.AbstractLightTestBase {

    @Override
    public int numListeners() {
        return xnis.numListeners();
    }

    DCCppInterfaceScaffold xnis;

    @Override
    public void checkOnMsgSent() {
        Assert.assertEquals("ON message", "a 6 0 1",
                xnis.outbound.elementAt(xnis.outbound.size() - 1).toString());
        Assert.assertEquals("ON state", jmri.Light.ON, t.getState());
    }

    @Override
    public void checkOffMsgSent() {
        Assert.assertEquals("OFF message", "a 6 0 0",
                xnis.outbound.elementAt(xnis.outbound.size() - 1).toString());
        Assert.assertEquals("OFF state", jmri.Light.OFF, t.getState());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        DCCppLightManager xlm = new DCCppLightManager(xnis, "DCCpp");

        t = new DCCppLight(xnis, xlm, "DCCppL21");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
