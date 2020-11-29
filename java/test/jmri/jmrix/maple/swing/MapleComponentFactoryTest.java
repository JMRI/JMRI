package jmri.jmrix.maple.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.maple.MapleSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of MapleComponentFactory
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MapleComponentFactoryTest {

    private MapleSystemConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        MapleComponentFactory action = new MapleComponentFactory(m);
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new MapleSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown(); 
    }
}
