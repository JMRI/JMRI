
package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }
    
    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
