package jmri.jmrix.loconet.locormi;

import org.junit.After;
<<<<<<< HEAD
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
=======
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
>>>>>>> JMRI/master
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
<<<<<<< HEAD
 * @author Paul Bender Copyright (C) 2017	
 */
public class LnMessageClientTest {

    @Test
    public void testCTor() {
        LnMessageClient t = new LnMessageClient();
        Assert.assertNotNull("exists",t);
=======
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
>>>>>>> JMRI/master
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

    // private final static Logger log = LoggerFactory.getLogger(LnMessageClientTest.class.getName());

}
