package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 * Test simple functioning of LightIcon
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LightIconTest extends PositionableTestBase {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LightIcon Constructor", p);
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new EditorScaffold();
            p = new LightIcon(editor);
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

}
