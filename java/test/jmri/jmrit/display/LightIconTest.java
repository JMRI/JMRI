package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LightIcon
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LightIconTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor ef = new EditorScaffold();
        LightIcon iti = new LightIcon(ef);
        Assert.assertNotNull("LightIcon Constructor",iti);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
