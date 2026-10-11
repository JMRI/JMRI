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
        Assertions.assertNull(light.getProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY),
                "default mode property is null (Accessory Decoder)");
    }

    @Test
    public void testSetModeCsVpin() {
        DCCppLight light = (DCCppLight) t;
        light.setProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY, DCCppLight.MODE_NAMES[1]);
        Assertions.assertEquals(DCCppLight.MODE_NAMES[1],
                light.getProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY), "mode is CS VPIN");
    }

    @Test
    public void testSetModeByName() {
        DCCppLight light = (DCCppLight) t;
        light.setProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY, "CS VPIN");
        Assertions.assertEquals("CS VPIN",
                light.getProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY), "mode is CS VPIN");
        light.setProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY, "Accessory Decoder");
        Assertions.assertEquals("Accessory Decoder",
                light.getProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY), "mode reverts to Accessory Decoder");
    }

    @Test
    public void testVpinModeOnSendsHighCommand() {
        DCCppLight light = (DCCppLight) t;
        light.setProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY, DCCppLight.MODE_NAMES[1]);
        light.setState(jmri.Light.ON);
        String msg = xnis.outbound.elementAt(xnis.outbound.size() - 1).toString();
        // <z 21> drives pin HIGH (ON)
        Assertions.assertEquals("z 21", msg, "CS VPIN ON sends <z vpin>");
        Assertions.assertEquals(jmri.Light.ON, light.getState(), "state is ON");
    }

    @Test
    public void testVpinModeOffSendsLowCommand() {
        DCCppLight light = (DCCppLight) t;
        light.setProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY, DCCppLight.MODE_NAMES[1]);
        light.setState(jmri.Light.ON);  // set to ON first
        light.setState(jmri.Light.OFF);
        String msg = xnis.outbound.elementAt(xnis.outbound.size() - 1).toString();
        // <z -21> drives pin LOW (OFF)
        Assertions.assertEquals("z -21", msg, "CS VPIN OFF sends <z -vpin>");
        Assertions.assertEquals(jmri.Light.OFF, light.getState(), "state is OFF");
    }

    @Test
    public void testModeNames() {
        String[] names = DCCppLight.MODE_NAMES;
        Assertions.assertEquals(2, names.length, "two valid mode names");
        Assertions.assertEquals("Accessory Decoder", names[0], "first mode name");
        Assertions.assertEquals("CS VPIN", names[1], "second mode name");
    }

}
