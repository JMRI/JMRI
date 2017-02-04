package jmri.implementation.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MatrixSignalMastXmlTest.java
 *
 * Description: tests for the MatrixSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class MatrixSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("MatrixSignalMastXml constructor",new MatrixSignalMastXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}

