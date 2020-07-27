package jmri.jmrix.openlcb;

import jmri.Light;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbLight class.
 *
 * @author Jeff Collell
 */
public class OlcbLightTest {

    private final static Logger log = LoggerFactory.getLogger(OlcbLightTest.class);

    @Test
    public void testLocalChangeSendsEvent() {
        OlcbLight l = new OlcbLight("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        l.finishLoad();
        t.waitForStartup();

        t.tc.rcvMessage = null;
        l.setState(Light.ON);
        Assert.assertEquals(Light.ON, l.getState());
        t.flush();
        Assert.assertNotNull(t.tc.rcvMessage);
        log.debug("recv msg: {} header {}",t.tc.rcvMessage,Integer.toHexString(t.tc.rcvMessage.getHeader()));
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").match(t.tc.rcvMessage));

        t.tc.rcvMessage = null;
        l.setState(Light.OFF);
        Assert.assertEquals(Light.OFF, l.getState());
        t.flush();
        Assert.assertNotNull(t.tc.rcvMessage);
        log.debug("recv msg: {} header {}", t.tc.rcvMessage, Integer.toHexString(t.tc.rcvMessage.getHeader()));
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(t.tc.rcvMessage));
    }

    OlcbTestInterface t;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // load dummy TrafficController
        t = new OlcbTestInterface();
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
