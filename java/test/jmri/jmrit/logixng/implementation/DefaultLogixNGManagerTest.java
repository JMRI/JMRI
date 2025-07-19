package jmri.jmrit.logixng.implementation;

import java.beans.PropertyVetoException;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.*;
import jmri.jmrit.logixng.expressions.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test DefaultLogixNG
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultLogixNGManagerTest {

    @Test
    public void testManager() {
        LogixNG_Manager manager = InstanceManager.getDefault(LogixNG_Manager.class);

        Assert.assertEquals("getXMLOrder() is correct", Manager.LOGIXNGS, manager.getXMLOrder());
        Assert.assertEquals("getBeanTypeHandled() is correct", Bundle.getMessage("BeanNameLogixNG"), manager.getBeanTypeHandled());
        Assert.assertEquals("getSystemPrefix() is correct", "I", manager.getSystemPrefix());
        Assert.assertEquals("typeLetter() is correct", 'Q', manager.typeLetter());

        Assert.assertEquals("bean type is correct", Bundle.getMessage("BeanNameLogixNG"), manager.getBeanTypeHandled(false));
        Assert.assertEquals("bean type is correct", Bundle.getMessage("BeanNameLogixNGs"), manager.getBeanTypeHandled(true));
    }

    @Test
    public void testValidSystemNameFormat() {
        LogixNG_Manager manager = InstanceManager.getDefault(LogixNG_Manager.class);

        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat(""));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.VALID, manager.validSystemNameFormat("IQ1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("iQ1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("Iq1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("iq1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.VALID, manager.validSystemNameFormat("IQ:AUTO:1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1A"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQA"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1 "));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.VALID, manager.validSystemNameFormat("IQ11111"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1AA"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1X"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQX1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQX1X"));
    }

    @Test
    public void testCreateNewLogixNG() {
        LogixNG_Manager manager = InstanceManager.getDefault(LogixNG_Manager.class);

        // Correct system name
        LogixNG logixNG = manager.createLogixNG("IQ1", "Some name");
        Assert.assertNotNull("exists", logixNG);
        LogixNG logixNG_2 = manager.getLogixNG("IQ1");
        Assert.assertEquals("logixNGs are the same", logixNG, logixNG_2);
        logixNG_2 = manager.getBySystemName("IQ1");
        Assert.assertEquals("logixNGs are the same", logixNG, logixNG_2);
        logixNG_2 = manager.getLogixNG("Some name");
        Assert.assertEquals("logixNGs are the same", logixNG, logixNG_2);
        logixNG_2 = manager.getByUserName("Some name");
        Assert.assertEquals("logixNGs are the same", logixNG, logixNG_2);
        logixNG_2 = manager.getLogixNG("Some other name");
        Assert.assertNull("logixNG not found", logixNG_2);

        // Correct system name. Neither system name or user name exists already
        logixNG = manager.createLogixNG("IQ2", "Other LogixNG");
        Assert.assertNotNull("exists", logixNG);

        // System name exists
        logixNG = manager.createLogixNG("IQ1", "Another name");
        Assert.assertNull("cannot create new", logixNG);

        // User name exists
        logixNG = manager.createLogixNG("IQ3", "Other LogixNG");
        Assert.assertNull("cannot create new", logixNG);

        // Bad system name
        boolean thrown = false;
        try {
            manager.createLogixNG("IQ4A", "Different name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);


        // Create LogixNG with user name
        logixNG = manager.createLogixNG("Only user name");
        Assert.assertNotNull("exists", logixNG);
        Assert.assertEquals("user name is correct", "Only user name", logixNG.getUserName());
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
    public void testSetupInitialConditionalNGTree() {
        // Correct system name
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("IQ1", "Some name");
        Assert.assertNotNull("exists", logixNG);

        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        Assert.assertNotNull("exists", conditionalNG);
        setupInitialConditionalNGTree(conditionalNG);

        FemaleSocket child = conditionalNG.getChild(0);
        Assert.assertEquals("action is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                child.getClass().getName());
        MaleSocket maleSocket = child.getConnectedSocket();
        Assert.assertEquals("action is of correct class",
                "jmri.jmrit.logixng.tools.debugger.DebuggerMaleDigitalActionSocket",
                maleSocket.getClass().getName());
        Assert.assertEquals("action is of correct class",
                "Many",
                maleSocket.getLongDescription());
        MaleSocket maleSocket2 = maleSocket.getChild(0).getConnectedSocket();
        Assert.assertEquals("action is of correct class",
                "jmri.jmrit.logixng.tools.debugger.DebuggerMaleDigitalActionSocket",
                maleSocket2.getClass().getName());
        Assert.assertEquals("action is of correct class",
                "If Then Else. Execute on change",
                maleSocket2.getLongDescription());
    }

    @Test
    public void testDeleteLogixNG() throws SocketAlreadyConnectedException, PropertyVetoException {
        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);
        ConditionalNG_Manager conditionalNG_Manager = InstanceManager.getDefault(ConditionalNG_Manager.class);
        AnalogActionManager analogActionManager = InstanceManager.getDefault(AnalogActionManager.class);
        AnalogExpressionManager analogExpressionManager = InstanceManager.getDefault(AnalogExpressionManager.class);
        DigitalActionManager digitalActionManager = InstanceManager.getDefault(DigitalActionManager.class);
        DigitalBooleanActionManager digitalBooleanActionManager = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        DigitalExpressionManager digitalExpressionManager = InstanceManager.getDefault(DigitalExpressionManager.class);
        StringActionManager stringActionManager = InstanceManager.getDefault(StringActionManager.class);
        StringExpressionManager stringExpressionManager = InstanceManager.getDefault(StringExpressionManager.class);

        LogixNG logixNG = logixNG_Manager.createLogixNG("IQ1", "Some name");
        Assert.assertNotNull("exists", logixNG);

        ConditionalNG conditionalNG = conditionalNG_Manager
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        Assert.assertNotNull("exists", conditionalNG);

        FemaleSocket femaleSocket = conditionalNG.getFemaleSocket();
        MaleDigitalActionSocket actionManySocket = digitalActionManager
                        .registerAction(new DigitalMany(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionManySocket);

        femaleSocket = actionManySocket.getChild(0);
        MaleDigitalActionSocket actionIfThenSocket = digitalActionManager
                        .registerAction(new IfThenElse(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionIfThenSocket);

        femaleSocket = actionIfThenSocket.getChild(0);
        MaleDigitalExpressionSocket expressionOrSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(new Or(digitalExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionOrSocket);

        femaleSocket = actionManySocket.getChild(1);
        MaleDigitalActionSocket actionDoAnalogActionSocket = digitalActionManager
                        .registerAction(new DoAnalogAction(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionDoAnalogActionSocket);

        femaleSocket = actionDoAnalogActionSocket.getChild(0);
        MaleAnalogExpressionSocket expressionAnalogExpressionConstantSocket =
                analogExpressionManager
                        .registerExpression(new AnalogExpressionConstant(analogExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionAnalogExpressionConstantSocket);

        femaleSocket = actionDoAnalogActionSocket.getChild(1);
        MaleAnalogActionSocket actionAnalogManySocket =
                InstanceManager.getDefault(AnalogActionManager.class)
                        .registerAction(new AnalogMany(analogActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionAnalogManySocket);

        femaleSocket = actionManySocket.getChild(2);
        MaleDigitalActionSocket actionDoStringActionSocket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(new DoStringAction(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionDoStringActionSocket);

        femaleSocket = actionDoStringActionSocket.getChild(0);
        MaleStringExpressionSocket expressionStringExpressionConstantSocket =
                stringExpressionManager
                        .registerExpression(new StringExpressionConstant(stringExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionStringExpressionConstantSocket);

        femaleSocket = actionDoStringActionSocket.getChild(1);
        MaleStringActionSocket actionStringManySocket = stringActionManager
                        .registerAction(new StringMany(stringActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionStringManySocket);

        femaleSocket = actionManySocket.getChild(3);
        MaleDigitalActionSocket logix = digitalActionManager
                        .registerAction(new Logix(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(logix);

        femaleSocket = logix.getChild(1);
        MaleDigitalBooleanActionSocket onChange = digitalBooleanActionManager
                        .registerAction(new DigitalBooleanLogixAction(
                                digitalBooleanActionManager.getAutoSystemName(),
                                null,
                                DigitalBooleanLogixAction.When.Either));
        femaleSocket.connect(onChange);


        LastResultOfDigitalExpression lastResultOfDigitalExpression =
                new LastResultOfDigitalExpression(
                                digitalExpressionManager.getAutoSystemName(), null);
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionOrSocket);


        Assert.assertNotNull(logixNG_Manager.getBySystemName(logixNG.getSystemName()));
        Assert.assertNotNull(conditionalNG_Manager.getBySystemName(conditionalNG.getSystemName()));
        Assert.assertNotNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        Assert.assertNotNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        Assert.assertNotNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        Assert.assertNotNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        Assert.assertNotNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        Assert.assertNotNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        Assert.assertNotNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));

        try {
            logixNG_Manager.deleteBean(logixNG, "CanDelete");
        } catch (PropertyVetoException e) {
            Assert.assertEquals("DoNotDelete", e.getPropertyChangeEvent().getPropertyName());
            Assert.assertEquals("In use by IQDE:AUTO:0002 - Last result of digital expression", e.getMessage());
        }
        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();

        try {
            logixNG_Manager.deleteBean(logixNG, "CanDelete");
        } catch (PropertyVetoException e) {
            Assert.assertEquals("CanDelete", e.getPropertyChangeEvent().getPropertyName());
            Assert.assertEquals("", e.getMessage());
        }
        logixNG_Manager.deleteBean(logixNG, "DoDelete");

        Assert.assertNull(logixNG_Manager.getBySystemName(logixNG.getSystemName()));
        Assert.assertNull(conditionalNG_Manager.getBySystemName(conditionalNG.getSystemName()));
        Assert.assertNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        Assert.assertNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        Assert.assertNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        Assert.assertNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        Assert.assertNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        Assert.assertNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        Assert.assertNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));
    }

    @Test
    public void testDeleteConditionalNG() throws SocketAlreadyConnectedException, PropertyVetoException {
        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);
        ConditionalNG_Manager conditionalNG_Manager = InstanceManager.getDefault(ConditionalNG_Manager.class);
        AnalogActionManager analogActionManager = InstanceManager.getDefault(AnalogActionManager.class);
        AnalogExpressionManager analogExpressionManager = InstanceManager.getDefault(AnalogExpressionManager.class);
        DigitalActionManager digitalActionManager = InstanceManager.getDefault(DigitalActionManager.class);
        DigitalBooleanActionManager digitalBooleanActionManager = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        DigitalExpressionManager digitalExpressionManager = InstanceManager.getDefault(DigitalExpressionManager.class);
        StringActionManager stringActionManager = InstanceManager.getDefault(StringActionManager.class);
        StringExpressionManager stringExpressionManager = InstanceManager.getDefault(StringExpressionManager.class);

        LogixNG logixNG = logixNG_Manager.createLogixNG("IQ1", "Some name");
        Assert.assertNotNull("exists", logixNG);

        ConditionalNG conditionalNG = conditionalNG_Manager
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        Assert.assertNotNull("exists", conditionalNG);

        FemaleSocket femaleSocket = conditionalNG.getFemaleSocket();
        MaleDigitalActionSocket actionManySocket = digitalActionManager
                        .registerAction(new DigitalMany(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionManySocket);

        femaleSocket = actionManySocket.getChild(0);
        MaleDigitalActionSocket actionIfThenSocket = digitalActionManager
                        .registerAction(new IfThenElse(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionIfThenSocket);

        femaleSocket = actionIfThenSocket.getChild(0);
        MaleDigitalExpressionSocket expressionOrSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(new Or(digitalExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionOrSocket);

        femaleSocket = actionManySocket.getChild(1);
        MaleDigitalActionSocket actionDoAnalogActionSocket = digitalActionManager
                        .registerAction(new DoAnalogAction(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionDoAnalogActionSocket);

        femaleSocket = actionDoAnalogActionSocket.getChild(0);
        MaleAnalogExpressionSocket expressionAnalogExpressionConstantSocket =
                analogExpressionManager
                        .registerExpression(new AnalogExpressionConstant(analogExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionAnalogExpressionConstantSocket);

        femaleSocket = actionDoAnalogActionSocket.getChild(1);
        MaleAnalogActionSocket actionAnalogManySocket =
                InstanceManager.getDefault(AnalogActionManager.class)
                        .registerAction(new AnalogMany(analogActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionAnalogManySocket);

        femaleSocket = actionManySocket.getChild(2);
        MaleDigitalActionSocket actionDoStringActionSocket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(new DoStringAction(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionDoStringActionSocket);

        femaleSocket = actionDoStringActionSocket.getChild(0);
        MaleStringExpressionSocket expressionStringExpressionConstantSocket =
                stringExpressionManager
                        .registerExpression(new StringExpressionConstant(stringExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionStringExpressionConstantSocket);

        femaleSocket = actionDoStringActionSocket.getChild(1);
        MaleStringActionSocket actionStringManySocket = stringActionManager
                        .registerAction(new StringMany(stringActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionStringManySocket);

        femaleSocket = actionManySocket.getChild(3);
        MaleDigitalActionSocket logix = digitalActionManager
                        .registerAction(new Logix(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(logix);

        femaleSocket = logix.getChild(1);
        MaleDigitalBooleanActionSocket onChange = digitalBooleanActionManager
                        .registerAction(new DigitalBooleanLogixAction(
                                digitalBooleanActionManager.getAutoSystemName(),
                                null,
                                DigitalBooleanLogixAction.When.Either));
        femaleSocket.connect(onChange);


        LastResultOfDigitalExpression lastResultOfDigitalExpression =
                new LastResultOfDigitalExpression(
                                digitalExpressionManager.getAutoSystemName(), null);
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionOrSocket);


        Assert.assertNotNull(logixNG_Manager.getBySystemName(logixNG.getSystemName()));
        Assert.assertNotNull(conditionalNG_Manager.getBySystemName(conditionalNG.getSystemName()));
        Assert.assertNotNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        Assert.assertNotNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        Assert.assertNotNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        Assert.assertNotNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        Assert.assertNotNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        Assert.assertNotNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        Assert.assertNotNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));

        try {
            conditionalNG_Manager.deleteBean(conditionalNG, "CanDelete");
        } catch (PropertyVetoException e) {
            Assert.assertEquals("DoNotDelete", e.getPropertyChangeEvent().getPropertyName());
            Assert.assertEquals("In use by IQDE:AUTO:0002 - Last result of digital expression", e.getMessage());
        }
        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();

        try {
            conditionalNG_Manager.deleteBean(conditionalNG, "CanDelete");
        } catch (PropertyVetoException e) {
            Assert.assertEquals("CanDelete", e.getPropertyChangeEvent().getPropertyName());
            Assert.assertEquals("", e.getMessage());
        }
        conditionalNG_Manager.deleteBean(conditionalNG, "DoDelete");

        Assert.assertNotNull(logixNG_Manager.getBySystemName(logixNG.getSystemName()));
        Assert.assertNull(conditionalNG_Manager.getBySystemName(conditionalNG.getSystemName()));
        Assert.assertNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        Assert.assertNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        Assert.assertNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        Assert.assertNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        Assert.assertNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        Assert.assertNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        Assert.assertNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));
    }

    @Test
    public void testDeleteModule() throws SocketAlreadyConnectedException, PropertyVetoException {
        FemaleSocketManager femaleSocketManager = InstanceManager.getDefault(FemaleSocketManager.class);
        ModuleManager moduleManager = InstanceManager.getDefault(ModuleManager.class);
        AnalogActionManager analogActionManager = InstanceManager.getDefault(AnalogActionManager.class);
        AnalogExpressionManager analogExpressionManager = InstanceManager.getDefault(AnalogExpressionManager.class);
        DigitalActionManager digitalActionManager = InstanceManager.getDefault(DigitalActionManager.class);
        DigitalBooleanActionManager digitalBooleanActionManager = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        DigitalExpressionManager digitalExpressionManager = InstanceManager.getDefault(DigitalExpressionManager.class);
        StringActionManager stringActionManager = InstanceManager.getDefault(StringActionManager.class);
        StringExpressionManager stringExpressionManager = InstanceManager.getDefault(StringExpressionManager.class);

        jmri.jmrit.logixng.Module module = moduleManager
                .createModule("A module", femaleSocketManager.getSocketTypeByType("DefaultFemaleDigitalActionSocket"));  // NOI18N
        Assert.assertNotNull("exists", module);

        FemaleSocket femaleSocket = module.getRootSocket();
        MaleDigitalActionSocket actionManySocket = digitalActionManager
                        .registerAction(new DigitalMany(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionManySocket);

        femaleSocket = actionManySocket.getChild(0);
        MaleDigitalActionSocket actionIfThenSocket = digitalActionManager
                        .registerAction(new IfThenElse(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionIfThenSocket);

        femaleSocket = actionIfThenSocket.getChild(0);
        MaleDigitalExpressionSocket expressionOrSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(new Or(digitalExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionOrSocket);

        femaleSocket = actionManySocket.getChild(1);
        MaleDigitalActionSocket actionDoAnalogActionSocket = digitalActionManager
                        .registerAction(new DoAnalogAction(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionDoAnalogActionSocket);

        femaleSocket = actionDoAnalogActionSocket.getChild(0);
        MaleAnalogExpressionSocket expressionAnalogExpressionConstantSocket =
                analogExpressionManager
                        .registerExpression(new AnalogExpressionConstant(analogExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionAnalogExpressionConstantSocket);

        femaleSocket = actionDoAnalogActionSocket.getChild(1);
        MaleAnalogActionSocket actionAnalogManySocket =
                InstanceManager.getDefault(AnalogActionManager.class)
                        .registerAction(new AnalogMany(analogActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionAnalogManySocket);

        femaleSocket = actionManySocket.getChild(2);
        MaleDigitalActionSocket actionDoStringActionSocket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(new DoStringAction(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionDoStringActionSocket);

        femaleSocket = actionDoStringActionSocket.getChild(0);
        MaleStringExpressionSocket expressionStringExpressionConstantSocket =
                stringExpressionManager
                        .registerExpression(new StringExpressionConstant(stringExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionStringExpressionConstantSocket);

        femaleSocket = actionDoStringActionSocket.getChild(1);
        MaleStringActionSocket actionStringManySocket = stringActionManager
                        .registerAction(new StringMany(stringActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionStringManySocket);

        femaleSocket = actionManySocket.getChild(3);
        MaleDigitalActionSocket logix = digitalActionManager
                        .registerAction(new Logix(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(logix);

        femaleSocket = logix.getChild(1);
        MaleDigitalBooleanActionSocket onChange = digitalBooleanActionManager
                        .registerAction(new DigitalBooleanLogixAction(
                                digitalBooleanActionManager.getAutoSystemName(),
                                null,
                                DigitalBooleanLogixAction.When.Either));
        femaleSocket.connect(onChange);


        LastResultOfDigitalExpression lastResultOfDigitalExpression =
                new LastResultOfDigitalExpression(
                                digitalExpressionManager.getAutoSystemName(), null);
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionOrSocket);


        Assert.assertNotNull(moduleManager.getBySystemName(module.getSystemName()));
        Assert.assertNotNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        Assert.assertNotNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        Assert.assertNotNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        Assert.assertNotNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        Assert.assertNotNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        Assert.assertNotNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        Assert.assertNotNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));

        try {
            moduleManager.deleteBean(module, "CanDelete");
        } catch (PropertyVetoException e) {
            Assert.assertEquals("DoNotDelete", e.getPropertyChangeEvent().getPropertyName());
            Assert.assertEquals("In use by IQDE:AUTO:0002 - Last result of digital expression", e.getMessage());
        }
        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();

        try {
            moduleManager.deleteBean(module, "CanDelete");
        } catch (PropertyVetoException e) {
            Assert.assertEquals("CanDelete", e.getPropertyChangeEvent().getPropertyName());
            Assert.assertEquals("", e.getMessage());
        }
        moduleManager.deleteBean(module, "DoDelete");

        Assert.assertNull(moduleManager.getBySystemName(module.getSystemName()));
        Assert.assertNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        Assert.assertNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        Assert.assertNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        Assert.assertNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        Assert.assertNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        Assert.assertNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        Assert.assertNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));
    }

    @Test
    public void testDeleteAction() throws SocketAlreadyConnectedException, PropertyVetoException {
//        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);
//        ConditionalNG_Manager conditionalNG_Manager = InstanceManager.getDefault(ConditionalNG_Manager.class);
        AnalogActionManager analogActionManager = InstanceManager.getDefault(AnalogActionManager.class);
        AnalogExpressionManager analogExpressionManager = InstanceManager.getDefault(AnalogExpressionManager.class);
        DigitalActionManager digitalActionManager = InstanceManager.getDefault(DigitalActionManager.class);
        DigitalBooleanActionManager digitalBooleanActionManager = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        DigitalExpressionManager digitalExpressionManager = InstanceManager.getDefault(DigitalExpressionManager.class);
        StringActionManager stringActionManager = InstanceManager.getDefault(StringActionManager.class);
        StringExpressionManager stringExpressionManager = InstanceManager.getDefault(StringExpressionManager.class);

//        LogixNG logixNG = logixNG_Manager.createLogixNG("IQ1", "Some name");
//        Assert.assertNotNull("exists", logixNG);

//        ConditionalNG conditionalNG = conditionalNG_Manager
//                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
//        Assert.assertNotNull("exists", conditionalNG);

//        FemaleSocket femaleSocket = conditionalNG.getFemaleSocket();
        MaleDigitalActionSocket actionManySocket = digitalActionManager
                        .registerAction(new DigitalMany(digitalActionManager.getAutoSystemName(), null));
//        femaleSocket.connect(actionManySocket);

        FemaleSocket femaleSocket = actionManySocket.getChild(0);
        MaleDigitalActionSocket actionIfThenSocket = digitalActionManager
                        .registerAction(new IfThenElse(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionIfThenSocket);

        femaleSocket = actionIfThenSocket.getChild(0);
        MaleDigitalExpressionSocket expressionOrSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(new Or(digitalExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionOrSocket);

        femaleSocket = actionManySocket.getChild(1);
        MaleDigitalActionSocket actionDoAnalogActionSocket = digitalActionManager
                        .registerAction(new DoAnalogAction(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionDoAnalogActionSocket);

        femaleSocket = actionDoAnalogActionSocket.getChild(0);
        MaleAnalogExpressionSocket expressionAnalogExpressionConstantSocket =
                analogExpressionManager
                        .registerExpression(new AnalogExpressionConstant(analogExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionAnalogExpressionConstantSocket);

        femaleSocket = actionDoAnalogActionSocket.getChild(1);
        MaleAnalogActionSocket actionAnalogManySocket =
                InstanceManager.getDefault(AnalogActionManager.class)
                        .registerAction(new AnalogMany(analogActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionAnalogManySocket);

        femaleSocket = actionManySocket.getChild(2);
        MaleDigitalActionSocket actionDoStringActionSocket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(new DoStringAction(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionDoStringActionSocket);

        femaleSocket = actionDoStringActionSocket.getChild(0);
        MaleStringExpressionSocket expressionStringExpressionConstantSocket =
                stringExpressionManager
                        .registerExpression(new StringExpressionConstant(stringExpressionManager.getAutoSystemName(), null));
        femaleSocket.connect(expressionStringExpressionConstantSocket);

        femaleSocket = actionDoStringActionSocket.getChild(1);
        MaleStringActionSocket actionStringManySocket = stringActionManager
                        .registerAction(new StringMany(stringActionManager.getAutoSystemName(), null));
        femaleSocket.connect(actionStringManySocket);

        femaleSocket = actionManySocket.getChild(3);
        MaleDigitalActionSocket logix = digitalActionManager
                        .registerAction(new Logix(digitalActionManager.getAutoSystemName(), null));
        femaleSocket.connect(logix);

        femaleSocket = logix.getChild(1);
        MaleDigitalBooleanActionSocket onChange = digitalBooleanActionManager
                        .registerAction(new DigitalBooleanLogixAction(
                                digitalBooleanActionManager.getAutoSystemName(),
                                null,
                                DigitalBooleanLogixAction.When.Either));
        femaleSocket.connect(onChange);


        LastResultOfDigitalExpression lastResultOfDigitalExpression =
                new LastResultOfDigitalExpression(
                                digitalExpressionManager.getAutoSystemName(), null);
        lastResultOfDigitalExpression.getSelectNamedBean().setNamedBean(expressionOrSocket);


//        Assert.assertNotNull(logixNG_Manager.getBySystemName(logixNG.getSystemName()));
//        Assert.assertNotNull(conditionalNG_Manager.getBySystemName(conditionalNG.getSystemName()));
        Assert.assertNotNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        Assert.assertNotNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        Assert.assertNotNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        Assert.assertNotNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        Assert.assertNotNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        Assert.assertNotNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        Assert.assertNotNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));

        try {
            digitalActionManager.deleteBean(actionManySocket, "CanDelete");
        } catch (PropertyVetoException e) {
            Assert.assertEquals("DoNotDelete", e.getPropertyChangeEvent().getPropertyName());
            Assert.assertEquals("In use by IQDE:AUTO:0002 - Last result of digital expression", e.getMessage());
        }
        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();

        try {
            digitalActionManager.deleteBean(actionManySocket, "CanDelete");
        } catch (PropertyVetoException e) {
            Assert.assertEquals("CanDelete", e.getPropertyChangeEvent().getPropertyName());
            Assert.assertEquals("", e.getMessage());
        }
        digitalActionManager.deleteBean(actionManySocket, "DoDelete");

//        Assert.assertNotNull(logixNG_Manager.getBySystemName(logixNG.getSystemName()));
//        Assert.assertNull(conditionalNG_Manager.getBySystemName(conditionalNG.getSystemName()));
        Assert.assertNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        Assert.assertNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        Assert.assertNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        Assert.assertNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        Assert.assertNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        Assert.assertNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        Assert.assertNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));
    }

    @Test
    @Disabled("Test requires further development")
    public void testDeleteTable() throws SocketAlreadyConnectedException {
    }


    // The minimal setup for log4J
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

}
