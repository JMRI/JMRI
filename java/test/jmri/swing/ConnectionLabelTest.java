package jmri.swing;

import jmri.jmrix.ConnectionStatus;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the jmri.jmrix.ConnectionLabel class
 *
 * @author Bob Jacobsen Copyright (c) 2001, 2002
 * @author Paul Bender Copyright (C) 2017
 */
public class ConnectionLabelTest {

    private jmri.jmrix.ConnectionConfig config = null;

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        ConnectionLabel action = new ConnectionLabel(config);
        assertNotNull(action);
    }

    @Test
    @DisabledIfHeadless
    public void checkSuccessColor() {
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_UP);
        ConnectionLabel action = new ConnectionLabel(config);
        assertEquals( java.awt.Color.BLACK, action.getForeground(), "Color for Success");
    }

    @Test
    @DisabledIfHeadless
    public void checkUnknownColor() {
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_UNKNOWN);
        ConnectionLabel action = new ConnectionLabel(config);
        // unknown is currently the same color as up.
        assertEquals( java.awt.Color.BLACK, action.getForeground(), "Color for Unknown");
    }

    @Test
    @DisabledIfHeadless
    public void checkFailColor() {
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_DOWN);
        ConnectionLabel action = new ConnectionLabel(config);
        assertEquals( java.awt.Color.RED, action.getForeground(), "Color for Failure");
    }

    @Test
    @DisabledIfHeadless
    public void checkColorOnChangeFromSuccess() {
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_UP);
        ConnectionLabel action = new ConnectionLabel(config);
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_DOWN);
        assertEquals( java.awt.Color.RED, action.getForeground(), "Color for Failure after success");
    }

    @Test
    @DisabledIfHeadless
    public void checkColorOnChangeFromFailure() {
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_DOWN);
        ConnectionLabel action = new ConnectionLabel(config);
        ConnectionStatus.instance().setConnectionState(config.getConnectionName(), config.getInfo(), ConnectionStatus.CONNECTION_UP);
        assertEquals( java.awt.Color.BLACK, action.getForeground(), "Color for Failure after success");
    }

    @BeforeEach
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

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
