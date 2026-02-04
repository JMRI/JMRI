package jmri.jmrit.logixng.util.parser.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ClockFunctions
 *
 * @author Daniel Bergqvist 2020
 */
public class ClockFunctionsTest {

    private final ExpressionNode expr_str_HOUR = new ExpressionNodeString(new Token(TokenType.NONE, "hour", 0));
    private final ExpressionNode expr_str_MIN = new ExpressionNodeString(new Token(TokenType.NONE, "min", 0));
    private final ExpressionNode expr_str_SEC = new ExpressionNodeString(new Token(TokenType.NONE, "sec", 0));
    private final ExpressionNode expr_str_MIN_OF_DAY = new ExpressionNodeString(new Token(TokenType.NONE, "minOfDay", 0));
    private final ExpressionNode expr_str_SEC_OF_DAY = new ExpressionNodeString(new Token(TokenType.NONE, "secOfDay", 0));


    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }

    // We cannot set the system clock so we allow some difference
    private boolean compare(int a, int b, int allowedDiff) {
        return Math.abs(a-b) <= allowedDiff;
    }

    @Test
    public void testCurrentTimeMillisFunction() throws JmriException {
        Function currentTimeMillisFunction = InstanceManager.getDefault(FunctionManager.class).get("currentTimeMillis");
        assertEquals( "currentTimeMillis", currentTimeMillisFunction.getName(), "strings matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        long currentTimeBefore = System.currentTimeMillis();
        JUnitUtil.waitFor(10);
        long result = (long) currentTimeMillisFunction.calculate(symbolTable, getParameterList());
        JUnitUtil.waitFor(10);
        long currentTimeAfter = System.currentTimeMillis();

        assertTrue(currentTimeBefore < result);
        assertTrue(currentTimeAfter > result);
    }

    @Test
    @SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
    public void testSystemClockFunction() throws JmriException {
        Function systemClockFunction = InstanceManager.getDefault(FunctionManager.class).get("systemClock");
        assertEquals( "systemClock", systemClockFunction.getName(), "strings matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        Date currentTime = Date.from(Instant.now());
        int minSinceMidnight = (currentTime.getHours() * 60) + currentTime.getMinutes();
        int secSinceMidnight = ((currentTime.getHours() * 60) + currentTime.getMinutes()) * 60 + currentTime.getSeconds();
        assertTrue(compare(minSinceMidnight, (Integer)systemClockFunction.calculate(symbolTable, getParameterList()), 1));
        assertTrue(compare(currentTime.getHours(), (Integer)systemClockFunction.calculate(symbolTable, getParameterList(expr_str_HOUR)), 1));
        assertTrue(compare(currentTime.getMinutes(), (Integer)systemClockFunction.calculate(symbolTable, getParameterList(expr_str_MIN)), 1));
        assertTrue(compare(currentTime.getSeconds(), (Integer)systemClockFunction.calculate(symbolTable, getParameterList(expr_str_SEC)), 1));
        assertTrue(compare(minSinceMidnight, (Integer)systemClockFunction.calculate(symbolTable, getParameterList(expr_str_MIN_OF_DAY)), 1));
        assertTrue(compare(secSinceMidnight, (Integer)systemClockFunction.calculate(symbolTable, getParameterList(expr_str_SEC_OF_DAY)), 20));
    }

    @Test
    @SuppressWarnings("deprecation")        // new Date(0,0,0,0,0)
    public void testFastClockFunction() throws JmriException {
        Function fastClockFunction = InstanceManager.getDefault(FunctionManager.class).get("fastClock");
        assertEquals( "fastClock", fastClockFunction.getName(), "strings matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        Timebase fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        fastClock.setRun(false);
        fastClock.setTime(new Date(0,0,0,11,05));   // 11:05

        int minSinceMidnight = (11 * 60) + 5;
        assertEquals(minSinceMidnight, (int)fastClockFunction.calculate(symbolTable, getParameterList()));
        assertEquals(11, (int)fastClockFunction.calculate(symbolTable, getParameterList(expr_str_HOUR)));
        assertEquals(5, (int)fastClockFunction.calculate(symbolTable, getParameterList(expr_str_MIN)));
        assertEquals(minSinceMidnight, (int)fastClockFunction.calculate(symbolTable, getParameterList(expr_str_MIN_OF_DAY)));
    }

    @Test
    public void testFastClockRateFunction() throws TimebaseRateException, JmriException {
        Function fastClockRateFunction = InstanceManager.getDefault(FunctionManager.class).get("fastClockRate");
        assertEquals( "fastClockRate", fastClockRateFunction.getName(), "strings matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        Timebase fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        fastClock.setRate(1.0);
        assertEquals(1.0, (double)fastClockRateFunction.calculate(symbolTable, getParameterList()), 0.000001);
        fastClock.setRate(2.0);
        assertEquals(2.0, (double)fastClockRateFunction.calculate(symbolTable, getParameterList()), 0.000001);
        fastClock.setRate(60.0);
        assertEquals(60.0, (double)fastClockRateFunction.calculate(symbolTable, getParameterList()), 0.000001);
        fastClock.setRate(1.0);
        assertEquals(1.0, (double)fastClockRateFunction.calculate(symbolTable, getParameterList()), 0.000001);
    }

    @Test
    public void testIsFastClockRunningFunction() throws JmriException {
        Function isFastClockFunction = InstanceManager.getDefault(FunctionManager.class).get("isFastClockRunning");
        assertEquals( "isFastClockRunning", isFastClockFunction.getName(), "strings matches");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        Timebase fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        fastClock.setRun(false);
        assertFalse((boolean)isFastClockFunction.calculate(symbolTable, getParameterList()));
        fastClock.setRun(true);
        assertTrue((boolean)isFastClockFunction.calculate(symbolTable, getParameterList()));
        fastClock.setRun(false);
        assertFalse((boolean)isFastClockFunction.calculate(symbolTable, getParameterList()));
        fastClock.setRun(true);
        assertTrue((boolean)isFastClockFunction.calculate(symbolTable, getParameterList()));
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

}
