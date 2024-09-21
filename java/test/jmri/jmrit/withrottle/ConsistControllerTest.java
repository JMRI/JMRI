package jmri.jmrit.withrottle;

import jmri.*;
import jmri.jmrit.consisttool.TestConsistManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple functioning of ConsistController
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ConsistControllerTest {

    @Test
    public void testConsistControllerCtor() {
        ConsistController t = new ConsistController();
        assertNotNull( t, "exists" );
        assertTrue(t.verifyCreation());
    }

    @Test
    public void testSendDataForConsist(){

        Consist consist = InstanceManager.getDefault(ConsistManager.class).getConsist(new DccLocoAddress(44, true));
        Assertions.assertNotNull(consist);

        ConsistController t = new ConsistController();
        ControllerInterfaceScaffold scaf = new ControllerInterfaceScaffold();
        t.addControllerListener(scaf);

        t.sendDataForConsist(consist);

        String last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals( "RCD}|{44(L)}|{44(L)", last);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugCommandStation();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
