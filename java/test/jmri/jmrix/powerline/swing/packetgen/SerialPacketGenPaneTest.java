package jmri.jmrix.powerline.swing.packetgen;

import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SerialPacketGenPane
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenPaneTest {


    private SerialTrafficControlScaffold tc = null;

    @Test
    public void testCtor() {
        SerialPacketGenPane action = new SerialPacketGenPane();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SerialTrafficControlScaffold();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();        tc = null;
    }
}
