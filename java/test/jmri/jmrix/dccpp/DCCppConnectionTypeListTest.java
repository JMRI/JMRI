package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * DCCppConnectionTypeListTest.java
 *
 * Description: tests for the jmri.jmrix.dccpp.DCCppConnectionTypeList class
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
