package jmri.jmrix.grapevine.nodeconfig;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.SerialTrafficControlScaffold;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NodeConfigActionTest {

    private GrapevineSystemConnectionMemo memo = null; 

    @Test
    public void testCTor() {
        NodeConfigAction t = new NodeConfigAction(memo);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new GrapevineSystemConnectionMemo();
        SerialTrafficController tc = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tc);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(NodeConfigActionTest.class);

}
