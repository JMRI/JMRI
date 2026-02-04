package jmri.jmrix.openlcb.swing.send;

import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.openlcb.OlcbSystemConnectionMemoScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

// import jmri.jmrix.can.TestTrafficController;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.mockito.Mockito;

/**
 * @author Bob Jacobsen Copyright 2013
 * @author Paul Bender Copyright(C) 2016
 */
public class OpenLcbCanSendActionTest {

    private jmri.jmrix.can.CanSystemConnectionMemo memo;
    // jmri.jmrix.can.TrafficController tc;

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCtor() {
        OpenLcbCanSendAction h = new OpenLcbCanSendAction();
        Assertions.assertNotNull( h, "Action object non-null");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        memo = Mockito.mock(OlcbSystemConnectionMemoScaffold.class);
        InstanceManager.setDefault(CanSystemConnectionMemo.class,memo);
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        JUnitUtil.tearDown();

    }
}
