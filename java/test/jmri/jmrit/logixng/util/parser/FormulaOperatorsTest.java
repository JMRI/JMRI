package jmri.jmrit.logixng.util.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;

import jmri.JmriException;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test comparing a decimal number with an integer number and the
 * short-circuit evaluation of the operators &amp;&amp; and ||.
 *
 * @author Keith Bruce 2026
 */
public class FormulaOperatorsTest {

    private Object calculate(String formula) throws ParserException, JmriException {
        ExpressionNode node = new RecursiveDescentParser(new HashMap<>()).parseExpression(formula);
        return node.calculate(new DefaultSymbolTable());
    }

    @Test
    public void testCompareDecimalWithInteger() throws ParserException, JmriException {
        assertEquals(false, calculate("1.5 == 2"), "1.5 == 2");
        assertEquals(true, calculate("1.5 != 2"), "1.5 != 2");
        assertEquals(true, calculate("2.0 != 3"), "2.0 != 3");
        assertEquals(true, calculate("2.0 == 2"), "2.0 == 2");
        assertEquals(true, calculate("2.5 < 3"), "2.5 < 3");
        assertEquals(true, calculate("2.5 <= 3"), "2.5 <= 3");
        assertEquals(false, calculate("2.5 > 3"), "2.5 > 3");
        assertEquals(false, calculate("2.5 >= 3"), "2.5 >= 3");
        assertEquals(true, calculate("3.5 > 3"), "3.5 > 3");

        // Integer on the left side already worked, check it still does
        assertEquals(false, calculate("2 == 1.5"), "2 == 1.5");
        assertEquals(true, calculate("3 > 2.5"), "3 > 2.5");
        assertEquals(true, calculate("2 == 2.0"), "2 == 2.0");
    }

    @Test
    public void testShortCircuit() throws ParserException, JmriException {
        // The right side is not a boolean value, and the operator throws an
        // exception if the right side is calculated
        assertEquals(false, calculate("(1 == 2) && \"abc\""), "false && error");
        assertEquals(true, calculate("(1 == 1) || \"abc\""), "true || error");
        assertThrows(CalculateException.class, () -> calculate("(1 == 1) && \"abc\""), "true && error");
        assertThrows(CalculateException.class, () -> calculate("(1 == 2) || \"abc\""), "false || error");

        // When the left side doesn't decide the result, the right side is calculated
        assertEquals(true, calculate("(1 == 1) && (2 == 2)"), "true && true");
        assertEquals(false, calculate("(1 == 1) && (2 == 3)"), "true && false");
        assertEquals(true, calculate("(1 == 2) || (2 == 2)"), "false || true");
        assertEquals(false, calculate("(1 == 2) || (2 == 3)"), "false || false");
        assertEquals(true, calculate("(1 == 2) ^^ (2 == 2)"), "false ^^ true");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
