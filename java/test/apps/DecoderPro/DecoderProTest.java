package apps.DecoderPro;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * Description: Tests for the DecoderPro application.
 *
 * @author  Paul Bender Copyright (C) 2016
 */
public class DecoderProTest {

    @Test
    @Ignore("This test works, but actually starts DecoderPro")
    public void testCtor() {
        apps.Apps a = new DecoderPro();
        Assert.assertNotNull(a);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }


}
