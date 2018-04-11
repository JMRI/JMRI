package jmri.jmrix.loconet.streamport;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for jmri.jmrix.loconet.streamport.StreamPortPacketizer
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2018
 */
public class LnStreamPortPacketizerTest extends jmri.jmrix.loconet.LnPacketizerTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnp = new LnStreamPortPacketizer();
    }

    @Override
    @After
    public void tearDown() {
        lnp = null;
        JUnitUtil.tearDown();
    }
}
