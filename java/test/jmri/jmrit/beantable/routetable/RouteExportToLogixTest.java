package jmri.jmrit.beantable.routetable;

import jmri.*;
import jmri.implementation.DefaultConditional;
import jmri.implementation.DefaultLogix;
import jmri.implementation.DefaultRoute;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for jmri.jmrit.beantable.routtable.RouteExportToLogix
 *
 * @author Paul Bender Copyright (C) 2020
 */
class RouteExportToLogixTest {

    private LogixManager lm;
    private ConditionalManager cm;
    private RouteManager rm;
    private Logix l;
    private Map<String,Conditional> conditionalMap;

    @BeforeEach
    void setUp() {
        JUnitUtil.setUpLoggingAndCommonProperties();
        conditionalMap = new HashMap<>();
        cm = Mockito.mock(ConditionalManager.class);
        Mockito.when(cm.typeLetter()).thenReturn('X');
        Mockito.when(cm.createNewConditional(Mockito.anyString(),Mockito.anyString())).thenAnswer(i -> generateConditional(i.getArgument(0),i.getArgument(1)));
        Mockito.when(cm.getBySystemName(Mockito.anyString())).thenAnswer(i -> conditionalMap.get(i.getArgument(0)));
        lm = Mockito.mock(LogixManager.class);
        Mockito.when(lm.getSystemNamePrefix()).thenReturn("IX");
        Mockito.when(lm.typeLetter()).thenReturn('X');
        Mockito.when(lm.createNewLogix(Mockito.anyString(),Mockito.anyString())).thenAnswer(i -> l = new DefaultLogix(i.getArgument(0),i.getArgument(1),cm));
        rm = Mockito.mock(RouteManager.class);
    }

    @AfterEach
    void tearDown() {
        lm = null;
        rm = null;
        cm = null;
        if(l!=null){
            l.deActivateLogix();
            l.dispose();
        }
        conditionalMap.clear();
        conditionalMap = null;
        l = null;
        JUnitUtil.tearDown();
    }

    private Conditional generateConditional(String systemName,String userName){
        conditionalMap.putIfAbsent(systemName,new DefaultConditional(systemName,userName));
        l.addConditional(systemName,conditionalMap.get(systemName));
        return conditionalMap.get(systemName);
    }

    private Turnout createMockTurnout(String systemName, String userName){
        Turnout t = Mockito.mock(Turnout.class);
        Mockito.when(t.getSystemName()).thenReturn(systemName);
        Mockito.when(t.getUserName()).thenReturn(userName);
        Mockito.when(t.getDisplayName()).thenReturn(userName);
        return t;
    }

    private Sensor createMockSensor(String systemName,String userName){
        Sensor s = Mockito.mock(Sensor.class);
        Mockito.when(s.getSystemName()).thenReturn(systemName);
        Mockito.when(s.getUserName()).thenReturn(userName);
        Mockito.when(s.getDisplayName()).thenReturn(userName);
        return s;
    }

    private void addRouteSensorToRoute(Sensor s,Route r,int sensorMode,int index){
        Mockito.when(r.getRouteSensor(index)).thenReturn(s);
        Mockito.when(r.getRouteSensorMode(index)).thenReturn(sensorMode);
        String displayName = s.getDisplayName();
        Mockito.when(r.getRouteSensorName(index)).thenReturn(displayName);
    }

    private void addControlTurnoutToRoute(Turnout t,Route r,int turnoutMode){
        Mockito.when(r.getCtlTurnout()).thenReturn(t);
        Mockito.when(r.getControlTurnoutState()).thenReturn(turnoutMode);
        String displayName = t.getDisplayName();
        Mockito.when(r.getControlTurnout()).thenReturn(displayName);
    }

    private void addOutputTurnoutToRoute(Turnout t,Route r,int turnoutMode,int index){
        Mockito.when(r.getOutputTurnout(index)).thenReturn(t);
        String displayName = t.getDisplayName();
        Mockito.when(r.getOutputTurnoutByIndex(index)).thenReturn(displayName);
        Mockito.when(r.getOutputTurnoutState(index)).thenReturn(turnoutMode);
        String systemName = t.getSystemName();
        Mockito.when(r.getOutputTurnoutSetState(systemName)).thenReturn(turnoutMode);
        Mockito.when(r.getNumOutputTurnouts()).thenReturn(index+1);
    }

    private void addOutputSensorToRoute(Sensor s,Route r,int sensorMode,int index){
        Mockito.when(r.getOutputSensor(index)).thenReturn(s);
        String displayName = s.getDisplayName();
        Mockito.when(r.getOutputSensorByIndex(index)).thenReturn(displayName);
        Mockito.when(r.getOutputSensorState(index)).thenReturn(sensorMode);
        String systemName = s.getSystemName();
        Mockito.when(r.getOutputSensorSetState(systemName)).thenReturn(sensorMode);
        Mockito.when(r.getNumOutputSensors()).thenReturn(index+1);
    }

    @Test
    void whenAnEmptyRouteIsExported_ThenAnEmptyLogixIsCreated_AndTheRouteIsDeleted() {
        Route r = new DefaultRoute("IO12345","Hello World");
        Mockito.when(rm.getBySystemName(Mockito.anyString())).thenReturn(r);
        Mockito.when(rm.getByUserName(Mockito.anyString())).thenReturn(r);
        new RouteExportToLogix("IO12345",rm,lm,cm).export();
        Mockito.verify(lm).createNewLogix("IX:RTX:IO12345","Hello World");
        Mockito.verify(rm).deleteRoute(r);
    }

    @Test
    void whenANullRouteIsExported_AnErrorIsReported() {
        Mockito.when(rm.getBySystemName(Mockito.anyString())).thenReturn(null);
        Mockito.when(rm.getByUserName(Mockito.anyString())).thenReturn(null);
        new RouteExportToLogix("IO12345",rm,lm,cm).export();
        JUnitAppender.assertErrorMessage("Route IO12345 does not exist");
    }

    @Test
    void whenRouteWithOneTurnoutIsExported_ThenALogixIsCreatedWithAConditionalAction_AndTheRouteIsDeleted() {
        Route r = Mockito.mock(Route.class);
        Mockito.when(r.getSystemName()).thenReturn("IO12345");
        Mockito.when(r.getUserName()).thenReturn("Hello World");
        Mockito.when(r.getDisplayName()).thenReturn("Hello World");
        Turnout t = createMockTurnout("IT1","Turnout");
        addOutputTurnoutToRoute(t,r,Turnout.THROWN,0);
        addControlTurnoutToRoute(t,r,Turnout.CLOSED);
        Mockito.when(rm.getBySystemName(Mockito.anyString())).thenReturn(r);
        Mockito.when(rm.getByUserName(Mockito.anyString())).thenReturn(r);
        new RouteExportToLogix("IO12345",rm,lm,cm).export();
        Mockito.verify(cm).createNewConditional(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(lm).createNewLogix("IX:RTX:IO12345","Hello World");
        Mockito.verify(rm).deleteRoute(r);
        assertThat(l).isNotNull();
        assertThat(l.getNumConditionals()).isEqualTo(1);
        assertThat(l.getConditionalByNumberOrder(0)).isNotNull();
        Conditional c = l.getConditional(l.getConditionalByNumberOrder(0));
        assertThat(c.getLogicType()).isEqualTo(Conditional.AntecedentOperator.ALL_AND);
        assertThat(c.getTriggerOnChange()).isTrue();
        assertThat(c.getCopyOfStateVariables()).isNotEmpty().hasSize(1);
        List<ConditionalVariable> sv = c.getCopyOfStateVariables();
        assertThat(sv.get(0).getNamedBean().getName()).isEqualTo(t.getDisplayName());
        assertThat(sv.get(0).getState()).isEqualTo(Turnout.CLOSED);
        assertThat(c.getCopyOfActions()).isNotEmpty().hasSize(1);
        List<ConditionalAction> a = c.getCopyOfActions();
        assertThat(a.get(0).getDeviceName()).isEqualTo("Turnout");
        assertThat(a.get(0).getActionData()).isEqualTo(Turnout.THROWN);
    }


    @Test
    void whenRouteWithOneSensorIsExported_ThenALogixIsCreatedWithAConditionalAction_AndTheRouteIsDeleted() {
        Route r = Mockito.mock(Route.class);
        Mockito.when(r.getSystemName()).thenReturn("IO12345");
        Mockito.when(r.getUserName()).thenReturn("Hello World");
        Mockito.when(r.getDisplayName()).thenReturn("Hello World");
        Turnout t = createMockTurnout("IT1","Turnout");
        addControlTurnoutToRoute(t,r,Turnout.CLOSED);
        Sensor s = createMockSensor("IS1","Sensor");
        addOutputSensorToRoute(s,r,Sensor.ACTIVE,0);
        Mockito.when(rm.getBySystemName(Mockito.anyString())).thenReturn(r);
        Mockito.when(rm.getByUserName(Mockito.anyString())).thenReturn(r);
        new RouteExportToLogix("IO12345",rm,lm,cm).export();
        Mockito.verify(cm).createNewConditional(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(lm).createNewLogix("IX:RTX:IO12345","Hello World");
        Mockito.verify(rm).deleteRoute(r);
        assertThat(l).isNotNull();
        assertThat(l.getNumConditionals()).isEqualTo(1);
        assertThat(l.getConditionalByNumberOrder(0)).isNotNull();
        Conditional c = l.getConditional(l.getConditionalByNumberOrder(0));
        assertThat(c.getLogicType()).isEqualTo(Conditional.AntecedentOperator.ALL_AND);
        assertThat(c.getTriggerOnChange()).isTrue();
        assertThat(c.getCopyOfStateVariables()).isNotEmpty().hasSize(1);
        List<ConditionalVariable> sv = c.getCopyOfStateVariables();
        assertThat(sv.get(0).getNamedBean().getName()).isEqualTo(t.getDisplayName());
        assertThat(sv.get(0).getState()).isEqualTo(Turnout.CLOSED);
        assertThat(c.getCopyOfActions()).isNotEmpty().hasSize(1);
        List<ConditionalAction> a = c.getCopyOfActions();
        assertThat(a.get(0).getDeviceName()).isEqualTo("Sensor");
        assertThat(a.get(0).getActionData()).isEqualTo(Sensor.ACTIVE);
    }

    @Test
    void whenRouteWithOneTurnoutAndRouteSensorIsExported_ThenALogixIsCreatedWithAConditionalAction_AndTheRouteIsDeleted() {
        Route r = Mockito.mock(Route.class);
        Mockito.when(r.getSystemName()).thenReturn("IO12345");
        Mockito.when(r.getUserName()).thenReturn("Hello World");
        Mockito.when(r.getDisplayName()).thenReturn("Hello World");
        Turnout t = createMockTurnout("IT1","Turnout");
        addOutputTurnoutToRoute(t,r,Turnout.THROWN,0);
        Sensor s = createMockSensor("IS1","Sensor");
        addRouteSensorToRoute(s,r,Route.ONACTIVE,0);
        Mockito.when(rm.getBySystemName(Mockito.anyString())).thenReturn(r);
        Mockito.when(rm.getByUserName(Mockito.anyString())).thenReturn(r);
        new RouteExportToLogix("IO12345",rm,lm,cm).export();
        Mockito.verify(cm).createNewConditional(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(lm).createNewLogix("IX:RTX:IO12345","Hello World");
        Mockito.verify(rm).deleteRoute(r);
        assertThat(l).isNotNull();
        assertThat(l.getNumConditionals()).isEqualTo(1);
        assertThat(l.getConditionalByNumberOrder(0)).isNotNull();
        Conditional c = l.getConditional(l.getConditionalByNumberOrder(0));
        assertThat(c.getLogicType()).isEqualTo(Conditional.AntecedentOperator.ALL_AND);
        assertThat(c.getTriggerOnChange()).isTrue();
        assertThat(c.getCopyOfStateVariables()).isNotEmpty().hasSize(1);
        List<ConditionalVariable> sv = c.getCopyOfStateVariables();
        assertThat(sv.get(0).getNamedBean().getName()).isEqualTo(s.getDisplayName());
        assertThat(sv.get(0).getState()).isEqualTo(Sensor.ACTIVE);
        assertThat(c.getCopyOfActions()).isNotEmpty().hasSize(1);
        List<ConditionalAction> a = c.getCopyOfActions();
        assertThat(a.get(0).getDeviceName()).isEqualTo("Turnout");
        assertThat(a.get(0).getActionData()).isEqualTo(Turnout.THROWN);
    }

    @Test
    void whenRouteWithALockTurnoutIsExported_ThenALogixIsCreatedWithAConditionalAction_AndTheRouteIsDeleted(){
        Route r = Mockito.mock(Route.class);
        Mockito.when(r.getSystemName()).thenReturn("IO12345");
        Mockito.when(r.getUserName()).thenReturn("Hello World");
        Mockito.when(r.getDisplayName()).thenReturn("Hello World");
        Turnout t = createMockTurnout("IT1","Turnout");
        addOutputTurnoutToRoute(t,r,Turnout.THROWN,0);
        Turnout lock = createMockTurnout("IT2","Lock");
        Mockito.when(r.getLockControlTurnout()).thenReturn("Lock");
        Mockito.when(r.getLockControlTurnoutState()).thenReturn(Turnout.THROWN);
        Mockito.when(r.getLockCtlTurnout()).thenReturn(lock);
        Mockito.when(rm.getBySystemName(Mockito.anyString())).thenReturn(r);
        Mockito.when(rm.getByUserName(Mockito.anyString())).thenReturn(r);
        new RouteExportToLogix("IO12345",rm,lm,cm).export();
        Mockito.verify(cm).createNewConditional(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(lm).createNewLogix("IX:RTX:IO12345","Hello World");
        Mockito.verify(rm).deleteRoute(r);
        assertThat(l).isNotNull();
        assertThat(l.getNumConditionals()).isEqualTo(1);
        assertThat(l.getConditionalByNumberOrder(0)).isNotNull();
        Conditional c = l.getConditional(l.getConditionalByNumberOrder(0));
        assertThat(c.getLogicType()).isEqualTo(Conditional.AntecedentOperator.ALL_AND);
        assertThat(c.getTriggerOnChange()).isTrue();
        assertThat(c.getCopyOfStateVariables()).isNotEmpty().hasSize(1);
        List<ConditionalVariable> sv = c.getCopyOfStateVariables();
        assertThat(sv.get(0).getNamedBean().getName()).isEqualTo(lock.getSystemName());
        assertThat(sv.get(0).getState()).isEqualTo(Turnout.CLOSED);
        assertThat(c.getCopyOfActions()).isNotEmpty().hasSize(2);
        List<ConditionalAction> a = c.getCopyOfActions();
        assertThat(a.get(0).getDeviceName()).isEqualTo("Turnout");
        assertThat(a.get(0).getActionData()).isEqualTo(Turnout.LOCKED);
        assertThat(a.get(1).getDeviceName()).isEqualTo("Turnout");
        assertThat(a.get(1).getActionData()).isEqualTo(Turnout.UNLOCKED);
    }
}
