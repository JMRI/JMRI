package jmri.jmrit.logixng.expressions;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertNotNull("block is not null", block);
        block.setState(Block.OCCUPIED);

        expression2 = new ExpressionBlock("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Block \"''\" is Occupied", expression2.getLongDescription());

        expression2 = new ExpressionBlock("IQDE321", "My block");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My block", expression2.getUserName());
        Assert.assertEquals("String matches", "Block \"''\" is Occupied", expression2.getLongDescription());

        expression2 = new ExpressionBlock("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(block);
        Assert.assertTrue("block is correct", block == expression2.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Block \"IB1\" is Occupied", expression2.getLongDescription());

        Block s = InstanceManager.getDefault(BlockManager.class).provide("IB2");
        expression2 = new ExpressionBlock("IQDE321", "My block");
        expression2.getSelectNamedBean().setNamedBean(s);
        Assert.assertTrue("block is correct", s == expression2.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My block", expression2.getUserName());
        Assert.assertEquals("String matches", "Block \"IB2\" is Occupied", expression2.getLongDescription());

        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionBlock("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        thrown = false;
        try {
            // Illegal system name
            new ExpressionBlock("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }

    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionBlock.getChildCount());

        boolean hasThrown = false;
        try {
            expressionBlock.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testBlockState() {
        Assert.assertEquals("String matches", "not Occupied", ExpressionBlock.BlockState.NotOccupied.toString());
        Assert.assertEquals("String matches", "Occupied", ExpressionBlock.BlockState.Occupied.toString());
        Assert.assertEquals("String matches", "some other state", ExpressionBlock.BlockState.Other.toString());
        Assert.assertEquals("String matches", "Allocated", ExpressionBlock.BlockState.Allocated.toString());
        Assert.assertEquals("String matches", "equal to", ExpressionBlock.BlockState.ValueMatches.toString());

        Assert.assertEquals("ID matches", Block.UNOCCUPIED, ExpressionBlock.BlockState.NotOccupied.getID());
        Assert.assertEquals("ID matches", Block.OCCUPIED, ExpressionBlock.BlockState.Occupied.getID());
        Assert.assertEquals("ID matches", -1, ExpressionBlock.BlockState.Other.getID());
    }

    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", LogixNG_Category.ITEM == _base.getCategory());
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionBlock.getSelectNamedBean().removeNamedBean();
        Assert.assertEquals("Block", expressionBlock.getShortDescription());
        Assert.assertEquals("Block \"''\" is Occupied", expressionBlock.getLongDescription());
        expressionBlock.getSelectNamedBean().setNamedBean(block);
        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionBlock.getSelectEnum().setEnum(ExpressionBlock.BlockState.NotOccupied);
        Assert.assertEquals("Block \"IB1\" is not Occupied", expressionBlock.getLongDescription());
        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        Assert.assertEquals("Block \"IB1\" is not not Occupied", expressionBlock.getLongDescription());
        expressionBlock.getSelectEnum().setEnum(ExpressionBlock.BlockState.Other);
        Assert.assertEquals("Block \"IB1\"  some other state", expressionBlock.getLongDescription());
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
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Activate the block. This should not execute the conditional.
        setBlockStatus(block, Block.OCCUPIED);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Inactivate the block. This should not execute the conditional.
        setBlockStatus(block, Block.UNOCCUPIED);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Activate the block. This should execute the conditional.
        setBlockStatus(block, Block.OCCUPIED);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Inactivate the block. This should not execute the conditional.
        setBlockStatus(block, Block.UNOCCUPIED);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());

        // Test IS_NOT
        expressionBlock.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Activate the block. This should not execute the conditional.
        setBlockStatus(block, Block.OCCUPIED);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Inactivate the block. This should not execute the conditional.
        setBlockStatus(block, Block.UNOCCUPIED);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
    }

    @Test
    public void testSetBlock() {
        expressionBlock.unregisterListeners();

        Block otherBlock = InstanceManager.getDefault(BlockManager.class).provide("IM99");
        Assert.assertNotEquals("Blocks are different", otherBlock, expressionBlock.getSelectNamedBean().getNamedBean().getBean());
        expressionBlock.getSelectNamedBean().setNamedBean(otherBlock);
        Assert.assertEquals("Blocks are equal", otherBlock, expressionBlock.getSelectNamedBean().getNamedBean().getBean());

        NamedBeanHandle<Block> otherBlockHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherBlock.getDisplayName(), otherBlock);
        expressionBlock.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("Block is null", expressionBlock.getSelectNamedBean().getNamedBean());
        expressionBlock.getSelectNamedBean().setNamedBean(otherBlockHandle);
        Assert.assertEquals("Blocks are equal", otherBlock, expressionBlock.getSelectNamedBean().getNamedBean().getBean());
        Assert.assertEquals("BlockHandles are equal", otherBlockHandle, expressionBlock.getSelectNamedBean().getNamedBean());
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
        Assert.assertNull("block handle is null", expressionBlock.getSelectNamedBean().getNamedBean());

        expressionBlock.getSelectNamedBean().setNamedBean(block11);
        Assert.assertTrue("block is correct", block11 == expressionBlock.getSelectNamedBean().getNamedBean().getBean());

        expressionBlock.getSelectNamedBean().removeNamedBean();
        Assert.assertNull("block handle is null", expressionBlock.getSelectNamedBean().getNamedBean());

        expressionBlock.getSelectNamedBean().setNamedBean(blockHandle12);
        Assert.assertTrue("block handle is correct", blockHandle12 == expressionBlock.getSelectNamedBean().getNamedBean());

        expressionBlock.getSelectNamedBean().setNamedBean("A non existent block");
        Assert.assertNull("block handle is null", expressionBlock.getSelectNamedBean().getNamedBean());
        JUnitAppender.assertErrorMessage("Block \"A non existent block\" is not found");

        expressionBlock.getSelectNamedBean().setNamedBean(block13.getSystemName());
        Assert.assertTrue("block is correct", block13 == expressionBlock.getSelectNamedBean().getNamedBean().getBean());

        String userName = block14.getUserName();
        Assert.assertNotNull("block is not null", userName);
        expressionBlock.getSelectNamedBean().setNamedBean(userName);
        Assert.assertTrue("block is correct", block14 == expressionBlock.getSelectNamedBean().getNamedBean().getBean());
    }

    @Test
    public void testSetBlockException() {
        Assert.assertNotNull("Block is not null", block);
        Assert.assertNotNull("Block is not null", expressionBlock.getSelectNamedBean().getNamedBean());
        expressionBlock.registerListeners();
        boolean thrown = false;
        try {
            expressionBlock.getSelectNamedBean().setNamedBean("A block");
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        thrown = false;
        try {
            Block block99 = InstanceManager.getDefault(BlockManager.class).provide("IS99");
            NamedBeanHandle<Block> blockHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(block99.getDisplayName(), block99);
            expressionBlock.getSelectNamedBean().setNamedBean(blockHandle99);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        thrown = false;
        try {
            expressionBlock.getSelectNamedBean().removeNamedBean();
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
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
        Assert.assertNotNull("Block is not null", block);
        expressionBlock.getSelectNamedBean().setNamedBean(block);

        // Get some other block for later use
        Block otherBlock = InstanceManager.getDefault(BlockManager.class).provide("IM99");
        Assert.assertNotNull("Block is not null", otherBlock);
        Assert.assertNotEquals("Block is not equal", block, otherBlock);

        // Test vetoableChange() for some other propery
        expressionBlock.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Block matches", block, expressionBlock.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for a string
        expressionBlock.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Block matches", block, expressionBlock.getSelectNamedBean().getNamedBean().getBean());
        expressionBlock.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Block matches", block, expressionBlock.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for another block
        expressionBlock.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherBlock, null));
        Assert.assertEquals("Block matches", block, expressionBlock.getSelectNamedBean().getNamedBean().getBean());
        expressionBlock.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherBlock, null));
        Assert.assertEquals("Block matches", block, expressionBlock.getSelectNamedBean().getNamedBean().getBean());

        // Test vetoableChange() for its own block
        boolean thrown = false;
        try {
            expressionBlock.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", block, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        Assert.assertEquals("Block matches", block, expressionBlock.getSelectNamedBean().getNamedBean().getBean());
        expressionBlock.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", block, null));
        Assert.assertNull("Block is null", expressionBlock.getSelectNamedBean().getNamedBean());
    }

    // The minimal setup for log4J
    @Before
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

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlockTest.class);

}
