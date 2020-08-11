package jmri.jmrix.roco.z21.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Z21ReporterManagerXml.java
 *
 * Test for the Z21ReporterManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class Z21ReporterManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("Z21ReporterManagerXml constructor",new Z21ReporterManagerXml());
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

