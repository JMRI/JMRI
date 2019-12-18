package jmri.implementation;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Timer;
import jmri.*;
import jmri.jmrit.Sound;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test the DefaultConditional implementation class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class DefaultConditionalTest {
    
    private static final boolean EXPECT_SUCCESS = true;
    private static final boolean EXPECT_FAILURE = false;

    /**
     * Operate parent NamedBeanTest tests.
     */
    @Test
    public void createInstance() {
        new DefaultConditional("IXIC 0");
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists",new DefaultConditional("IXIC 1"));
    }

    @Test
    public void testBasics() {
        Conditional ix1 = new DefaultConditional("IXIC 1");
        Assert.assertEquals("Conditional", ix1.getBeanType());
        
        // Check that a non existent item in a table results in -1
        int[] table = { 1, 2, 3 };
        Assert.assertEquals(-1, DefaultConditional.getIndexInTable(table, 5));
    }
    
    @Test
    public void testBasicBeanOperations() {
        Conditional ix1 = new DefaultConditional("IXIC 2");

        Conditional ix2 = new DefaultConditional("IXIC 3");

        Assert.assertTrue("object not equals", !ix1.equals(ix2));
        Assert.assertTrue("object not equals reverse", !ix2.equals(ix1));

        Assert.assertTrue("hash not equals", ix1.hashCode() != ix2.hashCode());
    }
    
    private void testValidate(boolean expectedResult, String antecedent, List<ConditionalVariable> conditionalVariablesList) {
        Conditional ix1 = new DefaultConditional("IXIC 1");
        
        if (expectedResult) {
            Assert.assertTrue("validateAntecedent() returns null for '"+antecedent+"'",
                    ix1.validateAntecedent(antecedent, conditionalVariablesList) == null);
        } else {
            Assert.assertTrue("validateAntecedent() returns error message for '"+antecedent+"'",
                    ix1.validateAntecedent(antecedent, conditionalVariablesList) != null);
        }
    }
    
    private void testCalculate(int expectedResult, String antecedent, List<ConditionalVariable> conditionalVariablesList, String errorMessage) {
        Conditional ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.MIXED, antecedent);
        ix1.setStateVariables(conditionalVariablesList);
        
        switch (expectedResult) {
            case NamedBean.UNKNOWN:
                Assert.assertTrue("validateAntecedent() returns UNKNOWN for '"+antecedent+"'",
                        ix1.calculate(false, null) == NamedBean.UNKNOWN);
                break;
                
            case Conditional.FALSE:
                Assert.assertTrue("validateAntecedent() returns FALSE for '"+antecedent+"'",
                        ix1.calculate(false, null) == Conditional.FALSE);
                break;
                
            case Conditional.TRUE:
                Assert.assertTrue("validateAntecedent() returns TRUE for '"+antecedent+"'",
                        ix1.calculate(false, null) == Conditional.TRUE);
                break;
                
            default:
                throw new RuntimeException(String.format("Unknown expected result: %d", expectedResult));
        }
        
        if (! errorMessage.isEmpty()) {
            jmri.util.JUnitAppender.assertErrorMessageStartsWith(errorMessage);
        }
    }
    
    @Test
    public void testValidate() {
        ConditionalVariable[] conditionalVariables_Empty = { };
        List<ConditionalVariable> conditionalVariablesList_Empty = Arrays.asList(conditionalVariables_Empty);
        
        ConditionalVariable[] conditionalVariables_True
                = { new ConditionalVariableStatic(Conditional.State.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        ConditionalVariable[] conditionalVariables_TrueTrueTrue
                = { new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_TrueTrueTrue = Arrays.asList(conditionalVariables_TrueTrueTrue);
        
        // Test empty antecedent string
        testValidate(EXPECT_FAILURE, "", conditionalVariablesList_Empty);
        
        testValidate(EXPECT_SUCCESS, "R1", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R2", conditionalVariablesList_True);
        
        // Test parentheses
        testValidate(EXPECT_SUCCESS, "([{R1)}]", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "(R2", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R2)", conditionalVariablesList_True);
        
        // Test several items
        testValidate(EXPECT_FAILURE, "R1 and R2 and R3", conditionalVariablesList_True);
        testValidate(EXPECT_FAILURE, "R1", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_SUCCESS, "R1 and R2 and R3", conditionalVariablesList_TrueTrueTrue);
        
        // Test uppercase and lowercase
        testValidate(EXPECT_SUCCESS, "R2 AND R1 or R3", conditionalVariablesList_TrueTrueTrue);
        
        // Test several items and parenthese
        testValidate(EXPECT_SUCCESS, "(R1 and R3) and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "(R1 and) R3 and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1( and R3) and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 (and R3 and) not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "(R1 and R3) and not R2)", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_SUCCESS, "(R1 and (R3) and not R2)", conditionalVariablesList_TrueTrueTrue);
        
        // Test invalid combinations
        testValidate(EXPECT_FAILURE, "R1 and or R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or or R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or and R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "and R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 or R3 and R2 or", conditionalVariablesList_TrueTrueTrue);
    }
    
    @Test
    @SuppressWarnings("unused") // test building in progress
    public void testCalculate() {
        ConditionalVariable[] conditionalVariables_Empty = { };
        List<ConditionalVariable> conditionalVariablesList_Empty = Arrays.asList(conditionalVariables_Empty);
        
        ConditionalVariable[] conditionalVariables_True
                = { new ConditionalVariableStatic(Conditional.State.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        ConditionalVariable[] conditionalVariables_False
                = { new ConditionalVariableStatic(Conditional.State.FALSE) };
        List<ConditionalVariable> conditionalVariablesList_False = Arrays.asList(conditionalVariables_False);
        
        ConditionalVariable[] conditionalVariables_NotTrue
                = { new ConditionalVariableStatic(Conditional.State.TRUE, true) };
        List<ConditionalVariable> conditionalVariablesList_NotTrue = Arrays.asList(conditionalVariables_NotTrue);
        
        ConditionalVariable[] conditionalVariables_NotFalse
                = { new ConditionalVariableStatic(Conditional.State.FALSE, true) };
        List<ConditionalVariable> conditionalVariablesList_NotFalse = Arrays.asList(conditionalVariables_NotFalse);
        
        ConditionalVariable[] conditionalVariables_TrueTrueTrue
                = { new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_TrueTrueTrue = Arrays.asList(conditionalVariables_TrueTrueTrue);
        
        ConditionalVariable[] conditionalVariables_FalseFalseFalse
                = {new ConditionalVariableStatic(Conditional.State.FALSE)
                        , new ConditionalVariableStatic(Conditional.State.FALSE)
                        , new ConditionalVariableStatic(Conditional.State.FALSE) };
        List<ConditionalVariable> conditionalVariablesList_FalseFalseFalse = Arrays.asList(conditionalVariables_FalseFalseFalse);
        
        ConditionalVariable[] conditionalVariables_TrueTrueFalse
                = {new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.FALSE) };
        List<ConditionalVariable> conditionalVariablesList_TrueTrueFalse = Arrays.asList(conditionalVariables_TrueTrueFalse);
        
        // Test with two digit variable numbers
        ConditionalVariable[] conditionalVariables_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse
                = {new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.FALSE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.FALSE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.FALSE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.TRUE)
                        , new ConditionalVariableStatic(Conditional.State.FALSE) };
        List<ConditionalVariable> conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse =
                Arrays.asList(conditionalVariables_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse);
        
        
        // Test empty antecedent string
        testCalculate(NamedBean.UNKNOWN, "", conditionalVariablesList_Empty, "");
        testCalculate(Conditional.FALSE, "", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= , ex= java.lang.StringIndexOutOfBoundsException");
        
        // Test illegal number
        testCalculate(Conditional.FALSE, "R#", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= R#, ex= java.lang.NumberFormatException");
        testCalculate(Conditional.FALSE, "R-", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= R-, ex= java.lang.NumberFormatException");
        testCalculate(Conditional.FALSE, "Ra", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= Ra, ex= java.lang.NumberFormatException");
        
        // Test single condition
        testCalculate(Conditional.TRUE, "R1", conditionalVariablesList_True, "");
        testCalculate(Conditional.FALSE, "R1", conditionalVariablesList_False, "");
        testCalculate(Conditional.FALSE, "not R1", conditionalVariablesList_True, "");
        testCalculate(Conditional.TRUE, "not R1", conditionalVariablesList_False, "");
        
        // Test single condition with variables that has the flag "not" set
        testCalculate(Conditional.FALSE, "R1", conditionalVariablesList_NotTrue, "");
        testCalculate(Conditional.TRUE, "R1", conditionalVariablesList_NotFalse, "");
        testCalculate(Conditional.FALSE, "R2", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= R2, ex= java.lang.ArrayIndexOutOfBoundsException");
        
        // Test single item but wrong item (R2 instead of R1)
        testCalculate(Conditional.FALSE, "R2)", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= R2), ex= java.lang.ArrayIndexOutOfBoundsException");
        
        // Test two digit variable numbers
        testCalculate(Conditional.TRUE, "R3 and R12 or R5 and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Conditional.FALSE, "R3 and (R12 or R5) and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Conditional.FALSE, "R12 and R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Conditional.TRUE, "R12 or R10",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        testCalculate(Conditional.FALSE, "not (R12 or R10)",
                conditionalVariablesList_TrueTrueFalseTrueTrueFalseTrueTrueFalseTrueTrueFalse, "");
        
        // Test parentheses
        testCalculate(Conditional.TRUE, "([{R1)}]", conditionalVariablesList_True, "");
        testCalculate(Conditional.FALSE, "(R2", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= (R2, ex= java.lang.ArrayIndexOutOfBoundsException");
        
        // Test several items
        testCalculate(Conditional.FALSE, "R1 and R2 and R3", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= R1 and R2 and R3, ex= java.lang.ArrayIndexOutOfBoundsException");
        testCalculate(Conditional.TRUE, "R1", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.TRUE, "R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.TRUE, "R3", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.TRUE, "R1 and R2 and R3", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.TRUE, "R2 AND R1 or R3", conditionalVariablesList_TrueTrueTrue, "");
        
        // Test invalid combinations of and, or, not
        testCalculate(Conditional.FALSE, "R1 and or R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error antecedent= R1 and or R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ORR3ANDR2 >");
        testCalculate(Conditional.FALSE, "R1 or or R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error antecedent= R1 or or R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ORR3ANDR2 >");
        testCalculate(Conditional.FALSE, "R1 or and R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error antecedent= R1 or and R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ANDR3ANDR2 >");
        testCalculate(Conditional.FALSE, "R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error antecedent= R1 not R3 and R2, ex= jmri.JmriException: Could not find expected operator < NOTR3ANDR2 >");
        testCalculate(Conditional.FALSE, "and R1 not R3 and R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error antecedent= and R1 not R3 and R2, ex= jmri.JmriException: Unexpected operator or characters < ANDR1NOTR3ANDR2 >");
        testCalculate(Conditional.FALSE, "R1 or R3 and R2 or", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error antecedent= R1 or R3 and R2 or, ex= java.lang.StringIndexOutOfBoundsException");
        
        // Test several items and parenthese
        testCalculate(Conditional.TRUE, "(R1 and R3) and R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.FALSE, "(R1 and R3) and not R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.FALSE, "(R1 and) R3 and not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error antecedent= (R1 and) R3 and not R2, ex= jmri.JmriException: Unexpected operator or characters < )R3ANDNOTR2 >");
        testCalculate(Conditional.FALSE, "R1( and R3) and not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error antecedent= R1( and R3) and not R2, ex= jmri.JmriException: Could not find expected operator < (ANDR3)ANDNOTR2 >");
        testCalculate(Conditional.FALSE, "R1 (and R3 and) not R2", conditionalVariablesList_TrueTrueTrue,
                "IXIC 1 parseCalculation error antecedent= R1 (and R3 and) not R2, ex= jmri.JmriException: Could not find expected operator < (ANDR3AND)NOTR2 >");
        testCalculate(Conditional.FALSE, "(R1 and R3) and not R2)", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.TRUE, "(R1 and (R3) and R2)", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.FALSE, "(R1 and (R3) and not R2)", conditionalVariablesList_TrueTrueTrue, "");
        
        // Test ALL_AND
        Conditional ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
        ix1.setStateVariables(conditionalVariablesList_TrueTrueTrue);
        Assert.assertTrue("calculate() returns NamedBean.TRUE", ix1.calculate(false, null) == Conditional.TRUE);
        
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
        ix1.setStateVariables(conditionalVariablesList_TrueTrueFalse);
        Assert.assertTrue("calculate() returns NamedBean.FALSE", ix1.calculate(false, null) == Conditional.FALSE);
        
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_AND, "");
        ix1.setStateVariables(conditionalVariablesList_FalseFalseFalse);
        Assert.assertTrue("calculate() returns NamedBean.FALSE", ix1.calculate(false, null) == Conditional.FALSE);
        
        
        // Test ALL_OR
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueTrueTrue);
        Assert.assertTrue("calculate() returns NamedBean.TRUE", ix1.calculate(false, null) == Conditional.TRUE);
        
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueTrueFalse);
        Assert.assertTrue("calculate() returns NamedBean.FALSE", ix1.calculate(false, null) == Conditional.TRUE);
        
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_FalseFalseFalse);
        Assert.assertTrue("calculate() returns NamedBean.FALSE", ix1.calculate(false, null) == Conditional.FALSE);
        
        
        // Test wrong logic
//        ix1 = new DefaultConditional("IXIC 1");
//        ix1.setLogicType(0xFFF, "");    // This logix does not exists
//        ix1.setStateVariables(conditionalVariablesList_TrueTrueTrue);
//        Assert.assertTrue("calculate() returns NamedBean.TRUE", ix1.calculate(false, null) == Conditional.TRUE);
//        jmri.util.JUnitAppender.assertWarnMessage("Conditional IXIC 1 fell through switch in calculate");
    }
    
    
    @Test
    public void testTriggers() {
        ConditionalVariable[] conditionalVariables_True
                = { new ConditionalVariableStatic(Conditional.State.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        ConditionalVariable[] conditionalVariables_TrueWithTrigger
                = { new ConditionalVariableStatic(Conditional.State.TRUE, "MyName", true) };
        List<ConditionalVariable> conditionalVariablesList_TrueWithTrigger = Arrays.asList(conditionalVariables_TrueWithTrigger);
        
        ConditionalVariable[] conditionalVariables_TrueWithNotTrigger
                = { new ConditionalVariableStatic(Conditional.State.TRUE, "MyName", false) };
        List<ConditionalVariable> conditionalVariablesList_TrueWithNotTrigger = Arrays.asList(conditionalVariables_TrueWithNotTrigger);
        
        TestConditionalAction testConditionalAction = new TestConditionalAction();
        List<ConditionalAction> conditionalActionList = new ArrayList<>();
        conditionalActionList.add(testConditionalAction);
        
        NamedBean namedBeanTestSystemName = new MyNamedBean("MyName", "AAA");
        NamedBean namedBeanTestUserName = new MyNamedBean("AAA", "MyName");
        
        Memory myMemory = InstanceManager.getDefault(MemoryManager.class).newMemory("MySystemName", "MemoryValue");
        
        // Test that trigger is not checked if enabled == false
        Conditional ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueWithTrigger);
        PropertyChangeEvent event = new PropertyChangeEvent(new Object(), "PropertyName", "OldValue", "NewValue") {
            @Override
            public Object getSource() {
                throw new RuntimeException();
            }
        };
        boolean success = false;
        try {
            ix1.calculate(false, event);
            success = true;
        } catch (RuntimeException ex) {
            // Do nothing
        }
        Assert.assertTrue("trigger has not been checked", success);
        
        // Test invalid event source object
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_True);
        int result = ix1.calculate(true, new PropertyChangeEvent(new Object(), "PropertyName", "OldValue", "NewValue"));
        Assert.assertTrue("calculate() returns NamedBean.TRUE", result == Conditional.TRUE);
        jmri.util.JUnitAppender.assertErrorMessageStartsWith("IXIC 1 PropertyChangeEvent source of unexpected type: java.beans.PropertyChangeEvent");
        
        // Test trigger event with bad device name
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueWithTrigger);
        ix1.setAction(conditionalActionList);
        testConditionalAction._namedBean = myMemory;
        myMemory.setValue("InitialValue");
        testConditionalAction._deviceName = null;
        ix1.calculate(true, new PropertyChangeEvent(namedBeanTestSystemName, "MyName", "OldValue", "NewValue"));
        Assert.assertTrue("action has not been executed", "InitialValue".equals(myMemory.getValue()));
        jmri.util.JUnitAppender.assertErrorMessageStartsWith("IXIC 1 - invalid memory name in action - ");
        
        // Test trigger event with system name.
        // This action wants to trigger the event.
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueWithTrigger);
        ix1.setAction(conditionalActionList);
        testConditionalAction._type = Conditional.Action.SET_MEMORY;
        testConditionalAction._namedBean = myMemory;
        myMemory.setValue("InitialValue");
        testConditionalAction._deviceName = "MyDeviceName";
        testConditionalAction._actionString = "NewValue";
        ix1.calculate(true, new PropertyChangeEvent(namedBeanTestSystemName, "MyName", "OldValue1", "NewValue2"));
        Assert.assertTrue("action has been executed", "NewValue".equals(myMemory.getValue()));
        
        // Test trigger event with user name.
        // This action wants to trigger the event.
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueWithTrigger);
        ix1.setAction(conditionalActionList);
        testConditionalAction._type = Conditional.Action.SET_MEMORY;
        testConditionalAction._namedBean = myMemory;
        myMemory.setValue("InitialValue");
        testConditionalAction._deviceName = "MyDeviceName";
        testConditionalAction._actionString = "NewValue";
        ix1.calculate(true, new PropertyChangeEvent(namedBeanTestUserName, "MyName", "OldValue1", "NewValue2"));
        Assert.assertTrue("action has been executed", "NewValue".equals(myMemory.getValue()));
        
        // Test trigger event with bad system and user name.
        // This action wants to trigger the event.
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueWithTrigger);
        ix1.setAction(conditionalActionList);
        testConditionalAction._type = Conditional.Action.SET_MEMORY;
        testConditionalAction._namedBean = myMemory;
        myMemory.setValue("InitialValue");
        testConditionalAction._deviceName = "MyDeviceName";
        testConditionalAction._actionString = "NewValue";
        ix1.calculate(true, new PropertyChangeEvent(namedBeanTestSystemName, "MyOtherName", "OldValue1", "NewValue2"));
        Assert.assertTrue("action has been executed", "NewValue".equals(myMemory.getValue()));
        
        // Test not trigger event.
        // This action does not want to trigger the event.
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueWithNotTrigger);
        ix1.setAction(conditionalActionList);
        testConditionalAction._type = Conditional.Action.SET_MEMORY;
        testConditionalAction._namedBean = myMemory;
        myMemory.setValue("InitialValue");
        testConditionalAction._deviceName = "MyDeviceName";
        testConditionalAction._actionString = "NewValue";
        ix1.calculate(true, new PropertyChangeEvent(namedBeanTestSystemName, "MyName", "OldValue1", "NewValue2"));
        Assert.assertTrue("action has not been executed", "InitialValue".equals(myMemory.getValue()));
        
        // Test trigger event on change.
        // _triggerActionsOnChange == true
        // This action want to trigger the event.
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueWithTrigger);
        ix1.setAction(conditionalActionList);
        ix1.setTriggerOnChange(true);
        testConditionalAction._type = Conditional.Action.SET_MEMORY;
        testConditionalAction._namedBean = myMemory;
        myMemory.setValue("InitialValue");
        testConditionalAction._deviceName = "MyDeviceName";
        testConditionalAction._actionString = "NewValue";
        // Calculate changes state from NamedBean.UNKNOWN to Conditional.TRUE
        ix1.calculate(true, new PropertyChangeEvent(namedBeanTestSystemName, "MyName", "OldValue1", "NewValue2"));
        Assert.assertTrue("action has been executed", "NewValue".equals(myMemory.getValue()));
        
        // Test trigger event on change.
        // _triggerActionsOnChange == true
        // This action want to trigger the event.
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueWithTrigger);
        ix1.setAction(conditionalActionList);
        ix1.setTriggerOnChange(true);
        testConditionalAction._type = Conditional.Action.SET_MEMORY;
        testConditionalAction._namedBean = myMemory;
        myMemory.setValue("InitialValue");
        testConditionalAction._deviceName = "MyDeviceName";
        testConditionalAction._actionString = "NewValue";
        // Use calculate to set state to Conditional.TRUE
        ix1.calculate(false, null);
        // Calculate doesn't change state since the state already is Conditional.TRUE
        ix1.calculate(true, new PropertyChangeEvent(namedBeanTestSystemName, "MyName", "OldValue1", "NewValue2"));
        Assert.assertTrue("action has not been executed", "InitialValue".equals(myMemory.getValue()));
        
        // Test trigger event on change.
        // _triggerActionsOnChange == false
        // This action want to trigger the event.
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueWithTrigger);
        ix1.setAction(conditionalActionList);
        ix1.setTriggerOnChange(false);
        testConditionalAction._type = Conditional.Action.SET_MEMORY;
        testConditionalAction._namedBean = myMemory;
        myMemory.setValue("InitialValue");
        testConditionalAction._deviceName = "MyDeviceName";
        testConditionalAction._actionString = "NewValue";
        // Calculate changes state from NamedBean.UNKNOWN to Conditional.TRUE
        ix1.calculate(true, new PropertyChangeEvent(namedBeanTestSystemName, "MyName", "OldValue1", "NewValue2"));
        Assert.assertTrue("action has been executed", "NewValue".equals(myMemory.getValue()));
        
        // Test trigger event on change.
        // _triggerActionsOnChange == false
        // This action want to trigger the event.
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueWithTrigger);
        ix1.setAction(conditionalActionList);
        ix1.setTriggerOnChange(false);
        testConditionalAction._type = Conditional.Action.SET_MEMORY;
        testConditionalAction._namedBean = myMemory;
        myMemory.setValue("InitialValue");
        testConditionalAction._deviceName = "MyDeviceName";
        testConditionalAction._actionString = "NewValue";
        // Use calculate to set state to Conditional.TRUE
        ix1.calculate(false, null);
        // Calculate doesn't change state since the state already is Conditional.TRUE
        ix1.calculate(true, new PropertyChangeEvent(namedBeanTestSystemName, "MyName", "OldValue1", "NewValue2"));
        Assert.assertTrue("action has been executed", "NewValue".equals(myMemory.getValue()));
    }
    
    
    private DefaultConditional getConditional(List<ConditionalVariable> conditionalVariablesList, ConditionalAction conditionalAction) {
        List<ConditionalAction> conditionalActionList = new ArrayList<>();
        conditionalActionList.add(conditionalAction);
        
        DefaultConditional ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.AntecedentOperator.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList);
        ix1.setAction(conditionalActionList);
        return ix1;
    }
    
    private TestConditionalAction getConditionalAction(Conditional.Action type, NamedBean namedBean) {
        TestConditionalAction testConditionalAction = new TestConditionalAction();
        testConditionalAction._type = type;
        testConditionalAction._namedBean = namedBean;
        testConditionalAction._deviceName = "MyDeviceName";
        return testConditionalAction;
    }
    
    // Test takeActionIfNeeded()
    // This test tests currentState and option
    @Test
    public void testActionCurrentState() {
        ConditionalVariable[] conditionalVariables_True
                = { new ConditionalVariableStatic(Conditional.State.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        ConditionalVariable[] conditionalVariables_False
                = { new ConditionalVariableStatic(Conditional.State.FALSE) };
        List<ConditionalVariable> conditionalVariablesList_False = Arrays.asList(conditionalVariables_False);
        
        TestConditionalAction testConditionalAction;
        
        // Test old state == TRUE && currentState == TRUE && option == ACTION_OPTION_ON_CHANGE_TO_TRUE
        Memory myMemory = InstanceManager.getDefault(MemoryManager.class).newMemory("MySystemName", "MemoryValue");
        testConditionalAction = getConditionalAction(Conditional.Action.SET_MEMORY, myMemory);
        testConditionalAction._option = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
        testConditionalAction._actionString = "NewValue";
        DefaultConditional ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        myMemory.setValue("OldValue");
        ix1.setStateVariables(conditionalVariablesList_True);
        ix1.calculate(true, null);
        Assert.assertTrue("memory has not been set", "OldValue".equals(myMemory.getValue()));
        
        // Test old state == FALSE && currentState == TRUE && option == ACTION_OPTION_ON_CHANGE_TO_TRUE
        testConditionalAction = getConditionalAction(Conditional.Action.SET_MEMORY, myMemory);
        testConditionalAction._actionString = "NewValue";
        testConditionalAction._option = Conditional.ACTION_OPTION_ON_CHANGE_TO_TRUE;
        ix1 = getConditional(conditionalVariablesList_False, testConditionalAction);
        ix1.calculate(true, null);
        ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        myMemory.setValue("OldValue");
        ix1.calculate(true, null);
        Assert.assertTrue("memory has been set", "NewValue".equals(myMemory.getValue()));
        
        // Test old state == FALSE && currentState == FALSE && option == ACTION_OPTION_ON_CHANGE_TO_FALSE
        testConditionalAction = getConditionalAction(Conditional.Action.SET_MEMORY, myMemory);
        testConditionalAction._option = Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE;
        testConditionalAction._actionString = "NewValue";
        ix1 = getConditional(conditionalVariablesList_False, testConditionalAction);
        ix1.calculate(true, null);
        myMemory.setValue("OldValue");
        ix1.setStateVariables(conditionalVariablesList_False);
        ix1.calculate(true, null);
        Assert.assertTrue("memory has not been set", "OldValue".equals(myMemory.getValue()));
        
        // Test old state == TRUE && currentState == FALSE && option == ACTION_OPTION_ON_CHANGE_TO_FALSE
        testConditionalAction = getConditionalAction(Conditional.Action.SET_MEMORY, myMemory);
        testConditionalAction._actionString = "NewValue";
        testConditionalAction._option = Conditional.ACTION_OPTION_ON_CHANGE_TO_FALSE;
        ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        myMemory.setValue("OldValue");
        ix1.setStateVariables(conditionalVariablesList_False);
        ix1 = getConditional(conditionalVariablesList_False, testConditionalAction);
        ix1.calculate(true, null);
        Assert.assertTrue("memory has been set", "NewValue".equals(myMemory.getValue()));
        
        // Test old state == FALSE && currentState == FALSE && option == ACTION_OPTION_ON_CHANGE
        testConditionalAction = getConditionalAction(Conditional.Action.SET_MEMORY, myMemory);
        testConditionalAction._option = Conditional.ACTION_OPTION_ON_CHANGE;
        testConditionalAction._actionString = "NewValue";
        ix1 = getConditional(conditionalVariablesList_False, testConditionalAction);
        ix1.calculate(true, null);
        myMemory.setValue("OldValue");
        ix1.setStateVariables(conditionalVariablesList_False);
        ix1.calculate(true, null);
        Assert.assertTrue("memory has not been set", "OldValue".equals(myMemory.getValue()));
        
        // Test old state == FALSE && currentState == TRUE && option == ACTION_OPTION_ON_CHANGE
        testConditionalAction = getConditionalAction(Conditional.Action.SET_MEMORY, myMemory);
        testConditionalAction._actionString = "NewValue";
        testConditionalAction._option = Conditional.ACTION_OPTION_ON_CHANGE;
        ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        myMemory.setValue("OldValue");
        ix1.setStateVariables(conditionalVariablesList_True);
        ix1 = getConditional(conditionalVariablesList_False, testConditionalAction);
        ix1.calculate(true, null);
        Assert.assertTrue("memory has been set", "NewValue".equals(myMemory.getValue()));
        
        // Test old state == TRUE && currentState == FALSE && option == ACTION_OPTION_ON_CHANGE
        testConditionalAction = getConditionalAction(Conditional.Action.SET_MEMORY, myMemory);
        testConditionalAction._option = Conditional.ACTION_OPTION_ON_CHANGE;
        testConditionalAction._actionString = "NewValue";
        ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        myMemory.setValue("OldValue");
        ix1.setStateVariables(conditionalVariablesList_False);
        ix1.calculate(true, null);
        Assert.assertTrue("memory has been set", "NewValue".equals(myMemory.getValue()));
        
        // Test old state == TRUE && currentState == TRUE && option == ACTION_OPTION_ON_CHANGE
        testConditionalAction = getConditionalAction(Conditional.Action.SET_MEMORY, myMemory);
        testConditionalAction._actionString = "NewValue";
        testConditionalAction._option = Conditional.ACTION_OPTION_ON_CHANGE;
        ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        myMemory.setValue("OldValue");
        ix1.setStateVariables(conditionalVariablesList_True);
        ix1.calculate(true, null);
        Assert.assertTrue("memory has not been set", "OldValue".equals(myMemory.getValue()));
    }
    
    @Test
    public void testAction() {
        ConditionalVariable[] conditionalVariables_True
                = { new ConditionalVariableStatic(Conditional.State.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        TestConditionalAction testConditionalAction;
        
        
        
        // Test ACTION_NONE
        
        // Test ACTION_SET_TURNOUT
        // Test ACTION_SET_SIGNAL_APPEARANCE
        // Test ACTION_SET_SIGNAL_HELD
        // Test ACTION_CLEAR_SIGNAL_HELD
        // Test ACTION_SET_SIGNAL_DARK
        // Test ACTION_SET_SIGNAL_LIT
        // Test ACTION_TRIGGER_ROUTE
        // Test ACTION_SET_SENSOR
        // Test ACTION_DELAYED_SENSOR
        // Test ACTION_SET_LIGHT
        // Test ACTION_SET_MEMORY
        Memory myMemory = InstanceManager.getDefault(MemoryManager.class).newMemory("MySystemName", "MemoryValue");
        testConditionalAction = getConditionalAction(Conditional.Action.SET_MEMORY, myMemory);
        testConditionalAction._actionString = "NewValue";
        DefaultConditional ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        Assert.assertTrue("memory has been set", "NewValue".equals(myMemory.getValue()));
        
        // Test ACTION_ENABLE_LOGIX
        // Test system name
        Logix x = InstanceManager.getDefault(jmri.LogixManager.class).createNewLogix("MySystemName", "MyUserName");
        x.setEnabled(false);
        testConditionalAction = getConditionalAction(Conditional.Action.ENABLE_LOGIX, myMemory);
        testConditionalAction._deviceName = x.getSystemName();
        ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        Assert.assertTrue("logix has been enabled", x.getEnabled());
        
        // Test user name
        x.setEnabled(false);
        testConditionalAction = getConditionalAction(Conditional.Action.ENABLE_LOGIX, myMemory);
        testConditionalAction._deviceName = x.getUserName();
        ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        Assert.assertTrue("logix has been enabled", x.getEnabled());
        
        // Test ACTION_DISABLE_LOGIX
        // Test system name
        x.setEnabled(true);
        testConditionalAction = getConditionalAction(Conditional.Action.DISABLE_LOGIX, myMemory);
        testConditionalAction._deviceName = x.getSystemName();
        ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        Assert.assertFalse("logix has been disabled", x.getEnabled());
        
        // Test user name
        x.setEnabled(true);
        testConditionalAction = getConditionalAction(Conditional.Action.DISABLE_LOGIX, myMemory);
        testConditionalAction._deviceName = x.getUserName();
        ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        Assert.assertFalse("logix has been disabled", x.getEnabled());
        
        // Test ACTION_RUN_SCRIPT
        // Test ACTION_DELAYED_TURNOUT
        // Test ACTION_LOCK_TURNOUT
        // Test ACTION_RESET_DELAYED_SENSOR
        // Test ACTION_CANCEL_SENSOR_TIMERS
        // Test ACTION_RESET_DELAYED_TURNOUT
        // Test ACTION_CANCEL_TURNOUT_TIMERS
        // Test ACTION_SET_FAST_CLOCK_TIME
        // Test ACTION_START_FAST_CLOCK
        // Test ACTION_STOP_FAST_CLOCK
        
        // Test ACTION_COPY_MEMORY
        // Test copy to memory by system name
        Memory destMemory = InstanceManager.getDefault(MemoryManager.class).newMemory("SomeSystemName", "SomeUserName");
        destMemory.setValue("OtherValue");
        myMemory.setValue("MemoryValue");
        testConditionalAction = getConditionalAction(Conditional.Action.COPY_MEMORY, myMemory);
        testConditionalAction._actionString = destMemory.getSystemName();
        ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        Assert.assertTrue("memory has been copied", myMemory.getValue().equals(destMemory.getValue()));
        
        // Test copy to memory by user name
        destMemory.setValue("OtherValue");
        myMemory.setValue("MemoryValue");
        testConditionalAction = getConditionalAction(Conditional.Action.COPY_MEMORY, myMemory);
        testConditionalAction._actionString = destMemory.getUserName();
        ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        Assert.assertTrue("memory has been copied", myMemory.getValue().equals(destMemory.getValue()));
        
        // Test ACTION_SET_LIGHT_INTENSITY
        // Test ACTION_SET_LIGHT_TRANSITION_TIME
        // Test ACTION_CONTROL_AUDIO
        // Test ACTION_JYTHON_COMMAND
        // Test ACTION_ALLOCATE_WARRANT_ROUTE
        // Test ACTION_DEALLOCATE_WARRANT_ROUTE
        // Test ACTION_SET_ROUTE_TURNOUTS
        // Test ACTION_AUTO_RUN_WARRANT
        // Test ACTION_CONTROL_TRAIN
        // Test ACTION_SET_TRAIN_ID
        // Test ACTION_SET_SIGNALMAST_ASPECT
        // Test ACTION_THROTTLE_FACTOR
        // Test ACTION_SET_SIGNALMAST_HELD
        // Test ACTION_CLEAR_SIGNALMAST_HELD
        // Test ACTION_SET_SIGNALMAST_DARK
        // Test ACTION_SET_SIGNALMAST_LIT
        // Test ACTION_SET_BLOCK_ERROR
        // Test ACTION_CLEAR_BLOCK_ERROR
        // Test ACTION_DEALLOCATE_BLOCK
        // Test ACTION_SET_BLOCK_OUT_OF_SERVICE
        // Test ACTION_SET_BLOCK_IN_SERVICE
        // Test ACTION_MANUAL_RUN_WARRANT
        // Test ACTION_SET_TRAIN_NAME
        // Test ACTION_SET_BLOCK_VALUE
        // Test ACTION_SET_NXPAIR_ENABLED
        // Test ACTION_SET_NXPAIR_DISABLED
        // Test ACTION_SET_NXPAIR_SEGMENT
    }
    
    @Test
    public void testActionPlaySound() {
        ConditionalVariable[] conditionalVariables_True
                = { new ConditionalVariableStatic(Conditional.State.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        TestConditionalAction testConditionalAction;
        
        // Test ACTION_PLAY_SOUND
        try {
            javax.sound.sampled.AudioSystem.getClip();
        } catch (IllegalArgumentException | javax.sound.sampled.LineUnavailableException ex) {
            Assume.assumeNoException("Unable to initialize AudioSystem", ex);
        }
        Memory myMemory = InstanceManager.getDefault(MemoryManager.class).newMemory("MySystemName2", "MemoryValue2");
        testConditionalAction = getConditionalAction(Conditional.Action.PLAY_SOUND, myMemory);
        testConditionalAction._actionString = "MySound.wav";
        MySound sound = new MySound();
        testConditionalAction.setSound(sound);
        Conditional ix1 = getConditional(conditionalVariablesList_True, testConditionalAction);
        ix1.calculate(true, null);
        Assert.assertTrue("sound has played", sound.hasPlayed);
    }

    
    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp(){
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initIdTagManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    
    private final class ConditionalVariableStatic extends ConditionalVariable {
        
        ConditionalVariableStatic(Conditional.State state) {
            super();
            setState(state.getIntValue());
        }
        
        ConditionalVariableStatic(Conditional.State state, boolean not) {
            super();
            setState(state.getIntValue());
            setNegation(not);
        }
        
        ConditionalVariableStatic(Conditional.State state, String name, boolean trigger) {
            super();
            setName(name);
            setState(state.getIntValue());
            setTriggerActions(trigger);
        }
        
        @Override
        public boolean evaluate() {
            return getState() == Conditional.State.TRUE.getIntValue();
        }
        
    }
    
    
    private class TestConditionalAction extends DefaultConditionalAction {
        
        Conditional.Action _type = Conditional.Action.NONE;
        int _option = Conditional.ACTION_OPTION_ON_CHANGE;
        String _deviceName = null;
        NamedBean _namedBean = null;
        String _actionString = null;

        @Override
        public int getActionData() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getActionDataString() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getActionString() {
            return _actionString;
        }

        @Override
        public String getDeviceName() {
            return _deviceName;
        }

        @Override
        public int getOption() {
            return _option;
        }

        @Override
        public String getOptionString(boolean type) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Conditional.Action getType() {
            return _type;
        }

        @Override
        public String getTypeString() {
            return "Not supported yet.";
        }

        @Override
        public void setActionData(String actionData) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setActionData(int actionData) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setActionString(String actionString) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setDeviceName(String deviceName) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setOption(int option) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setType(String type) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setType(Conditional.Action type) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String description(boolean triggerType) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Timer getTimer() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setTimer(Timer timer) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isTimerActive() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void startTimer() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void stopTimer() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ActionListener getListener() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setListener(ActionListener listener) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public NamedBeanHandle<?> getNamedBean() {
            if (_namedBean != null) {
                return new NamedBeanHandle<>("Bean", _namedBean);
            } else {
                return null;
            }
        }

        @Override
        public NamedBean getBean() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    
    }
    
    
    
    private class MyNamedBean extends AbstractNamedBean {

        public MyNamedBean(String systemName, String userName) {
            super(systemName);
            setUserName(userName);
        }

        @Override
        public void setState(int s) throws JmriException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getBeanType() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    
    private class MySound extends Sound {
        
        boolean hasPlayed = false;
        
        MySound() {
            super("program:resources/sounds/bell_stroke.wav");
        }
        
        @Override
        public void play() {
            hasPlayed = true;
        }
    }
    
}
