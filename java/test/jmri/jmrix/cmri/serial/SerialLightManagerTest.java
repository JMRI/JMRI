package jmri.jmrix.cmri.serial;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SerialLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold tcis = null;

    @Override
    public String getSystemName(int i) {
        return ("CL"+i);
    }

    @Test
    public void testCTor() {
        Assertions.assertNotNull(l, "exists");
    }

    @Test
    public void testCanAddRange() {
        Assertions.assertTrue(l.allowMultipleAdditions(getSystemName(2)));
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(tcis);
        Assertions.assertNotNull(new SerialNode(0, SerialNode.SMINI,tcis));
        l = new SerialLightManager(memo);
    }

    @AfterEach
    public void tearDown() {
        if (tcis != null) tcis.terminateThreads();
        tcis = null;
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
