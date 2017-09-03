package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SlipTurnoutTextEdit
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SlipTurnoutTextEditTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SlipTurnoutTextEdit frame = new SlipTurnoutTextEdit();
        Assert.assertNotNull("exists", frame);
        JUnitUtil.dispose(frame);
    }

    @Test
    public void initCheck() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SlipTurnoutTextEdit frame = new SlipTurnoutTextEdit();
        Editor ef = new EditorScaffold();
        SlipTurnoutIcon i = new SlipTurnoutIcon(ef);
        // this test (currently) makes sure there are no exceptions
        // thrown when initComponents is called.
        try {
            frame.initComponents(i, "foo");
        } catch (Exception e) {
            Assert.fail("Exception " + e + " Thrown during initComponents call ");
        }
        JUnitUtil.dispose(frame);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
