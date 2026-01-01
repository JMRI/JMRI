package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test FemaleSocket
 *
 * @author Daniel Bergqvist 2018
 */
public class SocketTest {

    @Test
    public void testCtor() {
        FemaleSocketListener listener = new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void disconnected(FemaleSocket socket) {
//                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "An empty conditionalNG");
        DefaultFemaleDigitalActionSocket b = new DefaultFemaleDigitalActionSocket(conditionalNG, listener, "A1");
        assertNotNull( b, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
