package jmri.jmrit.vsdecoder;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(VSDGeoFileTest.class);

}
