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
public class LightIconTest extends PositionableTestBase {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LightIcon Constructor",p);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if( !GraphicsEnvironment.isHeadless()) {
           editor = new EditorScaffold();
           p = new LightIcon(editor);
        }
    }

}
