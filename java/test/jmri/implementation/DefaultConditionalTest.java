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
    
    @Test
    public void testCalculate() {
        Conditional ix1 = new DefaultConditional("IXIC 1");
        Assert.assertTrue("calculate() returns NamedBean.UNKNOWN", ix1.calculate(false, null) == NamedBean.UNKNOWN);
        
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
        
        
        
        ix1 = new DefaultConditional("IXIC 1");
        ix1.setLogicType(Conditional.ALL_AND, "");
        ix1.setStateVariables(conditionalVariablesList_TrueTrueTrue);
        Assert.assertTrue("calculate() returns NamedBean.TRUE", ix1.calculate(false, null) == Conditional.TRUE);
        
        DefaultConditional ix2 = new DefaultConditional("IXIC 2");
        ix2.setLogicType(Conditional.ALL_AND, "");
        ix2.setStateVariables(conditionalVariablesList_TrueTrueFalse);
        Assert.assertTrue("calculate() returns NamedBean.FALSE", ix2.calculate(false, null) == Conditional.FALSE);
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
