package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of FunctionButtonPropertyEditor
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class FunctionButtonPropertyEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        FunctionButtonPropertyEditor dialog = new FunctionButtonPropertyEditor();
        Assert.assertNotNull("exists", dialog);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
