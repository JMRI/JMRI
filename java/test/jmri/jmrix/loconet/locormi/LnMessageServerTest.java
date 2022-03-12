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

    @Test
    public void testGetInstance() throws java.rmi.RemoteException {
        LnMessageServer t = LnMessageServer.getInstance();
        Assert.assertNotNull("exists", t);
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
