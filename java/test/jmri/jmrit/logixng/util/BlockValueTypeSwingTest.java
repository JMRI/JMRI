package jmri.jmrit.logixng.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.JDialog;
import javax.swing.JPanel;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.jmrit.logixng.actions.ActionLocalVariable;
import jmri.jmrit.logixng.actions.ActionTable;
import jmri.jmrit.logixng.actions.swing.ActionLocalVariableSwing;
import jmri.jmrit.logixng.actions.swing.ActionTableSwing;
import jmri.jmrit.logixng.expressions.ExpressionBlock;
import jmri.jmrit.logixng.expressions.swing.ExpressionBlockSwing;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test that the Block expression's and the Local Variable and Table actions' editors show, and save back, which of a
 * Block's values is used.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class BlockValueTypeSwingTest {

    private Block block;
    private JDialog dialog;

    @Test
    @DisabledIfHeadless
    public void testLocalVariableEditorKeepsTheChoice() throws ParserException {
        for (BlockValueType type : BlockValueType.values()) {
            ActionLocalVariable action = new ActionLocalVariable("IQDA1", null);
            action.setVariableOperation(ActionLocalVariable.VariableOperation.CopyBlockToVariable);
            action.getSelectBlockNamedBean().setNamedBean(block);
            action.setBlockValueType(type);

            ActionLocalVariableSwing editor = new ActionLocalVariableSwing();
            editor.setJDialog(dialog);
            editor.getConfigPanel(action, new JPanel());
            ActionLocalVariable saved = new ActionLocalVariable("IQDA2", null);
            editor.updateObject(saved);
            assertEquals(type, saved.getBlockValueType(), "saved back: " + type);
            assertEquals(ActionLocalVariable.VariableOperation.CopyBlockToVariable, saved.getVariableOperation());
        }
    }

    @Test
    @DisabledIfHeadless
    public void testTableEditorKeepsTheChoice() throws ParserException {
        for (BlockValueType type : BlockValueType.values()) {
            ActionTable action = new ActionTable("IQDA1", null);
            action.setVariableOperation(ActionTable.VariableOperation.CopyBlockToVariable);
            action.getSelectBlockNamedBean().setNamedBean(block);
            action.setBlockValueType(type);

            ActionTableSwing editor = new ActionTableSwing();
            editor.setJDialog(dialog);
            editor.getConfigPanel(action, new JPanel());
            ActionTable saved = new ActionTable("IQDA2", null);
            editor.updateObject(saved);
            assertEquals(type, saved.getBlockValueType(), "saved back: " + type);
            assertEquals(ActionTable.VariableOperation.CopyBlockToVariable, saved.getVariableOperation());
        }
    }

    @Test
    @DisabledIfHeadless
    public void testBlockExpressionEditorKeepsTheChoice() {
        for (ExpressionBlock.BlockState state : new ExpressionBlock.BlockState[]{
                ExpressionBlock.BlockState.ValueMatches,
                ExpressionBlock.BlockState.AllocatedValueMatches,
                ExpressionBlock.BlockState.DisplayValueMatches}) {
            ExpressionBlock expression = new ExpressionBlock("IQDE1", null);
            expression.getSelectNamedBean().setNamedBean(block);
            expression.getSelectEnum().setEnum(state);
            expression.getSelectBlockValue().setValue("AAA");

            ExpressionBlockSwing editor = new ExpressionBlockSwing();
            editor.setJDialog(dialog);
            editor.getConfigPanel(expression, new JPanel());
            ExpressionBlock saved = new ExpressionBlock("IQDE2", null);
            editor.updateObject(saved);
            assertEquals(state, saved.getSelectEnum().getEnum(), "saved back: " + state);
            assertEquals("AAA", saved.getSelectBlockValue().getValue(), "the string is kept for " + state);
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixNGManager();
        block = InstanceManager.getDefault(BlockManager.class).provide("IB1");
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            dialog = new JDialog();
        }
    }

    @AfterEach
    public void tearDown() {
        if (dialog != null) {
            JUnitUtil.dispose(dialog);
        }
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
