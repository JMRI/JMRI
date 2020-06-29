package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for the MatrixSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MatrixSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MatrixSignalMastXml constructor", new MatrixSignalMastXml());
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

