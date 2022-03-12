package jmri.jmrix.roco;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.roco.RocoConnectionTypeList class
 *
 * @author Paul Bender
 */
public class RocoConnectionTypeListTest {

    @Test
    public void testCtor() {

        RocoConnectionTypeList c = new RocoConnectionTypeList();
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
