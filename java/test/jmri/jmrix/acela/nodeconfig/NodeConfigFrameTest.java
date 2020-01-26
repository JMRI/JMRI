package jmri.jmrix.acela.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of NodeConfigFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NodeConfigFrameTest extends jmri.util.JmriJFrameTestBase {

    private AcelaSystemConnectionMemo memo = null;

    @Test
    public void testGetTitle(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        frame.initComponents();
        Assert.assertEquals("title","Configure Nodes",frame.getTitle());
    }

    // The minimal setup for log4J

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        memo = new AcelaSystemConnectionMemo(); 
        if(!GraphicsEnvironment.isHeadless()){
           frame = new NodeConfigFrame(memo);
        }
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
