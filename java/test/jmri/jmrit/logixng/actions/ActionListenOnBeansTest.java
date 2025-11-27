package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import javax.swing.JTextArea;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.script.swing.ScriptOutput;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ActionListenOnBeans
 *
 * @author Daniel Bergqvist 2019
 */
public class ActionListenOnBeansTest extends AbstractDigitalActionTestBase {

    private Sensor s1, s2, s3, sensorWait, s99;
    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private WaitForScaffold actionWaitFor;
    private ActionListenOnBeans actionListenOnBeans;


    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }

    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }

    @Override
    public MaleSocket getConnectableChild() {
        return null;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format("Listen on beans ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Many ::: Use default%n" +
                "            ::: Local variable \"bean\", init to None \"null\"%n" +
                "            ::: Local variable \"event\", init to None \"null\"%n" +
                "            ::: Local variable \"value\", init to None \"null\"%n" +
                "            ! A1%n" +
                "               Wait for ::: Use default%n" +
                "            ! A2%n" +
                "               Listen on beans ::: Use default%n" +
                "            ! A3%n" +
                "               Log data: Comma separated list ::: Use default%n" +
                "            ! A4%n" +
                "               Set sensor IS99 to state Active ::: Use default%n" +
                "            ! A5%n" +
                "               Socket not connected%n"
        );
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionListenOnBeans(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        ActionListenOnBeans t = new ActionListenOnBeans("IQDA1", null);
        assertNotNull( t, "not null");
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, actionListenOnBeans.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class,
            () -> actionListenOnBeans.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.OTHER, _base.getCategory(), "Category matches");
    }

    @Test
    public void testShortDescription() {
        assertEquals( "Listen on beans", _base.getShortDescription(), "String matches");
    }

    @Test
    public void testLongDescription() {
        ActionListenOnBeans a1 = new ActionListenOnBeans("IQDA321", null);
        assertEquals( "Listen on beans", a1.getShortDescription(), "strings are equal");
        ActionListenOnBeans a2 = new ActionListenOnBeans("IQDA321", null);
        assertEquals( "Listen on beans", a2.getLongDescription(), "strings are equal");
    }

    private JTextArea getOutputArea() {
        return ScriptOutput.getDefault().getOutputArea();
    }

    @Test
    public void testExecute() throws JmriException {

        var oldReleaseCondition = actionWaitFor.getReleaseCondition();

        actionWaitFor.setReleaseCondition(() -> { return sensorWait.getState() == Sensor.ACTIVE; });
        sensorWait.setState(Sensor.ACTIVE);

        conditionalNG.setRunDelayed(true);

        // Test listen on sensor s1
        assertTrue(conditionalNG.getCurrentThread().isQueueEmpty());
        getOutputArea().setText("");
        s99.setState(Sensor.INACTIVE);
        s1.setState(Sensor.INACTIVE);
        assertTrue(JUnitUtil.waitFor(() -> {return s99.getState() == Sensor.ACTIVE;}));
        assertTrue(conditionalNG.getCurrentThread().isQueueEmpty());
        assertEquals("IS1, KnownState, 4", getOutputArea().getText().trim());
        getOutputArea().setText("");
        s99.setState(Sensor.INACTIVE);
        assertEquals("", getOutputArea().getText());
        s1.setState(Sensor.ACTIVE);
        // LogixNG Thread updates the TextArea when complete.
        assertTrue(JUnitUtil.waitFor(() -> {return s99.getState() == Sensor.ACTIVE;}));
        assertTrue(conditionalNG.getCurrentThread().isQueueEmpty());
        assertEquals("IS1, KnownState, 2", getOutputArea().getText().trim());

        // Test listen on sensor s1
        assertTrue(conditionalNG.getCurrentThread().isQueueEmpty());
        getOutputArea().setText("");
        s99.setState(Sensor.INACTIVE);
        s1.setState(Sensor.INACTIVE);
        assertTrue(JUnitUtil.waitFor(() -> {return s99.getState() == Sensor.ACTIVE;}));
        assertTrue(conditionalNG.getCurrentThread().isQueueEmpty());
        assertEquals("IS1, KnownState, 4", getOutputArea().getText().trim());
        getOutputArea().setText("");
        s99.setState(Sensor.INACTIVE);
        assertEquals("", getOutputArea().getText());

        s1.setState(Sensor.ACTIVE);
        assertTrue(JUnitUtil.waitFor(() -> {return s99.getState() == Sensor.ACTIVE;}));
        assertTrue(conditionalNG.getCurrentThread().isQueueEmpty());
        assertEquals("IS1, KnownState, 2", getOutputArea().getText().trim());

        // Test listen on sensor s1, s2 and s3, when s1 and s2 goes active
        // while the conditionalNG is running.
        assertTrue(conditionalNG.getCurrentThread().isQueueEmpty());
        getOutputArea().setText("");
        sensorWait.setState(Sensor.INACTIVE);
        s99.setState(Sensor.INACTIVE);
        s1.setState(Sensor.INACTIVE);
        s2.setState(Sensor.INACTIVE);
        s3.setState(Sensor.INACTIVE);
        sensorWait.setState(Sensor.ACTIVE);
        assertTrue(JUnitUtil.waitFor(() -> {return s99.getState() == Sensor.ACTIVE;}));
        assertTrue(conditionalNG.getCurrentThread().isQueueEmpty());
        assertTrue(JUnitUtil.waitFor(() -> {return "IS1, KnownState, 4\nIS2, KnownState, 4\nIS3, KnownState, 4\n".equals(getOutputArea().getText());}));
        assertEquals("IS1, KnownState, 4\nIS2, KnownState, 4\nIS3, KnownState, 4\n", getOutputArea().getText());
        getOutputArea().setText("");
        s99.setState(Sensor.INACTIVE);
        assertEquals("", getOutputArea().getText());

        s1.setState(Sensor.ACTIVE);
        assertTrue(JUnitUtil.waitFor(() -> {return s99.getState() == Sensor.ACTIVE;}));
        assertTrue(conditionalNG.getCurrentThread().isQueueEmpty());
        assertEquals("IS1, KnownState, 2", getOutputArea().getText().trim());

        actionWaitFor.setReleaseCondition(oldReleaseCondition);
    }

    @Before
    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException, ParserException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        s1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        s2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        s3 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS3");
        sensorWait = InstanceManager.getDefault(SensorManager.class).provideSensor("ISWait");
        s99 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS99");

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        DigitalActionManager digitalActionManager = InstanceManager.getDefault(DigitalActionManager.class);

        DigitalMany many = new DigitalMany(digitalActionManager.getAutoSystemName(), null);
        MaleSocket socket = digitalActionManager.registerAction(many);
        socket.addLocalVariable("bean", SymbolTable.InitialValueType.None, null);
        socket.addLocalVariable("event", SymbolTable.InitialValueType.None, null);
        socket.addLocalVariable("value", SymbolTable.InitialValueType.None, null);
        conditionalNG.getChild(0).connect(socket);

        actionWaitFor = new WaitForScaffold(digitalActionManager.getAutoSystemName(), null, ()->{return true;});
        socket = digitalActionManager.registerAction(actionWaitFor);
        many.getChild(0).connect(socket);

        actionListenOnBeans = new ActionListenOnBeans(digitalActionManager.getAutoSystemName(), null);
        actionListenOnBeans.addReference("Sensor:IS1");
        actionListenOnBeans.addReference("Sensor:IS2");
        actionListenOnBeans.addReference("Sensor:IS3");
        actionListenOnBeans.setLocalVariableNamedBean("bean");
        actionListenOnBeans.setLocalVariableEvent("event");
        actionListenOnBeans.setLocalVariableNewValue("value");
        socket = digitalActionManager.registerAction(actionListenOnBeans);
        many.getChild(1).connect(socket);

        _base = actionListenOnBeans;
        _baseMaleSocket = socket;

        LogData logData = new LogData(digitalActionManager.getAutoSystemName(), null);
        logData.setFormatType(LogData.FormatType.CommaSeparatedList);
        logData.getDataList().add(new LogData.Data(LogData.DataType.LocalVariable, "bean"));
        logData.getDataList().add(new LogData.Data(LogData.DataType.LocalVariable, "event"));
        logData.getDataList().add(new LogData.Data(LogData.DataType.LocalVariable, "value"));
        logData.setLogToLog(false);
        logData.setLogToScriptOutput(true);
        socket = digitalActionManager.registerAction(logData);
        many.getChild(2).connect(socket);

        ActionSensor actionSensor = new ActionSensor(digitalActionManager.getAutoSystemName(), null);
        actionSensor.getSelectNamedBean().setNamedBean("IS99");
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        socket = digitalActionManager.registerAction(actionSensor);
        many.getChild(3).connect(socket);

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.removeMatchingThreads("ScriptOutput PipeListener");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
        s1 = s2 = s3 = s99 = null;
        logixNG = null;
        conditionalNG = null;
        actionWaitFor = null;
        actionListenOnBeans = null;
    }

}
