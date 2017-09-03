package jmri.jmrix.loconet.duplexgroup;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnDplxGrpInfoImplConstantsTest.class);

}
