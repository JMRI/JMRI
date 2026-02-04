package jmri.jmrix.openlcb.configurexml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import jmri.*;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.openlcb.OlcbSensor;
import jmri.jmrix.openlcb.OlcbSensorManager;
import jmri.jmrix.openlcb.OlcbTestInterface;
import jmri.jmrix.openlcb.OlcbUtils;
import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.jupiter.api.*;

/**
 * OlcbSensorManagerXmlTest.java
 *
 * Test for the OlcbSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 *           Balazs Racz    (C) 2018
 */
public class OlcbSensorManagerXmlTest {

    @Test
    public void testSaveAndRestoreWithProperties() throws JmriConfigureXmlException, JmriException {
        log.debug("FIRST START");
        t = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        OlcbSensorManager mgr = t.configurationManager.getSensorManager();
        OlcbSensorManagerXml xmlmgr = new OlcbSensorManagerXml();

        OlcbSensor s = (OlcbSensor)mgr.newSensor("MS1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", "sen1");
        t.flush();
        CanMessage expected = new CanMessage(new byte[]{1,2,3,4,5,6,7,8}, 0x198F4C4C);
        expected.setExtended(true);
        assertEquals(expected, t.tc.rcvMessage);
        t.tc.rcvMessage = null;

        // Send a state query command
        log.debug("SEND QUERY");

        s.setKnownState(Sensor.ACTIVE);
        CanMessage request = new CanMessage(new byte[]{1,2,3,4,5,6,7,8}, 0x198F4123);
        request.setExtended(true);
        t.sendMessage(request);
        t.flush();
        expected = new CanMessage(new byte[]{1,2,3,4,5,6,7,8}, 0x194C4C4C);
        expected.setExtended(true);
        assertEquals(expected, t.tc.rcvMessage);
        t.tc.rcvMessage = null;

        s.setProperty(OlcbUtils.PROPERTY_QUERY_AT_STARTUP, false);
        s.setAuthoritative(false);
        assertEquals(1, mgr.getNamedBeanSet().size());

        Element stored = xmlmgr.store(mgr);
        assertNotNull(stored);
        InstanceManager.getDefault().clearAll();

        log.debug("SECOND START");

        t = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        mgr = t.configurationManager.getSensorManager();

        xmlmgr.load(stored, null);

        Sensor s2 = mgr.getBySystemName("MS1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9");
        assertNotNull(s2);
        assertFalse( (Boolean)s2.getProperty(OlcbUtils.PROPERTY_QUERY_AT_STARTUP));
        assertNull(s2.getProperty(OlcbUtils.PROPERTY_IS_CONSUMER));

        t.flush();

        // The last message from the initialization is not a query (like above), because query at
        // init is disabled.
        expected = new CanMessage(new byte[]{1,2,3,4,5,6,7,9}, 0x194C7C4C);
        expected.setExtended(true);
        assertEquals(expected, t.tc.rcvMessage);
        t.tc.rcvMessage = null;

        log.debug("SET STATE");
        s2.setKnownState(Sensor.ACTIVE);
        t.flush();
        expected = new CanMessage(new byte[]{1,2,3,4,5,6,7,8}, 0x195B4C4C);
        expected.setExtended(true);
        assertEquals(expected, t.tc.rcvMessage);
        t.tc.rcvMessage = null;

        // Another query will get back unknown state due to the property loaded.
        t.sendMessage(request);
        t.flush();
        expected = new CanMessage(new byte[]{1,2,3,4,5,6,7,8}, 0x194C7C4C);
        expected.setExtended(true);
        assertEquals(expected, t.tc.rcvMessage);
        t.dispose();
    }

    private OlcbTestInterface t;
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbSensorManagerXmlTest.class);

    @BeforeAll
    static public void checkSeparate() {
       // this test is run separately because it leaves a lot of threads behind
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}

