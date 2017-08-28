package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of AnalogClock2Display
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class AnalogClock2DisplayTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor ef = new EditorScaffold();
        AnalogClock2Display frame = new AnalogClock2Display(ef);
        Assert.assertNotNull("AnalogClock2Display Constructor",frame);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
