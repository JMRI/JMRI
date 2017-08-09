package jmri.implementation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
<<<<<<< HEAD
public class NmraConsistManagerTest {

    @Test
    public void testCTor() {
        NmraConsistManager t = new NmraConsistManager();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
=======
public class NmraConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        cm = new NmraConsistManager();
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
>>>>>>> JMRI/master
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NmraConsistManagerTest.class.getName());

}
