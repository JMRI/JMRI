package jmri.jmrix.secsi;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SerialLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    private SerialTrafficControlScaffold tcis = null;
    private SecsiSystemConnectionMemo memo = null;

    @Override
    public String getSystemName(int i) {
        return "VL" + i;
    }

    @Test
    public void testCTor() {
        Assertions.assertNotNull(l, "exists");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SecsiSystemConnectionMemo();
        tcis = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tcis);
        tcis.registerNode(new SerialNode(0, SerialNode.DAUGHTER,tcis));
        l = new SerialLightManager(memo);
    }

    @AfterEach
    public void tearDown() {
        if ( l != null ){
            l.dispose();
        }
        l = null;
        tcis.terminateThreads();
        memo.dispose();
        tcis = null;
        memo = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialLightManagerTest.class);

}
