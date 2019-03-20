package jmri.jmrix.lenz.hornbyelite;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * EliteAdapterTest.java
 *
 * Description: tests for the jmri.jmrix.lenz.hornbyelite.EliteAdapter class
 *
 * @author Paul Bender
 */
public class EliteAdapterTest {

    @Test
    public void testCtor() {
        EliteAdapter a = new EliteAdapter();
        Assert.assertNotNull(a);
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
