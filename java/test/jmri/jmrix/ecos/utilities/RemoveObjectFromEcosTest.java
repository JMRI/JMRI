package jmri.jmrix.ecos.utilities;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * RemoveObjectFromEcosTest.java
 *
 * Test for the RemoveObjectFromEcos class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RemoveObjectFromEcosTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RemoveObjectFromEcos constructor",new RemoveObjectFromEcos());
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

