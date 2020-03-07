package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 * Test simple functioning of IndicatorTrackIcon
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class IndicatorTrackIconTest extends PositionableIconTest {

    @Test
    @Override
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("IndicatorTrackIcon Constructor",p);
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new EditorScaffold();
            p = new IndicatorTrackIcon(editor);
        }
    }

}
