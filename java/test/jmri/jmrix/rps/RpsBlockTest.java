package jmri.jmrix.rps;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RpsBlockTest {

    @Test
    public void testCTor() {
        RpsSensor s = new RpsSensor("RS(0,0,0);(1,0,0);(1,1,0);(0,1,0)", "R");
        jmri.SignalHead sh = new jmri.implementation.VirtualSignalHead("TS1", "test signal head");
        RpsBlock t = new RpsBlock(s, sh, 0.0f, 1.0f);
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsBlockTest.class);

}
