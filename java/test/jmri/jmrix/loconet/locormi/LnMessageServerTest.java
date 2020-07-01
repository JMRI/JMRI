package jmri.jmrix.loconet.locormi;

import java.security.Permission;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeAll;

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

    @BeforeAll
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

    // private final static Logger log = LoggerFactory.getLogger(LnMessageServerTest.class);

}
