package jmri.jmrix.loconet.logixng;

import jmri.jmrit.logixng.string.actions.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.digital.actions.DoStringAction;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test StringActionLocoNetOpcPeer
 * 
 * @author Daniel Bergqvist 2018
 */
public class StringActionLocoNetOpcPeerTest extends AbstractStringActionTestBase {

    private LnTrafficController lnis;
    private LocoNetSystemConnectionMemo memo;
    
    LogixNG logixNG;
    ConditionalNG conditionalNG;
    StringActionLocoNetOpcPeer stringActionLocoNet_OPC_PEER;
    protected Memory _memory;
    
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
        Many action = new Many("IQSA999", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(StringActionManager.class).registerAction(action);
        return maleSocket;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("LocoNet OPC_PEER string action%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Read string E and set string A%n" +
                "            ?s E%n" +
                "               Socket not connected%n" +
                "            !s A%n" +
                "               LocoNet OPC_PEER string action%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new Many(systemName, null);
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        StringActionLocoNetOpcPeer action2;
        Assert.assertNotNull("memory is not null", _memory);
        _memory.setValue(10.2);
        
        action2 = new StringActionLocoNetOpcPeer("IQSA11", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", null == action2.getUserName());
        Assert.assertTrue("String matches", "LocoNet OPC_PEER string action".equals(action2.getLongDescription()));
        
        action2 = new StringActionLocoNetOpcPeer("IQSA11", "My memory");
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", "My memory".equals(action2.getUserName()));
        Assert.assertTrue("String matches", "LocoNet OPC_PEER string action".equals(action2.getLongDescription()));
        
        action2 = new StringActionLocoNetOpcPeer("IQSA11", null);
//        action2.setMemory(_memory);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", null == action2.getUserName());
        Assert.assertTrue("String matches", "LocoNet OPC_PEER string action".equals(action2.getLongDescription()));
//        Assert.assertTrue("String matches", "Set memory IM1".equals(action2.getLongDescription()));
        
        action2 = new StringActionLocoNetOpcPeer("IQSA11", "My memory");
//        action2.setMemory(_memory);
        Assert.assertNotNull("object exists", action2);
        Assert.assertTrue("Username matches", "My memory".equals(action2.getUserName()));
        Assert.assertTrue("String matches", "LocoNet OPC_PEER string action".equals(action2.getLongDescription()));
//        Assert.assertTrue("String matches", "Set memory IM1".equals(action2.getLongDescription()));
        
        boolean thrown = false;
        try {
            // Illegal system name
            new StringActionLocoNetOpcPeer("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new StringActionLocoNetOpcPeer("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
/*    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, SocketAlreadyConnectedException {
        StringActionLocoNetOpcPeer action = (StringActionLocoNetOpcPeer)_base;
        action.setValue("");
        Assert.assertEquals("Memory has correct value", "", _memory.getValue());
        action.setValue("Test");
        Assert.assertEquals("Memory has correct value", "Test", _memory.getValue());
        action.setMemory((Memory)null);
        action.setValue("Other test");
        Assert.assertEquals("Memory has correct value", "Test", _memory.getValue());
    }
    
    @Test
    public void testMemory() {
        StringActionLocoNetOpcPeer action = (StringActionLocoNetOpcPeer)_base;
        action.setMemory((Memory)null);
        Assert.assertNull("Memory is null", action.getMemory());
        ((StringActionLocoNetOpcPeer)_base).setMemory(_memory);
        Assert.assertTrue("Memory matches", _memory == action.getMemory().getBean());
        
        action.setMemory((NamedBeanHandle<Memory>)null);
        Assert.assertNull("Memory is null", action.getMemory());
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("memory is not null", otherMemory);
        NamedBeanHandle<Memory> memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(otherMemory.getDisplayName(), otherMemory);
        ((StringActionLocoNetOpcPeer)_base).setMemory(memoryHandle);
        Assert.assertTrue("Memory matches", memoryHandle == action.getMemory());
        Assert.assertTrue("Memory matches", otherMemory == action.getMemory().getBean());
        
        action.setMemory((String)null);
        Assert.assertNull("Memory is null", action.getMemory());
        action.setMemory(memoryHandle.getName());
        Assert.assertTrue("Memory matches", memoryHandle == action.getMemory());
        
        // Test setMemory with a memory name that doesn't exists
        action.setMemory("Non existent memory");
        Assert.assertTrue("Memory matches", memoryHandle == action.getMemory());
        JUnitAppender.assertWarnMessage("memory 'Non existent memory' does not exists");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get some other memory for later use
        Memory otherMemory = InstanceManager.getDefault(MemoryManager.class).provide("IM99");
        Assert.assertNotNull("Memory is not null", otherMemory);
        Assert.assertNotEquals("Memory is not equal", _memory, otherMemory);
        
        // Get the expression and set the memory
        StringActionLocoNetOpcPeer action = (StringActionLocoNetOpcPeer)_base;
        action.setMemory(_memory);
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for another memory
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherMemory, null));
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        
        // Test vetoableChange() for its own memory
        boolean thrown = false;
        try {
            action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", _memory, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Memory matches", _memory, action.getMemory().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", _memory, null));
        Assert.assertNull("Memory is null", action.getMemory());
    }
*/    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.OTHER == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertTrue("String matches", "LocoNet OPC_PEER string action".equals(_base.getShortDescription()));
//        Assert.assertTrue("String matches", "Set memory IM1".equals(_base.getShortDescription()));
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertTrue("String matches", "LocoNet OPC_PEER string action".equals(_base.getLongDescription()));
//        Assert.assertTrue("String matches", "Set memory IM1".equals(_base.getLongDescription()));
    }
    
    @Test
    public void testChild() {
        Assert.assertTrue("Num children is zero", 0 == _base.getChildCount());
        boolean hasThrown = false;
        try {
            _base.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "Not supported.".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
/*    
    @Test
    public void testGetTopBitsByte2() {
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x00, stringActionLocoNet_OPC_PEER.getTopBitsByte(0,0));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x01, stringActionLocoNet_OPC_PEER.getTopBitsByte(0x80,0));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x02, stringActionLocoNet_OPC_PEER.getTopBitsByte(0x8000,0));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x04, stringActionLocoNet_OPC_PEER.getTopBitsByte(0,0x80));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x08, stringActionLocoNet_OPC_PEER.getTopBitsByte(0,0x8000));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x03, stringActionLocoNet_OPC_PEER.getTopBitsByte(0xFFFF, 0));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x0C, stringActionLocoNet_OPC_PEER.getTopBitsByte(0,0xFFFF));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x0F, stringActionLocoNet_OPC_PEER.getTopBitsByte(0xFFFF, 0xFFFF));
    }
    
    @Test
    public void testGetTopBitsByte4() {
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x00, stringActionLocoNet_OPC_PEER.getTopBitsByte(0,0,0,0));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x01, stringActionLocoNet_OPC_PEER.getTopBitsByte(0,0,0,0x80));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x02, stringActionLocoNet_OPC_PEER.getTopBitsByte(0,0,0x80,0));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x04, stringActionLocoNet_OPC_PEER.getTopBitsByte(0,0x80,0,0));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x08, stringActionLocoNet_OPC_PEER.getTopBitsByte(0x80,0,0,0));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x03, stringActionLocoNet_OPC_PEER.getTopBitsByte(0,0,0xFF,0xFF));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x0C, stringActionLocoNet_OPC_PEER.getTopBitsByte(0xFF,0xFF,0,0));
        
        Assert.assertEquals("getTopBitsByte() returns correct value",
                0x0F, stringActionLocoNet_OPC_PEER.getTopBitsByte(0xFF,0xFF,0xFF,0xFF));
    }
*/    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        
        // The class under test uses one LocoNet connection it pulls from the InstanceManager.
        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        lnis = new jmri.jmrix.loconet.LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, false, false, false);
        memo.configureManagers();
        jmri.InstanceManager.store(memo, jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);
        
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initMemoryManager();
        
        // Ensure we have a working LocoNet connection
        Assert.assertTrue("Has LocoNet", Common.hasLocoNet());
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setEnabled(true);
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        DoStringAction doStringAction = new DoStringAction("IQDA321", null);
        MaleSocket maleSocketDoStringAction =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(doStringAction);
        conditionalNG.getChild(0).connect(maleSocketDoStringAction);
        _memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        stringActionLocoNet_OPC_PEER = new StringActionLocoNetOpcPeer("IQSA321", "StringIO_Memory");
        MaleSocket maleSocketStringActionLocoNet_OPC_PEER =
                InstanceManager.getDefault(StringActionManager.class).registerAction(stringActionLocoNet_OPC_PEER);
        doStringAction.getChild(1).connect(maleSocketStringActionLocoNet_OPC_PEER);
//        stringActionMemory.setMemory(_memory);
        _base = stringActionLocoNet_OPC_PEER;
        _baseMaleSocket = maleSocketStringActionLocoNet_OPC_PEER;
        
	logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        _base.dispose();
        JUnitUtil.tearDown();
    }
    
}
