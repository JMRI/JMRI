package jmri.jmrix.oaktree.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.oaktree.OakTreeSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of NodeConfigFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NodeConfigFrameTest extends jmri.util.JmriJFrameTestBase {

    private OakTreeSystemConnectionMemo memo = null;

    @Test
    public void testGetTitle(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        frame.initComponents();
        Assert.assertEquals("title", "Configure Nodes", frame.getTitle());
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        memo = new OakTreeSystemConnectionMemo();
        if(!GraphicsEnvironment.isHeadless()) {
           frame = new NodeConfigFrame(memo);
	}
    }

    @After
    @Override
    public void tearDown() {
        memo.dispose();
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

}
