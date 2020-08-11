package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the {@link jmri.jmrix.lenz.XNetLight} class.
 *
 * @author Paul Bender
 */
public class XNetLightTest extends jmri.implementation.AbstractLightTestBase {

    @Override
    public int numListeners() {
        return xnis.numListeners();
    }

    XNetInterfaceScaffold xnis;

    @Override
    public void checkOnMsgSent() {
        Assert.assertEquals("ON message", "52 05 80 D7",
                xnis.outbound.elementAt(xnis.outbound.size() - 1).toString());
        Assert.assertEquals("ON state", jmri.Light.ON, t.getState());
    }

    @Override
    public void checkOffMsgSent() {
        Assert.assertEquals("OFF message", "52 05 81 D6",
                xnis.outbound.elementAt(xnis.outbound.size() - 1).toString());
        Assert.assertEquals("OFF state", jmri.Light.OFF, t.getState());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetLightManager xlm = new XNetLightManager(xnis.getSystemConnectionMemo());

        t = new XNetLight(xnis, xlm, "XL21");
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
