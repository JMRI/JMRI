package jmri.jmrix.direct;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2018	
 */
public class DirectConnectionTypeListTest {

    @Test
    public void testCTor() {
        DirectConnectionTypeList t = new DirectConnectionTypeList();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
