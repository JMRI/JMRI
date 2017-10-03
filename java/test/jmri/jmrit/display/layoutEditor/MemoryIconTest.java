package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of MemoryIcon
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MemoryIconTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MemoryIcon t = new MemoryIcon("test", new LayoutEditor());
        Assert.assertNotNull("exists", t);
        t.getEditor().dispose();
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
