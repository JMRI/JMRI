package jmri.jmrix.openlcb.configurexml;

import jmri.InstanceManager;
import jmri.Light;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.openlcb.OlcbLight;
import jmri.jmrix.openlcb.OlcbLightManager;
import jmri.jmrix.openlcb.OlcbTestInterface;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OlcbLightManagerXmlTest.java
 *
 * Description: tests for the OlcbLightManagerXml class
 *
 * @author   Jeff Collell
 */
public class OlcbLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("OlcbLightManagerXml constructor",new OlcbLightManagerXml());
    }

    @Test
    public void testSaveAndRestore() throws Exception {
        log.debug("FIRST START");
        t = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        OlcbLightManager mgr = t.configurationManager.getLightManager();
        OlcbLightManagerXml xmlmgr = new OlcbLightManagerXml();

        OlcbLight l = (OlcbLight)mgr.newLight("ML1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", "light1");
        t.flush();
        CanMessage expected = new CanMessage(new byte[]{1,2,3,4,5,6,7,8}, 0x198F4C4C);
        expected.setExtended(true);
        Assert.assertEquals(expected, t.tc.rcvMessage);
        t.tc.rcvMessage = null;

        // Send a state query command
        log.debug("SEND QUERY");

        l.setState(Light.ON);
        CanMessage request = new CanMessage(new byte[]{1,2,3,4,5,6,7,8}, 0x198F4123);
        request.setExtended(true);
        t.sendMessage(request);
        t.flush();
        expected = new CanMessage(new byte[]{1,2,3,4,5,6,7,8}, 0x194C4C4C);
        expected.setExtended(true);
        Assert.assertEquals(expected, t.tc.rcvMessage);
        t.tc.rcvMessage = null;

        Element stored = xmlmgr.store(mgr);
        Assert.assertNotNull(stored);
        InstanceManager.getDefault().clearAll();

        log.debug("SECOND START");

        t = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        mgr = t.configurationManager.getLightManager();

        xmlmgr.load(stored, null);

        Light l2 = mgr.getBySystemName("ML1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9");
        Assert.assertNotNull(l2);

    }

    OlcbTestInterface t;
    private final static Logger log = LoggerFactory.getLogger(OlcbLightManagerXmlTest.class);

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}

