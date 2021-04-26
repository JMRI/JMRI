package jmri.jmrix.lenz.li100f;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for the jmri.jmrix.lenz.li100f.LI100fAdapter class
 *
 * @author Paul Bender
 */
public class LI100fAdapterTest {

    @Test
    public void testCtor() {
        LI100fAdapter a = new LI100fAdapter();
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
