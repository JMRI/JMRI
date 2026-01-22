package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Category;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test True
 *
 * @author Daniel Bergqvist 2018
 */
public class TrueTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private True expressionTrue;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;


    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }

    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }

    @Override
    public MaleSocket getConnectableChild() {
        return null;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format("Always true ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Always true ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new True(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        True expression2;

        expression2 = new True("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Always true", expression2.getLongDescription(), "String matches");

        expression2 = new True("IQDE321", "My expression");
        assertNotNull( expression2, "object exists");
        assertEquals( "My expression", expression2.getUserName(), "Username matches");
        assertEquals( "Always true", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new True("IQE55:12:XY11", null);
            fail("Should have thrown above " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            // Illegal system name
            var t = new True("IQE55:12:XY11", "A name");
            fail("Should have thrown above " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionTrue.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionTrue.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertEquals( LogixNG_Category.OTHER, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        DigitalExpressionBean e1 = new True("IQDE321", null);
        assertTrue("Always true".equals(e1.getShortDescription()));
        assertTrue("Always true".equals(e1.getLongDescription()));
    }

    @Test
    public void testExpression() throws JmriException {
        DigitalExpressionBean t = new True("IQDE321", null);
        assertTrue( t.evaluate(), "Expression is true");
    }

    @Before
    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.COMMON;
        _isExternal = false;

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);

        expressionTrue = new True("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionTrue);
        ifThenElse.getChild(0).connect(maleSocket2);

        _base = expressionTrue;
        _baseMaleSocket = maleSocket2;

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
