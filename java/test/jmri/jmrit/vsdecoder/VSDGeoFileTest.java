package jmri.jmrit.vsdecoder;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the vsdecoder.VSDGeoFile class.
 *
 * Based on ToggleSoundEventTest by Paul Bender
 * @author Klaus Killinger Copyright (C) 2018
 */
public class VSDGeoFileTest {

    @Test
    public void testCTor() {
        VSDGeoFile t = new VSDGeoFile();
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(VSDGeoFileTest.class);

}
