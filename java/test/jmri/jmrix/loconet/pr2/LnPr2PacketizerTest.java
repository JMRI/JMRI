package jmri.jmrix.loconet.pr2;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnPr2PacketizerTest {

    @Test
    public void testCTor() {
        LnPr2Packetizer t = new LnPr2Packetizer();
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnPr2PacketizerTest.class);

}
