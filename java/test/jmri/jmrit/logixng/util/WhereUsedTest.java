package jmri.jmrit.logixng.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.ActionSensor;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.expressions.Or;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test WhereUsed
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class WhereUsedTest {

    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String EXPECTED_RESULT =
            "LogixNG: A new logixng for test" + NEW_LINE +
            "   ConditionalNG: IQC1" + NEW_LINE +
            "      ! A" + NEW_LINE +
            "         Set sensor IS1 to state Active   <<====" + NEW_LINE +
            "            ! E" + NEW_LINE +
            "               Set sensor IS1 to state Active   <<====" + NEW_LINE +
            "                  ! E" + NEW_LINE +
            "                     Set sensor IS1 to state Active   <<====" + NEW_LINE +
            "                        ! E" + NEW_LINE +
            "                           If Then Else. Execute on change" + NEW_LINE +
            "                              ? If" + NEW_LINE +
            "                                 Or. Evaluate All" + NEW_LINE +
            "                                    ? E1" + NEW_LINE +
            "                                       Or. Evaluate All" + NEW_LINE +
            "                                          ? E1" + NEW_LINE +
            "                                             Sensor IS1 is Active   <<====" + NEW_LINE +
            "" + NEW_LINE +
            "LogixNG: Another logixng for test" + NEW_LINE +
            "   ConditionalNG: IQC2" + NEW_LINE +
            "      ! A" + NEW_LINE +
            "         If Then Else. Execute on change" + NEW_LINE +
            "            ? If" + NEW_LINE +
            "               Sensor IS1 is Active   <<====" + NEW_LINE +
            "" + NEW_LINE +
            "Module: A new module for test" + NEW_LINE +
            "   ! Root" + NEW_LINE +
            "      Set sensor IS1 to state Active   <<====" + NEW_LINE +
            "         ! E" + NEW_LINE +
            "            If Then Else. Execute on change" + NEW_LINE +
            "               ? If" + NEW_LINE +
            "                  Sensor IS1 is Active   <<====" + NEW_LINE +
            "" + NEW_LINE +
            "Clipboard" + NEW_LINE +
            "   * A" + NEW_LINE +
            "      Many" + NEW_LINE +
            "         * X2" + NEW_LINE +
            "            Set sensor IS1 to state Active   <<====" + NEW_LINE +
            "               ! E" + NEW_LINE +
            "                  If Then Else. Execute on change" + NEW_LINE +
            "                     ? If" + NEW_LINE +
            "                        Sensor IS1 is Active   <<====" + NEW_LINE +
            "         * X1" + NEW_LINE +
            "            Set sensor IS1 to state Active   <<====" + NEW_LINE +
            "               ! E" + NEW_LINE +
            "                  If Then Else. Execute on change" + NEW_LINE +
            "                     ? If" + NEW_LINE +
            "                        Sensor IS1 is Active   <<====" + NEW_LINE;

    public void setUpLogixNG() throws SocketAlreadyConnectedException {

        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");


        // Create a LogixNG

        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logixng for test");  // NOI18N
        ConditionalNG conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

        ActionSensorWithChildren actionSensorWithChildren1 = new ActionSensorWithChildren("IQDA111", null);
        actionSensorWithChildren1.getSelectNamedBean().setNamedBean(sensor);
        actionSensorWithChildren1.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensorWithChildren1);
        conditionalNG.getChild(0).connect(maleSocket);

        ActionSensorWithChildren actionSensorWithChildren2 = new ActionSensorWithChildren("IQDA112", null);
        actionSensorWithChildren2.getSelectNamedBean().setNamedBean(sensor);
        actionSensorWithChildren2.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensorWithChildren2);
        actionSensorWithChildren1.getChild(0).connect(maleSocket);

        ActionSensorWithChildren actionSensorWithChildren3 = new ActionSensorWithChildren("IQDA113", null);
        actionSensorWithChildren3.getSelectNamedBean().setNamedBean(sensor);
        actionSensorWithChildren3.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensorWithChildren3);
        actionSensorWithChildren2.getChild(0).connect(maleSocket);

        IfThenElse ifThenElse = new IfThenElse("IQDA114", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        actionSensorWithChildren3.getChild(0).connect(maleSocket);

        Or or1 = new Or("IQDE111", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(or1);
        ifThenElse.getChild(0).connect(maleSocket2);

        Or or2 = new Or("IQDE112", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(or2);
        or1.getChild(0).connect(maleSocket2);

        ExpressionSensor expressionSensor = new ExpressionSensor("IQDE113", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        or2.getChild(0).connect(maleSocket2);

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        ActionAtomicBoolean actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        expressionSensor.getSelectNamedBean().setNamedBean(sensor);
        expressionSensor.getSelectEnum().setEnum(ExpressionSensor.SensorState.Active);

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.activate();
        logixNG.setEnabled(true);


        // Create another LogixNG

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("Another logixng for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC2", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

        ifThenElse = new IfThenElse("IQDA222", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);

        expressionSensor = new ExpressionSensor("IQDE221", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        ifThenElse.getChild(0).connect(maleSocket2);

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        expressionSensor.getSelectNamedBean().setNamedBean(sensor);
        expressionSensor.getSelectEnum().setEnum(ExpressionSensor.SensorState.Active);

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.activate();
        logixNG.setEnabled(true);


        // Create a third LogixNG that doesn't have the sensor

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A third logixng for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC3", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

        ifThenElse = new IfThenElse("IQDA322", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);

        expressionSensor = new ExpressionSensor("IQDE321", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        ifThenElse.getChild(0).connect(maleSocket2);

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

//        expressionSensor.getSelectNamedBean().setNamedBean(sensor);
        expressionSensor.getSelectEnum().setEnum(ExpressionSensor.SensorState.Active);

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.activate();
        logixNG.setEnabled(true);


        // Create a Module

        Module module = InstanceManager.getDefault(ModuleManager.class)
                .createModule("A new module for test",
                        InstanceManager.getDefault(FemaleSocketManager.class)
                                .getSocketTypeByType("DefaultFemaleDigitalActionSocket"));  // NOI18N

        actionSensorWithChildren3 = new ActionSensorWithChildren("IQDA421", null);
        actionSensorWithChildren3.getSelectNamedBean().setNamedBean(sensor);
        actionSensorWithChildren3.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensorWithChildren3);
        module.getRootSocket().connect(maleSocket);

        ifThenElse = new IfThenElse("IQDA422", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        actionSensorWithChildren3.getChild(0).connect(maleSocket);

        expressionSensor = new ExpressionSensor("IQDE421", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        ifThenElse.getChild(0).connect(maleSocket2);

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        expressionSensor.getSelectNamedBean().setNamedBean(sensor);
        sensor.setCommandedState(Sensor.ACTIVE);

        if (! module.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();


        // Add an item to the clipboard

        actionSensorWithChildren3 = new ActionSensorWithChildren("IQDA521", null);
        actionSensorWithChildren3.getSelectNamedBean().setNamedBean(sensor);
        actionSensorWithChildren3.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensorWithChildren3);
        List<String> errors = new ArrayList<>();
        InstanceManager.getDefault(LogixNG_Manager.class).getClipboard().add(maleSocket, errors);
        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join(String.format(", "), errors));
        }

        ifThenElse = new IfThenElse("IQDA522", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        actionSensorWithChildren3.getChild(0).connect(maleSocket);

        expressionSensor = new ExpressionSensor("IQDE521", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        ifThenElse.getChild(0).connect(maleSocket2);

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        expressionSensor.getSelectNamedBean().setNamedBean(sensor);
        sensor.setCommandedState(Sensor.ACTIVE);

        if (! module.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();


        // Add another item to the clipboard

        actionSensorWithChildren3 = new ActionSensorWithChildren("IQDA621", null);
        actionSensorWithChildren3.getSelectNamedBean().setNamedBean(sensor);
        actionSensorWithChildren3.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensorWithChildren3);
        errors = new ArrayList<>();
        InstanceManager.getDefault(LogixNG_Manager.class).getClipboard().add(maleSocket, errors);
        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join(String.format(", "), errors));
        }

        ifThenElse = new IfThenElse("IQDA622", null);
        maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        actionSensorWithChildren3.getChild(0).connect(maleSocket);

        expressionSensor = new ExpressionSensor("IQDE621", null);
        maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        ifThenElse.getChild(0).connect(maleSocket2);

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        expressionSensor.getSelectNamedBean().setNamedBean(sensor);
        sensor.setCommandedState(Sensor.ACTIVE);

        if (! module.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
    }

    @Test
    public void testWhereUsed() throws SocketAlreadyConnectedException {
        setUpLogixNG();


//        org.apache.commons.lang3.mutable.MutableInt lineNumber = new org.apache.commons.lang3.mutable.MutableInt();
//        java.io.PrintWriter writer = new java.io.PrintWriter(System.out);
//        InstanceManager.getDefault(LogixNG_Manager.class).printTree(writer, "   ", lineNumber);
//        writer.flush();


        Sensor s1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        String result = WhereUsed.whereUsed(s1);
//        System.out.format("%n%n---------------%nResult:%n%s-----------------------%n%n", result);
        Assert.assertEquals(EXPECTED_RESULT, result);


        Turnout t1 = InstanceManager.getDefault(TurnoutManager.class).provide("Turnout1");
        result = WhereUsed.whereUsed(t1);
//        System.out.format("%n%n---------------%nResult:%n%s-----------------------%n%n", result);
        Assert.assertEquals("", result);    // Turnout Turnout1 is not used so empty string is expected
    }

    // The minimal setup for log4J
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


    private static class ActionSensorWithChildren extends ActionSensor implements FemaleSocketListener {

        FemaleSocket _socket;

        public ActionSensorWithChildren(String sys, String user) {
            super(sys, user);
            _socket = InstanceManager.getDefault(DigitalActionManager.class)
                    .createFemaleSocket(this, this, "E");
        }

        /** {@inheritDoc} */
        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            return _socket;
        }

        /** {@inheritDoc} */
        @Override
        public int getChildCount() {
            return 1;
        }

        @Override
        public void connected(FemaleSocket socket) {
        }

        @Override
        public void disconnected(FemaleSocket socket) {
        }

    }

}
