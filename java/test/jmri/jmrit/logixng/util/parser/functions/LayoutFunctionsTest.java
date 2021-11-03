package jmri.jmrit.logixng.util.parser.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test LayoutFunctions
 * 
 * @author Daniel Bergqvist 2021
 */
public class LayoutFunctionsTest {

    private final ExpressionNode exprTurnoutIT1 = new ExpressionNodeString(new Token(TokenType.NONE, "IT1", 0));
    private final ExpressionNode exprTurnoutMyTurnout = new ExpressionNodeString(new Token(TokenType.NONE, "My turnout", 0));
    private final ExpressionNode exprTurnoutClosed = new ExpressionNodeString(new Token(TokenType.NONE, Integer.toString(Turnout.CLOSED), 0));
    private final ExpressionNode exprTurnoutThrown = new ExpressionNodeString(new Token(TokenType.NONE, Integer.toString(Turnout.THROWN), 0));
    
    private final ExpressionNode exprSensorIS1 = new ExpressionNodeString(new Token(TokenType.NONE, "IS1", 0));
    private final ExpressionNode exprSensorMySensor = new ExpressionNodeString(new Token(TokenType.NONE, "My sensor", 0));
    private final ExpressionNode exprSensorInactive = new ExpressionNodeString(new Token(TokenType.NONE, Integer.toString(Sensor.INACTIVE), 0));
    private final ExpressionNode exprSensorActive = new ExpressionNodeString(new Token(TokenType.NONE, Integer.toString(Sensor.ACTIVE), 0));
    
    private final int SignalHead_Red = 1;
    private final int SignalHead_Green = 2;
    
    private final ExpressionNode exprSignalHeadIH1 = new ExpressionNodeString(new Token(TokenType.NONE, "IH1", 0));
    private final ExpressionNode exprSignalHeadMySignalHead = new ExpressionNodeString(new Token(TokenType.NONE, "My signal head", 0));
    private final ExpressionNode exprSignalHeadRed = new ExpressionNodeString(new Token(TokenType.NONE, Integer.toString(SignalHead_Red), 0));
    private final ExpressionNode exprSignalHeadGreen = new ExpressionNodeString(new Token(TokenType.NONE, Integer.toString(SignalHead_Green), 0));
    
    private final ExpressionNode exprSignalMastIF1 = new ExpressionNodeString(new Token(TokenType.NONE, "IF1", 0));
    private final ExpressionNode exprSignalMastMySignalMast = new ExpressionNodeString(new Token(TokenType.NONE, "My signal mast", 0));
    private final ExpressionNode exprSignalMastRed = new ExpressionNodeString(new Token(TokenType.NONE, "Red", 0));
    private final ExpressionNode exprSignalMastGreen = new ExpressionNodeString(new Token(TokenType.NONE, "Green", 0));
    
    
    private List<ExpressionNode> getParameterList(ExpressionNode... exprNodes) {
        List<ExpressionNode> list = new ArrayList<>();
        Collections.addAll(list, exprNodes);
        return list;
    }
    
    @Test
    public void testGetSetTurnoutStateFunction() throws Exception {
        LayoutFunctions.GetTurnoutStateFunction getTurnoutStateFunction = new LayoutFunctions.GetTurnoutStateFunction();
        Assert.assertEquals("strings matches", "getTurnoutState", getTurnoutStateFunction.getName());
        Assert.assertNotNull("Function has description", getTurnoutStateFunction.getDescription());
        
        LayoutFunctions.SetTurnoutStateFunction setTurnoutStateFunction = new LayoutFunctions.SetTurnoutStateFunction();
        Assert.assertEquals("strings matches", "setTurnoutState", setTurnoutStateFunction.getName());
        Assert.assertNotNull("Function has description", setTurnoutStateFunction.getDescription());
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        hasThrown.set(false);
        try {
            getTurnoutStateFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        Assertions.assertThrows(CalculateException.class, () -> {
            Assert.assertEquals("Turnout has correct state", Turnout.THROWN,
                    (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutIT1)));
        });
        
        Assertions.assertThrows(CalculateException.class, () -> {
            Assert.assertEquals("Turnout has correct state", Turnout.THROWN,
                    (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutMyTurnout)));
        });
        
        MyTurnout t = new MyTurnout();
        InstanceManager.getDefault(TurnoutManager.class).register(t);
        t.setState(Turnout.UNKNOWN);
        
        t._lastSetState = -1;
        t._knownState = Turnout.THROWN;
        Assert.assertEquals("Turnout has correct state", Turnout.THROWN,
                (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutIT1)));
        Assert.assertEquals("Turnout is not set", -1, t._lastSetState);
        
        t._knownState = Turnout.THROWN;
        Assert.assertEquals("Turnout has correct state", Turnout.THROWN,
                (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutMyTurnout)));
        Assert.assertEquals("Turnout is not set", -1, t._lastSetState);
        
        t._knownState = Turnout.CLOSED;
        Assert.assertEquals("Turnout has correct state", Turnout.CLOSED,
                (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutIT1)));
        Assert.assertEquals("Turnout is not set", -1, t._lastSetState);
        
        t._knownState = Turnout.CLOSED;
        Assert.assertEquals("Turnout has correct state", Turnout.CLOSED,
                (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutMyTurnout)));
        Assert.assertEquals("Turnout is not set", -1, t._lastSetState);
        
        t._lastSetState = -1;
        t._knownState = Turnout.THROWN;
        Assert.assertEquals("Turnout has correct state", Turnout.UNKNOWN,
                (int)setTurnoutStateFunction.calculate(symbolTable,
                        getParameterList(exprTurnoutIT1, exprTurnoutThrown)));
        Assert.assertEquals("Turnout is set", Turnout.THROWN, t._lastSetState);
        
        t._lastSetState = -1;
        t._knownState = Turnout.THROWN;
        Assert.assertEquals("Turnout has correct state", Turnout.UNKNOWN,
                (int)setTurnoutStateFunction.calculate(symbolTable,
                        getParameterList(exprTurnoutMyTurnout, exprTurnoutThrown)));
        Assert.assertEquals("Turnout is set", Turnout.THROWN, t._lastSetState);
        
        t._lastSetState = -1;
        t._knownState = Turnout.CLOSED;
        Assert.assertEquals("Turnout has correct state", Turnout.UNKNOWN,
                (int)setTurnoutStateFunction.calculate(symbolTable,
                        getParameterList(exprTurnoutIT1, exprTurnoutClosed)));
        Assert.assertEquals("Turnout is set", Turnout.CLOSED, t._lastSetState);
        
        t._lastSetState = -1;
        t._knownState = Turnout.CLOSED;
        Assert.assertEquals("Turnout has correct state", Turnout.UNKNOWN,
                (int)setTurnoutStateFunction.calculate(symbolTable,
                        getParameterList(exprTurnoutMyTurnout, exprTurnoutClosed)));
        Assert.assertEquals("Turnout is set", Turnout.CLOSED, t._lastSetState);
    }
    
    @Test
    public void testGetSetSensorStateFunction() throws Exception {
        LayoutFunctions.GetSensorStateFunction getSensorStateFunction = new LayoutFunctions.GetSensorStateFunction();
        Assert.assertEquals("strings matches", "getSensorState", getSensorStateFunction.getName());
        Assert.assertNotNull("Function has description", getSensorStateFunction.getDescription());
        
        LayoutFunctions.SetSensorStateFunction setSensorStateFunction = new LayoutFunctions.SetSensorStateFunction();
        Assert.assertEquals("strings matches", "setSensorState", setSensorStateFunction.getName());
        Assert.assertNotNull("Function has description", setSensorStateFunction.getDescription());
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        hasThrown.set(false);
        try {
            getSensorStateFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        Assertions.assertThrows(CalculateException.class, () -> {
            Assert.assertEquals("Sensor has correct state", Sensor.ACTIVE,
                    (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorIS1)));
        });
        
        Assertions.assertThrows(CalculateException.class, () -> {
            Assert.assertEquals("Sensor has correct state", Sensor.ACTIVE,
                    (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorMySensor)));
        });
        
        MySensor s = new MySensor();
        InstanceManager.getDefault(SensorManager.class).register(s);
        s.setState(Sensor.UNKNOWN);
        
        s._lastSetState = -1;
        s.setTestKnownState(Sensor.ACTIVE);
        Assert.assertEquals("Sensor has correct state", Sensor.ACTIVE,
                (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorIS1)));
        Assert.assertEquals("Sensor is not set", -1, s._lastSetState);
        
        s.setTestKnownState(Sensor.ACTIVE);
        Assert.assertEquals("Sensor has correct state", Sensor.ACTIVE,
                (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorMySensor)));
        Assert.assertEquals("Sensor is not set", -1, s._lastSetState);
        
        s.setTestKnownState(Sensor.INACTIVE);
        Assert.assertEquals("Sensor has correct state", Sensor.INACTIVE,
                (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorIS1)));
        Assert.assertEquals("Sensor is not set", -1, s._lastSetState);
        
        s.setTestKnownState(Sensor.INACTIVE);
        Assert.assertEquals("Sensor has correct state", Sensor.INACTIVE,
                (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorMySensor)));
        Assert.assertEquals("Sensor is not set", -1, s._lastSetState);
        
        s._lastSetState = -1;
        s.setTestKnownState(Sensor.ACTIVE);
        Assert.assertEquals("Sensor has correct state", Sensor.UNKNOWN,
                (int)setSensorStateFunction.calculate(symbolTable,
                        getParameterList(exprSensorIS1, exprSensorActive)));
        Assert.assertEquals("Sensor is set", Sensor.ACTIVE, s._lastSetState);
        
        s._lastSetState = -1;
        s.setTestKnownState(Sensor.ACTIVE);
        Assert.assertEquals("Sensor has correct state", Sensor.UNKNOWN,
                (int)setSensorStateFunction.calculate(symbolTable,
                        getParameterList(exprSensorMySensor, exprSensorActive)));
        Assert.assertEquals("Sensor is set", Sensor.ACTIVE, s._lastSetState);
        
        s._lastSetState = -1;
        s.setTestKnownState(Sensor.INACTIVE);
        Assert.assertEquals("Sensor has correct state", Sensor.UNKNOWN,
                (int)setSensorStateFunction.calculate(symbolTable,
                        getParameterList(exprSensorIS1, exprSensorInactive)));
        Assert.assertEquals("Sensor is set", Sensor.INACTIVE, s._lastSetState);
        
        s._lastSetState = -1;
        s.setTestKnownState(Sensor.INACTIVE);
        Assert.assertEquals("Sensor has correct state", Sensor.UNKNOWN,
                (int)setSensorStateFunction.calculate(symbolTable,
                        getParameterList(exprSensorMySensor, exprSensorInactive)));
        Assert.assertEquals("Sensor is set", Sensor.INACTIVE, s._lastSetState);
    }
    
    @Test
    public void testGetSetSignalHeadAppearanceFunction() throws Exception {
        LayoutFunctions.GetSignalHeadAppearanceFunction getSignalHeadStateFunction = new LayoutFunctions.GetSignalHeadAppearanceFunction();
        Assert.assertEquals("strings matches", "getSignalHeadAppearance", getSignalHeadStateFunction.getName());
        Assert.assertNotNull("Function has description", getSignalHeadStateFunction.getDescription());
        
        LayoutFunctions.SetSignalHeadAppearanceFunction setSignalHeadStateFunction = new LayoutFunctions.SetSignalHeadAppearanceFunction();
        Assert.assertEquals("strings matches", "setSignalHeadAppearance", setSignalHeadStateFunction.getName());
        Assert.assertNotNull("Function has description", setSignalHeadStateFunction.getDescription());
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        hasThrown.set(false);
        try {
            getSignalHeadStateFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        Assertions.assertThrows(CalculateException.class, () -> {
            Assert.assertEquals("SignalHead has correct appearance", SignalHead_Green,
                    (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)));
        });
        
        Assertions.assertThrows(CalculateException.class, () -> {
            Assert.assertEquals("SignalHead has correct appearance", SignalHead_Green,
                    (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)));
        });
        
        MySignalHead sh = new MySignalHead();
        InstanceManager.getDefault(SignalHeadManager.class).register(sh);
//        InstanceManager.getDefault(SignalHeadManager.class).newSignalHead("IT1", "My turnout");
        sh.setState(SignalHead.UNKNOWN);
        sh.setState(SignalHead_Red);
        
        sh._lastAppearance = -1;
        sh._appearance = SignalHead_Green;
        Assert.assertEquals("SignalHead has correct appearance", SignalHead_Green,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)));
        Assert.assertEquals("SignalHead is not set", -1, sh._lastAppearance);
        
        sh._appearance = SignalHead_Green;
        Assert.assertEquals("SignalHead has correct appearance", SignalHead_Green,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)));
        Assert.assertEquals("SignalHead is not set", -1, sh._lastAppearance);
        
        sh._appearance = SignalHead_Red;
        Assert.assertEquals("SignalHead has correct appearance", SignalHead_Red,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)));
        Assert.assertEquals("SignalHead is not set", -1, sh._lastAppearance);
        
        sh._appearance = SignalHead_Red;
        Assert.assertEquals("SignalHead has correct appearance", SignalHead_Red,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)));
        Assert.assertEquals("SignalHead is not set", -1, sh._lastAppearance);
        
        sh._lastAppearance = -1;
        sh._appearance = SignalHead_Green;
        Assert.assertEquals("SignalHead has correct appearance", -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadIH1, exprSignalHeadGreen)));
        Assert.assertEquals("SignalHead is set", SignalHead_Green, sh._lastAppearance);
        
        sh._lastAppearance = -1;
        sh._appearance = SignalHead_Green;
        Assert.assertEquals("SignalHead has correct appearance", -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadMySignalHead, exprSignalHeadGreen)));
        Assert.assertEquals("SignalHead is set", SignalHead_Green, sh._lastAppearance);
        
        sh._lastAppearance = -1;
        sh._appearance = SignalHead_Red;
        Assert.assertEquals("SignalHead has correct appearance", -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadIH1, exprSignalHeadRed)));
        Assert.assertEquals("SignalHead is set", SignalHead_Red, sh._lastAppearance);
        
        sh._lastAppearance = -1;
        sh._appearance = SignalHead_Red;
        Assert.assertEquals("SignalHead has correct appearance", -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadMySignalHead, exprSignalHeadRed)));
        Assert.assertEquals("SignalHead is set", SignalHead_Red, sh._lastAppearance);
    }
    
    @Test
    public void testGetSetSignalMastAspectFunction() throws Exception {
        LayoutFunctions.GetSignalMastAspectFunction getSignalMastAspectFunction = new LayoutFunctions.GetSignalMastAspectFunction();
        Assert.assertEquals("strings matches", "getSignalMastAspect", getSignalMastAspectFunction.getName());
        Assert.assertNotNull("Function has description", getSignalMastAspectFunction.getDescription());
        
        LayoutFunctions.SetSignalMastAspectFunction setSignalMastAspectFunction = new LayoutFunctions.SetSignalMastAspectFunction();
        Assert.assertEquals("strings matches", "setSignalMastAspect", setSignalMastAspectFunction.getName());
        Assert.assertNotNull("Function has description", setSignalMastAspectFunction.getDescription());
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        
        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        
        hasThrown.set(false);
        try {
            getSignalMastAspectFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());
        
        Assertions.assertThrows(CalculateException.class, () -> {
            Assert.assertEquals("SignalMast has correct aspect", "Green",
                    getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastIF1)));
        });
        
        Assertions.assertThrows(CalculateException.class, () -> {
            Assert.assertEquals("SignalMast has correct aspect", "Green",
                    getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastMySignalMast)));
        });
        
        MySignalMast sm = new MySignalMast();
        InstanceManager.getDefault(SignalMastManager.class).register(sm);
        sm.setState(SignalMast.UNKNOWN);
        
        sm._lastAspect = null;
        sm._aspect = "Green";
        Assert.assertEquals("SignalMast has correct aspect", "Green",
                getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastIF1)));
        Assert.assertNull("SignalMast is not set", sm._lastAspect);
        
        sm._aspect = "Green";
        Assert.assertEquals("SignalMast has correct aspect", "Green",
                getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastMySignalMast)));
        Assert.assertNull("SignalMast is not set", sm._lastAspect);
        
        sm._aspect = "Red";
        Assert.assertEquals("SignalMast has correct aspect", "Red",
                getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastIF1)));
        Assert.assertNull("SignalMast is not set", sm._lastAspect);
        
        sm._aspect = "Red";
        Assert.assertEquals("SignalMast has correct aspect", "Red",
                getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastMySignalMast)));
        Assert.assertNull("SignalMast is not set", sm._lastAspect);
        
        sm._lastAspect = null;
        sm._aspect = "Green";
        Assert.assertNull("SignalMast has correct aspect",
                setSignalMastAspectFunction.calculate(symbolTable,
                        getParameterList(exprSignalMastIF1, exprSignalMastGreen)));
        Assert.assertEquals("SignalMast is set", "Green", sm._lastAspect);
        
        sm._lastAspect = null;
        sm._aspect = "Green";
        Assert.assertNull("SignalMast has correct aspect",
                setSignalMastAspectFunction.calculate(symbolTable,
                        getParameterList(exprSignalMastMySignalMast, exprSignalMastGreen)));
        Assert.assertEquals("SignalMast is set", "Green", sm._lastAspect);
        
        sm._lastAspect = null;
        sm._aspect = "Red";
        Assert.assertNull("SignalMast has correct aspect",
                setSignalMastAspectFunction.calculate(symbolTable,
                        getParameterList(exprSignalMastIF1, exprSignalMastRed)));
        Assert.assertEquals("SignalMast is set", "Red", sm._lastAspect);
        
        sm._lastAspect = null;
        sm._aspect = "Red";
        Assert.assertNull("SignalMast has correct aspect",
                setSignalMastAspectFunction.calculate(symbolTable,
                        getParameterList(exprSignalMastMySignalMast, exprSignalMastRed)));
        Assert.assertEquals("SignalMast is set", "Red", sm._lastAspect);
    }
    
    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
    
    private static class MyTurnout extends jmri.implementation.AbstractTurnout {

        private int _knownState = Turnout.UNKNOWN;
        private int _lastSetState = Turnout.UNKNOWN;

        public MyTurnout() {
            super("IT1", "My turnout");
        }

        /** {@inheritDoc} */
        @Override
        public int getKnownState() {
            return _knownState;
        }

        @Override
        protected void forwardCommandChangeToLayout(int s) {
            _lastSetState = s;
            // We simulate a two sensor feedback, where state is unknown while moving the turnout
            _knownState = Turnout.UNKNOWN;
        }

        @Override
        protected void turnoutPushbuttonLockout(boolean locked) {
            throw new UnsupportedOperationException("Not supported");
        }

    }
    
    
    private static class MySensor extends jmri.implementation.AbstractSensor {

        private int _lastSetState = Sensor.UNKNOWN;

        public MySensor() {
            super("IS1", "My sensor");
        }

        /** {@inheritDoc} */
        @Override
        public int getKnownState() {
            return _knownState;
        }

        public void setTestKnownState(int newState) throws jmri.JmriException {
            _knownState = newState;
        }

        @Override
        public void setKnownState(int newState) throws jmri.JmriException {
            _lastSetState = newState;
            _knownState = Sensor.UNKNOWN;
        }

        @Override
        public void requestUpdateFromLayout() {
            throw new UnsupportedOperationException("Not supported");
        }

    }
    
    
    private static class MySignalHead extends jmri.implementation.VirtualSignalHead {

        private int _lastAppearance = -1;
        private int _appearance = -1;

        public MySignalHead() {
            super("IH1", "My signal head");
        }

        /** {@inheritDoc} */
        @Override
        public int getAppearance() {
            return _appearance;
        }

        @Override
        public void setAppearance(int appearance) {
            _lastAppearance = appearance;
            _appearance = -1;
        }

        @Override
        public void setLit(boolean newLit) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setHeld(boolean newHeld) {
            throw new UnsupportedOperationException("Not supported");
        }

    }
    
    
    private static class MySignalMast extends jmri.implementation.AbstractSignalMast {

        private String _lastAspect = null;
        private String _aspect = null;

        public MySignalMast() {
            super("IF1", "My signal mast");
        }

        /** {@inheritDoc} */
        @Override
        public String getAspect() {
            return _aspect;
        }

        @Override
        public void setAspect(String aspect) {
            _lastAspect = aspect;
            _aspect = null;
        }

    }
    
}
