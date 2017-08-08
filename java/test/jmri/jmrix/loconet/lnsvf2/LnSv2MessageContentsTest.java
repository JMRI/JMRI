package jmri.jmrix.loconet.lnsvf2;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.LocoNetMessage;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LnSv2MessageContentsTest {

    @Test(expected = IllegalArgumentException.class )
    public void testCTorIllegalArgument() {
        LocoNetMessage lm = new LocoNetMessage(3);
        // lm is not the right message type, so the constructor is supposed
        // to throw an IllegalArgumentException
        LnSv2MessageContents t = new LnSv2MessageContents(lm);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LnSv2MessageContentsTest.class.getName());

}
