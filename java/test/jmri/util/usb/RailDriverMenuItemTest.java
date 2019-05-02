package jmri.util.usb;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RailDriverMenuItemTest {

    @Test
    public void testCTor() {
        RailDriverMenuItem t = new RailDriverMenuItem();
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
