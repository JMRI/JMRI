package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of IndicatorTrackIcon
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class IndicatorTrackIconTest extends PositionableIconTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("IndicatorTrackIcon Constructor",p);
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            editor = new EditorScaffold();
            p = new IndicatorTrackIcon(editor);
        }
    }

    @After
    public void tearDown() {
        editor = null;
        p = null;
        JUnitUtil.tearDown();
    }

}
