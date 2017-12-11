package jmri.jmrit.display;

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
public class AnalogClock2DisplayTest extends PositionableJComponentTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("AnalogClock2Display Constructor",p);
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        Editor ef = new EditorScaffold();
        p = new AnalogClock2Display(ef);
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
