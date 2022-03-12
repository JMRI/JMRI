package jmri.jmrix.ecos.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * EcosLocoAddressManagerXmlTest.java
 *
 * Test for the EcosLocoAddressManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class EcosLocoAddressManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("EcosLocoAddressManagerXml constructor",new EcosLocoAddressManagerXml());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

