package jmri.jmrix.dccpp.dccppovertcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
    @Ignore("Test in superclass hangs with DCCppPacketizer")
    public void testOutbound() throws Exception {
    }

    @Test
    @Override
    @Ignore("Test in superclass generates an exception with DCCppPacketizer")
    public void testInbound() throws Exception {
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        tc = new DCCppOverTcpPacketizer(new jmri.jmrix.dccpp.DCCppCommandStation());
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
