package jmri.jmrix.loconet.locormi;

import java.security.Permission;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnMessageServerTest {

    private static final SecurityManager SM = System.getSecurityManager();

    @Test
    public void testGetInstance() throws java.rmi.RemoteException {
        LnMessageServer t = LnMessageServer.getInstance();
        Assert.assertNotNull("exists", t);
    }

    @BeforeClass
    public static void setUpClass() {
        if (SM == null) {
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm) {
                }

                @Override
                public void checkPermission(Permission perm, Object context) {
                }

                @Override
                public void checkExit(int status) {
                    String message = "System exit requested with error " + status;
                    throw new SecurityException(message);
                }
            });
        }
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

    // private final static Logger log = LoggerFactory.getLogger(LnMessageServerTest.class);

}
