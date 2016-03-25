// XNetTurnoutTest.java
package jmri.jmrix.lenz;

import junit.framework.Assert;

/**
 * Tests for the {@link jmri.jmrix.lenz.XNetLight} class.
 *
 * @author	Paul Bender
 */
public class XNetLightTest extends jmri.implementation.AbstractLightTest {

    public int numListeners() {
        return xnis.numListeners();
    }

    XNetInterfaceScaffold xnis;

    public void checkOnMsgSent() {
        Assert.assertEquals("ON message", "52 05 80 D7",
                xnis.outbound.elementAt(xnis.outbound.size() - 1).toString());
        Assert.assertEquals("ON state", jmri.Light.ON, t.getState());
    }

    public void checkOffMsgSent() {
        Assert.assertEquals("OFF message", "52 05 81 D6",
                xnis.outbound.elementAt(xnis.outbound.size() - 1).toString());
        Assert.assertEquals("OFF state", jmri.Light.OFF, t.getState());
    }

    // from here down is testing infrastructure
    public XNetLightTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface
        xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetLightManager xlm = new XNetLightManager(xnis, "X");

        t = new XNetLight(xnis, xlm, "XL21");
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
