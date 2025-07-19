package jmri.jmrix.bidib.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBSignalMastXml class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class BiDiBSignalMastXmlTest {

    @Test
    public void testCtor(){
        BiDiBSignalMastXml t = new BiDiBSignalMastXml();
        Assertions.assertNotNull(t, "BiDiBSignalMastXml constructor");
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
