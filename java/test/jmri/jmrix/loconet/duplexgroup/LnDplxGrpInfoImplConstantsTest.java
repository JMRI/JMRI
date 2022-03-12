package jmri.jmrix.loconet.duplexgroup;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnDplxGrpInfoImplConstantsTest {

    @Test
    public void testCTor() {
        LnDplxGrpInfoImplConstants t = new LnDplxGrpInfoImplConstants();
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

    // private final static Logger log = LoggerFactory.getLogger(LnDplxGrpInfoImplConstantsTest.class);

}
