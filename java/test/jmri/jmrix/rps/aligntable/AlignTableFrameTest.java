package jmri.jmrix.rps.aligntable;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.rps.RpsSystemConnectionMemo;

/**
 * Test simple functioning of AlignTableFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class AlignTableFrameTest {

    private RpsSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        AlignTableFrame action = new AlignTableFrame(memo);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new RpsSystemConnectionMemo();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
