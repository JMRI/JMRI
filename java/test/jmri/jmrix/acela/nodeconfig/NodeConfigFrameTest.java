package jmri.jmrix.acela.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NodeConfigFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NodeConfigFrameTest {

    private AcelaSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        NodeConfigFrame f = new NodeConfigFrame(memo);
        Assert.assertNotNull("exists", f);
        f.dispose();
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

    // The minimal setup for log4J

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new AcelaSystemConnectionMemo(); 
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
