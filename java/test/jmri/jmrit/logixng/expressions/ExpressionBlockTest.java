package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ExpressionBlock
 *
 * @author Daniel Bergqvist 2022
 */
public class ExpressionBlockTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionBlock expressionBlock;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    private Block block;


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
        return String.format("Block \"IB1\" is Occupied ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Block \"IB1\" is Occupied ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionBlock(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() throws JmriException {
        ExpressionBlock expression2;
        assertNotNull( block, "block is not null");
        block.setState(Block.OCCUPIED);

        expression2 = new ExpressionBlock("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Block \"''\" is Occupied", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionBlock("IQDE321", "My block");
        assertNotNull( expression2, "object exists");
        assertEquals( "My block", expression2.getUserName(), "Username matches");
        assertEquals( "Block \"''\" is Occupied", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionBlock("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(block);
        assertSame( block, expression2.getSelectNamedBean().getNamedBean().getBean(), "block is correct");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Block \"IB1\" is Occupied", expression2.getLongDescription(), "String matches");

        Block s = InstanceManager.getDefault(BlockManager.class).provide("IB2");
        expression2 = new ExpressionBlock("IQDE321", "My block");
        expression2.getSelectNamedBean().setNamedBean(s);
        assertSame( s, expression2.getSelectNamedBean().getNamedBean().getBean(), "block is correct");
        assertNotNull( expression2, "object exists");
        assertEquals( "My block", expression2.getUserName(), "Username matches");
        assertEquals( "Block \"IB2\" is Occupied", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionBlock("IQE55:12:XY11", null);
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionBlock("IQE55:12:XY11", "A name");
            fail("Should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionBlock.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionBlock.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testBlockState() {
        assertEquals( "not Occupied", ExpressionBlock.BlockState.NotOccupied.toString(), "String matches");
        assertEquals( "Occupied", ExpressionBlock.BlockState.Occupied.toString(), "String matches");
        assertEquals( "some other state", ExpressionBlock.BlockState.Other.toString(), "String matches");
        assertEquals( "Allocated", ExpressionBlock.BlockState.Allocated.toString(), "String matches");
        assertEquals( "equal to", ExpressionBlock.BlockState.ValueMatches.toString(), "String matches");

        assertEquals( Block.UNOCCUPIED, ExpressionBlock.BlockState.NotOccupied.getID(), "ID matches");
        assertEquals( Block.OCCUPIED, ExpressionBlock.BlockState.Occupied.getID(), "ID matches");
        assertEquals( -1, ExpressionBlock.BlockState.Other.getID(), "ID matches");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionBlock.getSelectNamedBean().removeNamedBean();
        assertEquals("Block", expressionBlock.getShortDescription());
        assertEquals("Block \"''\" is Occupied", expressionBlock.getLongDescription());
        expressionBlock.getSelectNamedBean().setNamedBean(block);
        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionBlock.getSelectEnum().setEnum(ExpressionBlock.BlockState.NotOccupied);
        assertEquals("Block \"IB1\" is not Occupied", expressionBlock.getLongDescription());
        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        assertEquals("Block \"IB1\" is not not Occupied", expressionBlock.getLongDescription());
        expressionBlock.getSelectEnum().setEnum(ExpressionBlock.BlockState.Other);
        assertEquals("Block \"IB1\"  some other state", expressionBlock.getLongDescription());
    }

    private void setBlockStatus(Block b, int status) {
        Sensor sensor = b.getSensor();

        switch (status) {
            case Block.UNOCCUPIED:
                if (sensor != null) {
                    try {
                        sensor.setKnownState(Sensor.INACTIVE);
                    } catch (JmriException ex) {
                        log.debug("Exception setting sensor inactive");
                    }
                } else {
                    throw new IllegalArgumentException("Block.getSensor() is null: " + block.getDisplayName());
                }
                break;

            case Block.OCCUPIED:
                if (sensor != null) {
                    try {
                        sensor.setKnownState(Sensor.ACTIVE);
                    } catch (JmriException ex) {
                        log.debug("Exception setting sensor active");
                    }
                } else {
                    throw new IllegalArgumentException("Block.getSensor() is null: " + block.getDisplayName());
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown status " + Integer.toString(status)+" for block " + block.getDisplayName());
        }
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Clear flag
        atomicBoolean.set(false);
        // Turn light off
        setBlockStatus(block, Block.UNOCCUPIED);
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionBlock.getSelectNamedBean().setNamedBean(block);
        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionBlock.getSelectEnum().setEnum(ExpressionBlock.BlockState.Occupied);

        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Activate the block. This should not execute the conditional.
        setBlockStatus(block, Block.OCCUPIED);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Inactivate the block. This should not execute the conditional.
        setBlockStatus(block, Block.UNOCCUPIED);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Activate the block. This should execute the conditional.
        setBlockStatus(block, Block.OCCUPIED);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Inactivate the block. This should not execute the conditional.
        setBlockStatus(block, Block.UNOCCUPIED);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Test IS_NOT
        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Activate the block. This should not execute the conditional.
        setBlockStatus(block, Block.OCCUPIED);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Inactivate the block. This should not execute the conditional.
        setBlockStatus(block, Block.UNOCCUPIED);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
    }

    @Test
    public void testSetBlock() {
        expressionBlock.unregisterListeners();

        Block otherBlock = InstanceManager.getDefault(BlockManager.class).provide("IM99");
        assertNotEquals( otherBlock, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "Blocks are different");
        expressionBlock.getSelectNamedBean().setNamedBean(otherBlock);
        assertEquals( otherBlock, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "Blocks are equal");

        NamedBeanHandle<Block> otherBlockHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherBlock.getDisplayName(), otherBlock);
        expressionBlock.getSelectNamedBean().removeNamedBean();
        assertNull( expressionBlock.getSelectNamedBean().getNamedBean(), "Block is null");
        expressionBlock.getSelectNamedBean().setNamedBean(otherBlockHandle);
        assertEquals( otherBlock, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "Blocks are equal");
        assertEquals( otherBlockHandle, expressionBlock.getSelectNamedBean().getNamedBean(), "BlockHandles are equal");
    }

    @Test
    public void testSetBlock2() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        Block block11 = InstanceManager.getDefault(BlockManager.class).provide("IB11");
        Block block12 = InstanceManager.getDefault(BlockManager.class).provide("IB12");
        NamedBeanHandle<Block> blockHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(block12.getDisplayName(), block12);
        Block block13 = InstanceManager.getDefault(BlockManager.class).provide("IB13");
        Block block14 = InstanceManager.getDefault(BlockManager.class).provide("IB14");
        block14.setUserName("Some user name");

        expressionBlock.getSelectNamedBean().removeNamedBean();
        assertNull( expressionBlock.getSelectNamedBean().getNamedBean(), "block handle is null");

        expressionBlock.getSelectNamedBean().setNamedBean(block11);
        assertSame( block11, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "block is correct");

        expressionBlock.getSelectNamedBean().removeNamedBean();
        assertNull( expressionBlock.getSelectNamedBean().getNamedBean(), "block handle is null");

        expressionBlock.getSelectNamedBean().setNamedBean(blockHandle12);
        assertSame( blockHandle12, expressionBlock.getSelectNamedBean().getNamedBean(), "block handle is correct");

        expressionBlock.getSelectNamedBean().setNamedBean("A non existent block");
        assertNull( expressionBlock.getSelectNamedBean().getNamedBean(), "block handle is null");
        JUnitAppender.assertErrorMessage("Block \"A non existent block\" is not found");

        expressionBlock.getSelectNamedBean().setNamedBean(block13.getSystemName());
        assertSame( block13, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "block is correct");

        String userName = block14.getUserName();
        assertNotNull( userName, "block is not null");
        expressionBlock.getSelectNamedBean().setNamedBean(userName);
        assertSame( block14, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "block is correct");
    }

    @Test
    public void testSetBlockException() {
        assertNotNull( block, "Block is not null");
        assertNotNull( expressionBlock.getSelectNamedBean().getNamedBean(), "Block is not null");
        expressionBlock.registerListeners();
        RuntimeException ex = assertThrows( RuntimeException.class, () ->
            expressionBlock.getSelectNamedBean().setNamedBean("A block"), "Expected exception thrown");
        assertNotNull(ex);

        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () -> {
            Block block99 = InstanceManager.getDefault(BlockManager.class).provide("IS99");
            NamedBeanHandle<Block> blockHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(block99.getDisplayName(), block99);
            expressionBlock.getSelectNamedBean().setNamedBean(blockHandle99);
        });
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () ->
            expressionBlock.getSelectNamedBean().removeNamedBean(), "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");
    }

    @Test
    public void testRegisterListeners() {
        // Test registerListeners() when the ExpressionBlock has no block
        conditionalNG.setEnabled(false);
        expressionBlock.getSelectNamedBean().removeNamedBean();
        conditionalNG.setEnabled(true);
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Get the expressionBlock and set the block
        assertNotNull( block, "Block is not null");
        expressionBlock.getSelectNamedBean().setNamedBean(block);

        // Get some other block for later use
        Block otherBlock = InstanceManager.getDefault(BlockManager.class).provide("IM99");
        assertNotNull( otherBlock, "Block is not null");
        assertNotEquals( block, otherBlock, "Block is not equal");

        // Test vetoableChange() for some other propery
        expressionBlock.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( block, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "Block matches");

        // Test vetoableChange() for a string
        expressionBlock.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( block, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "Block matches");
        expressionBlock.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( block, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "Block matches");

        // Test vetoableChange() for another block
        expressionBlock.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherBlock, null));
        assertEquals( block, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "Block matches");
        expressionBlock.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherBlock, null));
        assertEquals( block, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "Block matches");

        // Test vetoableChange() for its own block
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            expressionBlock.getSelectNamedBean().vetoableChange(
                    new PropertyChangeEvent(this, "CanDelete", block, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( block, expressionBlock.getSelectNamedBean().getNamedBean().getBean(), "Block matches");
        expressionBlock.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", block, null));
        assertNull( expressionBlock.getSelectNamedBean().getNamedBean(), "Block is null");
    }

    @Before
    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

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

        expressionBlock = new ExpressionBlock("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionBlock);
        ifThenElse.getChild(0).connect(maleSocket2);

        _base = expressionBlock;
        _baseMaleSocket = maleSocket2;

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        block = InstanceManager.getDefault(BlockManager.class).provide("IB1");
        block.setSensor(sensor.getSystemName());
        expressionBlock.getSelectNamedBean().setNamedBean(block);
        setBlockStatus(block, Block.OCCUPIED);

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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlockTest.class);

}
