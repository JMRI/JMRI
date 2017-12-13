package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
           Editor ef = new EditorScaffold();
           p = new SlipTurnoutIcon(ef);
        }
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
