package jmri.util;

import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class QuickPromptUtilTest {

    @Test
    public void testCTor() {
        QuickPromptUtil t = new QuickPromptUtil();
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
