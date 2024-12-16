package jmri.jmrix.openlcb;

import jmri.Light;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbLight class.
 *
 * @author Jeff Collell
 */
public class OlcbLightTest {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbLightTest.class);

    @Test
    public void testLocalChangeSendsEvent() {
        OlcbLight l = new OlcbLight("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.systemConnectionMemo);
        l.finishLoad();
        t.waitForStartup();

        t.tc.rcvMessage = null;
        l.setState(Light.ON);
        Assert.assertEquals(Light.ON, l.getState());
        t.flush();
        Assert.assertNotNull(t.tc.rcvMessage);
        log.debug("recv msg: {} header {}",t.tc.rcvMessage,Integer.toHexString(t.tc.rcvMessage.getHeader()));
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8", null).match(t.tc.rcvMessage));

        t.tc.rcvMessage = null;
        l.setState(Light.OFF);
        Assert.assertEquals(Light.OFF, l.getState());
        t.flush();
        Assert.assertNotNull(t.tc.rcvMessage);
        log.debug("recv msg: {} header {}", t.tc.rcvMessage, Integer.toHexString(t.tc.rcvMessage.getHeader()));
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9", null).match(t.tc.rcvMessage));
    }

    OlcbTestInterface t;

    @BeforeAll
    static public void checkSeparate() {
        // this test is run separately because it leaves a lot of threads behind
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // load dummy TrafficController
        t = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        t.waitForStartup();
    }

    @AfterEach
    public void tearDown() {
        t.dispose();
        t = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
