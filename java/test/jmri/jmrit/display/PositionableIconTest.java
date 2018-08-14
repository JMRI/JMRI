package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of PositionableIcon
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class PositionableIconTest extends PositionableTestBase {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("PositionableIcon Constructor",p);
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           editor = new EditorScaffold();
           p = new PositionableIcon(editor);
        }
    }

    @After
    public void tearDown() {
        p = null;
        editor = null;
        JUnitUtil.tearDown();
    }

}
