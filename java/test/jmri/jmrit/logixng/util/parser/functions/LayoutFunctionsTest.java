package jmri.jmrit.logixng.util.parser.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logix.OBlockManager;
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

    private final ExpressionNode exprLightIL1 = new ExpressionNodeString(new Token(TokenType.NONE, "IL1", 0));
    private final ExpressionNode exprLightMyLight = new ExpressionNodeString(new Token(TokenType.NONE, "My light", 0));
    private final ExpressionNode exprLightOff = new ExpressionNodeString(new Token(TokenType.NONE, Integer.toString(Sensor.INACTIVE), 0));
    private final ExpressionNode exprLightOn = new ExpressionNodeString(new Token(TokenType.NONE, Integer.toString(Sensor.ACTIVE), 0));

    private final static int SIGNAL_HEAD_RED = 1;
    private final static int SIGNAL_HEAD_GREEN = 2;

    private final ExpressionNode exprSignalHeadIH1 = new ExpressionNodeString(new Token(TokenType.NONE, "IH1", 0));
    private final ExpressionNode exprSignalHeadMySignalHead = new ExpressionNodeString(new Token(TokenType.NONE, "My signal head", 0));
    private final ExpressionNode exprSignalHeadRed = new ExpressionNodeString(new Token(TokenType.NONE, Integer.toString(SIGNAL_HEAD_RED), 0));
    private final ExpressionNode exprSignalHeadGreen = new ExpressionNodeString(new Token(TokenType.NONE, Integer.toString(SIGNAL_HEAD_GREEN), 0));

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
    public void testTurnoutExistsFunction() throws Exception {
        Function turnoutExistsFunction = InstanceManager.getDefault(FunctionManager.class).get("turnoutExists");
        Assert.assertEquals("strings matches", "turnoutExists", turnoutExistsFunction.getName());
        Assert.assertNotNull("Function has description", turnoutExistsFunction.getDescription());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        hasThrown.set(false);
        try {
            turnoutExistsFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertFalse("Turnout doesn't exists",
                (boolean)turnoutExistsFunction.calculate(symbolTable, getParameterList(exprTurnoutIT1)));

        Assert.assertFalse("Turnout doesn't exists",
                (boolean)turnoutExistsFunction.calculate(symbolTable, getParameterList(exprTurnoutMyTurnout)));

        MyTurnout t = new MyTurnout();
        InstanceManager.getDefault(TurnoutManager.class).register(t);
        Assert.assertTrue("Turnout exists",
                (boolean)turnoutExistsFunction.calculate(symbolTable, getParameterList(exprTurnoutIT1)));
        Assert.assertTrue("Turnout exists",
                (boolean)turnoutExistsFunction.calculate(symbolTable, getParameterList(exprTurnoutMyTurnout)));
    }

    @Test
    public void testGetSetTurnoutStateFunction() throws Exception {
        Function getTurnoutStateFunction = InstanceManager.getDefault(FunctionManager.class).get("getTurnoutState");
        Assert.assertEquals("strings matches", "getTurnoutState", getTurnoutStateFunction.getName());
        Assert.assertNotNull("Function has description", getTurnoutStateFunction.getDescription());

        Function setTurnoutStateFunction = InstanceManager.getDefault(FunctionManager.class).get("setTurnoutState");
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
    public void testSensorExistsFunction() throws Exception {
        Function sensorExistsFunction = InstanceManager.getDefault(FunctionManager.class).get("sensorExists");
        Assert.assertEquals("strings matches", "sensorExists", sensorExistsFunction.getName());
        Assert.assertNotNull("Function has description", sensorExistsFunction.getDescription());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        hasThrown.set(false);
        try {
            sensorExistsFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertFalse("Sensor doesn't exists",
                (boolean)sensorExistsFunction.calculate(symbolTable, getParameterList(exprSensorIS1)));

        Assert.assertFalse("Sensor doesn't exists",
                (boolean)sensorExistsFunction.calculate(symbolTable, getParameterList(exprSensorMySensor)));

        MySensor t = new MySensor();
        InstanceManager.getDefault(SensorManager.class).register(t);
        Assert.assertTrue("Sensor exists",
                (boolean)sensorExistsFunction.calculate(symbolTable, getParameterList(exprSensorIS1)));
        Assert.assertTrue("Sensor exists",
                (boolean)sensorExistsFunction.calculate(symbolTable, getParameterList(exprSensorMySensor)));
    }

    @Test
    public void testGetSetSensorStateFunction() throws Exception {
        Function getSensorStateFunction = InstanceManager.getDefault(FunctionManager.class).get("getSensorState");
        Assert.assertEquals("strings matches", "getSensorState", getSensorStateFunction.getName());
        Assert.assertNotNull("Function has description", getSensorStateFunction.getDescription());

        Function setSensorStateFunction = InstanceManager.getDefault(FunctionManager.class).get("setSensorState");
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
    public void testLightExistsFunction() throws Exception {
        Function lightExistsFunction = InstanceManager.getDefault(FunctionManager.class).get("lightExists");
        Assert.assertEquals("strings matches", "lightExists", lightExistsFunction.getName());
        Assert.assertNotNull("Function has description", lightExistsFunction.getDescription());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        hasThrown.set(false);
        try {
            lightExistsFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertFalse("Light doesn't exists",
                (boolean)lightExistsFunction.calculate(symbolTable, getParameterList(exprLightIL1)));

        Assert.assertFalse("Light doesn't exists",
                (boolean)lightExistsFunction.calculate(symbolTable, getParameterList(exprLightMyLight)));

        MyLight t = new MyLight();
        InstanceManager.getDefault(LightManager.class).register(t);
        Assert.assertTrue("Light exists",
                (boolean)lightExistsFunction.calculate(symbolTable, getParameterList(exprLightIL1)));
        Assert.assertTrue("Light exists",
                (boolean)lightExistsFunction.calculate(symbolTable, getParameterList(exprLightMyLight)));
    }

    @Test
    public void testGetSetLightStateFunction() throws Exception {
        Function getLightStateFunction = InstanceManager.getDefault(FunctionManager.class).get("getLightState");
        Assert.assertEquals("strings matches", "getLightState", getLightStateFunction.getName());
        Assert.assertNotNull("Function has description", getLightStateFunction.getDescription());

        Function setLightStateFunction = InstanceManager.getDefault(FunctionManager.class).get("setLightState");
        Assert.assertEquals("strings matches", "setLightState", setLightStateFunction.getName());
        Assert.assertNotNull("Function has description", setLightStateFunction.getDescription());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        hasThrown.set(false);
        try {
            getLightStateFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assertions.assertThrows(CalculateException.class, () -> {
            Assert.assertEquals("Light has correct state", Light.ON,
                    (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightIL1)));
        });

        Assertions.assertThrows(CalculateException.class, () -> {
            Assert.assertEquals("Light has correct state", Light.ON,
                    (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightMyLight)));
        });

        MyLight s = new MyLight();
        InstanceManager.getDefault(LightManager.class).register(s);
        s.setState(Light.UNKNOWN);

        s._lastSetState = -1;
        s.setTestKnownState(Light.ON);
        Assert.assertEquals("Light has correct state", Light.ON,
                (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightIL1)));
        Assert.assertEquals("Light is not set", -1, s._lastSetState);

        s.setTestKnownState(Light.ON);
        Assert.assertEquals("Light has correct state", Light.ON,
                (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightMyLight)));
        Assert.assertEquals("Light is not set", -1, s._lastSetState);

        s.setTestKnownState(Light.OFF);
        Assert.assertEquals("Light has correct state", Light.OFF,
                (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightIL1)));
        Assert.assertEquals("Light is not set", -1, s._lastSetState);

        s.setTestKnownState(Light.OFF);
        Assert.assertEquals("Light has correct state", Light.OFF,
                (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightMyLight)));
        Assert.assertEquals("Light is not set", -1, s._lastSetState);

        s._lastSetState = -1;
        s.setTestKnownState(Light.ON);
        Assert.assertEquals("Light has correct state", Light.UNKNOWN,
                (int)setLightStateFunction.calculate(symbolTable,
                        getParameterList(exprLightIL1, exprLightOn)));
        Assert.assertEquals("Light is set", Light.ON, s._lastSetState);

        s._lastSetState = -1;
        s.setTestKnownState(Light.ON);
        Assert.assertEquals("Light has correct state", Light.UNKNOWN,
                (int)setLightStateFunction.calculate(symbolTable,
                        getParameterList(exprLightMyLight, exprLightOn)));
        Assert.assertEquals("Light is set", Light.ON, s._lastSetState);

        s._lastSetState = -1;
        s.setTestKnownState(Light.OFF);
        Assert.assertEquals("Light has correct state", Light.UNKNOWN,
                (int)setLightStateFunction.calculate(symbolTable,
                        getParameterList(exprLightIL1, exprLightOff)));
        Assert.assertEquals("Light is set", Light.OFF, s._lastSetState);

        s._lastSetState = -1;
        s.setTestKnownState(Light.OFF);
        Assert.assertEquals("Light has correct state", Light.UNKNOWN,
                (int)setLightStateFunction.calculate(symbolTable,
                        getParameterList(exprLightMyLight, exprLightOff)));
        Assert.assertEquals("Light is set", Light.OFF, s._lastSetState);
    }

    @Test
    public void testSignalHeadExistsFunction() throws Exception {
        Function signalHeadExistsFunction = InstanceManager.getDefault(FunctionManager.class).get("signalHeadExists");
        Assert.assertEquals("strings matches", "signalHeadExists", signalHeadExistsFunction.getName());
        Assert.assertNotNull("Function has description", signalHeadExistsFunction.getDescription());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        hasThrown.set(false);
        try {
            signalHeadExistsFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertFalse("SignalHead doesn't exists",
                (boolean)signalHeadExistsFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)));

        Assert.assertFalse("SignalHead doesn't exists",
                (boolean)signalHeadExistsFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)));

        MySignalHead t = new MySignalHead();
        InstanceManager.getDefault(SignalHeadManager.class).register(t);
        Assert.assertTrue("SignalHead exists",
                (boolean)signalHeadExistsFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)));
        Assert.assertTrue("SignalHead exists",
                (boolean)signalHeadExistsFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)));
    }

    @Test
    public void testGetSetSignalHeadAppearanceFunction() throws Exception {
        Function getSignalHeadStateFunction = InstanceManager.getDefault(FunctionManager.class).get("getSignalHeadAppearance");
        Assert.assertEquals("strings matches", "getSignalHeadAppearance", getSignalHeadStateFunction.getName());
        Assert.assertNotNull("Function has description", getSignalHeadStateFunction.getDescription());

        Function setSignalHeadStateFunction = InstanceManager.getDefault(FunctionManager.class).get("setSignalHeadAppearance");
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
            Assert.assertEquals("SignalHead has correct appearance", SIGNAL_HEAD_GREEN,
                    (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)));
        });

        Assertions.assertThrows(CalculateException.class, () -> {
            Assert.assertEquals("SignalHead has correct appearance", SIGNAL_HEAD_GREEN,
                    (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)));
        });

        MySignalHead sh = new MySignalHead();
        InstanceManager.getDefault(SignalHeadManager.class).register(sh);
        sh.setState(SignalHead.UNKNOWN);
        sh.setState(SIGNAL_HEAD_RED);

        sh._lastAppearance = -1;
        sh._appearance = SIGNAL_HEAD_GREEN;
        Assert.assertEquals("SignalHead has correct appearance", SIGNAL_HEAD_GREEN,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)));
        Assert.assertEquals("SignalHead is not set", -1, sh._lastAppearance);

        sh._appearance = SIGNAL_HEAD_GREEN;
        Assert.assertEquals("SignalHead has correct appearance", SIGNAL_HEAD_GREEN,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)));
        Assert.assertEquals("SignalHead is not set", -1, sh._lastAppearance);

        sh._appearance = SIGNAL_HEAD_RED;
        Assert.assertEquals("SignalHead has correct appearance", SIGNAL_HEAD_RED,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)));
        Assert.assertEquals("SignalHead is not set", -1, sh._lastAppearance);

        sh._appearance = SIGNAL_HEAD_RED;
        Assert.assertEquals("SignalHead has correct appearance", SIGNAL_HEAD_RED,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)));
        Assert.assertEquals("SignalHead is not set", -1, sh._lastAppearance);

        sh._lastAppearance = -1;
        sh._appearance = SIGNAL_HEAD_GREEN;
        Assert.assertEquals("SignalHead has correct appearance", -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadIH1, exprSignalHeadGreen)));
        Assert.assertEquals("SignalHead is set", SIGNAL_HEAD_GREEN, sh._lastAppearance);

        sh._lastAppearance = -1;
        sh._appearance = SIGNAL_HEAD_GREEN;
        Assert.assertEquals("SignalHead has correct appearance", -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadMySignalHead, exprSignalHeadGreen)));
        Assert.assertEquals("SignalHead is set", SIGNAL_HEAD_GREEN, sh._lastAppearance);

        sh._lastAppearance = -1;
        sh._appearance = SIGNAL_HEAD_RED;
        Assert.assertEquals("SignalHead has correct appearance", -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadIH1, exprSignalHeadRed)));
        Assert.assertEquals("SignalHead is set", SIGNAL_HEAD_RED, sh._lastAppearance);

        sh._lastAppearance = -1;
        sh._appearance = SIGNAL_HEAD_RED;
        Assert.assertEquals("SignalHead has correct appearance", -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadMySignalHead, exprSignalHeadRed)));
        Assert.assertEquals("SignalHead is set", SIGNAL_HEAD_RED, sh._lastAppearance);
    }

    @Test
    public void testSignalMastExistsFunction() throws Exception {
        Function signalMastExistsFunction = InstanceManager.getDefault(FunctionManager.class).get("signalMastExists");
        Assert.assertEquals("strings matches", "signalMastExists", signalMastExistsFunction.getName());
        Assert.assertNotNull("Function has description", signalMastExistsFunction.getDescription());

        AtomicBoolean hasThrown = new AtomicBoolean(false);

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        hasThrown.set(false);
        try {
            signalMastExistsFunction.calculate(symbolTable, getParameterList());
        } catch (WrongNumberOfParametersException e) {
            hasThrown.set(true);
        }
        Assert.assertTrue("exception is thrown", hasThrown.get());

        Assert.assertFalse("SignalMast doesn't exists",
                (boolean)signalMastExistsFunction.calculate(symbolTable, getParameterList(exprSignalMastIF1)));

        Assert.assertFalse("SignalMast doesn't exists",
                (boolean)signalMastExistsFunction.calculate(symbolTable, getParameterList(exprSignalMastMySignalMast)));

        MySignalMast t = new MySignalMast();
        InstanceManager.getDefault(SignalMastManager.class).register(t);
        Assert.assertTrue("SignalMast exists",
                (boolean)signalMastExistsFunction.calculate(symbolTable, getParameterList(exprSignalMastIF1)));
        Assert.assertTrue("SignalMast exists",
                (boolean)signalMastExistsFunction.calculate(symbolTable, getParameterList(exprSignalMastMySignalMast)));
    }

    @Test
    public void testGetSetSignalMastAspectFunction() throws Exception {
        Function getSignalMastAspectFunction = InstanceManager.getDefault(FunctionManager.class).get("getSignalMastAspect");
        Assert.assertEquals("strings matches", "getSignalMastAspect", getSignalMastAspectFunction.getName());
        Assert.assertNotNull("Function has description", getSignalMastAspectFunction.getDescription());

        Function setSignalMastAspectFunction = InstanceManager.getDefault(FunctionManager.class).get("setSignalMastAspect");
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

    @Test
    public void testManagers() {
        FunctionManager functionManager = InstanceManager.getDefault(FunctionManager.class);

        Assert.assertEquals(functionManager.getConstant("sensors").getValue(), InstanceManager.getDefault(SensorManager.class));
        Assert.assertEquals(functionManager.getConstant("turnouts").getValue(), InstanceManager.getDefault(TurnoutManager.class));
        Assert.assertEquals(functionManager.getConstant("lights").getValue(), InstanceManager.getDefault(LightManager.class));
        Assert.assertEquals(functionManager.getConstant("signals").getValue(), InstanceManager.getDefault(SignalHeadManager.class));
        Assert.assertEquals(functionManager.getConstant("masts").getValue(), InstanceManager.getDefault(SignalMastManager.class));
        Assert.assertEquals(functionManager.getConstant("routes").getValue(), InstanceManager.getDefault(RouteManager.class));
        Assert.assertEquals(functionManager.getConstant("blocks").getValue(), InstanceManager.getDefault(BlockManager.class));
        Assert.assertEquals(functionManager.getConstant("oblocks").getValue(), InstanceManager.getDefault(OBlockManager.class));
        Assert.assertEquals(functionManager.getConstant("reporters").getValue(), InstanceManager.getDefault(ReporterManager.class));
        Assert.assertEquals(functionManager.getConstant("memories").getValue(), InstanceManager.getDefault(MemoryManager.class));
        Assert.assertEquals(functionManager.getConstant("powermanager").getValue(), InstanceManager.getDefault(PowerManager.class));
        Assert.assertEquals(functionManager.getConstant("addressedProgrammers").getValue(), InstanceManager.getDefault(AddressedProgrammerManager.class));
        Assert.assertEquals(functionManager.getConstant("globalProgrammers").getValue(), InstanceManager.getDefault(GlobalProgrammerManager.class));
        Assert.assertEquals(functionManager.getConstant("dcc").getValue(), InstanceManager.getDefault(CommandStation.class));
        Assert.assertEquals(functionManager.getConstant("audio").getValue(), InstanceManager.getDefault(AudioManager.class));
        Assert.assertEquals(functionManager.getConstant("shutdown").getValue(), InstanceManager.getDefault(ShutDownManager.class));
        Assert.assertEquals(functionManager.getConstant("layoutblocks").getValue(), InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class));
        Assert.assertEquals(functionManager.getConstant("warrants").getValue(), InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class));
        Assert.assertEquals(functionManager.getConstant("sections").getValue(), InstanceManager.getDefault(SectionManager.class));
        Assert.assertEquals(functionManager.getConstant("transits").getValue(), InstanceManager.getDefault(TransitManager.class));

    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugPowerManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initDebugProgrammerManager();
        JUnitUtil.initDebugCommandStation();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDefaultSignalMastManager();
//        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initSectionManager();
        JUnitUtil.initWarrantManager();
    }

    @AfterEach
    public void tearDown() {
        LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }


    private static class MyTurnout extends jmri.implementation.AbstractTurnout {

        int _knownState = Turnout.UNKNOWN;
        int _lastSetState = Turnout.UNKNOWN;

        MyTurnout() {
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

        int _lastSetState = Sensor.UNKNOWN;

        MySensor() {
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


    private static class MyLight extends jmri.implementation.AbstractLight {

        int _knownState = Turnout.UNKNOWN;
        int _lastSetState = Sensor.UNKNOWN;

        MyLight() {
            super("IL1", "My light");
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
        public void setState(int newState) {
            _lastSetState = newState;
            _knownState = Sensor.UNKNOWN;
        }

        @Override
        public void requestUpdateFromLayout() {
            throw new UnsupportedOperationException("Not supported");
        }

    }


    private static class MySignalHead extends jmri.implementation.VirtualSignalHead {

        int _lastAppearance = -1;
        int _appearance = -1;

        MySignalHead() {
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

        String _lastAspect = null;
        String _aspect = null;

        MySignalMast() {
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
