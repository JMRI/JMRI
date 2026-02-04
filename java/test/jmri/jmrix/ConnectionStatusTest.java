package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ConnectionStatus
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ConnectionStatusTest {

    @Test
    public void testInstance() {
        ConnectionStatus cs = ConnectionStatus.instance();
        Assert.assertNotNull("exists", cs);
    }

    @Test
    public void test2ParamterGetState() {
        ConnectionStatus cs = ConnectionStatus.instance();
        Assert.assertEquals("connection status", ConnectionStatus.CONNECTION_UNKNOWN, cs.getConnectionState("Foo", "Bar"));
    }

    @Test
    public void testAddAnd2ParameterGetState() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.addConnection("Foo", "Bar");
        // set the status of the new connection so we know we are not
        // retreiving a new value.
        cs.setConnectionState("Foo", "Bar", ConnectionStatus.CONNECTION_UP);
        Assert.assertEquals("connection status", ConnectionStatus.CONNECTION_UP, cs.getConnectionState("Foo", "Bar"));
    }

    @Test
    public void test2ParameterSetAndGetState() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.setConnectionState("Foo", "Bar", ConnectionStatus.CONNECTION_UP);
        Assert.assertEquals("connection status", ConnectionStatus.CONNECTION_UP, cs.getConnectionState("Foo", "Bar"));
    }

    @Test
    public void test2ParamterIsConnectionOk() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.setConnectionState("Foo", "Bar", ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK", cs.isConnectionOk("Foo", "Bar"));
        cs.setConnectionState("Foo", "Bar", ConnectionStatus.CONNECTION_DOWN);
        Assert.assertFalse("connection OK", cs.isConnectionOk("Foo", "Bar"));
    }

    @Test
    public void testIsSystemOk() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.setConnectionState("Foo", "Bar", ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK", cs.isSystemOk("Foo"));
        cs.setConnectionState("Foo", "Bar", ConnectionStatus.CONNECTION_DOWN);
        Assert.assertFalse("connection OK", cs.isSystemOk("Foo"));
    }

    @Test
    public void testIsUnrecognizedSystemOk() {
        ConnectionStatus cs = ConnectionStatus.instance();
        Assert.assertTrue("connection OK", cs.isConnectionOk("Foo", "Bar"));
        Assert.assertTrue("connection OK", cs.isConnectionOk(null, "Bar"));
    }

    @Test
    public void testGetSateForSystemName() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.addConnection("Foo", "Bar");
        // set the status of the new connection so we know we are not
        // retreiving a new value.
        cs.setConnectionState("Foo", "Bar", ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK", cs.isConnectionOk("Foo", "Bar"));
        Assert.assertEquals("connection status", ConnectionStatus.CONNECTION_UP, cs.getSystemState("Foo"));
    }

    @Test
    public void testIsConnectionOkWNull() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.setConnectionState(null, "Bar", ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK", cs.isConnectionOk(null, "Bar"));
        cs.setConnectionState(null, "Bar", ConnectionStatus.CONNECTION_DOWN);
        Assert.assertFalse("connection OK", cs.isConnectionOk(null, "Bar"));
    }

    @BeforeEach
    public void setUp() {
        ConnectionStatus.clearInstance();
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
