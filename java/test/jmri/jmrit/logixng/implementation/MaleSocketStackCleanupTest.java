package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.Continue;
import jmri.jmrit.logixng.actions.Exit;
import jmri.jmrit.logixng.actions.For;
import jmri.jmrit.logixng.actions.Return;
import jmri.jmrit.logixng.expressions.DigitalFormula;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test that a male socket removes its local variables from the stack when
 * the action throws an exception that is passed thru, like a Return action.
 *
 * @author Keith Bruce 2026
 */
public class MaleSocketStackCleanupTest {

    private void testStackCount(DigitalActionBean action) throws JmriException {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC1", null);
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);

        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        socket.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Hello");
        conditionalNG.getChild(0).connect(socket);

        logixNG.setParentForAllChildren(new ArrayList<>());
        logixNG.activate();
        logixNG.setEnabled(true);

        for (int i = 1; i <= 3; i++) {
            conditionalNG.execute();
            assertEquals(0, conditionalNG.getStack().getCount(),
                    "stack is empty after execute number " + i);
        }
    }

    @Test
    public void testReturn() throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        testStackCount(new Return(manager.getAutoSystemName(), null));
    }

    @Test
    public void testExit() throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        testStackCount(new Exit(manager.getAutoSystemName(), null));
    }

    /**
     * A loop whose body has a local variable and executes Continue. Each
     * pass must remove the local variable again, or the next pass fails
     * with "Symbol table already contains the variable".
     */
    @Test
    public void testContinueInLoop() throws JmriException, ParserException {
        DigitalActionManager actionManager = InstanceManager.getDefault(DigitalActionManager.class);
        DigitalExpressionManager expressionManager = InstanceManager.getDefault(DigitalExpressionManager.class);

        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG("IQC1", null);
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);

        For forAction = new For(actionManager.getAutoSystemName(), null);
        MaleSocket forSocket = actionManager.registerAction(forAction);
        forSocket.addLocalVariable("count", SymbolTable.InitialValueType.Integer, "0");
        conditionalNG.getChild(0).connect(forSocket);

        DigitalFormula whileExpression = new DigitalFormula(expressionManager.getAutoSystemName(), null);
        whileExpression.setFormula("count++ < 3");
        forAction.getChild(1).connect(expressionManager.registerExpression(whileExpression));

        MaleSocket body = actionManager.registerAction(new Continue(actionManager.getAutoSystemName(), null));
        body.addLocalVariable("myVar", SymbolTable.InitialValueType.String, "Hello");
        forAction.getChild(2).connect(body);

        logixNG.setParentForAllChildren(new ArrayList<>());
        logixNG.activate();
        logixNG.setEnabled(true);

        conditionalNG.execute();
        assertNotNull(conditionalNG.getStack(), "the ConditionalNG has run");
        assertEquals(0, conditionalNG.getStack().getCount(), "stack is empty after the loop");
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
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
