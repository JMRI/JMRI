package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AbstractDigitalAction;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test DefaultConditionalNG
 *
 * @author Daniel Bergqvist 2020
 */
public class DefaultConditionalNGTest {

    @Test
    public void testCtor() {
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        assertNotNull( conditionalNG, "exists");

        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            var dc = new DefaultConditionalNG("IQCAbc", null);
            fail("Should not have got here " + dc);
        });
        assertEquals( "system name is not valid", e.getMessage(), "Error message is correct");
    }

    @Test
    public void testState() throws JmriException {
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        conditionalNG.setState(NamedBean.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in DefaultConditionalNG.");

        assertEquals( NamedBean.UNKNOWN, conditionalNG.getState(), "State is correct");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultConditionalNG.");
    }

    @Test
    public void testExecute() throws SocketAlreadyConnectedException, JmriException {
        DefaultLogixNG logixNG = new DefaultLogixNG("IQ1", null);
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        conditionalNG.setParent(logixNG);
        conditionalNG.setRunDelayed(false);
        MyDigitalAction action = new MyDigitalAction("IQDA1", null);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(action);
        conditionalNG.getChild(0).connect(socket);
        assertTrue( conditionalNG.setParentForAllChildren(new ArrayList<>()));

        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.ThrowException);

        action.throwOnExecute = false;
        action.hasExecuted = false;
        conditionalNG.execute();
        assertTrue( action.hasExecuted, "Action is executed");

        action.throwOnExecute = true;
        action.hasExecuted = false;
        conditionalNG.execute();
        JUnitAppender.assertWarnMessage("ConditionalNG IQC123 got an exception during execute: jmri.JmriException: An error has occured");
    }

    @Test
    public void testDescription() {
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC123", null);
        assertEquals( "ConditionalNG: IQC123", conditionalNG.getShortDescription(), "Short description is correct");
        assertEquals( "ConditionalNG: IQC123", conditionalNG.getLongDescription(), "Long description is correct");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        InstanceManager.getDefault(LogixNGPreferences.class).setInstallDebugger(false);
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }



    private static class MyDigitalAction extends AbstractDigitalAction {

        boolean hasExecuted;
        boolean throwOnExecute;

        MyDigitalAction(String sys, String user) throws BadUserNameException, BadSystemNameException {
            super(sys, user);
        }

        @Override
        protected void registerListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected void unregisterListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        protected void disposeMe() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getShortDescription(Locale locale) {
            return "MyDigitalAction";
        }

        @Override
        public String getLongDescription(Locale locale) {
            return "MyDigitalAction";
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported6");
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public LogixNG_Category getCategory() {
            throw new UnsupportedOperationException("Not supported7");
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported9");
        }

        @Override
        public void execute() throws JmriException {
            if (throwOnExecute) {
                throw new JmriException ("An error has occured");
            } else {
                hasExecuted = true;
            }
        }

        @Override
        public Base getDeepCopy(Map<String, String> map, Map<String, String> map1) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base deepCopyChildren(Base base, Map<String, String> map, Map<String, String> map1) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

    }

}
