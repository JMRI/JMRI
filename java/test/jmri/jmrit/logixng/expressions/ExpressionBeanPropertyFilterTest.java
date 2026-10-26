package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test that the expressions Block, Section and Transit only execute the
 * ConditionalNG when a property they check has changed, and that
 * ConditionalNG_Manager.setRunOnGUIDelayed() uses its argument.
 *
 * @author Keith Bruce 2026
 */
public class ExpressionBeanPropertyFilterTest {

    private LogixNG logixNG;
    private DefaultConditionalNG conditionalNG;
    private AtomicLong executions;

    private void setupConditionalNG(DigitalExpressionBean expression) throws SocketAlreadyConnectedException {
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNG("IQC1", null);
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        logixNG.addConditionalNG(conditionalNG);

        IfThenElse ifThenElse = new IfThenElse("IQDA1", null);
        ifThenElse.setExecuteType(IfThenElse.ExecuteType.AlwaysExecute);
        conditionalNG.getChild(0).connect(
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse));
        ifThenElse.getChild(0).connect(
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression));

        // Count every execution, whether the expression is true or false
        executions = new AtomicLong();
        ifThenElse.getChild(1).connect(InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(new ActionAtomicBoolean(new AtomicBoolean(), true, executions)));
        ifThenElse.getChild(2).connect(InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(new ActionAtomicBoolean(new AtomicBoolean(), true, executions)));

        assertTrue(logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
        executions.set(0);
    }

    @Test
    public void testBlock() throws SocketAlreadyConnectedException, JmriException {
        Block block = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        ExpressionBlock expression = new ExpressionBlock("IQDE1", null);
        expression.getSelectNamedBean().setNamedBean(block);
        expression.getSelectEnum().setEnum(ExpressionBlock.BlockState.Occupied);
        setupConditionalNG(expression);

        block.setComment("A comment");
        assertEquals(0, executions.get(), "a new comment doesn't execute the ConditionalNG");

        block.setValue("Loco 1234");
        assertEquals(0, executions.get(), "a new value doesn't execute the ConditionalNG when the state is checked");

        block.setState(Block.OCCUPIED);
        assertEquals(1, executions.get(), "a new state executes the ConditionalNG");

        expression.unregisterListeners();
        expression.getSelectEnum().setEnum(ExpressionBlock.BlockState.ValueMatches);
        expression.getSelectBlockValue().setValue("Loco 4321");
        expression.registerListeners();
        block.setValue("Loco 4321");
        assertEquals(2, executions.get(), "a new value executes the ConditionalNG when the value is checked");
    }

    @Test
    public void testSection() throws SocketAlreadyConnectedException, JmriException {
        Section section = InstanceManager.getDefault(SectionManager.class).createNewSection("TS1", null);
        ExpressionSection expression = new ExpressionSection("IQDE1", null);
        expression.getSelectNamedBean().setNamedBean(section);
        expression.getSelectEnum().setEnum(ExpressionSection.SectionState.Forward);
        setupConditionalNG(expression);

        section.setComment("A comment");
        assertEquals(0, executions.get(), "a new comment doesn't execute the ConditionalNG");

        section.setState(Section.FORWARD);
        assertEquals(1, executions.get(), "a new state executes the ConditionalNG");
    }

    @Test
    public void testTransit() throws SocketAlreadyConnectedException, JmriException {
        Transit transit = InstanceManager.getDefault(TransitManager.class).createNewTransit("TT1", null);
        ExpressionTransit expression = new ExpressionTransit("IQDE1", null);
        expression.getSelectNamedBean().setNamedBean(transit);
        expression.getSelectEnum().setEnum(ExpressionTransit.TransitState.Assigned);
        setupConditionalNG(expression);

        transit.setComment("A comment");
        assertEquals(0, executions.get(), "a new comment doesn't execute the ConditionalNG");

        transit.setState(Transit.ASSIGNED);
        assertEquals(1, executions.get(), "a new state executes the ConditionalNG");
    }

    @Test
    public void testSetRunOnGUIDelayed() throws SocketAlreadyConnectedException {
        setupConditionalNG(new ExpressionBlock("IQDE1", null));

        InstanceManager.getDefault(ConditionalNG_Manager.class).setRunOnGUIDelayed(true);
        assertTrue(conditionalNG.getRunDelayed(), "run delayed is set");

        InstanceManager.getDefault(ConditionalNG_Manager.class).setRunOnGUIDelayed(false);
        assertFalse(conditionalNG.getRunDelayed(), "run delayed is cleared");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
