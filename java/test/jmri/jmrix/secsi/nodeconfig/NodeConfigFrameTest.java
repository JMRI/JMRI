package jmri.jmrix.secsi.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.secsi.SerialTrafficController;
import jmri.jmrix.secsi.SerialTrafficControlScaffold;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Test simple functioning of NodeConfigFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NodeConfigFrameTest {

    private SecsiSystemConnectionMemo memo = null;

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigFrame action = new NodeConfigFrame(memo);
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testInitComponents() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        NodeConfigFrame t = new NodeConfigFrame(memo);
        // for now, just makes ure there isn't an exception.
        t.initComponents();
        t.dispose();
    }

    @Test
    public void testGetTitle(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        NodeConfigFrame t = new NodeConfigFrame(memo);
        t.initComponents();
        Assert.assertEquals("title","Configure Nodes",t.getTitle());
        t.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SerialTrafficController tc = new SerialTrafficControlScaffold();
        memo = new SecsiSystemConnectionMemo();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
