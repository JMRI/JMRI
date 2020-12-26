package jmri.jmrix.rps;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RpsBlockTest.class);

}
