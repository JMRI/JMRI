package jmri.jmrix;

import java.util.Comparator;

import jmri.NamedBean;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ConnectionStatus
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ConnectionStatusTest {

    private DefaultSystemConnectionMemo _memo = null;

    @Test
    public void testInstance() {
        ConnectionStatus cs = ConnectionStatus.instance();
        Assert.assertNotNull("exists", cs);
    }

    @Test
    public void testGetState() {
        ConnectionStatus cs = ConnectionStatus.instance();
        Assert.assertEquals("connection status", ConnectionStatus.CONNECTION_UNKNOWN, cs.getConnectionState(_memo));
    }

    @Test
    public void testAddAndGetState() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.addConnection(_memo);
        // set the status of the new connection so we know we are not
        // retreiving a new value.
        cs.setConnectionState(_memo, ConnectionStatus.CONNECTION_UP);
        Assert.assertEquals("connection status", ConnectionStatus.CONNECTION_UP, cs.getConnectionState(_memo));
    }

    @Test
    public void testSetAndGetState() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.setConnectionState(_memo, ConnectionStatus.CONNECTION_UP);
        Assert.assertEquals("connection status", ConnectionStatus.CONNECTION_UP, cs.getConnectionState(_memo));
    }

    @Test
    public void testIsConnectionOk() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.setConnectionState(_memo, ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK", cs.isConnectionOk(_memo));
        cs.setConnectionState(_memo, ConnectionStatus.CONNECTION_DOWN);
        Assert.assertFalse("connection OK", cs.isConnectionOk(_memo));
    }

    @Test
    public void testIsSystemOk() {
        ConnectionStatus cs = ConnectionStatus.instance();
        cs.setConnectionState(_memo, ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK", cs.isSystemOk("Test"));
        cs.setConnectionState(_memo, ConnectionStatus.CONNECTION_DOWN);
        Assert.assertFalse("connection OK", cs.isSystemOk("Test"));
    }

    @Test
    public void testIsUnrecognizedSystemOk() {
        ConnectionStatus cs = ConnectionStatus.instance();
        Assert.assertTrue("connection OK", cs.isConnectionOk(_memo));
    }

    @Test
    public void testGetStateForSystemName() {
        ConnectionStatus cs = ConnectionStatus.instance();
//        cs.addConnection(_memo);
        // set the status of the new connection so we know we are not
        // retreiving a new value.
        cs.setConnectionState(_memo, ConnectionStatus.CONNECTION_UP);
        Assert.assertTrue("connection OK", cs.isConnectionOk(_memo));
    }

    @BeforeEach
    public void setUp() {
        ConnectionStatus.clearInstance();
        JUnitUtil.setUp();
        JUnitUtil.initDebugCommandStation();
        JUnitUtil.initDebugProgrammerManager();
        _memo = new DefaultSystemConnectionMemo("T", "Test") {
            @Override
            protected java.util.ResourceBundle getActionModelResourceBundle() {
                return null;
            }

            @Override
            public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
                return null;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        _memo = null;
        JUnitUtil.tearDown();
    }

}
