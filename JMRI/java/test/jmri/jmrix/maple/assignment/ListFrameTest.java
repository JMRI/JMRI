package jmri.jmrix.maple.assignment;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ListFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ListFrameTest {

    private MapleSystemConnectionMemo _memo = null;

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ListFrame action = new ListFrame(_memo);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        _memo = new MapleSystemConnectionMemo("K", "Maple");
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
