package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Locale;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.BlockValueType;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test which of a Block's values the Local Variable and Table actions copy, and when they copy it.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ActionBlockValueTypeTest {

    private Block block;
    private Memory memory;
    private NamedTable table;
    private ConditionalNG conditionalNG;
    private LogixNG logixNG;

    /** Let the LogixNG thread finish what a change set off: executions run there in turn. */
    private static void settle() {
        LogixNG_Thread.getThread(LogixNG_Thread.DEFAULT_LOGIXNG_THREAD).runOnLogixNG(() -> {
        });
    }

    private static void set(Runnable change) {
        change.run();
        settle();
    }

    /**
     * A ConditionalNG that copies the chosen value of the Block into a local variable, listening to the Block, and
     * then copies the variable into a memory.
     */
    private ActionLocalVariable localVariableAction(BlockValueType type)
            throws SocketAlreadyConnectedException, ParserException {
        DigitalActionManager actions = InstanceManager.getDefault(DigitalActionManager.class);
        DigitalMany many = new DigitalMany(actions.getAutoSystemName(), null);
        MaleSocket manySocket = actions.registerAction(many);
        manySocket.addLocalVariable("result", SymbolTable.InitialValueType.None, null);
        conditionalNG.getChild(0).connect(manySocket);

        ActionLocalVariable copyBlock = new ActionLocalVariable(actions.getAutoSystemName(), null);
        copyBlock.setLocalVariable("result");
        copyBlock.setVariableOperation(ActionLocalVariable.VariableOperation.CopyBlockToVariable);
        copyBlock.getSelectBlockNamedBean().setNamedBean(block);
        copyBlock.setBlockValueType(type);
        copyBlock.setListenToBlock(true);
        many.getChild(0).connect(actions.registerAction(copyBlock));

        ActionMemory copyVariable = new ActionMemory(actions.getAutoSystemName(), null);
        copyVariable.getSelectNamedBean().setNamedBean(memory);
        copyVariable.setMemoryOperation(ActionMemory.MemoryOperation.CopyVariableToMemory);
        copyVariable.setOtherLocalVariable("result");
        many.getChild(1).connect(actions.registerAction(copyVariable));
        return copyBlock;
    }

    /** A ConditionalNG that copies the chosen value of the Block into cell 1, 1 of a table. */
    private ActionTable tableAction(BlockValueType type) throws SocketAlreadyConnectedException, ParserException {
        DigitalActionManager actions = InstanceManager.getDefault(DigitalActionManager.class);
        ActionTable copyBlock = new ActionTable(actions.getAutoSystemName(), null);
        copyBlock.getSelectTableToSet().setTable(table);
        copyBlock.getSelectTableToSet().setTableRowName("1");
        copyBlock.getSelectTableToSet().setTableColumnName("1");
        copyBlock.setVariableOperation(ActionTable.VariableOperation.CopyBlockToVariable);
        copyBlock.getSelectBlockNamedBean().setNamedBean(block);
        copyBlock.setBlockValueType(type);
        copyBlock.setListenToBlock(true);
        MaleSocket socket = actions.registerAction(copyBlock);
        socket.setErrorHandlingType(MaleSocket.ErrorHandlingType.LogError);
        conditionalNG.getChild(0).connect(socket);
        return copyBlock;
    }

    private void start() {
        assertTrue(logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
        settle();
    }

    private Object cell() {
        return table.getCell(1, 1);
    }

    @Test
    public void testBlockValueTypes() {
        block.setValue("BBB");
        block.setAllocatedValue("AAA");
        assertEquals("BBB", BlockValueType.Value.getValue(block));
        assertEquals("AAA", BlockValueType.AllocatedValue.getValue(block));
        assertEquals("BBB", BlockValueType.DisplayValue.getValue(block), "the value, when there is one");
        block.setValue(null);
        assertNull(BlockValueType.Value.getValue(block));
        assertEquals("AAA", BlockValueType.DisplayValue.getValue(block), "the allocated value, when there is no value");

        assertEquals(java.util.List.of(Block.PROPERTY_VALUE), BlockValueType.Value.getPropertyNames());
        assertEquals(java.util.List.of(Block.PROPERTY_ALLOCATED_VALUE), BlockValueType.AllocatedValue.getPropertyNames());
        assertEquals(java.util.List.of(Block.PROPERTY_VALUE, Block.PROPERTY_ALLOCATED_VALUE),
                BlockValueType.DisplayValue.getPropertyNames());
        assertEquals("allocated value", BlockValueType.AllocatedValue.toString());
    }

    @Test
    public void testLocalVariableCopiesTheValue() throws JmriException {
        localVariableAction(BlockValueType.Value);
        start();
        set(() -> block.setAllocatedValue("AAA"));
        assertNull(memory.getValue(), "not run: the value doesn't change with the allocated value");
        set(() -> block.setValue("BBB"));
        assertEquals("BBB", memory.getValue());
    }

    @Test
    public void testLocalVariableCopiesTheAllocatedValue() throws JmriException {
        localVariableAction(BlockValueType.AllocatedValue);
        start();
        set(() -> block.setValue("BBB"));
        assertNull(memory.getValue(), "not run: the allocated value doesn't change with the value");
        set(() -> block.setAllocatedValue("AAA"));
        assertEquals("AAA", memory.getValue());
    }

    @Test
    public void testLocalVariableCopiesTheDisplayValue() throws JmriException {
        localVariableAction(BlockValueType.DisplayValue);
        start();
        set(() -> block.setAllocatedValue("AAA"));
        assertEquals("AAA", memory.getValue(), "the allocated value, while the Block has no value");
        set(() -> block.setValue("BBB"));
        assertEquals("BBB", memory.getValue(), "the value");
        set(() -> block.setValue(null));
        assertEquals("AAA", memory.getValue(), "the allocated value again");
    }

    @Test
    public void testTableCopiesEachValue() throws JmriException {
        tableAction(BlockValueType.DisplayValue);
        start();
        set(() -> block.setAllocatedValue("AAA"));
        assertEquals("AAA", cell(), "the allocated value, while the Block has no value");
        set(() -> block.setValue("BBB"));
        assertEquals("BBB", cell(), "the value");
    }

    @Test
    public void testTableCopiesTheAllocatedValue() throws JmriException {
        tableAction(BlockValueType.AllocatedValue);
        start();
        set(() -> block.setValue("BBB"));
        assertEquals("", String.valueOf(cell() == null ? "" : cell()), "not run: the allocated value doesn't change with the value");
        set(() -> block.setAllocatedValue("AAA"));
        assertEquals("AAA", cell());
    }

    @Test
    public void testLocalVariableStopsListeningWhenDisabled() throws JmriException {
        ActionLocalVariable local = localVariableAction(BlockValueType.DisplayValue);
        start();
        set(() -> block.setAllocatedValue("AAA"));
        assertEquals("AAA", memory.getValue());
        set(() -> logixNG.setEnabled(false));
        set(() -> block.setValue("BBB"));
        set(() -> block.setAllocatedValue("CCC"));
        assertEquals("AAA", memory.getValue(), "not run: disabled, so no longer listening to either property");
        // changing the choice logs an error while listeners are registered
        local.setBlockValueType(BlockValueType.AllocatedValue);
        set(() -> logixNG.setEnabled(true));
        set(() -> block.setAllocatedValue("DDD"));
        assertEquals("DDD", memory.getValue(), "listening again, to the new choice");
    }

    @Test
    public void testTableStopsListeningWhenDisabled() throws JmriException {
        ActionTable inTable = tableAction(BlockValueType.DisplayValue);
        start();
        set(() -> block.setAllocatedValue("AAA"));
        assertEquals("AAA", cell());
        set(() -> logixNG.setEnabled(false));
        set(() -> block.setValue("BBB"));
        set(() -> block.setAllocatedValue("CCC"));
        assertEquals("AAA", cell(), "not run: disabled, so no longer listening to either property");
        inTable.setBlockValueType(BlockValueType.AllocatedValue);
        set(() -> logixNG.setEnabled(true));
        set(() -> block.setAllocatedValue("DDD"));
        assertEquals("DDD", cell(), "listening again, to the new choice");
    }

    @Test
    public void testDescriptions() throws JmriException {
        ActionLocalVariable local = localVariableAction(BlockValueType.Value);
        assertEquals("Set variable result to the value of block IB1 (Listen)", local.getLongDescription(Locale.ENGLISH));
        local.setBlockValueType(BlockValueType.AllocatedValue);
        assertEquals("Set variable result to the allocated value of block IB1 (Listen)",
                local.getLongDescription(Locale.ENGLISH));
        local.setBlockValueType(BlockValueType.DisplayValue);
        assertEquals("Set variable result to the display value of block IB1 (Listen)",
                local.getLongDescription(Locale.ENGLISH));
    }

    /** The item a copy's male sockets hold. */
    private static Base item(Base copy) {
        Base b = copy;
        while (b instanceof MaleSocket) {
            b = ((MaleSocket) b).getObject();
        }
        return b;
    }

    @Test
    public void testCopy() throws JmriException {
        ActionLocalVariable local = localVariableAction(BlockValueType.DisplayValue);
        ActionLocalVariable copy = (ActionLocalVariable) item(local.getDeepCopy(new java.util.HashMap<>(),
                new java.util.HashMap<>()));
        assertEquals(BlockValueType.DisplayValue, copy.getBlockValueType());
        DigitalActionManager actions = InstanceManager.getDefault(DigitalActionManager.class);
        ActionTable inTable = new ActionTable(actions.getAutoSystemName(), null);
        inTable.setBlockValueType(BlockValueType.AllocatedValue);
        ActionTable tableCopy = (ActionTable) item(inTable.getDeepCopy(new java.util.HashMap<>(),
                new java.util.HashMap<>()));
        assertEquals(BlockValueType.AllocatedValue, tableCopy.getBlockValueType());
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
        memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        table = InstanceManager.getDefault(NamedTableManager.class).newInternalTable("IQT1", null, 2, 2);

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("IQ1", "A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
