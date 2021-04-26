package jmri.jmrix.dccpp.network;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

/**
 * <p>
 * Title: DCCppEthernetPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class DCCppEthernetPacketizerTest extends jmri.jmrix.dccpp.DCCppPacketizerTest {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppEthernetPacketizer(new jmri.jmrix.dccpp.DCCppCommandStation()) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
    }

    @AfterEach
    @Override
    public void tearDown() {
        tc.terminateThreads();
        JUnitUtil.tearDown();
    }

}
