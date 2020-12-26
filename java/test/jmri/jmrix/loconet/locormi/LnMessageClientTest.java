package jmri.jmrix.loconet.locormi;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @AfterAll
    public static void tearDownClass() throws Exception {
        System.setSecurityManager(SM);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnMessageClientTest.class);

}
