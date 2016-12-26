package jmri.jmrix;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test simple functioning of ConnectionStatus
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ConnectionStatusTest {

    @Test
    public void testCtor() {
        ConnectionStatus cs = new ConnectionStatus();
        Assert.assertNotNull("exists", cs);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }

}
