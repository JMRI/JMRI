package jmri.jmrix.lenz.hornbyelite;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * EliteConnectionTypeListTest.java
 *
 * Description: tests for the jmri.jmrix.lenz.EliteConnectionTypeList class
 *
 * @author Paul Bender
 */
public class EliteConnectionTypeListTest {

    @Test
    public void testCtor() {

        EliteConnectionTypeList c = new EliteConnectionTypeList();
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
