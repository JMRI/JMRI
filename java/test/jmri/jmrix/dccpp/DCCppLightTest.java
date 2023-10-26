package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the {@link jmri.jmrix.dccpp.DCCppLight} class.
 *
 * @author Paul Bender
 * @author Mark Underwood (C) 2015
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

}
