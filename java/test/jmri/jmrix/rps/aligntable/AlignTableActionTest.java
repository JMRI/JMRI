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
 * Test simple functioning of AlignTableAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class AlignTableActionTest {

    private RpsSystemConnectionMemo memo = null;

    @Test
    public void testStringMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AlignTableAction action = new AlignTableAction("RPS test Action",memo);
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AlignTableAction action = new AlignTableAction(memo);
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
