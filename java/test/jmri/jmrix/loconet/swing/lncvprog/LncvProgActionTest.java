package jmri.jmrix.loconet.swing.lncvprog;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;
import jmri.jmrix.loconet.hexfile.LnHexFileAction;
import jmri.jmrix.loconet.swing.LnNamedPaneAction;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class LncvProgActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        LncvProgAction t = new LncvProgAction();
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        LocoNetSystemConnectionMemo memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class, memo);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
