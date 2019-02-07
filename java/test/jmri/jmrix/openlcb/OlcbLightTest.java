package jmri.jmrix.openlcb;

import static junit.framework.TestCase.assertNotNull;

import jmri.Light;
import jmri.util.JUnitUtil;
import jmri.util.PropertyChangeListenerScaffold;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbLight class.
 *
 * @author	Jeff Collell
 */
public class OlcbLightTest extends TestCase {
    private final static Logger log = LoggerFactory.getLogger(OlcbLightTest.class);
    protected PropertyChangeListenerScaffold listener; 

    public void testLocalChangeSendsEvent() throws jmri.JmriException {
        OlcbLight l = new OlcbLight("M", "1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", t.iface);
        l.finishLoad();
        t.waitForStartup();

        t.tc.rcvMessage = null;
        l.setState(Light.ON);
        Assert.assertEquals(Light.ON, l.getState());
        t.flush();
        assertNotNull(t.tc.rcvMessage);
        log.debug("recv msg: " + t.tc.rcvMessage + " header " + Integer.toHexString(t.tc.rcvMessage.getHeader()));
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").match(t.tc.rcvMessage));

        t.tc.rcvMessage = null;
        l.setState(Light.OFF);
        Assert.assertEquals(Light.OFF, l.getState());
        t.flush();
        assertNotNull(t.tc.rcvMessage);
        log.debug("recv msg: " + t.tc.rcvMessage + " header " + Integer.toHexString(t.tc.rcvMessage.getHeader()));
        Assert.assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").match(t.tc.rcvMessage));
    }

    OlcbTestInterface t;

    // from here down is testing infrastructure
    public OlcbLightTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OlcbLightTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbLightTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
        // load dummy TrafficController
        t = new OlcbTestInterface();
        t.waitForStartup();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}
