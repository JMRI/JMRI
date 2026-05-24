package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the {@link jmri.jmrix.dccpp.DCCppLight} class.
 *
 * @author Paul Bender
 * @author Mark Underwood (C) 2015
 * @author Chad Francis (C) 2026
 */
public class DCCppLightTest extends jmri.implementation.AbstractLightTestBase {

    @Override
    public int numListeners() {
        return xnis.numListeners();
    }

    @Override
    public void checkOnMsgSent() {
        Assertions.assertEquals( "a 6 0 1",
                xnis.outbound.elementAt(xnis.outbound.size() - 1).toString(), "ON message");
        Assertions.assertEquals( jmri.Light.ON, t.getState(), "ON state");
    }

    @Override
    public void checkOffMsgSent() {
        Assertions.assertEquals( "a 6 0 0",
                xnis.outbound.elementAt(xnis.outbound.size() - 1).toString(), "OFF message");
        Assertions.assertEquals( jmri.Light.OFF, t.getState(), "OFF state");
    }

    private DCCppInterfaceScaffold xnis = null;
    private DCCppSystemConnectionMemo memo = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(xnis);
        xnis.setSystemConnectionMemo(memo);
        memo.setSystemPrefix("d2");
        DCCppLightManager xlm = new DCCppLightManager(xnis.getSystemConnectionMemo());

        t = new DCCppLight(xnis, xlm, "d2L21");
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // --- CS VPIN mode tests ---

    @Test
    public void testDefaultModeIsStandard() {
        DCCppLight light = (DCCppLight) t;
        Assertions.assertEquals(DCCppLight.STANDARD, light.getMode(), "default mode is STANDARD");
        Assertions.assertEquals("Accessory Decoder", light.getModeName(), "default mode name");
    }

    @Test
    public void testSetModeByConstant() {
        DCCppLight light = (DCCppLight) t;
        light.setMode(DCCppLight.CS_VPIN);
        Assertions.assertEquals(DCCppLight.CS_VPIN, light.getMode(), "mode is CS_VPIN after setMode");
        Assertions.assertEquals("CS VPIN", light.getModeName(), "mode name is CS VPIN");
    }

    @Test
    public void testSetModeByName() {
        DCCppLight light = (DCCppLight) t;
        light.setModeByName("CS VPIN");
        Assertions.assertEquals(DCCppLight.CS_VPIN, light.getMode(), "mode is CS_VPIN after setModeByName");
        light.setModeByName("Accessory Decoder");
        Assertions.assertEquals(DCCppLight.STANDARD, light.getMode(), "mode reverts to STANDARD");
    }

    @Test
    public void testVpinModeOnSendsHighCommand() {
        DCCppLight light = (DCCppLight) t;
        light.setMode(DCCppLight.CS_VPIN);
        light.setState(jmri.Light.ON);
        String msg = xnis.outbound.elementAt(xnis.outbound.size() - 1).toString();
        // <z 21> drives pin HIGH (ON)
        Assertions.assertEquals("z 21", msg, "CS VPIN ON sends <z vpin>");
        Assertions.assertEquals(jmri.Light.ON, light.getState(), "state is ON");
    }

    @Test
    public void testVpinModeOffSendsLowCommand() {
        DCCppLight light = (DCCppLight) t;
        light.setMode(DCCppLight.CS_VPIN);
        light.setState(jmri.Light.ON);  // set to ON first
        light.setState(jmri.Light.OFF);
        String msg = xnis.outbound.elementAt(xnis.outbound.size() - 1).toString();
        // <z -21> drives pin LOW (OFF)
        Assertions.assertEquals("z -21", msg, "CS VPIN OFF sends <z -vpin>");
        Assertions.assertEquals(jmri.Light.OFF, light.getState(), "state is OFF");
    }

    @Test
    public void testValidModeNames() {
        DCCppLight light = (DCCppLight) t;
        String[] names = light.getValidModeNames();
        Assertions.assertEquals(2, names.length, "two valid mode names");
        Assertions.assertEquals("Accessory Decoder", names[0], "first mode name");
        Assertions.assertEquals("CS VPIN", names[1], "second mode name");
    }

}
