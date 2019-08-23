package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.*;

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
            public void write(int b) throws java.io.IOException {
            }
        });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);

        SimplePowerServer a = new SimplePowerServer(input, output);

        jmri.util.JUnitAppender.assertErrorMessage("No power manager instance found");
        Assert.assertNotNull(a);
    }

    @Test
    public void testConnectionCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            // null output string drops characters
            // could be replaced by one that checks for specific outputs
            @Override
            public void write(int b) throws java.io.IOException {
            }
        });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimplePowerServer a = new SimplePowerServer(jcs);

        Assert.assertNotNull(a);
    }

    // test sending a status string.
    @Test
    public void testSendStatusString() throws Exception {
        ((SimplePowerServer)ps).sendStatus("Hello World\n");
        Assert.assertEquals("send status string", "Hello World\n", sb.toString());
    }

    // test sending a status string.
    @Test
    public void testSendStatusStringWithConnection() throws Exception {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimplePowerServer a = new SimplePowerServer(jcs);
        a.sendStatus("Hello World\n");
        Assert.assertEquals("send status string", "Hello World\n", jcs.getOutput());
    }

    // test parsing an ON status message.
    @Test
    public void testParseOnStatus() throws Exception {
         ps.parseStatus("POWER ON\n");
         Assert.assertEquals("Parse On Status Check", jmri.InstanceManager
                        .getDefault(jmri.PowerManager.class).getPower(),
                        jmri.PowerManager.ON);
        Assert.assertEquals("status as a result of parsing on", "POWER ON\n", sb.toString());
    }

    // test parsing an OFF status message.
    @Test
    public void testParseOffStatus() throws Exception {
        ps.parseStatus("POWER OFF\n");
        Assert.assertEquals("Parse OFF Status Check", jmri.InstanceManager
                        .getDefault(jmri.PowerManager.class).getPower(),
                        jmri.PowerManager.OFF);
        Assert.assertEquals("status as a result of parsing off", "POWER OFF\n", sb.toString());
    }

    @Test
    // test parsing a bad status message.
    public void testParseBadStatus() throws Exception {
        // this should just trigger an error message sent to the client.
        ps.parseStatus("POWER FFO\n");
        Assert.assertEquals("error from bad parse", "POWER ERROR\n", sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOnSent(){
             Assert.assertEquals("status as a result of on property change", "POWER ON\n", sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPowerOffSent(){
            Assert.assertEquals("status as a result of off property change", "POWER OFF\n", sb.toString());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void checkErrorStatusSent() {
        Assert.assertEquals("sendErrorStatus check", "POWER ERROR\n", sb.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkUnknownStatusSent() {
        Assert.assertEquals("send UNKNOWN status check", "POWER UNKNOWN\n", sb.toString());
    }

    // The minimal setup for log4J
    @Before
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
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        ps = new SimplePowerServer(input, output);
    }

    @After
    public void tearDown() throws Exception {
        ps.dispose();
        ps = null;
        sb = null;
        JUnitUtil.tearDown();
    }

}
