package jmri.jmrix.dccpp.network;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * <p>
 * Title: DCCppEthernetPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class DCCppEthernetPacketizerTest extends jmri.jmrix.dccpp.DCCppPacketizerTest {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppEthernetPacketizer(new jmri.jmrix.dccpp.DCCppCommandStation()) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
    }

    @After
    @Override
    public void tearDown() {
        tc.terminateThreads();
        JUnitUtil.tearDown();
    }

}
