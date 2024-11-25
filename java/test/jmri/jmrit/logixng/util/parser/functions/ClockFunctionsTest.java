package jmri.jmrit.logixng.util.parser.functions;

import java.time.Instant;
import java.util.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ClockFunctions
 *
 * @author Daniel Bergqvist 2020
 */
public class ClockFunctionsTest {

    ExpressionNode expr_str_HOUR = new ExpressionNodeString(new Token(TokenType.NONE, "hour", 0));
    ExpressionNode expr_str_MIN = new ExpressionNodeString(new Token(TokenType.NONE, "min", 0));
    ExpressionNode expr_str_SEC = new ExpressionNodeString(new Token(TokenType.NONE, "sec", 0));
    ExpressionNode expr_str_MIN_OF_DAY = new ExpressionNodeString(new Token(TokenType.NONE, "minOfDay", 0));
    ExpressionNode expr_str_SEC_OF_DAY = new ExpressionNodeString(new Token(TokenType.NONE, "secOfDay", 0));


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
    @SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
    public void testSystemClockFunction() throws Exception {
        Function systemClockFunction = InstanceManager.getDefault(FunctionManager.class).get("systemClock");
        Assert.assertEquals("strings matches", "systemClock", systemClockFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        Date currentTime = Date.from(Instant.now());
        int minSinceMidnight = (currentTime.getHours() * 60) + currentTime.getMinutes();
        int secSinceMidnight = ((currentTime.getHours() * 60) + currentTime.getMinutes()) * 60 + currentTime.getSeconds();
        Assert.assertTrue(compare(minSinceMidnight, (Integer)systemClockFunction.calculate(symbolTable, getParameterList()), 1));
        Assert.assertTrue(compare(currentTime.getHours(), (Integer)systemClockFunction.calculate(symbolTable, getParameterList(expr_str_HOUR)), 1));
        Assert.assertTrue(compare(currentTime.getMinutes(), (Integer)systemClockFunction.calculate(symbolTable, getParameterList(expr_str_MIN)), 1));
        Assert.assertTrue(compare(currentTime.getSeconds(), (Integer)systemClockFunction.calculate(symbolTable, getParameterList(expr_str_SEC)), 1));
        Assert.assertTrue(compare(minSinceMidnight, (Integer)systemClockFunction.calculate(symbolTable, getParameterList(expr_str_MIN_OF_DAY)), 1));
        Assert.assertTrue(compare(secSinceMidnight, (Integer)systemClockFunction.calculate(symbolTable, getParameterList(expr_str_SEC_OF_DAY)), 20));
    }

    @Test
    @SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
    public void testFastClockFunction() throws Exception {
        Function fastClockFunction = InstanceManager.getDefault(FunctionManager.class).get("fastClock");
        Assert.assertEquals("strings matches", "fastClock", fastClockFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        jmri.Timebase fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        fastClock.setRun(false);
        fastClock.setTime(new Date(0,0,0,11,05));   // 11:05

        int minSinceMidnight = (11 * 60) + 5;
        Assert.assertEquals(minSinceMidnight, (int)fastClockFunction.calculate(symbolTable, getParameterList()));
        Assert.assertEquals(11, (int)fastClockFunction.calculate(symbolTable, getParameterList(expr_str_HOUR)));
        Assert.assertEquals(5, (int)fastClockFunction.calculate(symbolTable, getParameterList(expr_str_MIN)));
        Assert.assertEquals(minSinceMidnight, (int)fastClockFunction.calculate(symbolTable, getParameterList(expr_str_MIN_OF_DAY)));
    }

    @Test
    @SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
    public void testFastClockRateFunction() throws Exception {
        Function fastClockRateFunction = InstanceManager.getDefault(FunctionManager.class).get("fastClockRate");
        Assert.assertEquals("strings matches", "fastClockRate", fastClockRateFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        jmri.Timebase fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        fastClock.setRate(1.0);
        Assert.assertEquals(1.0, (double)fastClockRateFunction.calculate(symbolTable, getParameterList()), 0.000001);
        fastClock.setRate(2.0);
        Assert.assertEquals(2.0, (double)fastClockRateFunction.calculate(symbolTable, getParameterList()), 0.000001);
        fastClock.setRate(60.0);
        Assert.assertEquals(60.0, (double)fastClockRateFunction.calculate(symbolTable, getParameterList()), 0.000001);
        fastClock.setRate(1.0);
        Assert.assertEquals(1.0, (double)fastClockRateFunction.calculate(symbolTable, getParameterList()), 0.000001);
    }

    @Test
    @SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
    public void testIsFastClockRunningFunction() throws Exception {
        Function isFastClockFunction = InstanceManager.getDefault(FunctionManager.class).get("isFastClockRunning");
        Assert.assertEquals("strings matches", "isFastClockRunning", isFastClockFunction.getName());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        jmri.Timebase fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        fastClock.setRun(false);
        Assert.assertFalse((boolean)isFastClockFunction.calculate(symbolTable, getParameterList()));
        fastClock.setRun(true);
        Assert.assertTrue((boolean)isFastClockFunction.calculate(symbolTable, getParameterList()));
        fastClock.setRun(false);
        Assert.assertFalse((boolean)isFastClockFunction.calculate(symbolTable, getParameterList()));
        fastClock.setRun(true);
        Assert.assertTrue((boolean)isFastClockFunction.calculate(symbolTable, getParameterList()));
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
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
