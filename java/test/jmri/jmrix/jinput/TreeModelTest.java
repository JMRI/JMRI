package jmri.jmrix.jinput;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of TreeModel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TreeModelTest {

    @Test
    public void testInstance() throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", TreeModel.instance());
        // then kill the thread
        TreeModel.instance().terminateThreads();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
