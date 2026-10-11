package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.beans.PropertyVetoException;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.*;
import jmri.jmrit.logixng.expressions.*;
import jmri.util.JUnitUtil;

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

        assertEquals( Manager.LOGIXNGS, manager.getXMLOrder(), "getXMLOrder() is correct");
        assertEquals( Bundle.getMessage("BeanNameLogixNG"), manager.getBeanTypeHandled(), "getBeanTypeHandled() is correct");
        assertEquals( "I", manager.getSystemPrefix(), "getSystemPrefix() is correct");
        assertEquals( 'Q', manager.typeLetter(), "typeLetter() is correct");

        assertEquals( Bundle.getMessage("BeanNameLogixNG"), manager.getBeanTypeHandled(false), "bean type is correct");
        assertEquals( Bundle.getMessage("BeanNameLogixNGs"), manager.getBeanTypeHandled(true), "bean type is correct");
    }

    @Test
    public void testValidSystemNameFormat() {
        LogixNG_Manager manager = InstanceManager.getDefault(LogixNG_Manager.class);

        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat(""), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.VALID, manager.validSystemNameFormat("IQ1"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat("iQ1"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat("Iq1"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat("iq1"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.VALID, manager.validSystemNameFormat("IQ:AUTO:1"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1A"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQA"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1 "), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.VALID, manager.validSystemNameFormat("IQ11111"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1AA"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1X"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQX1"), "validSystemNameFormat()");
        assertEquals( Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQX1X"), "validSystemNameFormat()");
    }

    @Test
    public void testCreateNewLogixNG() {
        LogixNG_Manager manager = InstanceManager.getDefault(LogixNG_Manager.class);

        // Correct system name
        LogixNG logixNG = manager.createLogixNG("IQ1", "Some name");
        assertNotNull( logixNG, "exists");
        LogixNG logixNG_2 = manager.getLogixNG("IQ1");
        assertEquals( logixNG, logixNG_2, "logixNGs are the same");
        logixNG_2 = manager.getBySystemName("IQ1");
        assertEquals( logixNG, logixNG_2, "logixNGs are the same");
        logixNG_2 = manager.getLogixNG("Some name");
        assertEquals( logixNG, logixNG_2, "logixNGs are the same");
        logixNG_2 = manager.getByUserName("Some name");
        assertEquals( logixNG, logixNG_2, "logixNGs are the same");
        logixNG_2 = manager.getLogixNG("Some other name");
        assertNull( logixNG_2, "logixNG not found");

        // Correct system name. Neither system name or user name exists already
        logixNG = manager.createLogixNG("IQ2", "Other LogixNG");
        assertNotNull( logixNG, "exists");

        // System name exists
        logixNG = manager.createLogixNG("IQ1", "Another name");
        assertNull( logixNG, "cannot create new");

        // User name exists
        logixNG = manager.createLogixNG("IQ3", "Other LogixNG");
        assertNull( logixNG, "cannot create new");

        // Bad system name
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> manager.createLogixNG("IQ4A", "Different name"),
            "Expected exception thrown");
        assertNotNull(ex);


        // Create LogixNG with user name
        logixNG = manager.createLogixNG("Only user name");
        assertNotNull( logixNG, "exists");
        assertEquals( "Only user name", logixNG.getUserName(), "user name is correct");
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
    public void testSetupInitialConditionalNGTree() {
        // Correct system name
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("IQ1", "Some name");
        assertNotNull( logixNG, "exists");

        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        assertNotNull( conditionalNG, "exists");
        setupInitialConditionalNGTree(conditionalNG);

        FemaleSocket child = conditionalNG.getChild(0);
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                child.getClass().getName(), "action is of correct class");
        MaleSocket maleSocket = child.getConnectedSocket();
        assertEquals( "jmri.jmrit.logixng.tools.debugger.DebuggerMaleDigitalActionSocket",
                maleSocket.getClass().getName(), "action is of correct class");
        assertEquals( "Many",
                maleSocket.getLongDescription());
        MaleSocket maleSocket2 = maleSocket.getChild(0).getConnectedSocket();
        assertEquals( "jmri.jmrit.logixng.tools.debugger.DebuggerMaleDigitalActionSocket",
                maleSocket2.getClass().getName(), "action is of correct class");
        assertEquals( "If Then Else. Execute on change",
                maleSocket2.getLongDescription(), "action is of correct class");
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
        assertNotNull( logixNG, "exists");

        ConditionalNG conditionalNG = conditionalNG_Manager
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        assertNotNull( conditionalNG, "exists");

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


        assertNotNull(logixNG_Manager.getBySystemName(logixNG.getSystemName()));
        assertNotNull(conditionalNG_Manager.getBySystemName(conditionalNG.getSystemName()));
        assertNotNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        assertNotNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        assertNotNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        assertNotNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        assertNotNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        assertNotNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        assertNotNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));

        PropertyVetoException e = assertThrows( PropertyVetoException.class,
            () -> logixNG_Manager.deleteBean(logixNG, "CanDelete"));
        assertEquals("DoNotDelete", e.getPropertyChangeEvent().getPropertyName());
        assertEquals("In use by IQDE:AUTO:0002 - Last result of digital expression", e.getMessage());

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();

        assertDoesNotThrow( () -> logixNG_Manager.deleteBean(logixNG, "CanDelete"));


        logixNG_Manager.deleteBean(logixNG, "DoDelete");

        assertNull(logixNG_Manager.getBySystemName(logixNG.getSystemName()));
        assertNull(conditionalNG_Manager.getBySystemName(conditionalNG.getSystemName()));
        assertNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        assertNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        assertNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        assertNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        assertNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        assertNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        assertNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));
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
        assertNotNull( logixNG, "exists");

        ConditionalNG conditionalNG = conditionalNG_Manager
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        assertNotNull( conditionalNG, "exists");

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


        assertNotNull(logixNG_Manager.getBySystemName(logixNG.getSystemName()));
        assertNotNull(conditionalNG_Manager.getBySystemName(conditionalNG.getSystemName()));
        assertNotNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        assertNotNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        assertNotNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        assertNotNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        assertNotNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        assertNotNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        assertNotNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));

        PropertyVetoException e = assertThrows( PropertyVetoException.class,
            () -> conditionalNG_Manager.deleteBean(conditionalNG, "CanDelete"));
        assertEquals("DoNotDelete", e.getPropertyChangeEvent().getPropertyName());
        assertEquals("In use by IQDE:AUTO:0002 - Last result of digital expression", e.getMessage());

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();

        assertDoesNotThrow( () -> conditionalNG_Manager.deleteBean(conditionalNG, "CanDelete"));

        conditionalNG_Manager.deleteBean(conditionalNG, "DoDelete");

        assertNotNull(logixNG_Manager.getBySystemName(logixNG.getSystemName()));
        assertNull(conditionalNG_Manager.getBySystemName(conditionalNG.getSystemName()));
        assertNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        assertNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        assertNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        assertNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        assertNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        assertNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        assertNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));
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
        assertNotNull( module, "exists");

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


        assertNotNull(moduleManager.getBySystemName(module.getSystemName()));
        assertNotNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        assertNotNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        assertNotNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        assertNotNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        assertNotNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        assertNotNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        assertNotNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));

        PropertyVetoException e = assertThrows( PropertyVetoException.class,
            () -> moduleManager.deleteBean(module, "CanDelete"));
        assertEquals("DoNotDelete", e.getPropertyChangeEvent().getPropertyName());
        assertEquals("In use by IQDE:AUTO:0002 - Last result of digital expression", e.getMessage());

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();

        assertDoesNotThrow( () -> moduleManager.deleteBean(module, "CanDelete"));

        moduleManager.deleteBean(module, "DoDelete");

        assertNull(moduleManager.getBySystemName(module.getSystemName()));
        assertNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        assertNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        assertNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        assertNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        assertNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        assertNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        assertNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));
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
        assertNotNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        assertNotNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        assertNotNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        assertNotNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        assertNotNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        assertNotNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        assertNotNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));

        PropertyVetoException e = assertThrows( PropertyVetoException.class,
            () -> digitalActionManager.deleteBean(actionManySocket, "CanDelete"));
        assertEquals("DoNotDelete", e.getPropertyChangeEvent().getPropertyName());
        assertEquals("In use by IQDE:AUTO:0002 - Last result of digital expression", e.getMessage());

        lastResultOfDigitalExpression.getSelectNamedBean().removeNamedBean();

        assertDoesNotThrow( () -> digitalActionManager.deleteBean(actionManySocket, "CanDelete"));

        digitalActionManager.deleteBean(actionManySocket, "DoDelete");

//        Assert.assertNotNull(logixNG_Manager.getBySystemName(logixNG.getSystemName()));
//        Assert.assertNull(conditionalNG_Manager.getBySystemName(conditionalNG.getSystemName()));
        assertNull(analogActionManager.getBySystemName(actionAnalogManySocket.getSystemName()));
        assertNull(analogExpressionManager.getBySystemName(expressionAnalogExpressionConstantSocket.getSystemName()));
        assertNull(digitalActionManager.getBySystemName(actionManySocket.getSystemName()));
        assertNull(digitalExpressionManager.getBySystemName(expressionOrSocket.getSystemName()));
        assertNull(stringActionManager.getBySystemName(actionStringManySocket.getSystemName()));
        assertNull(stringExpressionManager.getBySystemName(expressionStringExpressionConstantSocket.getSystemName()));
        assertNull(digitalBooleanActionManager.getBySystemName(onChange.getSystemName()));
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
