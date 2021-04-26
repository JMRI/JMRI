package jmri.jmrix.dccpp.serial;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * <p>
 * Title: SerialDCCppPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class SerialDCCppPacketizerTest extends jmri.jmrix.dccpp.DCCppPacketizerTest {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SerialDCCppPacketizer(new jmri.jmrix.dccpp.DCCppCommandStation()) {
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
