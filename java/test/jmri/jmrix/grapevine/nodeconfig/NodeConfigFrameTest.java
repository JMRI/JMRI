package jmri.jmrix.grapevine.nodeconfig;

import java.awt.GraphicsEnvironment;
import org.junit.*;
import jmri.util.JUnitUtil;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.SerialTrafficControlScaffold;

/**
 * @author Paul Bender Copyright (C) 2017	
 */
public class NodeConfigFrameTest extends jmri.util.JmriJFrameTestBase {

    private GrapevineSystemConnectionMemo memo = null;

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

        memo = new GrapevineSystemConnectionMemo();
        SerialTrafficController tc = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tc);
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

    // private final static Logger log = LoggerFactory.getLogger(NodeConfigFrameTest.class);

}
