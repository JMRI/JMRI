package jmri.implementation;

import java.util.Arrays;
import java.util.List;
import jmri.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the DefaultConditional implementation class
 *
 * @author Bob Jacobsen Copyright (C) 2015
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
        ix1.setLogicType(Conditional.MIXED, antecedent);
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
            jmri.util.JUnitAppender.assertErrorMessage(errorMessage);
        }
    }
    
    @Test
    public void testValidate() {
        ConditionalVariable[] conditionalVariables_Empty
                = { new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_Empty = Arrays.asList(conditionalVariables_Empty);
        
        ConditionalVariable[] conditionalVariables_True
                = { new ConditionalVariableStatic(Conditional.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        ConditionalVariable[] conditionalVariables_TrueTrueTrue
                = { new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.TRUE) };
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
        testValidate(EXPECT_SUCCESS, "R2 AND R1 or R3", conditionalVariablesList_TrueTrueTrue);
        
        // Test several items and parenthese
        testValidate(EXPECT_SUCCESS, "(R1 and R3) and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "(R1 and) R3 and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1( and R3) and not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "R1 (and R3 and) not R2", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_FAILURE, "(R1 and R3) and not R2)", conditionalVariablesList_TrueTrueTrue);
        testValidate(EXPECT_SUCCESS, "(R1 and (R3) and not R2)", conditionalVariablesList_TrueTrueTrue);
    }
    
    @Test
    public void testCalculate() {
        ConditionalVariable[] conditionalVariables_Empty
                = { new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_Empty = Arrays.asList(conditionalVariables_Empty);
        
        ConditionalVariable[] conditionalVariables_True
                = { new ConditionalVariableStatic(Conditional.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_True = Arrays.asList(conditionalVariables_True);
        
        ConditionalVariable[] conditionalVariables_False
                = { new ConditionalVariableStatic(Conditional.FALSE) };
        List<ConditionalVariable> conditionalVariablesList_False = Arrays.asList(conditionalVariables_False);
        
        ConditionalVariable[] conditionalVariables_TrueTrueTrue
                = { new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_TrueTrueTrue = Arrays.asList(conditionalVariables_TrueTrueTrue);
        
        ConditionalVariable[] conditionalVariables_FalseFalseFalse
                = {new ConditionalVariableStatic(Conditional.FALSE)
                        , new ConditionalVariableStatic(Conditional.FALSE)
                        , new ConditionalVariableStatic(Conditional.FALSE) };
        List<ConditionalVariable> conditionalVariablesList_FalseFalseFalse = Arrays.asList(conditionalVariables_FalseFalseFalse);
        
        ConditionalVariable[] conditionalVariables_TrueTrueFalse
                = {new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.FALSE) };
        List<ConditionalVariable> conditionalVariablesList_TrueTrueFalse = Arrays.asList(conditionalVariables_TrueTrueFalse);
        
        ConditionalVariable[] conditionalVariables_FalseTrueTrue
                = {new ConditionalVariableStatic(Conditional.FALSE)
                        , new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_FalseTrueTrue = Arrays.asList(conditionalVariables_FalseTrueTrue);
        
        ConditionalVariable[] conditionalVariables_TrueFalseTrue
                = {new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.FALSE)
                        , new ConditionalVariableStatic(Conditional.TRUE) };
        List<ConditionalVariable> conditionalVariablesList_TrueFalseTrue = Arrays.asList(conditionalVariables_TrueFalseTrue);
        
        ConditionalVariable[] conditionalVariables_FalseTrueFalse
                = {new ConditionalVariableStatic(Conditional.FALSE)
                        , new ConditionalVariableStatic(Conditional.TRUE)
                        , new ConditionalVariableStatic(Conditional.FALSE) };
        List<ConditionalVariable> conditionalVariablesList_FalseTrueFalse = Arrays.asList(conditionalVariables_FalseTrueFalse);
        
        
        // Test empty antecedent string
        testCalculate(Conditional.FALSE, "", conditionalVariablesList_Empty,
                "IXIC 1 parseCalculation error antecedent= , ex= java.lang.StringIndexOutOfBoundsException: String index out of range: 0");
        
        testCalculate(Conditional.TRUE, "R1", conditionalVariablesList_True, "");
        testCalculate(Conditional.FALSE, "R1", conditionalVariablesList_False, "");
        testCalculate(Conditional.FALSE, "R2", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= R2, ex= java.lang.ArrayIndexOutOfBoundsException: 1");
        
        // Test parentheses
        testCalculate(Conditional.TRUE, "([{R1)}]", conditionalVariablesList_True, "");
        testCalculate(Conditional.FALSE, "(R2", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= (R2, ex= java.lang.ArrayIndexOutOfBoundsException: 1");
        testCalculate(Conditional.FALSE, "R2)", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= R2), ex= java.lang.ArrayIndexOutOfBoundsException: 1");
        
        // Test several items
        testCalculate(Conditional.FALSE, "R1 and R2 and R3", conditionalVariablesList_True,
                "IXIC 1 parseCalculation error antecedent= R1 and R2 and R3, ex= java.lang.ArrayIndexOutOfBoundsException: 1");
        testCalculate(Conditional.TRUE, "R1", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.TRUE, "R2", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.TRUE, "R3", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.TRUE, "R1 and R2 and R3", conditionalVariablesList_TrueTrueTrue, "");
        testCalculate(Conditional.TRUE, "R2 AND R1 or R3", conditionalVariablesList_TrueTrueTrue, "");
        
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
        ix1.setLogicType(Conditional.ALL_AND, "");
        ix1.setStateVariables(conditionalVariablesList_TrueTrueTrue);
        Assert.assertTrue("calculate() returns NamedBean.TRUE", ix1.calculate(false, null) == Conditional.TRUE);
        
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.ALL_AND, "");
        ix1.setStateVariables(conditionalVariablesList_TrueTrueFalse);
        Assert.assertTrue("calculate() returns NamedBean.FALSE", ix1.calculate(false, null) == Conditional.FALSE);
        
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.ALL_AND, "");
        ix1.setStateVariables(conditionalVariablesList_FalseFalseFalse);
        Assert.assertTrue("calculate() returns NamedBean.FALSE", ix1.calculate(false, null) == Conditional.FALSE);
        
        
        // Test ALL_OR
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueTrueTrue);
        Assert.assertTrue("calculate() returns NamedBean.TRUE", ix1.calculate(false, null) == Conditional.TRUE);
        
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_TrueTrueFalse);
        Assert.assertTrue("calculate() returns NamedBean.FALSE", ix1.calculate(false, null) == Conditional.TRUE);
        
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.ALL_OR, "");
        ix1.setStateVariables(conditionalVariablesList_FalseFalseFalse);
        Assert.assertTrue("calculate() returns NamedBean.FALSE", ix1.calculate(false, null) == Conditional.FALSE);
    }

    
    // from here down is testing infrastructure

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    
    private final class ConditionalVariableStatic extends ConditionalVariable {
        
        ConditionalVariableStatic(int state) {
            super();
            super.setState(state);
        }
        
        @Override
        public boolean evaluate() {
            return this.getState() == Conditional.TRUE;
        }
        
    }
    
}
