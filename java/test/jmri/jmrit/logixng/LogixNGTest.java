package jmri.jmrit.logixng;

import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.actions.DigitalMany;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.Base.Lock;
import jmri.jmrit.logixng.implementation.DefaultLogixNG;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.expressions.And;
import jmri.jmrit.logixng.expressions.ExpressionTurnout;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test LogixNG
 * 
 * @author Daniel Bergqvist 2018
 */
public class LogixNGTest {
    
    @Test
    public void testSetParent() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.setParent(null);
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Test
    public void testGetParent() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        Assert.assertNull("getParent() returns null", logixNG.getParent());
    }
    
    @Test
    public void testState() throws JmriException {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        Assert.assertTrue("getState() returns UNKNOWN", logixNG.getState() == LogixNG.UNKNOWN);
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultLogixNG.");
        logixNG.setState(LogixNG.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in DefaultLogixNG.");
        Assert.assertTrue("getState() returns UNKNOWN", logixNG.getState() == LogixNG.UNKNOWN);
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultLogixNG.");
    }
    
    @Test
    public void testShortDescription() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        Assert.assertEquals("getShortDescription() returns correct value",
                "LogixNG", logixNG.getShortDescription(Locale.US));
    }
    
    @Test
    public void testLongDescription() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        Assert.assertEquals("getLongDescription() returns correct value",
                "LogixNG: A new logix for test", logixNG.getLongDescription(Locale.US));
    }
    
    @Test
    public void testGetChild() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.getChild(0);
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Test
    public void testGetChildCount() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.getChildCount();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Test
    public void testGetCategory() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.getCategory();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Test
    public void testIsExternal() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.isExternal();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Test
    public void testGetLock() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.getLock();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Test
    public void testSetLock() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.setLock(Lock.NONE);
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Test
    public void testSwapConditionalNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_1);
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A second conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_2);
        ConditionalNG conditionalNG_3 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A third conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_3);
        ConditionalNG conditionalNG_4 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A forth conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_4);
        
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_1 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(1));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_3 == logixNG.getConditionalNG(2));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_4 == logixNG.getConditionalNG(3));
        
        logixNG.swapConditionalNG(0, 0);
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_1 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(1));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_3 == logixNG.getConditionalNG(2));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_4 == logixNG.getConditionalNG(3));
        
        logixNG.swapConditionalNG(1, 0);
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_1 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(1));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_3 == logixNG.getConditionalNG(2));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_4 == logixNG.getConditionalNG(3));
        
        logixNG.swapConditionalNG(0, 1);
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_1 == logixNG.getConditionalNG(1));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_3 == logixNG.getConditionalNG(2));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_4 == logixNG.getConditionalNG(3));
        
        logixNG.swapConditionalNG(0, 2);
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_3 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(1));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_1 == logixNG.getConditionalNG(2));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_4 == logixNG.getConditionalNG(3));
        
        logixNG.swapConditionalNG(2, 3);
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_3 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(1));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_4 == logixNG.getConditionalNG(2));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_1 == logixNG.getConditionalNG(3));
    }
    
    @Test
    public void testGetConditionalNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_1);
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A second conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_2);
        ConditionalNG conditionalNG_3 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A third conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_3);
        ConditionalNG conditionalNG_4 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A forth conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_4);
        
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_1 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(1));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_3 == logixNG.getConditionalNG(2));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_4 == logixNG.getConditionalNG(3));
        Assert.assertTrue("ConditionalNG is correct", null == logixNG.getConditionalNG(-1));
        Assert.assertTrue("ConditionalNG is correct", null == logixNG.getConditionalNG(4));
    }
    
    @Test
    public void testAddConditionalNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(null);
        Assert.assertTrue("conditionalNG added", logixNG.addConditionalNG(conditionalNG_1));
        ConditionalNG conditionalNG_2 =
                new DefaultConditionalNG(conditionalNG_1.getSystemName(), null);
        Assert.assertFalse("conditionalNG not added", logixNG.addConditionalNG(conditionalNG_2));
        JUnitAppender.assertWarnMessage("ConditionalNG 'IQC:AUTO:0001' has already been added to LogixNG 'IQ:AUTO:0001'");
        ConditionalNG conditionalNG_3 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(null);
        Assert.assertTrue("conditionalNG added", logixNG.addConditionalNG(conditionalNG_3));
    }
    
    @Test
    public void testGetConditionalNGByUserName() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("Abc");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_1);
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("Def");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_2);
        ConditionalNG conditionalNG_3 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("Ghi");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_3);
        ConditionalNG conditionalNG_4 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("Jkl");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_4);
        
        Assert.assertTrue("ConditionalNG is correct",
                conditionalNG_1 == logixNG.getConditionalNGByUserName("Abc"));
        Assert.assertTrue("ConditionalNG is correct",
                conditionalNG_2 == logixNG.getConditionalNGByUserName("Def"));
        Assert.assertTrue("ConditionalNG is correct",
                conditionalNG_3 == logixNG.getConditionalNGByUserName("Ghi"));
        Assert.assertTrue("ConditionalNG is correct",
                conditionalNG_4 == logixNG.getConditionalNGByUserName("Jkl"));
        Assert.assertTrue("ConditionalNG is correct",
                null == logixNG.getConditionalNGByUserName("Non existing bean"));
    }
    
    @Test
    public void testDeleteConditionalNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A first conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_1);
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A second conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_2);
        ConditionalNG conditionalNG_3 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A third conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_3);
        ConditionalNG conditionalNG_4 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A forth conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_4);
        ConditionalNG conditionalNG_5 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A fifth conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_5);
        ConditionalNG conditionalNG_6 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A sixth conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_6);
        
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_1 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(1));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_3 == logixNG.getConditionalNG(2));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_4 == logixNG.getConditionalNG(3));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_5 == logixNG.getConditionalNG(4));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_6 == logixNG.getConditionalNG(5));
        
        logixNG.deleteConditionalNG(conditionalNG_1);
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_3 == logixNG.getConditionalNG(1));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_4 == logixNG.getConditionalNG(2));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_5 == logixNG.getConditionalNG(3));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_6 == logixNG.getConditionalNG(4));
        
        logixNG.deleteConditionalNG(conditionalNG_1);
        JUnitAppender.assertErrorMessage("attempt to delete ConditionalNG not in LogixNG: IQC:AUTO:0001");
        
        logixNG.deleteConditionalNG(conditionalNG_6);
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_3 == logixNG.getConditionalNG(1));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_4 == logixNG.getConditionalNG(2));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_5 == logixNG.getConditionalNG(3));
        
        logixNG.deleteConditionalNG(conditionalNG_3);
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_4 == logixNG.getConditionalNG(1));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_5 == logixNG.getConditionalNG(2));
        
        logixNG.deleteConditionalNG(conditionalNG_4);
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_2 == logixNG.getConditionalNG(0));
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_5 == logixNG.getConditionalNG(1));
        
        logixNG.deleteConditionalNG(conditionalNG_2);
        Assert.assertTrue("ConditionalNG is correct", conditionalNG_5 == logixNG.getConditionalNG(0));
        
        logixNG.deleteConditionalNG(conditionalNG_5);
        Assert.assertTrue("LogixNG has no more conditionalNGs", 0 == logixNG.getNumConditionalNGs());
        
        logixNG.deleteConditionalNG(conditionalNG_5);
        JUnitAppender.assertErrorMessage("attempt to delete ConditionalNG not in LogixNG: IQC:AUTO:0005");
    }
    
    @Test
    public void testActivateLogixNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        MyConditionalNG conditionalNG_1 = new MyConditionalNG("IQC1", null);
        logixNG.addConditionalNG(conditionalNG_1);
        conditionalNG_1.setEnabled(false);
        MyConditionalNG conditionalNG_2 = new MyConditionalNG("IQC2", null);
        logixNG.addConditionalNG(conditionalNG_2);
        conditionalNG_2.setEnabled(true);
        MyConditionalNG conditionalNG_3 = new MyConditionalNG("IQC3", null);
        logixNG.addConditionalNG(conditionalNG_3);
        conditionalNG_3.setEnabled(false);
        logixNG.setParentForAllChildren();
        
        Assert.assertFalse("listeners for conditionalNG_1 are not registered", conditionalNG_1.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_2 are not registered", conditionalNG_2.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_3 are not registered", conditionalNG_3.listenersAreRegistered);
        
        logixNG.setEnabled(true);
        Assert.assertFalse("listeners for conditionalNG_1 are not registered", conditionalNG_1.listenersAreRegistered);
        Assert.assertTrue("listeners for conditionalNG_2 are registered", conditionalNG_2.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_3 are not registered", conditionalNG_3.listenersAreRegistered);
        
        // Activate LogixNG multiple times should not be a problem
        logixNG.setEnabled(true);
        Assert.assertFalse("listeners for conditionalNG_1 are not registered", conditionalNG_1.listenersAreRegistered);
        Assert.assertTrue("listeners for conditionalNG_2 are registered", conditionalNG_2.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_3 are not registered", conditionalNG_3.listenersAreRegistered);
        
        logixNG.setEnabled(false);
        Assert.assertFalse("listeners for conditionalNG_1 are not registered", conditionalNG_1.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_2 are not registered", conditionalNG_2.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_3 are not registered", conditionalNG_3.listenersAreRegistered);
        
        // DeActivate LogixNG multiple times should not be a problem
        logixNG.setEnabled(false);
        Assert.assertFalse("listeners for conditionalNG_1 are not registered", conditionalNG_1.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_2 are not registered", conditionalNG_2.listenersAreRegistered);
        Assert.assertFalse("listeners for conditionalNG_3 are not registered", conditionalNG_3.listenersAreRegistered);
    }
    
    @Test
    public void testGetConditionalNG_WithoutParameters() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        boolean hasThrown = false;
        try {
            logixNG.getConditionalNG();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
    
    @Test
    public void testGetLogixNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        Assert.assertTrue("logixNG is correct", logixNG == logixNG.getLogixNG());
    }
    
    @Test
    public void testGetRoot() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        Assert.assertTrue("root is correct", logixNG == logixNG.getRoot());
    }
    
    @Test
    public void testPrintTree() {
        final String newLine = System.lineSeparator();
        StringBuilder expectedResult = new StringBuilder();
        expectedResult
                .append("LogixNG: A new logix for test").append(newLine)
                .append("...ConditionalNG: A conditionalNG").append(newLine)
                .append("......! A").append(newLine)
                .append(".........Many ::: Log error").append(newLine)
                .append("............! A1").append(newLine)
                .append("...............If Then Else. Trigger action ::: Log error").append(newLine)
                .append("..................? If").append(newLine)
                .append(".....................Socket not connected").append(newLine)
                .append("..................! Then").append(newLine)
                .append(".....................Socket not connected").append(newLine)
                .append("..................! Else").append(newLine)
                .append(".....................Socket not connected").append(newLine)
                .append("............! A2").append(newLine)
                .append("...............Socket not connected").append(newLine);
        
        StringWriter writer = new StringWriter();
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        setupInitialConditionalNGTree(conditionalNG);
        logixNG.printTree(new PrintWriter(writer), "...", new MutableInt(0));
        String resultStr = writer.toString();
/*        
        System.err.format("=======================================%n");
        System.err.format("=======================================%n");
        System.err.format("=======================================%n");
        System.err.format("=======================================%n");
        System.err.format(expectedResult.toString());
        System.err.format("=======================================%n");
        System.err.format("=======================================%n");
        System.err.format(resultStr);
        System.err.format("=======================================%n");
        System.err.format("=======================================%n");
        System.err.format("=======================================%n");
*/        
        Assert.assertEquals("Strings matches", expectedResult.toString(), resultStr);
    }
    
    @Test
    public void testBaseLock() {
        Assert.assertTrue("isChangeableByUser is correct", Base.Lock.NONE.isChangeableByUser());
        Assert.assertTrue("isChangeableByUser is correct", Base.Lock.USER_LOCK.isChangeableByUser());
        Assert.assertFalse("isChangeableByUser is correct", Base.Lock.HARD_LOCK.isChangeableByUser());
    }
    
    @Test
    public void testBundleClass() {
        Assert.assertTrue("bundle is correct", "Test Bundle bb aa cc".equals(Bundle.getMessage("TestBundle", "aa", "bb", "cc")));
        Assert.assertTrue("bundle is correct", "Generic".equals(Bundle.getMessage(Locale.US, "SocketTypeGeneric")));
        Assert.assertTrue("bundle is correct", "Test Bundle bb aa cc".equals(Bundle.getMessage(Locale.US, "TestBundle", "aa", "bb", "cc")));
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("isChangeableByUser is correct", "Item".equals(Category.ITEM.toString()));
        Assert.assertTrue("isChangeableByUser is correct", "Common".equals(Category.COMMON.toString()));
        Assert.assertTrue("isChangeableByUser is correct", "Other".equals(Category.OTHER.toString()));
    }
    
    public void setupInitialConditionalNGTree(ConditionalNG conditionalNG) {
        try {
            DigitalActionManager digitalActionManager =
                    InstanceManager.getDefault(DigitalActionManager.class);
            
            FemaleSocket femaleSocket = conditionalNG.getFemaleSocket();
            MaleDigitalActionSocket actionManySocket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .registerAction(new DigitalMany(digitalActionManager.getAutoSystemName(), null));
            femaleSocket.connect(actionManySocket);
//            femaleSocket.setLock(Base.Lock.HARD_LOCK);

            femaleSocket = actionManySocket.getChild(0);
            MaleDigitalActionSocket actionIfThenSocket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .registerAction(new IfThenElse(digitalActionManager.getAutoSystemName(), null));
            femaleSocket.connect(actionIfThenSocket);
        } catch (SocketAlreadyConnectedException e) {
            // This should never be able to happen.
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testManagers() throws SocketAlreadyConnectedException {
        String systemName;
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        setupInitialConditionalNGTree(conditionalNG);
        MaleSocket many = conditionalNG.getChild(0).getConnectedSocket();
//        System.err.format("aa: %s%n", many.getLongDescription());
        Assert.assertTrue("description is correct", "Many".equals(many.getLongDescription()));
        MaleSocket ifThen = many.getChild(0).getConnectedSocket();
//        System.err.format("aa: %s%n", ifThen.getLongDescription());
        Assert.assertTrue("description is correct", "If Then Else. Trigger action".equals(ifThen.getLongDescription()));
        systemName = InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName();
        DigitalExpressionBean expression = new ExpressionTurnout(systemName, "An expression for test");  // NOI18N
        MaleSocket digitalExpressionBean = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        ifThen.getChild(0).connect(digitalExpressionBean);
//        InstanceManager.getDefault(jmri.DigitalExpressionManager.class).addExpression(new ExpressionTurnout(systemName, "LogixNG 102, DigitalExpressionBean 26"));  // NOI18N
        systemName = InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
        DigitalActionBean action = new ActionTurnout(systemName, "An action for test");  // NOI18N
        MaleSocket digitalActionBean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        ifThen.getChild(1).connect(digitalActionBean);
        
        logixNG.setParentForAllChildren();
        
        Assert.assertTrue("conditionalng is correct", conditionalNG == digitalActionBean.getConditionalNG());
        Assert.assertTrue("conditionalng is correct", conditionalNG == conditionalNG.getConditionalNG());
        Assert.assertTrue("logixlng is correct", logixNG == digitalActionBean.getLogixNG());
        Assert.assertTrue("logixlng is correct", logixNG == logixNG.getLogixNG());
    }
    
    @Test
    public void testSetup() throws SocketAlreadyConnectedException {
        
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        DefaultConditionalNG conditionalNG =
                (DefaultConditionalNG) InstanceManager.getDefault(ConditionalNG_Manager.class)
                        .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.setConditionalNG_SystemName(0, conditionalNG.getSystemName());
//        logixNG.addConditionalNG(conditionalNG);
        
        String systemName = InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
        DigitalActionBean action = new ActionTurnout(systemName, "An action for test");  // NOI18N
        MaleSocket digitalActionBean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        
        conditionalNG.setSocketSystemName(systemName);
        logixNG.setup();
        
        logixNG.setParentForAllChildren();
        
//        System.err.format("%s%n", conditionalNG.getChild(0).getConnectedSocket().getLongDescription());
        Assert.assertTrue("conditionalng child is correct",
                "Set turnout '' to state Thrown"
                        .equals(conditionalNG.getChild(0).getConnectedSocket().getLongDescription()));
        Assert.assertEquals("conditionalng is correct", conditionalNG, digitalActionBean.getConditionalNG());
        Assert.assertEquals("logixlng is correct", logixNG, digitalActionBean.getLogixNG());
    }
    
    @Test
    public void testExceptions() {
        new SocketAlreadyConnectedException().getMessage();
    }
    
    @Test
    public void testBundle() {
        Assert.assertTrue("bean type is correct", "LogixNG".equals(new DefaultLogixNG("IQ55", null).getBeanType()));
        Assert.assertTrue("bean type is correct", "Digital action".equals(new IfThenElse("IQDA321", null).getBeanType()));
        Assert.assertTrue("bean type is correct", "Digital expression".equals(new And("IQDE321", null).getBeanType()));
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
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
    
    private class MyConditionalNG extends DefaultConditionalNG {

        private boolean listenersAreRegistered;
        
        public MyConditionalNG(String sys, String user) throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException {
            super(sys, user);
        }
        
        /** {@inheritDoc} */
        @Override
        public void registerListenersForThisClass() {
            listenersAreRegistered = true;
        }

        /** {@inheritDoc} */
        @Override
        public void unregisterListenersForThisClass() {
            listenersAreRegistered = false;
        }
    }
    
}
