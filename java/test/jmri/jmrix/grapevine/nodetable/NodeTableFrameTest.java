package jmri.jmrix.grapevine.nodetable;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.SerialTrafficControlScaffold;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NodeTableFrameTest extends jmri.util.JmriJFrameTestBase {

    private GrapevineSystemConnectionMemo memo = null; 

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new GrapevineSystemConnectionMemo();
        SerialTrafficController tc = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tc);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new NodeTableFrame(memo);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NodeTableFrameTest.class);

}
