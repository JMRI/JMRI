package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of BlockContentsIcon
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class BlockContentsIconTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor ef = new EditorScaffold();
        BlockContentsIcon bci = new BlockContentsIcon("foo",ef);
        Assert.assertNotNull("BlockContentsIcon Constructor",bci);
        ef.getTargetFrame().dispose();
        JUnitUtil.dispose(ef);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }


}
