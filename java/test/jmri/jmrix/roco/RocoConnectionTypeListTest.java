package jmri.jmrix.roco;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
