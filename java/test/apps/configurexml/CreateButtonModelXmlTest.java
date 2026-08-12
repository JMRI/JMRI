package apps.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * CreateButtonModelXmlTest.java
 *
 * Test for the CreateButtonModelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class CreateButtonModelXmlTest {

    @Test
    public void testCtor(){
      Assertions.assertNotNull(new CreateButtonModelXml(), "CreateButtonModelXml constructor");
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

