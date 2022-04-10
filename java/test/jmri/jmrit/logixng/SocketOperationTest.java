package jmri.jmrit.logixng;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;

import jmri.*;
import jmri.util.*;

import org.junit.*;

/**
 * Try to do a lot of random female socket operations on the female sockets
 * to see if it works. This test does not checks the result, but at least
 * tries to check if a socket operation throws an exception.
 */
public class SocketOperationTest {

    @Test
    public void testLogixNGs() throws PropertyVetoException, Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Add new LogixNG actions and expressions to jmri.jmrit.logixng.CreateLogixNGTreeScaffold
        CreateLogixNGTreeScaffold.createLogixNGTree();

        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);

        java.util.Set<LogixNG> newLogixNG_Set = new java.util.HashSet<>(logixNG_Manager.getNamedBeanSet());
        for (LogixNG aLogixNG : newLogixNG_Set) {
            for (int i=0; i < aLogixNG.getNumConditionalNGs(); i++) {
                FemaleSocket originSocket = aLogixNG.getConditionalNG(i).getFemaleSocket();

                if (originSocket.isConnected()) {
                    Base origin = originSocket.getConnectedSocket();

                    for (int count=0; i < 100; i++) {

                        FemaleSocket child = origin.getChild(random(origin.getChildCount()));

                        FemaleSocketOperation fso = FemaleSocketOperation.values()[
                                random(FemaleSocketOperation.values().length)];

                        if (child.isSocketOperationAllowed(fso)) {
                            child.doSocketOperation(fso);
                        }
                    }
                }
            }
        }
    }


    private int random(int count) {
        return (int) (Math.random() * count);
    }


    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugPowerManager();

        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDefaultSignalMastManager();
//        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initWarrantManager();

//        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
//        JUnitAppender.clearBacklog();    // REMOVE THIS!!!
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeepCopyTest.class);

}
