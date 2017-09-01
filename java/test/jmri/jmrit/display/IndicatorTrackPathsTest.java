package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of IndicatorTrackPaths
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class IndicatorTrackPathsTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        IndicatorTrackPaths itp = new IndicatorTrackPaths();
        Assert.assertNotNull("IndicatorTrackPaths Constructor",itp);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
