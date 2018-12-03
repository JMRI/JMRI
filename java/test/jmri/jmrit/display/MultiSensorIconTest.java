package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of MultiSensorIcon
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MultiSensorIconTest extends PositionableTestBase {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("MultiSensorIcon Constructor",p);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
           editor = new EditorScaffold();
           p = new MultiSensorIcon(editor);
        }
    }

    @After
    public void tearDown() {
        editor = null;
        p = null;
        JUnitUtil.tearDown();
    }

}
