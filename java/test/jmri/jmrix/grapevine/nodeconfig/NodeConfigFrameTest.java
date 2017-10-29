package jmri.jmrix.grapevine.nodeconfig;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.SerialListener;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NodeConfigFrameTest {

    private GrapevineSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigFrame t = new NodeConfigFrame();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testInitComponents() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        NodeConfigFrame t = new NodeConfigFrame();
        // for now, just makes ure there isn't an exception.
        t.initComponents();
        t.dispose();
    }

    @Test
    public void testGetTitle(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        NodeConfigFrame t = new NodeConfigFrame();
        t.initComponents();
        Assert.assertEquals("title","Configure Nodes",t.getTitle());
        t.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SerialTrafficController tc = new SerialTrafficController(){
           @Override
           public void sendSerialMessage(SerialMessage m,SerialListener reply) {
           }
        };
        memo = new GrapevineSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NodeConfigFrameTest.class);

}
