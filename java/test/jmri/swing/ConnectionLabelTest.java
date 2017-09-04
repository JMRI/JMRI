package jmri.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.ConnectionStatus;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.ConnectionLabel class
 *
 * @author	Bob Jacobsen Copyright (c) 2001, 2002
 * @author Paul Bender Copyright (C) 2017
 */
public class ConnectionLabelTest {

    private jmri.jmrix.ConnectionConfig config = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConnectionLabel action = new ConnectionLabel(config);
        Assert.assertNotNull(action);
    }

    @Test
    public void checkSuccessColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_UP);
        ConnectionLabel action = new ConnectionLabel(config);
        Assert.assertEquals("Color for Success", java.awt.Color.BLACK, action.getForeground());
    }

    @Test
    public void checkUnknownColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_UNKNOWN);
        ConnectionLabel action = new ConnectionLabel(config);
        // unknown is currently the same color as up.
        Assert.assertEquals("Color for Unknown", java.awt.Color.BLACK, action.getForeground());
    }

    @Test
    public void checkFailColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_DOWN);
        ConnectionLabel action = new ConnectionLabel(config);
        Assert.assertEquals("Color for Failure", java.awt.Color.RED, action.getForeground());
    }

    @Test
    public void checkColorOnChangeFromSuccess() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_UP);
        ConnectionLabel action = new ConnectionLabel(config);
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_DOWN);
        Assert.assertEquals("Color for Failure after success", java.awt.Color.RED, action.getForeground());
    }

    @Test
    public void checkColorOnChangeFromFailure() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_DOWN);
        ConnectionLabel action = new ConnectionLabel(config);
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_UP);
        Assert.assertEquals("Color for Failure after success", java.awt.Color.BLACK, action.getForeground());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        config = new jmri.jmrix.AbstractConnectionConfig() {
            @Override
            protected void checkInitDone() {
            }

            @Override
            public void updateAdapter() {
            }

            @Override
            protected void setInstance() {
            }

            @Override
            public String getInfo() {
                return "foo";
            }

            @Override
            public void loadDetails(final javax.swing.JPanel details) {
            }

            @Override
            protected void showAdvancedItems() {
            }

            @Override
            public String getManufacturer() {
                return "foo";
            }

            @Override
            public void setManufacturer(String manufacturer) {
            }

            @Override
            public String getConnectionName() {
                return "bar";
            }

            @Override
            public boolean getDisabled() {
                return false;
            }

            @Override
            public void setDisabled(boolean disabled) {
            }

            @Override
            public jmri.jmrix.PortAdapter getAdapter() {
                return null;
            }

            @Override
            public String name() {
                return "bar";
            }
        };
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
