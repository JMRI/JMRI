package jmri.jmrix.loconet.lnsvf2;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnSv2MessageContentsTest {

    @Test
    public void testCTorIllegalArgument() {
        LocoNetMessage lm = new LocoNetMessage(3);
        Assert.assertThrows(IllegalArgumentException.class, () -> new LnSv2MessageContents(lm));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnSv2MessageContentsTest.class);

}
