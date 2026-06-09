package jmri.jmrix.dccpp.configurexml;

import java.util.List;

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
    public void testStoreModeAttribute() {
        Light light = lm.provide("DL26");
        ((DCCppLight) light).setMode(DCCppLight.CS_VPIN);

        DCCppLightManagerXml xml = new DCCppLightManagerXml();
        Element e = xml.store(lm);
        assertNotNull(e, "store returns element");

        List<Element> lights = e.getChildren("light");
        assertEquals(1, lights.size(), "one light element");
        assertEquals("CS VPIN", lights.get(0).getAttributeValue("mode"), "mode attribute stored");
    }

    @Test
    public void testStoreNoModeAttributeForStandard() {
        lm.provide("DL26");

        DCCppLightManagerXml xml = new DCCppLightManagerXml();
        Element e = xml.store(lm);
        assertNotNull(e);

        List<Element> lights = e.getChildren("light");
        assertEquals(1, lights.size());
        assertNull(lights.get(0).getAttributeValue("mode"), "no mode attribute for STANDARD");
    }

    @Test
    public void testLoadModeAttributeRoundTrip() {
        Light light = lm.provide("DL26");
        ((DCCppLight) light).setMode(DCCppLight.CS_VPIN);

        DCCppLightManagerXml xml = new DCCppLightManagerXml();
        Element stored = xml.store(lm);

        // deregister so loadLights re-creates it
        lm.deregister(light);
        assertNull(lm.getBySystemName("DL26"), "light gone before load");

        xml.loadLights(stored);

        Light loaded = lm.getBySystemName("DL26");
        assertNotNull(loaded, "light restored after load");
        assertEquals(DCCppLight.CS_VPIN, ((DCCppLight) loaded).getMode(), "CS_VPIN mode preserved");
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
