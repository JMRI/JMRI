package jmri.jmrix.lenz.hornbyelite;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * EliteConnectionTypeListTest.java
 *
 * Test for the jmri.jmrix.lenz.EliteConnectionTypeList class
 *
 * @author Paul Bender
 */
public class EliteConnectionTypeListTest {

    @Test
    public void testCtor() {

        EliteConnectionTypeList c = new EliteConnectionTypeList();
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
