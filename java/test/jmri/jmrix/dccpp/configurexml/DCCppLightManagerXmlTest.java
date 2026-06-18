package jmri.jmrix.dccpp.configurexml;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppLight;
import jmri.jmrix.dccpp.DCCppLightManager;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DCCppLightManagerXmlTest.java
 *
 * Test for the DCCppLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 * @author   Chad Francis  Copyright (C) 2026
 */
public class DCCppLightManagerXmlTest {

    private DCCppInterfaceScaffold xnis;
    private DCCppSystemConnectionMemo memo;
    private DCCppLightManager lm;

    @Test
    public void testCtor(){
      Assert.assertNotNull("DCCppLightManagerXml constructor",new DCCppLightManagerXml());
    }

    @Test
    public void testRoundTrip() {
        Light light = lm.provide("DL26");
        light.setProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY, DCCppLight.MODE_NAMES[1]);

        DCCppLightManagerXml xml = new DCCppLightManagerXml();
        Element stored = xml.store(lm);
        assertNotNull(stored, "store returns element");

        lm.deregister(light);
        assertNull(lm.getBySystemName("DL26"), "light deregistered");

        xml.load(stored, null);

        Light loaded = lm.getBySystemName("DL26");
        assertNotNull(loaded, "light restored after load");
        assertEquals(DCCppLight.MODE_NAMES[1],
                loaded.getProperty(DCCppLightManager.DCCPP_LIGHT_MODE_KEY),
                "CS VPIN mode preserved through store/load");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        xnis = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(xnis);
        xnis.setSystemConnectionMemo(memo);
        memo.setSystemPrefix("D");
        lm = new DCCppLightManager(memo);
        InstanceManager.setDefault(LightManager.class, lm);
    }

    @AfterEach
    public void tearDown() {
        xnis.terminateThreads();
        memo.dispose();
        JUnitUtil.tearDown();
    }

}
