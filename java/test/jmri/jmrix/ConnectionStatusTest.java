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

    @Test
    public void testInstance() {
        ConnectionStatus cs = ConnectionStatus.instance();
        Assert.assertNotNull("exists", cs);
    }

    @Test
    public void testGetState(){
        ConnectionStatus cs = new ConnectionStatus();
        Assert.assertEquals("connection status",ConnectionStatus.CONNECTION_UNKNOWN,cs.getConnectionState("Bar"));
    }

    @Test
    public void testAddAndGetState(){
        ConnectionStatus cs = new ConnectionStatus();
        cs.addConnection("Foo","Bar");
        Assert.assertEquals("connection status",ConnectionStatus.CONNECTION_UNKNOWN,cs.getConnectionState("Bar"));
    }

    @Test
    public void testSetAndGetState(){
        ConnectionStatus cs = new ConnectionStatus();
        cs.setConnectionState("Bar",ConnectionStatus.CONNECTION_UP);
        Assert.assertEquals("connection status",ConnectionStatus.CONNECTION_UP,cs.getConnectionState("Bar"));
    }

    @Test
    public void testIsConnectionOk(){
        ConnectionStatus cs = new ConnectionStatus();
        cs.setConnectionState("Bar",ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK",cs.isConnectionOk("Bar"));
        cs.setConnectionState("Bar",ConnectionStatus.CONNECTION_DOWN);
        Assert.assertFalse("connection OK",cs.isConnectionOk("Bar"));
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
