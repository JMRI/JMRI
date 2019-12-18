package jmri.jmrix.secsi.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.secsi.SerialTrafficController;
import jmri.jmrix.secsi.SerialTrafficControlScaffold;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Test simple functioning of NodeConfigFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NodeConfigFrameTest extends jmri.util.JmriJFrameTestBase {

    private SecsiSystemConnectionMemo memo = null;

    @Test
    public void testGetTitle(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        frame.initComponents();
        Assert.assertEquals("title","Configure Nodes",frame.getTitle());
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        memo = new SecsiSystemConnectionMemo();
        memo.setTrafficController(new SerialTrafficControlScaffold(memo));
        if(!GraphicsEnvironment.isHeadless()){
           frame = new NodeConfigFrame(memo);
	    }
    }

    @After
    @Override
    public void tearDown() {
	    memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    	super.tearDown();
    }
}
