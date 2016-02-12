// DCCppTurnoutTest.java
package jmri.jmrix.dccpp;

import junit.framework.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link jmri.jmrix.dccpp.DCCppLight} class.
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 * @version $Revision$
 */
public class DCCppLightTest extends jmri.implementation.AbstractLightTest {

    public int numListeners() {
        return xnis.numListeners();
    }

    DCCppInterfaceScaffold xnis;

    public void checkOnMsgSent() {
        Assert.assertEquals("ON message", "a 5 0 1",
                xnis.outbound.elementAt(xnis.outbound.size() - 1).toString());
        Assert.assertEquals("ON state", jmri.Light.ON, t.getState());
    }

    public void checkOffMsgSent() {
        Assert.assertEquals("OFF message", "a 5 0 0",
                xnis.outbound.elementAt(xnis.outbound.size() - 1).toString());
        Assert.assertEquals("OFF state", jmri.Light.OFF, t.getState());
    }

    // from here down is testing infrastructure
    public DCCppLightTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        DCCppLightManager xlm = new DCCppLightManager(xnis, "DCCpp");

        t = new DCCppLight(xnis, xlm, "DCCppL21");
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppLightTest.class.getName());

}
