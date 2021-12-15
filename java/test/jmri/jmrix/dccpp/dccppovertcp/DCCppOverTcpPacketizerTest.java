package jmri.jmrix.dccpp.dccppovertcp;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

/**
 * <p>
 * Title: DCCppOverTcpPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class DCCppOverTcpPacketizerTest extends jmri.jmrix.dccpp.DCCppPacketizerTest {

    @Test
    @Override
    @Disabled("Test in superclass hangs with DCCppPacketizer")
    // TODO: correct initialization and remove this overriden test so that parent class test can run or reimplement test so that it works with DCCppPacketizer
    public void testOutbound() throws Exception {
    }

    @Test
    @Override
    @Disabled("Test in superclass generates an exception with DCCppPacketizer")
    // TODO: investigate failure in parent class test and make corrections, either to initialization or to this overriden test
    public void testInbound() throws Exception {
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.jmrix.dccpp.DCCppSystemConnectionMemo memo = new jmri.jmrix.dccpp.DCCppSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.dccpp.DCCppSystemConnectionMemo.class, memo);
     
        memo.setDCCppTrafficController(new DCCppOverTcpPacketizer(new jmri.jmrix.dccpp.DCCppCommandStation()));
        tc = memo.getDCCppTrafficController();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
