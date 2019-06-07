package jmri.jmrix.rps;

import jmri.Manager.NameValidity;
import jmri.util.JUnitAppender;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RpsAddressTest {

    @Test
    public void testCTor() {
        RpsAddress t = new RpsAddress();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testValidateSystemNameFormat() {
        Assert.assertTrue("valid format - RS(0,0,0);(1,0,0);(1,1,0)", NameValidity.VALID == RpsAddress.validSystemNameFormat("RS(0,0,0);(1,0,0);(1,1,0)", 'S', "R"));

        Assert.assertTrue("invalid format - RS(0,0,0)", NameValidity.VALID != RpsAddress.validSystemNameFormat("RS(0,0,0)", 'S', "R"));
        JUnitAppender.assertWarnMessage("need to have at least 3 points in RS(0,0,0)");

        Assert.assertTrue("invalid format - R2S(0,0,0);(1,0,0);1,1,0)", NameValidity.VALID != RpsAddress.validSystemNameFormat("R2S(0,0,0);(1,0,0);1,1,0)", 'S', "R2"));
        JUnitAppender.assertWarnMessage("missing brackets in point 2: \"1,1,0)\"");

        Assert.assertTrue("invalid format - R2S(0,0,0);(1,0,0);(1)", NameValidity.VALID != RpsAddress.validSystemNameFormat("R2S(0,0,0);(1,0,0);(1)", 'S', "R2"));
        JUnitAppender.assertWarnMessage("need to have three coordinates in point 2: \"(1)\"");
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
