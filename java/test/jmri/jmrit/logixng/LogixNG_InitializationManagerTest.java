package jmri.jmrit.logixng;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.actions.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test LogixNG_InitializationManager
 * 
 * @author Daniel Bergqvist 2021
 */
public class LogixNG_InitializationManagerTest {

    List<AtomicBoolean> abList = new ArrayList<>();
    
    private AtomicBoolean getAB() {
        AtomicBoolean ab = new AtomicBoolean();
        abList.add(ab);
        return ab;
    }
    
    private boolean checkAB() {
        for (AtomicBoolean ab : abList) {
            if (!ab.get()) return false;
        }
        return true;
    }
    
    @Test
    public void testInitialization() throws SocketAlreadyConnectedException {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        
        LogixNG_Thread threadL2 = LogixNG_Thread.createNewThread("Another thread");
        LogixNG_Thread threadL7 = LogixNG_Thread.createNewThread("Some other thread");
        LogixNG_Thread threadL5 = LogixNG_Thread.createNewThread("A different thread");
        
        MyAction.getLogixNG("IQ4", "LogixNG 4", getAB(), printWriter, 0, LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
        MyAction.getLogixNG("IQ5", "LogixNG 5", getAB(), printWriter, 100, threadL5.getThreadId());   // Long delay on separate thread
        MyAction.getLogixNG("IQ2", "LogixNG 2", getAB(), printWriter, 500, threadL2.getThreadId());   // Long delay on separate thread
        MyAction.getLogixNG("IQ6", "LogixNG 6", getAB(), printWriter, 0, LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
        MyAction.getLogixNG("IQ7", "LogixNG 7", getAB(), printWriter, 100, threadL7.getThreadId());   // Long delay on separate thread
        MyAction.getLogixNG("IQ1", "LogixNG 1", getAB(), printWriter, 0, LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
        MyAction.getLogixNG("IQ9", "LogixNG 9", getAB(), printWriter, 100, LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
        MyAction.getLogixNG("IQ8", "LogixNG 8", getAB(), printWriter, 0, LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
        MyAction.getLogixNG("IQ3", "LogixNG 3", getAB(), printWriter, 0, LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
        
        LogixNG l2 = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ2");
        LogixNG l7 = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ7");
        LogixNG l8 = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ8");
        
        LogixNG_InitializationManager initManager =
                InstanceManager.getDefault(LogixNG_InitializationManager.class);
        
        initManager.add(l7);
        initManager.add(l2);
        initManager.add(l8);
        
        // No LogixNG has been executed yet.
        Assert.assertEquals("Strings are equal", "", stringWriter.toString());
        
        InstanceManager.getDefault(LogixNG_Manager.class)
                .activateAllLogixNGs(true, true);
        
        boolean result = JUnitUtil.waitFor(() -> {return checkAB();});
        Assert.assertTrue(result);
        
        String expectedResult =
                // These are registered in the init manager
                "LogixNG 7: start\n" +
                "LogixNG 7: end\n" +
                "LogixNG 2: start\n" +
                "LogixNG 2: end\n" +
                "LogixNG 8: start\n" +
                "LogixNG 8: end\n";
        Assert.assertTrue(stringWriter.toString().startsWith(expectedResult));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager(false);
    }

    @After
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
    
    
    private static final class MyAction extends ActionAtomicBoolean {
        
        private final AtomicBoolean _ab;
        private final PrintWriter _printWriter;
        private final long _delay;
        
        public MyAction(
                String userName,
                AtomicBoolean ab,
                PrintWriter printWriter,
                long delay) {
            
            super(ab, false);
            setUserName(userName);
            _ab = ab;
            _printWriter = printWriter;
            _delay = delay;
        }
        
        @Override
        public void execute() {
//            System.out.format("%s: start\n", getUserName());
            _printWriter.format("%s: start\n", getUserName());
            try {
                Thread.sleep(_delay);
            } catch (InterruptedException ex) {
                ex.printStackTrace(_printWriter);
            }
//            System.out.format("%s: end\n", getUserName());
            _printWriter.format("%s: end\n", getUserName());
            _printWriter.flush();
            _ab.set(true);
        }
        
        public static LogixNG getLogixNG(
                String systemName,
                String userName,
                AtomicBoolean ab,
                PrintWriter printWriter,
                long delay,
                int threadID)
                throws SocketAlreadyConnectedException {
            
            LogixNG logixNG =
                    InstanceManager.getDefault(LogixNG_Manager.class)
                            .createLogixNG(systemName, null);
            
            systemName =
                    InstanceManager.getDefault(ConditionalNG_Manager.class)
                            .getAutoSystemName();
            ConditionalNG conditionalNG =
                    new DefaultConditionalNGScaffold(systemName, null, threadID);
            InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
            conditionalNG.setEnabled(true);
            logixNG.addConditionalNG(conditionalNG);
            
            MyAction action = new MyAction(userName, ab, printWriter, delay);
            MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
            conditionalNG.getChild(0).connect(socket);
            
            logixNG.setEnabled(true);
            
            return logixNG;
        }
        
    }
    
}
