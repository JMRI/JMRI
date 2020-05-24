package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.simpleserver.SimplePowerServer class
 *
 * @author Paul Bender
 */
public class SimplePowerServerTest extends jmri.jmris.AbstractPowerServerTestBase {

    private StringBuilder sb = null;

    @Test
    public void testCtorFailure() {
        jmri.util.JUnitUtil.resetInstanceManager(); // remove the debug power manager for this test only.
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            // null output string drops characters
            // could be replaced by one that checks for specific outputs
            @Override
            public void write(int b) {
            }
        });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);

        SimplePowerServer a = new SimplePowerServer(input, output);

        jmri.util.JUnitAppender.assertErrorMessage("No power manager instance found");
        assertThat(a).isNotNull();
    }

    @Test
    public void testConnectionCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            // null output string drops characters
            // could be replaced by one that checks for specific outputs
            @Override
            public void write(int b) {
            }
        });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimplePowerServer a = new SimplePowerServer(jcs);
        assertThat(a).isNotNull();
    }

    // test sending a status string.
    @Test
    public void testSendStatusString() throws Exception {
        ((SimplePowerServer) ps).sendStatus("Hello World\n");
        assertThat(sb.toString()).withFailMessage("send status string").isEqualTo("Hello World\n");
    }

    // test sending a status string.
    @Test
    public void testSendStatusStringWithConnection() throws Exception {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) {
                sb.append((char) b);
            }
        });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimplePowerServer a = new SimplePowerServer(jcs);
        a.sendStatus("Hello World\n");
        assertThat(jcs.getOutput()).withFailMessage("send status string").isEqualTo("Hello World\n");
    }

    // test parsing an ON status message.
    @Test
    public void testParseOnStatus() throws Exception {
        ps.parseStatus("POWER ON\n");
        assertThat(jmri.PowerManager.ON).withFailMessage("Parse On Status Check").isEqualTo(jmri.InstanceManager
                .getDefault(jmri.PowerManager.class).getPower());
        assertThat(sb.toString()).withFailMessage("status as a result of parsing on").isEqualTo("POWER ON\n");
    }

    // test parsing an OFF status message.
    @Test
    public void testParseOffStatus() throws Exception {
        ps.parseStatus("POWER OFF\n");
        assertThat(jmri.PowerManager.OFF).withFailMessage("Parse OFF Status Check").isEqualTo(jmri.InstanceManager
                .getDefault(jmri.PowerManager.class).getPower());
        assertThat(sb.toString()).withFailMessage("status as a result of parsing off").isEqualTo("POWER OFF\n");
    }

    @Test
    // test parsing a bad status message.
    public void testParseBadStatus() throws Exception {
        // this should just trigger an error message sent to the client.
        ps.parseStatus("POWER FFO\n");
        assertThat(sb.toString()).withFailMessage("error from bad parse").isEqualTo("POWER ERROR\n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOnSent() {
        assertThat(sb.toString()).withFailMessage("status as a result of on property change").isEqualTo("POWER ON\n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOffSent() {
        assertThat(sb.toString()).isEqualTo("POWER OFF\n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkErrorStatusSent() {
        assertThat(sb.toString()).withFailMessage("sendErrorStatus check").isEqualTo("POWER ERROR\n");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkUnknownStatusSent() {
        assertThat(sb.toString()).withFailMessage("send UNKNOWN status check").isEqualTo("POWER UNKNOWN\n");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitUtil.initDebugPowerManager();
        sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) {
                sb.append((char) b);
            }
        });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        ps = new SimplePowerServer(input, output);
    }

    @AfterEach
    public void tearDown() {
        ps.dispose();
        ps = null;
        sb = null;
        JUnitUtil.tearDown();
    }

}
