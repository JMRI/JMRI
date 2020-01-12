package jmri.jmrix.openlcb;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OlcbUtilsTest {

    @Test
    public void testCTor() {
        OlcbUtils t = new OlcbUtils();
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
