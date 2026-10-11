package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.actions.DigitalMany;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.implementation.DefaultLogixNG;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.expressions.And;
import jmri.jmrit.logixng.expressions.ExpressionTurnout;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.apache.commons.lang3.mutable.MutableInt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test LogixNG
 *
 * @author Daniel Bergqvist 2018
 */
public class LogixNGTest {

    @Test
    public void testSetParent() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        UnsupportedOperationException e = assertThrows( UnsupportedOperationException.class,
            () -> logixNG.setParent(null),
            "exception thrown");
        assertNotNull(e);
    }

    @Test
    public void testGetParent() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        assertNull( logixNG.getParent(), "getParent() returns null");
    }

    @Test
    public void testState() throws JmriException {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        assertEquals( LogixNG.UNKNOWN, logixNG.getState(), "getState() returns UNKNOWN");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultLogixNG.");
        logixNG.setState(LogixNG.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in DefaultLogixNG.");
        assertEquals( LogixNG.UNKNOWN, logixNG.getState(), "getState() returns UNKNOWN");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultLogixNG.");
    }

    @Test
    public void testShortDescription() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        assertEquals( "LogixNG", logixNG.getShortDescription(Locale.US),
            "getShortDescription() returns correct value");
    }

    @Test
    public void testLongDescription() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        assertEquals( "LogixNG: A new logix for test", logixNG.getLongDescription(Locale.US),
            "getLongDescription() returns correct value");
    }

    @Test
    public void testGetChild() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        UnsupportedOperationException e = assertThrows( UnsupportedOperationException.class,
            () -> logixNG.getChild(0),
            "exception thrown");
        assertNotNull(e);
    }

    @Test
    public void testGetChildCount() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        UnsupportedOperationException e = assertThrows( UnsupportedOperationException.class,
            () -> logixNG.getChildCount(),
            "exception thrown");
        assertNotNull(e);
    }

    @Test
    public void testGetCategory() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        UnsupportedOperationException e = assertThrows( UnsupportedOperationException.class,
            () -> logixNG.getCategory(),
            "exception thrown");
        assertNotNull(e);
    }

    @Test
    public void testSwapConditionalNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A second conditionalNG");  // NOI18N
        ConditionalNG conditionalNG_3 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A third conditionalNG");  // NOI18N
        ConditionalNG conditionalNG_4 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A forth conditionalNG");  // NOI18N

        assertTrue( conditionalNG_1 == logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertTrue( conditionalNG_2 == logixNG.getConditionalNG(1), "ConditionalNG is correct");
        assertTrue( conditionalNG_3 == logixNG.getConditionalNG(2), "ConditionalNG is correct");
        assertTrue( conditionalNG_4 == logixNG.getConditionalNG(3), "ConditionalNG is correct");

        logixNG.swapConditionalNG(0, 0);
        assertTrue( conditionalNG_1 == logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertTrue( conditionalNG_2 == logixNG.getConditionalNG(1), "ConditionalNG is correct");
        assertTrue( conditionalNG_3 == logixNG.getConditionalNG(2), "ConditionalNG is correct");
        assertTrue( conditionalNG_4 == logixNG.getConditionalNG(3), "ConditionalNG is correct");

        logixNG.swapConditionalNG(1, 0);
        assertTrue( conditionalNG_1 == logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertTrue( conditionalNG_2 == logixNG.getConditionalNG(1), "ConditionalNG is correct");
        assertTrue( conditionalNG_3 == logixNG.getConditionalNG(2), "ConditionalNG is correct");
        assertTrue( conditionalNG_4 == logixNG.getConditionalNG(3), "ConditionalNG is correct");

        logixNG.swapConditionalNG(0, 1);
        assertTrue( conditionalNG_2 == logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertTrue( conditionalNG_1 == logixNG.getConditionalNG(1), "ConditionalNG is correct");
        assertTrue( conditionalNG_3 == logixNG.getConditionalNG(2), "ConditionalNG is correct");
        assertTrue( conditionalNG_4 == logixNG.getConditionalNG(3), "ConditionalNG is correct");

        logixNG.swapConditionalNG(0, 2);
        assertTrue( conditionalNG_3 == logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertTrue( conditionalNG_2 == logixNG.getConditionalNG(1), "ConditionalNG is correct");
        assertTrue( conditionalNG_1 == logixNG.getConditionalNG(2), "ConditionalNG is correct");
        assertTrue( conditionalNG_4 == logixNG.getConditionalNG(3), "ConditionalNG is correct");

        logixNG.swapConditionalNG(2, 3);
        assertTrue( conditionalNG_3 == logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertTrue( conditionalNG_2 == logixNG.getConditionalNG(1), "ConditionalNG is correct");
        assertTrue( conditionalNG_4 == logixNG.getConditionalNG(2), "ConditionalNG is correct");
        assertTrue( conditionalNG_1 == logixNG.getConditionalNG(3), "ConditionalNG is correct");
    }

    @Test
    public void testGetConditionalNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A second conditionalNG");  // NOI18N
        ConditionalNG conditionalNG_3 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A third conditionalNG");  // NOI18N
        ConditionalNG conditionalNG_4 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A forth conditionalNG");  // NOI18N

        assertTrue( conditionalNG_1 == logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertTrue( conditionalNG_2 == logixNG.getConditionalNG(1), "ConditionalNG is correct");
        assertTrue( conditionalNG_3 == logixNG.getConditionalNG(2), "ConditionalNG is correct");
        assertTrue( conditionalNG_4 == logixNG.getConditionalNG(3), "ConditionalNG is correct");
        assertNull( logixNG.getConditionalNG(-1), "ConditionalNG is correct");
        assertNull( logixNG.getConditionalNG(4), "ConditionalNG is correct");
    }

//     @Test
//     public void testAddConditionalNG() {
//         LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
//         ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, null);
// //         Assert.assertTrue("conditionalNG added", logixNG.addConditionalNG(conditionalNG_1));
//         ConditionalNG conditionalNG_2 =
//                 new DefaultConditionalNG(conditionalNG_1.getSystemName(), null);
//         Assert.assertFalse("conditionalNG not added", logixNG.addConditionalNG(conditionalNG_2));
//         JUnitAppender.assertWarnMessage("ConditionalNG 'IQC:AUTO:0001' has already been added to LogixNG 'IQ:AUTO:0001'");
//         ConditionalNG conditionalNG_3 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, null);
//         Assert.assertTrue("conditionalNG added", logixNG.addConditionalNG(conditionalNG_3));
//     }

    @Test
    public void testGetConditionalNGByUserName() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "Abc");  // NOI18N
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "Def");  // NOI18N
        ConditionalNG conditionalNG_3 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "Ghi");  // NOI18N
        ConditionalNG conditionalNG_4 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "Jkl");  // NOI18N

        assertEquals( conditionalNG_1, logixNG.getConditionalNGByUserName("Abc"),
                "ConditionalNG is correct");
        assertEquals( conditionalNG_2, logixNG.getConditionalNGByUserName("Def"),
                "ConditionalNG is correct");
        assertEquals( conditionalNG_3, logixNG.getConditionalNGByUserName("Ghi"),
                "ConditionalNG is correct");
        assertEquals( conditionalNG_4, logixNG.getConditionalNGByUserName("Jkl"),
                "ConditionalNG is correct");
        assertNull( logixNG.getConditionalNGByUserName("Non existing bean"),
                "ConditionalNG is correct");
    }

    @Test
    public void testDeleteConditionalNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A first conditionalNG");  // NOI18N
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A second conditionalNG");  // NOI18N
        ConditionalNG conditionalNG_3 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A third conditionalNG");  // NOI18N
        ConditionalNG conditionalNG_4 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A forth conditionalNG");  // NOI18N
        ConditionalNG conditionalNG_5 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A fifth conditionalNG");  // NOI18N
        ConditionalNG conditionalNG_6 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A sixth conditionalNG");  // NOI18N

        assertEquals( conditionalNG_1, logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertEquals( conditionalNG_2, logixNG.getConditionalNG(1), "ConditionalNG is correct");
        assertEquals( conditionalNG_3, logixNG.getConditionalNG(2), "ConditionalNG is correct");
        assertEquals( conditionalNG_4, logixNG.getConditionalNG(3), "ConditionalNG is correct");
        assertEquals( conditionalNG_5, logixNG.getConditionalNG(4), "ConditionalNG is correct");
        assertEquals( conditionalNG_6, logixNG.getConditionalNG(5), "ConditionalNG is correct");

        logixNG.deleteConditionalNG(conditionalNG_1);
        assertEquals( conditionalNG_2, logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertEquals( conditionalNG_3, logixNG.getConditionalNG(1), "ConditionalNG is correct");
        assertEquals( conditionalNG_4, logixNG.getConditionalNG(2), "ConditionalNG is correct");
        assertEquals( conditionalNG_5, logixNG.getConditionalNG(3), "ConditionalNG is correct");
        assertEquals( conditionalNG_6, logixNG.getConditionalNG(4), "ConditionalNG is correct");

        logixNG.deleteConditionalNG(conditionalNG_1);
        JUnitAppender.assertErrorMessage("attempt to delete ConditionalNG not in LogixNG: IQC:AUTO:0001");

        logixNG.deleteConditionalNG(conditionalNG_6);
        assertEquals( conditionalNG_2, logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertEquals( conditionalNG_3, logixNG.getConditionalNG(1), "ConditionalNG is correct");
        assertEquals( conditionalNG_4, logixNG.getConditionalNG(2), "ConditionalNG is correct");
        assertEquals( conditionalNG_5, logixNG.getConditionalNG(3), "ConditionalNG is correct");

        logixNG.deleteConditionalNG(conditionalNG_3);
        assertEquals( conditionalNG_2, logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertEquals( conditionalNG_4, logixNG.getConditionalNG(1), "ConditionalNG is correct");
        assertEquals( conditionalNG_5, logixNG.getConditionalNG(2), "ConditionalNG is correct");

        logixNG.deleteConditionalNG(conditionalNG_4);
        assertEquals( conditionalNG_2, logixNG.getConditionalNG(0), "ConditionalNG is correct");
        assertEquals( conditionalNG_5, logixNG.getConditionalNG(1), "ConditionalNG is correct");

        logixNG.deleteConditionalNG(conditionalNG_2);
        assertEquals( conditionalNG_5, logixNG.getConditionalNG(0), "ConditionalNG is correct");

        logixNG.deleteConditionalNG(conditionalNG_5);
        assertEquals( 0, logixNG.getNumConditionalNGs(), "ConditionalNG is correct");

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
        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();

        assertFalse( conditionalNG_1.listenersAreRegistered, "listeners for conditionalNG_1 are not registered");
        assertFalse( conditionalNG_2.listenersAreRegistered, "listeners for conditionalNG_2 are not registered");
        assertFalse( conditionalNG_3.listenersAreRegistered, "listeners for conditionalNG_3 are not registered");

        logixNG.setEnabled(true);
        assertFalse( conditionalNG_1.listenersAreRegistered,
                "listeners for conditionalNG_1 are not registered");
        assertTrue( conditionalNG_2.listenersAreRegistered,
                "listeners for conditionalNG_2 are registered");
        assertFalse( conditionalNG_3.listenersAreRegistered,
                "listeners for conditionalNG_3 are not registered");

        // Activate LogixNG multiple times should not be a problem
        logixNG.setEnabled(true);
        assertFalse( conditionalNG_1.listenersAreRegistered, "listeners for conditionalNG_1 are not registered");
        assertTrue( conditionalNG_2.listenersAreRegistered, "listeners for conditionalNG_2 are registered");
        assertFalse( conditionalNG_3.listenersAreRegistered, "listeners for conditionalNG_3 are not registered");

        logixNG.setEnabled(false);
        assertFalse( conditionalNG_1.listenersAreRegistered, "listeners for conditionalNG_1 are not registered");
        assertFalse( conditionalNG_2.listenersAreRegistered, "listeners for conditionalNG_2 are not registered");
        assertFalse( conditionalNG_3.listenersAreRegistered, "listeners for conditionalNG_3 are not registered");

        // DeActivate LogixNG multiple times should not be a problem
        logixNG.setEnabled(false);
        assertFalse( conditionalNG_1.listenersAreRegistered, "listeners for conditionalNG_1 are not registered");
        assertFalse( conditionalNG_2.listenersAreRegistered, "listeners for conditionalNG_2 are not registered");
        assertFalse( conditionalNG_3.listenersAreRegistered, "listeners for conditionalNG_3 are not registered");
    }

    @Test
    public void testGetConditionalNG_WithoutParameters() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        UnsupportedOperationException e = assertThrows( UnsupportedOperationException.class,
            () -> logixNG.getConditionalNG(),
            "exception thrown");
        assertNotNull(e);
    }

    @Test
    public void testGetLogixNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        assertSame( logixNG, logixNG.getLogixNG(), "logixNG is correct");
    }

    @Test
    public void testGetRoot() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        assertSame( logixNG, logixNG.getRoot(), "root is correct");
    }

    @Test
    public void testPrintTree() {
        final String newLine = System.lineSeparator();
        StringBuilder expectedResult = new StringBuilder();
        expectedResult
                .append("LogixNG: A new logix for test").append(newLine)
                .append("...ConditionalNG: A conditionalNG").append(newLine)
                .append("......! A").append(newLine)
                .append(".........Many ::: Use default").append(newLine)
                .append("............! A1").append(newLine)
                .append("...............If Then Else. Execute on change ::: Use default").append(newLine)
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
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
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
        assertEquals( expectedResult.toString(), resultStr, "Strings matches");
    }

    @Test
    public void testBundleClass() {
        assertEquals( "Test Bundle bb aa cc", Bundle.getMessage("TestBundle", "aa", "bb", "cc"),
            "bundle is correct");
        assertEquals( "Generic", Bundle.getMessage(Locale.US, "SocketTypeGeneric"),
                "bundle is correct");
        assertEquals( "Test Bundle bb aa cc", Bundle.getMessage(Locale.US, "TestBundle", "aa", "bb", "cc"),
                "bundle is correct");
    }

    @Test
    public void testCategory() {
        assertEquals( "Item", LogixNG_Category.ITEM.toString(), "isChangeableByUser is correct");
        assertEquals( "Common", LogixNG_Category.COMMON.toString(), "isChangeableByUser is correct");
        assertEquals( "Other", LogixNG_Category.OTHER.toString(), "isChangeableByUser is correct");
    }

    public void setupInitialConditionalNGTree(ConditionalNG conditionalNG) {
        assertDoesNotThrow( () -> {
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
        });
    }

    @Test
    public void testManagers() throws SocketAlreadyConnectedException {
        String systemName;
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        setupInitialConditionalNGTree(conditionalNG);
        MaleSocket many = conditionalNG.getChild(0).getConnectedSocket();
//        System.err.format("aa: %s%n", many.getLongDescription());
        assertEquals( "Many", many.getLongDescription(), "description is correct");
        MaleSocket ifThen = many.getChild(0).getConnectedSocket();
//        System.err.format("aa: %s%n", ifThen.getLongDescription());
        assertEquals( "If Then Else. Execute on change", ifThen.getLongDescription(), "description is correct");
        systemName = InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName();
        DigitalExpressionBean expression = new ExpressionTurnout(systemName, "An expression for test");  // NOI18N
        MaleSocket digitalExpressionBean = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        ifThen.getChild(0).connect(digitalExpressionBean);
//        InstanceManager.getDefault(jmri.DigitalExpressionManager.class).addExpression(new ExpressionTurnout(systemName, "LogixNG 102, DigitalExpressionBean 26"));  // NOI18N
        systemName = InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
        DigitalActionBean action = new ActionTurnout(systemName, "An action for test");  // NOI18N
        MaleSocket digitalActionBean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        ifThen.getChild(1).connect(digitalActionBean);

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));

        assertEquals( conditionalNG, digitalActionBean.getConditionalNG(), "conditionalng is correct");
        assertEquals( conditionalNG, conditionalNG.getConditionalNG(), "conditionalng is correct");
        assertEquals( logixNG, digitalActionBean.getLogixNG(), "logixlng is correct");
        assertEquals( logixNG, logixNG.getLogixNG(), "logixlng is correct");
    }

    @Test
    public void testSetup() throws SocketAlreadyConnectedException {

        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        DefaultConditionalNG conditionalNG =
                (DefaultConditionalNG) InstanceManager.getDefault(ConditionalNG_Manager.class)
                        .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N

        String systemName = InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
        DigitalActionBean action = new ActionTurnout(systemName, "An action for test");  // NOI18N
        MaleSocket digitalActionBean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);

        conditionalNG.setSocketSystemName(systemName);
        logixNG.setup();

        assertTrue ( logixNG.setParentForAllChildren(new ArrayList<>()));

        assertEquals( "Set turnout '' to state Thrown",
            conditionalNG.getChild(0).getConnectedSocket().getLongDescription(),
            "conditionalng child is correct");
        assertEquals( conditionalNG, digitalActionBean.getConditionalNG(), "conditionalng is correct");
        assertEquals( logixNG, digitalActionBean.getLogixNG(), "logixlng is correct");
    }

    @Test
    public void testExceptions() {
        new SocketAlreadyConnectedException().getMessage();
    }

    @Test
    public void testBundle() {
        assertEquals( "LogixNG", new DefaultLogixNG("IQ55", null).getBeanType(), "bean type is correct");
        assertEquals( "Digital action", new IfThenElse("IQDA321", null).getBeanType(), "bean type is correct");
        assertEquals( "Digital expression", new And("IQDE321", null).getBeanType(), "bean type is correct");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }


    private static class MyConditionalNG extends DefaultConditionalNG {

        boolean listenersAreRegistered;

        MyConditionalNG(String sys, String user) throws NamedBean.BadUserNameException, NamedBean.BadSystemNameException {
            super(sys, user);
        }

        /** {@inheritDoc} */
        @Override
        public synchronized void registerListenersForThisClass() {
            // The method DefaultConditionalNG.registerListenersForThisClass()
            // is synchronized so this method needs to be it as well.
            listenersAreRegistered = true;
        }

        /** {@inheritDoc} */
        @Override
        public synchronized void unregisterListenersForThisClass() {
            // The method DefaultConditionalNG.unregisterListenersForThisClass()
            // is synchronized so this method needs to be it as well.
            listenersAreRegistered = false;
        }
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGTest.class);

}
