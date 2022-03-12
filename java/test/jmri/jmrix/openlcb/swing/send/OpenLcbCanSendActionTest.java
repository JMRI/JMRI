package jmri.jmrix.openlcb.swing.send;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.openlcb.OlcbSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.can.TestTrafficController;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.mockito.Mockito;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Bob Jacobsen Copyright 2013
 * @author Paul Bender Copyright(C) 2016
 */
public class OpenLcbCanSendActionTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo;
    jmri.jmrix.can.TrafficController tc;

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCtor() {
        OpenLcbCanSendAction h = new OpenLcbCanSendAction();
        assertThat(h).withFailMessage("Action object non-null").isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        memo = Mockito.mock(OlcbSystemConnectionMemo.class);
        InstanceManager.setDefault(CanSystemConnectionMemo.class,memo);
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        JUnitUtil.tearDown();

    }
}
