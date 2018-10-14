package jmri.jmrix.maple.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of MapleComponentFactory
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MapleComponentFactoryTest {

    private MapleSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        MapleComponentFactory action = new MapleComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        m = new MapleSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown(); 
    }
}
