package jmri.jmrit.logixng;

import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.actions.*;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test call module from JMRI
 *
 * @author Daniel Bergqvist 2024
 */
public class CallModuleTest {

    @Test
    public void testCallModuleWithOneParameter() throws JmriException {
        // Get the module. Note that the module must have exactly one
        // parameter and that the parameter must have "input" selected.
        Module module = InstanceManager.getDefault(ModuleManager.class).getModule("My module");

        // The parameter to the module
        Object parameter = "My sensor";

        // Execute the module
        InstanceManager.getDefault(LogixNG_Manager.class).executeModule(module, parameter);

        // Ensure that the module has been executed and has set the sensor
        Sensor sensor = InstanceManager.getDefault(SensorManager.class)
                .newSensor("IS1", "My sensor");  // NOI18N

        JUnitUtil.waitFor(() -> sensor.getState() == Sensor.ACTIVE,
                "Sensor did not go active: " + sensor.getDisplayName());
    }

    @Test
    public void testCallModuleWithTwoParameters() throws JmriException {
        // Get the module
        Module module = InstanceManager.getDefault(ModuleManager.class).getModule("My other module");

        // Get the parameters for the module
        Collection<Module.Parameter> parameterNames = module.getParameters();

        // Get the parameter
        Module.Parameter[] params = parameterNames.toArray(Module.Parameter[]::new);

        // Set the value of the parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(params[0].getName(), "My sensor");
        parameters.put(params[1].getName(), "My turnout");

        // Execute the module
        InstanceManager.getDefault(LogixNG_Manager.class).executeModule(module, parameters);

        // Ensure that the module has been executed and has set the sensor and the turnout
        Sensor sensor = InstanceManager.getDefault(SensorManager.class)
                .newSensor("IS1", "My sensor");  // NOI18N
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class)
                .newTurnout("IT1", "My turnout");  // NOI18N

        JUnitUtil.waitFor(() -> sensor.getState() == Sensor.ACTIVE,
                "Sensor did not go active: " + sensor.getDisplayName());
        JUnitUtil.waitFor(() -> turnout.getState() == Turnout.THROWN,
                "Turnout did not go thrown: " + turnout.getDisplayName());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException, ParserException, JmriException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        DigitalActionManager digitalActionManager =
                InstanceManager.getDefault(DigitalActionManager.class);

        Sensor sensor = InstanceManager.getDefault(SensorManager.class)
                .newSensor("IS1", "My sensor");  // NOI18N
        sensor.setState(Sensor.INACTIVE);

        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class)
                .newTurnout("IT1", "My turnout");  // NOI18N
        turnout.setState(Turnout.CLOSED);

        FemaleSocketManager.SocketType socketType = InstanceManager.getDefault(FemaleSocketManager.class)
                .getSocketTypeByType("DefaultFemaleDigitalActionSocket");

        Module myModule = InstanceManager.getDefault(ModuleManager.class)
                .createModule("My module", socketType);  // NOI18N

        myModule.addParameter("sensor", true, false);

        ActionSensor actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionSensor.getSelectNamedBean().setLocalVariable("sensor");
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        myModule.getRootSocket().connect(digitalActionManager.registerAction(actionSensor));

        Module myOtherModule = InstanceManager.getDefault(ModuleManager.class)
                .createModule("My other module", socketType);  // NOI18N

        myOtherModule.addParameter("sensor", true, false);
        myOtherModule.addParameter("turnout", true, false);

        DigitalMany many = new DigitalMany(digitalActionManager.getAutoSystemName(), null);
        myOtherModule.getRootSocket().connect(digitalActionManager.registerAction(many));

        ActionSensor otherActionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        otherActionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        otherActionSensor.getSelectNamedBean().setLocalVariable("sensor");
        otherActionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        many.getChild(0).connect(digitalActionManager.registerAction(otherActionSensor));

        ActionTurnout actionTurnout = new ActionTurnout(digitalActionManager.getAutoSystemName(), null);
        actionTurnout.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        actionTurnout.getSelectNamedBean().setLocalVariable("turnout");
        actionTurnout.getSelectEnum().setEnum(ActionTurnout.TurnoutState.Thrown);
        many.getChild(1).connect(digitalActionManager.registerAction(actionTurnout));
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGTest.class);

}
