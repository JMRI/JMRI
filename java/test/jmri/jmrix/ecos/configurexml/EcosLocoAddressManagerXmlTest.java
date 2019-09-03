package jmri.jmrix.ecos.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * EcosLocoAddressManagerXmlTest.java
 *
 * Description: tests for the EcosLocoAddressManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class EcosLocoAddressManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("EcosLocoAddressManagerXml constructor",new EcosLocoAddressManagerXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

