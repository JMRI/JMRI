package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Test simple functioning of IndicatorTrackPaths
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class IndicatorTrackPathsTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IndicatorTrackPaths itp = new IndicatorTrackPaths();
        Assert.assertNotNull("IndicatorTrackPaths Constructor",itp);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }


}
