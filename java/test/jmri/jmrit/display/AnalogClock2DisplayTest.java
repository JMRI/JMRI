package jmri.jmrit.display;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of AnalogClock2Display
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class AnalogClock2DisplayTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor ef = new jmri.jmrit.display.layoutEditor.LayoutEditor();
        AnalogClock2Display frame = new AnalogClock2Display(ef);
        Assert.assertNotNull("AnalogClock2Display Constructor",frame);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }


}
