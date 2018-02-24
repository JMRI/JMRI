package jmri.jmrix.loconet.lnsvf2;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LnSv2MessageContentsTest {

    @Test(expected = IllegalArgumentException.class )
    public void testCTorIllegalArgument() {
        LocoNetMessage lm = new LocoNetMessage(3);
        new LnSv2MessageContents(lm);
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

    // private final static Logger log = LoggerFactory.getLogger(LnSv2MessageContentsTest.class);

}
