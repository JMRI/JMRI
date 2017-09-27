package jmri.jmrix.dccpp.serial;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * <p>
 * Title: SerialDCCppPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class SerialDCCppPacketizerTest extends jmri.jmrix.dccpp.DCCppPacketizerTest {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new SerialDCCppPacketizer(new jmri.jmrix.dccpp.DCCppCommandStation()) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
