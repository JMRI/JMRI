package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of LightIcon
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LightIconTest extends PositionableTestBase {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LightIcon Constructor",p);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if ( !GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            editor = new EditorScaffold();
            p = new LightIcon(editor);
        }
    }

    @After
    public void tearDown() {
        editor = null;
        p = null;
        JUnitUtil.tearDown();
    }

}
