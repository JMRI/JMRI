package jmri.jmrix.loconet.locormi;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnMessageClientTest {

    private static final SecurityManager SM = System.getSecurityManager();

    @Test
    public void testCTor() {
        LnMessageClient t = new LnMessageClient();
        Assert.assertNotNull("exists", t);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.setSecurityManager(SM);
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

    // private final static Logger log = LoggerFactory.getLogger(LnMessageClientTest.class);

}
