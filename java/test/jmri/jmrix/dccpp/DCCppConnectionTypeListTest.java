package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * DCCppConnectionTypeListTest.java
 *
 * Test for the jmri.jmrix.dccpp.DCCppConnectionTypeList class
 *
 * @author Paul Bender
 * @author Mark Underwood
 */
public class DCCppConnectionTypeListTest {

    @Test
    public void testCtor() {

        DCCppConnectionTypeList c = new DCCppConnectionTypeList();
        Assert.assertNotNull(c);
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
