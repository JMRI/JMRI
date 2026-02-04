package jmri.jmrit.logixng.util.parser.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.*;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
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
    public void testTurnoutExistsFunction() throws JmriException {
        Function turnoutExistsFunction = InstanceManager.getDefault(FunctionManager.class).get("turnoutExists");
        assertEquals( "turnoutExists", turnoutExistsFunction.getName(), "strings matches");
        assertNotNull( turnoutExistsFunction.getDescription(), "Function has description");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class,
            () -> turnoutExistsFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(e);

        assertFalse( (boolean)turnoutExistsFunction.calculate(symbolTable, getParameterList(exprTurnoutIT1)),
            "Turnout doesn't exist");

        assertFalse( (boolean)turnoutExistsFunction.calculate(symbolTable, getParameterList(exprTurnoutMyTurnout)),
            "Turnout doesn't exist 2");

        MyTurnout t = new MyTurnout();
        InstanceManager.getDefault(TurnoutManager.class).register(t);
        assertTrue( (boolean)turnoutExistsFunction.calculate(symbolTable, getParameterList(exprTurnoutIT1)),
            "Turnout exists");
        assertTrue( (boolean)turnoutExistsFunction.calculate(symbolTable, getParameterList(exprTurnoutMyTurnout)),
            "Turnout exists 2");
    }

    @Test
    public void testGetSetTurnoutStateFunction() throws JmriException {
        Function getTurnoutStateFunction = InstanceManager.getDefault(FunctionManager.class).get("getTurnoutState");
        assertEquals( "getTurnoutState", getTurnoutStateFunction.getName(), "strings matches");
        assertNotNull( getTurnoutStateFunction.getDescription(), "Function has description");

        Function setTurnoutStateFunction = InstanceManager.getDefault(FunctionManager.class).get("setTurnoutState");
        assertEquals( "setTurnoutState", setTurnoutStateFunction.getName(), "strings matches");
        assertNotNull( setTurnoutStateFunction.getDescription(), "Function has description");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows(WrongNumberOfParametersException.class,
            () -> getTurnoutStateFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(e);

        CalculateException ex = assertThrows(CalculateException.class, () -> {
            int tmp = (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutIT1));
            assertEquals( Turnout.THROWN, tmp, "Turnout has correct state, Should have thrown" );
        });
        assertNotNull(ex);

        ex = assertThrows(CalculateException.class, () -> {
            int tmp = (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutMyTurnout));
            assertEquals( Turnout.THROWN, tmp, "Turnout has correct state, Should have thrown" );
        });
        assertNotNull(ex);

        MyTurnout t = new MyTurnout();
        InstanceManager.getDefault(TurnoutManager.class).register(t);
        t.setState(Turnout.UNKNOWN);

        t._lastSetState = -1;
        t._knownState = Turnout.THROWN;
        assertEquals( Turnout.THROWN,
                (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutIT1)),
                "Turnout has correct state");
        assertEquals( -1, t._lastSetState, "Turnout is not set");

        t._knownState = Turnout.THROWN;
        assertEquals( Turnout.THROWN,
                (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutMyTurnout)),
                "Turnout has correct state");
        assertEquals( -1, t._lastSetState, "Turnout is not set");

        t._knownState = Turnout.CLOSED;
        assertEquals( Turnout.CLOSED,
                (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutIT1)),
                "Turnout has correct state");
        assertEquals( -1, t._lastSetState, "Turnout is not set");

        t._knownState = Turnout.CLOSED;
        assertEquals( Turnout.CLOSED,
                (int)getTurnoutStateFunction.calculate(symbolTable, getParameterList(exprTurnoutMyTurnout)),
                "Turnout has correct state");
        assertEquals( -1, t._lastSetState, "Turnout is not set");

        t._lastSetState = -1;
        t._knownState = Turnout.THROWN;
        assertEquals( Turnout.UNKNOWN,
                (int)setTurnoutStateFunction.calculate(symbolTable,
                        getParameterList(exprTurnoutIT1, exprTurnoutThrown)),
                "Turnout has correct state");
        assertEquals( Turnout.THROWN, t._lastSetState, "Turnout is set");

        t._lastSetState = -1;
        t._knownState = Turnout.THROWN;
        assertEquals( Turnout.UNKNOWN,
                (int)setTurnoutStateFunction.calculate(symbolTable,
                        getParameterList(exprTurnoutMyTurnout, exprTurnoutThrown)),
                "Turnout has correct state");
        assertEquals( Turnout.THROWN, t._lastSetState, "Turnout is set");

        t._lastSetState = -1;
        t._knownState = Turnout.CLOSED;
        assertEquals( Turnout.UNKNOWN,
                (int)setTurnoutStateFunction.calculate(symbolTable,
                        getParameterList(exprTurnoutIT1, exprTurnoutClosed)),
                "Turnout has correct state");
        assertEquals( Turnout.CLOSED, t._lastSetState, "Turnout is set");

        t._lastSetState = -1;
        t._knownState = Turnout.CLOSED;
        assertEquals( Turnout.UNKNOWN,
                (int)setTurnoutStateFunction.calculate(symbolTable,
                        getParameterList(exprTurnoutMyTurnout, exprTurnoutClosed)),
                "Turnout has correct state");
        assertEquals( Turnout.CLOSED, t._lastSetState, "Turnout is set");
    }

    @Test
    public void testSensorExistsFunction() throws JmriException {
        Function sensorExistsFunction = InstanceManager.getDefault(FunctionManager.class).get("sensorExists");
        assertEquals( "sensorExists", sensorExistsFunction.getName(), "strings matches");
        assertNotNull( sensorExistsFunction.getDescription(), "Function has description");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class,
            () -> sensorExistsFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull( e);

        assertFalse( (boolean)sensorExistsFunction.calculate(symbolTable, getParameterList(exprSensorIS1)),
            "Sensor doesn't exist");

        assertFalse( (boolean)sensorExistsFunction.calculate(symbolTable, getParameterList(exprSensorMySensor)),
            "Sensor doesn't exist");

        MySensor t = new MySensor();
        InstanceManager.getDefault(SensorManager.class).register(t);
        assertTrue( (boolean)sensorExistsFunction.calculate(symbolTable, getParameterList(exprSensorIS1)),
            "Sensor exists");
        assertTrue( (boolean)sensorExistsFunction.calculate(symbolTable, getParameterList(exprSensorMySensor)),
            "Sensor exists");
    }

    @Test
    public void testGetSetSensorStateFunction() throws JmriException {
        Function getSensorStateFunction = InstanceManager.getDefault(FunctionManager.class).get("getSensorState");
        assertEquals( "getSensorState", getSensorStateFunction.getName(), "strings matches");
        assertNotNull( getSensorStateFunction.getDescription(), "Function has description");

        Function setSensorStateFunction = InstanceManager.getDefault(FunctionManager.class).get("setSensorState");
        assertEquals( "setSensorState", setSensorStateFunction.getName(), "strings matches");
        assertNotNull( setSensorStateFunction.getDescription(), "Function has description");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));
        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class,
            () -> getSensorStateFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(e);

        CalculateException ex = assertThrows(CalculateException.class, () -> {
            int noInt = (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorIS1));
            assertEquals( Sensor.ACTIVE, noInt, "Should have thrown, Sensor has correct state");
        });
        assertNotNull(ex);

        ex = assertThrows(CalculateException.class, () -> {
            int noInt = (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorMySensor));
            assertEquals( Sensor.ACTIVE, noInt, "Should have thrown, Sensor has correct state");
        });
        assertNotNull(ex);

        MySensor s = new MySensor();
        InstanceManager.getDefault(SensorManager.class).register(s);
        s.setState(Sensor.UNKNOWN);

        s._lastSetState = -1;
        s.setTestKnownState(Sensor.ACTIVE);
        assertEquals( Sensor.ACTIVE,
                (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorIS1)),
                "Sensor has correct state");
        assertEquals( -1, s._lastSetState, "Sensor is not set");

        s.setTestKnownState(Sensor.ACTIVE);
        assertEquals( Sensor.ACTIVE,
                (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorMySensor)),
                "Sensor has correct state");
        assertEquals( -1, s._lastSetState, "Sensor is not set");

        s.setTestKnownState(Sensor.INACTIVE);
        assertEquals( Sensor.INACTIVE,
                (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorIS1)),
                "Sensor has correct state");
        assertEquals( -1, s._lastSetState, "Sensor is not set");

        s.setTestKnownState(Sensor.INACTIVE);
        assertEquals( Sensor.INACTIVE,
                (int)getSensorStateFunction.calculate(symbolTable, getParameterList(exprSensorMySensor)),
                "Sensor has correct state");
        assertEquals( -1, s._lastSetState, "Sensor is not set");

        s._lastSetState = -1;
        s.setTestKnownState(Sensor.ACTIVE);
        assertEquals( Sensor.UNKNOWN,
                (int)setSensorStateFunction.calculate(symbolTable,
                        getParameterList(exprSensorIS1, exprSensorActive)),
                "Sensor has correct state");
        assertEquals( Sensor.ACTIVE, s._lastSetState, "Sensor is set");

        s._lastSetState = -1;
        s.setTestKnownState(Sensor.ACTIVE);
        assertEquals( Sensor.UNKNOWN,
                (int)setSensorStateFunction.calculate(symbolTable,
                        getParameterList(exprSensorMySensor, exprSensorActive)),
                "Sensor has correct state");
        assertEquals( Sensor.ACTIVE, s._lastSetState, "Sensor is set");

        s._lastSetState = -1;
        s.setTestKnownState(Sensor.INACTIVE);
        assertEquals( Sensor.UNKNOWN,
                (int)setSensorStateFunction.calculate(symbolTable,
                        getParameterList(exprSensorIS1, exprSensorInactive)),
                "Sensor has correct state");
        assertEquals( Sensor.INACTIVE, s._lastSetState, "Sensor is set");

        s._lastSetState = -1;
        s.setTestKnownState(Sensor.INACTIVE);
        assertEquals( Sensor.UNKNOWN,
                (int)setSensorStateFunction.calculate(symbolTable,
                        getParameterList(exprSensorMySensor, exprSensorInactive)),
                "Sensor has correct state");
        assertEquals( Sensor.INACTIVE, s._lastSetState, "Sensor is set");
    }

    @Test
    public void testLightExistsFunction() throws JmriException {
        Function lightExistsFunction = InstanceManager.getDefault(FunctionManager.class).get("lightExists");
        assertEquals( "lightExists", lightExistsFunction.getName(), "string matches");
        assertNotNull("Function has description", lightExistsFunction.getDescription());

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class,
            () -> lightExistsFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(e);

        assertFalse( (boolean)lightExistsFunction.calculate(symbolTable, getParameterList(exprLightIL1)),
            "Light doesn't exist");

        assertFalse( (boolean)lightExistsFunction.calculate(symbolTable, getParameterList(exprLightMyLight)),
            "Light doesn't exist 2");

        MyLight t = new MyLight();
        InstanceManager.getDefault(LightManager.class).register(t);
        assertTrue( (boolean)lightExistsFunction.calculate(symbolTable, getParameterList(exprLightIL1)),
                "Light exists");
        assertTrue( (boolean)lightExistsFunction.calculate(symbolTable, getParameterList(exprLightMyLight)),
                "Light exists 2");
    }

    @Test
    public void testGetSetLightStateFunction() throws JmriException {
        Function getLightStateFunction = InstanceManager.getDefault(FunctionManager.class).get("getLightState");
        assertEquals( "getLightState", getLightStateFunction.getName(), "strings match");
        assertNotNull( getLightStateFunction.getDescription(), "Function has description");

        Function setLightStateFunction = InstanceManager.getDefault(FunctionManager.class).get("setLightState");
        assertEquals( "setLightState", setLightStateFunction.getName(), "strings match 2");
        assertNotNull( setLightStateFunction.getDescription(), "Function has description 2");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class,
            () -> getLightStateFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(e);

        CalculateException ex = assertThrows(CalculateException.class, () -> {
            int notInt = (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightIL1));
            assertEquals( Light.ON, notInt, "Light has correct state");
        });
        assertNotNull(ex);

        ex = assertThrows(CalculateException.class, () -> {
            int notInt = (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightMyLight));
            assertEquals( Light.ON, notInt, "Light has correct state 2");
        });
        assertNotNull(ex);

        MyLight s = new MyLight();
        InstanceManager.getDefault(LightManager.class).register(s);
        s.setState(Light.UNKNOWN);

        s._lastSetState = -1;
        s.setTestKnownState(Light.ON);
        assertEquals( Light.ON,
                (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightIL1)),
                "Light has correct state");
        assertEquals( -1, s._lastSetState, "Light is not set");

        s.setTestKnownState(Light.ON);
        assertEquals( Light.ON,
                (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightMyLight)),
                "Light has correct state");
        assertEquals( -1, s._lastSetState, "Light is not set");

        s.setTestKnownState(Light.OFF);
        assertEquals( Light.OFF,
                (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightIL1)),
                "Light has correct state");
        assertEquals( -1, s._lastSetState, "Light is not set");

        s.setTestKnownState(Light.OFF);
        assertEquals( Light.OFF,
                (int)getLightStateFunction.calculate(symbolTable, getParameterList(exprLightMyLight)),
                "Light has correct state");
        assertEquals( -1, s._lastSetState, "Light is not set");

        s._lastSetState = -1;
        s.setTestKnownState(Light.ON);
        assertEquals( Light.UNKNOWN,
                (int)setLightStateFunction.calculate(symbolTable,
                        getParameterList(exprLightIL1, exprLightOn)),
                "Light has correct state");
        assertEquals( Light.ON, s._lastSetState, "Light is set");

        s._lastSetState = -1;
        s.setTestKnownState(Light.ON);
        assertEquals( Light.UNKNOWN,
                (int)setLightStateFunction.calculate(symbolTable,
                        getParameterList(exprLightMyLight, exprLightOn)),
                "Light has correct state");
        assertEquals( Light.ON, s._lastSetState, "Light is set");

        s._lastSetState = -1;
        s.setTestKnownState(Light.OFF);
        assertEquals( Light.UNKNOWN,
                (int)setLightStateFunction.calculate(symbolTable,
                        getParameterList(exprLightIL1, exprLightOff)),
                "Light has correct state");
        assertEquals( Light.OFF, s._lastSetState, "Light is set");

        s._lastSetState = -1;
        s.setTestKnownState(Light.OFF);
        assertEquals( Light.UNKNOWN,
                (int)setLightStateFunction.calculate(symbolTable,
                        getParameterList(exprLightMyLight, exprLightOff)),
                "Light has correct state");
        assertEquals( Light.OFF, s._lastSetState, "Light is set");
    }

    @Test
    public void testSignalHeadExistsFunction() throws JmriException {
        Function signalHeadExistsFunction = InstanceManager.getDefault(FunctionManager.class).get("signalHeadExists");
        assertEquals( "signalHeadExists", signalHeadExistsFunction.getName(), "strings matches");
        assertNotNull( signalHeadExistsFunction.getDescription(), "Function has description");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class,
            () -> signalHeadExistsFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(e);

        assertFalse( (boolean)signalHeadExistsFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)),
            "SignalHead doesn't exists");

        assertFalse( (boolean)signalHeadExistsFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)),
            "SignalHead doesn't exists");

        MySignalHead t = new MySignalHead();
        InstanceManager.getDefault(SignalHeadManager.class).register(t);
        assertTrue( (boolean)signalHeadExistsFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)),
            "SignalHead exists");
        assertTrue( (boolean)signalHeadExistsFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)),
            "SignalHead exists");
    }

    @Test
    public void testGetSetSignalHeadAppearanceFunction() throws JmriException {
        Function getSignalHeadStateFunction = InstanceManager.getDefault(FunctionManager.class).get("getSignalHeadAppearance");
        assertEquals( "getSignalHeadAppearance", getSignalHeadStateFunction.getName(), "strings matches");
        assertNotNull( getSignalHeadStateFunction.getDescription(), "Function has description");

        Function setSignalHeadStateFunction = InstanceManager.getDefault(FunctionManager.class).get("setSignalHeadAppearance");
        assertEquals( "setSignalHeadAppearance", setSignalHeadStateFunction.getName(), "strings matches");
        assertNotNull( setSignalHeadStateFunction.getDescription(), "Function has description");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class,
            () -> getSignalHeadStateFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(e);

        CalculateException ex = assertThrows(CalculateException.class, () -> {
            int notInt = (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1));
            assertEquals( SIGNAL_HEAD_GREEN, notInt, "Throws, not SignalHead has correct appearance");
        });
        assertNotNull(ex);

        ex = assertThrows(CalculateException.class, () -> {
            int notInt = (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead));
            assertEquals( SIGNAL_HEAD_GREEN, notInt, "SignalHead has correct appearance");
        });
        assertNotNull(ex);

        MySignalHead sh = new MySignalHead();
        InstanceManager.getDefault(SignalHeadManager.class).register(sh);
        sh.setState(SignalHead.UNKNOWN);
        sh.setState(SIGNAL_HEAD_RED);

        sh._lastAppearance = -1;
        sh._appearance = SIGNAL_HEAD_GREEN;
        assertEquals( SIGNAL_HEAD_GREEN,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)),
                "SignalHead has correct appearance");
        assertEquals( -1, sh._lastAppearance, "SignalHead is not set");

        sh._appearance = SIGNAL_HEAD_GREEN;
        assertEquals( SIGNAL_HEAD_GREEN,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)),
                "SignalHead has correct appearance");
        assertEquals( -1, sh._lastAppearance, "SignalHead is not set");

        sh._appearance = SIGNAL_HEAD_RED;
        assertEquals( SIGNAL_HEAD_RED,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadIH1)),
                "SignalHead has correct appearance");
        assertEquals( -1, sh._lastAppearance, "SignalHead is not set");

        sh._appearance = SIGNAL_HEAD_RED;
        assertEquals( SIGNAL_HEAD_RED,
                (int)getSignalHeadStateFunction.calculate(symbolTable, getParameterList(exprSignalHeadMySignalHead)),
                "SignalHead has correct appearance");
        assertEquals( -1, sh._lastAppearance, "SignalHead is not set");

        sh._lastAppearance = -1;
        sh._appearance = SIGNAL_HEAD_GREEN;
        assertEquals( -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadIH1, exprSignalHeadGreen)),
                "SignalHead has correct appearance");
        assertEquals( SIGNAL_HEAD_GREEN, sh._lastAppearance, "SignalHead is set");

        sh._lastAppearance = -1;
        sh._appearance = SIGNAL_HEAD_GREEN;
        assertEquals( -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadMySignalHead, exprSignalHeadGreen)),
                "SignalHead has correct appearance");
        assertEquals( SIGNAL_HEAD_GREEN, sh._lastAppearance, "SignalHead is set");

        sh._lastAppearance = -1;
        sh._appearance = SIGNAL_HEAD_RED;
        assertEquals( -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadIH1, exprSignalHeadRed)),
                "SignalHead has correct appearance");
        assertEquals( SIGNAL_HEAD_RED, sh._lastAppearance, "SignalHead is set");

        sh._lastAppearance = -1;
        sh._appearance = SIGNAL_HEAD_RED;
        assertEquals( -1,
                (int)setSignalHeadStateFunction.calculate(symbolTable,
                        getParameterList(exprSignalHeadMySignalHead, exprSignalHeadRed)),
                "SignalHead has correct appearance");
        assertEquals( SIGNAL_HEAD_RED, sh._lastAppearance, "SignalHead is set");
    }

    @Test
    public void testSignalMastExistsFunction() throws JmriException {
        Function signalMastExistsFunction = InstanceManager.getDefault(FunctionManager.class).get("signalMastExists");
        assertEquals( "signalMastExists", signalMastExistsFunction.getName(), "strings matches");
        assertNotNull( signalMastExistsFunction.getDescription(), "Function has description");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class,
            () -> signalMastExistsFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(e);

        assertFalse( (boolean)signalMastExistsFunction.calculate(symbolTable, getParameterList(exprSignalMastIF1)),
            "SignalMast doesn't exist");

        assertFalse( (boolean)signalMastExistsFunction.calculate(symbolTable, getParameterList(exprSignalMastMySignalMast)),
            "SignalMast doesn't exist 2");

        MySignalMast t = new MySignalMast();
        InstanceManager.getDefault(SignalMastManager.class).register(t);
        assertTrue( (boolean)signalMastExistsFunction.calculate(symbolTable, getParameterList(exprSignalMastIF1)),
            "SignalMast exists");
        assertTrue( (boolean)signalMastExistsFunction.calculate(symbolTable, getParameterList(exprSignalMastMySignalMast)),
            "SignalMast exists 2");
    }

    @Test
    public void testGetSetSignalMastAspectFunction() throws JmriException {
        Function getSignalMastAspectFunction = InstanceManager.getDefault(FunctionManager.class).get("getSignalMastAspect");
        assertEquals( "getSignalMastAspect", getSignalMastAspectFunction.getName(), "string matches");
        assertNotNull( getSignalMastAspectFunction.getDescription(), "Function has description");

        Function setSignalMastAspectFunction = InstanceManager.getDefault(FunctionManager.class).get("setSignalMastAspect");
        assertEquals( "setSignalMastAspect", setSignalMastAspectFunction.getName(), "string matches 2");
        assertNotNull( setSignalMastAspectFunction.getDescription(), "Function has description");

        SymbolTable symbolTable = new DefaultSymbolTable(new DefaultConditionalNG("IQC1", null));

        WrongNumberOfParametersException e = assertThrows( WrongNumberOfParametersException.class,
            () -> getSignalMastAspectFunction.calculate(symbolTable, getParameterList()),
            "exception is thrown");
        assertNotNull(e);

        CalculateException ex = assertThrows(CalculateException.class, () -> {
            Object notExist = getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastIF1));
            assertEquals( "Green", notExist, "Should have thrown SignalMast has correct aspect");
        });
        assertNotNull(ex);

        ex = assertThrows(CalculateException.class, () -> {
            Object notExist = getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastMySignalMast));
            assertEquals( "Green", notExist, "SignalMast has correct aspect");
        });
        assertNotNull(ex);

        MySignalMast sm = new MySignalMast();
        InstanceManager.getDefault(SignalMastManager.class).register(sm);
        sm.setState(SignalMast.UNKNOWN);

        sm._lastAspect = null;
        sm._aspect = "Green";
        assertEquals( "Green",
                getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastIF1)),
                "SignalMast has correct aspect");
        assertNull( sm._lastAspect, "SignalMast is not set");

        sm._aspect = "Green";
        assertEquals( "Green",
                getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastMySignalMast)),
                "SignalMast has correct aspect");
        assertNull( sm._lastAspect, "SignalMast is not set");

        sm._aspect = "Red";
        assertEquals( "Red",
                getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastIF1)),
                "SignalMast has correct aspect");
        assertNull( sm._lastAspect, "SignalMast is not set");

        sm._aspect = "Red";
        assertEquals( "Red",
                getSignalMastAspectFunction.calculate(symbolTable, getParameterList(exprSignalMastMySignalMast)),
                "SignalMast has correct aspect");
        assertNull( sm._lastAspect, "SignalMast is not set");

        sm._lastAspect = null;
        sm._aspect = "Green";
        assertNull( setSignalMastAspectFunction.calculate(symbolTable,
                getParameterList(exprSignalMastIF1, exprSignalMastGreen)),
                "SignalMast has Null aspect");
        assertEquals( "Green", sm._lastAspect, "SignalMast is set");

        sm._lastAspect = null;
        sm._aspect = "Green";
        assertNull( setSignalMastAspectFunction.calculate(symbolTable,
                getParameterList(exprSignalMastMySignalMast, exprSignalMastGreen)),
                "SignalMast has correct aspect");
        assertEquals( "Green", sm._lastAspect, "SignalMast is set");

        sm._lastAspect = null;
        sm._aspect = "Red";
        assertNull( setSignalMastAspectFunction.calculate(symbolTable,
                getParameterList(exprSignalMastIF1, exprSignalMastRed)),
                "SignalMast has correct aspect");
        assertEquals( "Red", sm._lastAspect, "SignalMast is set");

        sm._lastAspect = null;
        sm._aspect = "Red";
        assertNull( setSignalMastAspectFunction.calculate(symbolTable,
               getParameterList(exprSignalMastMySignalMast, exprSignalMastRed)),
                "SignalMast has correct aspect");
        assertEquals( "Red", sm._lastAspect, "SignalMast is set");
    }

    @Test
    public void testManagers() {
        FunctionManager functionManager = InstanceManager.getDefault(FunctionManager.class);

        assertEquals(functionManager.getConstant("sensors").getValue(), InstanceManager.getDefault(SensorManager.class));
        assertEquals(functionManager.getConstant("turnouts").getValue(), InstanceManager.getDefault(TurnoutManager.class));
        assertEquals(functionManager.getConstant("lights").getValue(), InstanceManager.getDefault(LightManager.class));
        assertEquals(functionManager.getConstant("signals").getValue(), InstanceManager.getDefault(SignalHeadManager.class));
        assertEquals(functionManager.getConstant("masts").getValue(), InstanceManager.getDefault(SignalMastManager.class));
        assertEquals(functionManager.getConstant("routes").getValue(), InstanceManager.getDefault(RouteManager.class));
        assertEquals(functionManager.getConstant("blocks").getValue(), InstanceManager.getDefault(BlockManager.class));
        assertEquals(functionManager.getConstant("oblocks").getValue(), InstanceManager.getDefault(OBlockManager.class));
        assertEquals(functionManager.getConstant("reporters").getValue(), InstanceManager.getDefault(ReporterManager.class));
        assertEquals(functionManager.getConstant("memories").getValue(), InstanceManager.getDefault(MemoryManager.class));
        assertEquals(functionManager.getConstant("powermanager").getValue(), InstanceManager.getDefault(PowerManager.class));
        assertEquals(functionManager.getConstant("addressedProgrammers").getValue(), InstanceManager.getDefault(AddressedProgrammerManager.class));
        assertEquals(functionManager.getConstant("globalProgrammers").getValue(), InstanceManager.getDefault(GlobalProgrammerManager.class));
        assertEquals(functionManager.getConstant("dcc").getValue(), InstanceManager.getDefault(CommandStation.class));
        assertEquals(functionManager.getConstant("audio").getValue(), InstanceManager.getDefault(AudioManager.class));
        assertEquals(functionManager.getConstant("shutdown").getValue(), InstanceManager.getDefault(ShutDownManager.class));
        assertEquals(functionManager.getConstant("layoutblocks").getValue(), InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class));
        assertEquals(functionManager.getConstant("warrants").getValue(), InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class));
        assertEquals(functionManager.getConstant("sections").getValue(), InstanceManager.getDefault(SectionManager.class));
        assertEquals(functionManager.getConstant("transits").getValue(), InstanceManager.getDefault(TransitManager.class));

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
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
