
package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Brett Hoffman Copyright (C) 2018
 */
public class FastClockControllerTest {
    
    @Test
    public void testCtor() {
        FastClockController panel = new FastClockController();
        Assert.assertNotNull("exists", panel );
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
