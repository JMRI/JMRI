package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 * Test simple functioning of SlipTurnoutIcon
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SlipTurnoutIconTest extends PositionableTestBase {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("SlipTurnoutIcon Constructor", p);
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
           editor = new EditorScaffold();
           p = new SlipTurnoutIcon(editor);
        }
    }

}
