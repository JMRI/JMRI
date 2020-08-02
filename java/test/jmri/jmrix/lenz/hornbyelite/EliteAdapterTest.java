package jmri.jmrix.lenz.hornbyelite;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * EliteAdapterTest.java
 *
 * Test for the jmri.jmrix.lenz.hornbyelite.EliteAdapter class
 *
 * @author Paul Bender
 */
public class EliteAdapterTest {

    @Test
    public void testCtor() {
        EliteAdapter a = new EliteAdapter();
        Assert.assertNotNull(a);
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
