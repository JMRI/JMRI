package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import jmri.*;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Check the results of Conditionals with a MIXED antecedent, with one and
 * two digit variable numbers, and that every variable is evaluated once,
 * from left to right.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DefaultConditionalMixedTest {

    private final List<Integer> evaluated = new ArrayList<>();

    private class FixedVariable extends ConditionalVariable {

        private final int number;
        private boolean value;

        FixedVariable(int number, boolean negated) {
            super(negated, Conditional.Operator.AND, Conditional.Type.SENSOR_ACTIVE, "IS" + number, true);
            this.number = number;
        }

        @Override
        public boolean evaluate() {
            evaluated.add(number);
            return value;
        }
    }

    /**
     * Antecedents with their results for variable values given as a string
     * of 0 and 1 for R1 to R12, and the variables evaluated in order.
     * MIXED antecedents are worked out strictly from left to right.
     */
    private static final Object[][] CASES = {
        {"R1", "100000000000", true, List.of(1)},
        {"not R1", "100000000000", false, List.of(1)},
        {"R1 and R2", "100000000000", false, List.of(1, 2)},
        {"R1 or R2 and R3", "100000000000", false, List.of(1, 2, 3)},   // (R1 or R2) and R3
        {"R1 or R2 and R3", "101000000000", true, List.of(1, 2, 3)},
        {"R1 and R2 or R3", "001000000000", true, List.of(1, 2, 3)},
        {"R1 or (R2 and R3)", "100000000000", true, List.of(1, 2, 3)},
        {"not (R1 or R2) and not R3", "000000000000", true, List.of(1, 2, 3)},
        {"{R1 or R2} and [R3 or not R4]", "010000000000", true, List.of(1, 2, 3, 4)},
        {"r1 OR r2 aNd R3", "011000000000", true, List.of(1, 2, 3)},
        {"R10 or R2 and R11", "000000000110", true, List.of(10, 2, 11)},
        {"R10 or R2 and R11", "000000000100", false, List.of(10, 2, 11)},
        {"R1 and R12", "100000000001", true, List.of(1, 12)},
        {"R9 and not R10 or R11", "000000001000", true, List.of(9, 10, 11)},
        {"(R1 and R2)", "110000000000", true, List.of(1, 2)},
        {"R1 and (R12)", "100000000001", true, List.of(1, 12)},
        {"R1 or R2) and R3", "100000000000", true, List.of(1, 2)},     // text after ")" ignored
    };

    private DefaultConditional makeConditional(String values, boolean negateThird) {
        List<ConditionalVariable> vars = new ArrayList<>();
        for (int i = 1; i <= values.length(); i++) {
            FixedVariable v = new FixedVariable(i, negateThird && i == 3);
            v.value = values.charAt(i - 1) == '1';
            vars.add(v);
        }
        DefaultConditional c = new DefaultConditional("IX1C1");
        c.setStateVariables(vars);
        return c;
    }

    @Test
    public void testResultsAndOrder() {
        for (Object[] t : CASES) {
            String antecedent = (String) t[0];
            DefaultConditional c = makeConditional((String) t[1], false);
            c.setLogicType(Conditional.AntecedentOperator.MIXED, antecedent);
            evaluated.clear();
            int state = c.calculate(false, null);
            assertEquals(((Boolean) t[2]) ? Conditional.TRUE : Conditional.FALSE, state, antecedent + " " + t[1]);
            assertEquals(t[3], evaluated, "variables evaluated in order: " + antecedent);
        }
    }

    @Test
    public void testNegatedVariable() {
        // R3 is negated in its state variable, so R3 is true when its sensor is not
        DefaultConditional c = makeConditional("100000000000", true);
        c.setLogicType(Conditional.AntecedentOperator.MIXED, "R1 and R3");
        assertEquals(Conditional.TRUE, c.calculate(false, null));
        c.setLogicType(Conditional.AntecedentOperator.MIXED, "R1 and not R3");
        assertEquals(Conditional.FALSE, c.calculate(false, null));
    }

    @Test
    public void testErrorsReportedOnEveryCalculation() {
        DefaultConditional c = makeConditional("111", false);
        c.setLogicType(Conditional.AntecedentOperator.MIXED, "R1 and R4");
        for (int n = 0; n < 2; n++) {
            assertEquals(Conditional.FALSE, c.calculate(false, null), "missing variable");
            JUnitAppender.assertErrorMessageStartsWith(
                    "IX1C1 parseCalculation error antecedent= R1 and R4, ex= java.lang.IndexOutOfBoundsException");
        }
        c.setLogicType(Conditional.AntecedentOperator.MIXED, "R1 xor R2");
        for (int n = 0; n < 2; n++) {
            assertEquals(Conditional.FALSE, c.calculate(false, null), "bad operator");
            JUnitAppender.assertErrorMessageStartsWith(
                    "IX1C1 parseCalculation error antecedent= R1 xor R2, ex= jmri.JmriException");
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        for (int i = 1; i <= 12; i++) {
            InstanceManager.sensorManagerInstance().provideSensor("IS" + i);
        }
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
