package jmri.jmrix.ecos.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * EcosPreferencesXmlTest.java
 *
 * Test for the EcosPreferencesXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class EcosPreferencesXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("EcosPreferencesXml constructor",new EcosPreferencesXml());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

