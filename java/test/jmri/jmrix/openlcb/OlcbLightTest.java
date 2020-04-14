package jmri.jmrix.openlcb;

import jmri.Light;
import jmri.util.JUnitUtil;
import jmri.util.PropertyChangeListenerScaffold;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbLight class.
 *
 * @author	Jeff Collell
 */
public class OlcbLightTest {

    private final static Logger log = LoggerFactory.getLogger(OlcbLightTest.class);
    protected PropertyChangeListenerScaffold listener; 

    @Test
    public void testLocalChangeSendsEvent() throws jmri.JmriException {
        OlcbLight l = new OlcbLight("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        l.finishLoad();
        t.waitForStartup();

        t.tc.rcvMessage = null;
        l.setState(Light.ON);
        Assert.assertEquals(Light.ON, l.getState());
        t.flush();
        Assert.assertNotNull(t.tc.rcvMessage);
        log.debug("recv msg: " + t.tc.rcvMessage + " header " + Integer.toHexString(t.tc.rcvMessage.getHeader()));
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").match(t.tc.rcvMessage));

        t.tc.rcvMessage = null;
        l.setState(Light.OFF);
        Assert.assertEquals(Light.OFF, l.getState());
        t.flush();
        Assert.assertNotNull(t.tc.rcvMessage);
        log.debug("recv msg: " + t.tc.rcvMessage + " header " + Integer.toHexString(t.tc.rcvMessage.getHeader()));
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(t.tc.rcvMessage));
    }

    OlcbTestInterface t;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // load dummy TrafficController
        t = new OlcbTestInterface();
        t.waitForStartup();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
