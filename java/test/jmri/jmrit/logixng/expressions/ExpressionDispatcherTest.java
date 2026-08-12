package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.implementation.DefaultSection;
import jmri.jmrit.dispatcher.*;
import jmri.jmrit.logixng.LogixNG_Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.util.DispatcherActiveTrainManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.jdom2.JDOMException;

import org.junit.jupiter.api.*;

/**
 * Test ExpressionDispatcher
 *
 * @author Daniel Bergqvist 2018
 */
@DisabledIfHeadless
@Disabled("This test class currently fails on Windows CI")
public class ExpressionDispatcherTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionDispatcher expressionDispatcher;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    private ActiveTrain _myActiveTrain;
    private final String _myActiveTrainFileName = "MyActiveTrainFile.xml";


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
        return String.format("Train \"MyActiveTrainFile.xml\" is Automatic mode ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Train \"MyActiveTrainFile.xml\" is Automatic mode ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionDispatcher(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        ExpressionDispatcher expression2;

        expression2 = new ExpressionDispatcher("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Train \"\" is -  Train Mode  -", expression2.getLongDescription(),
            "String matches");

        expression2 = new ExpressionDispatcher("IQDE321", "My expr");
        assertNotNull( expression2, "object exists");
        assertEquals( "My expr", expression2.getUserName(), "Username matches");
        assertEquals( "Train \"\" is -  Train Mode  -", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionDispatcher("IQDE321", null);
        expression2.setTrainInfoFileName(_myActiveTrainFileName);
        assertEquals(_myActiveTrainFileName, expression2.getTrainInfoFileName());
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Train \"MyActiveTrainFile.xml\" is -  Train Mode  -",
            expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionDispatcher("IQDE321", "My expr");
        expression2.setTrainInfoFileName(_myActiveTrainFileName);
        assertEquals(_myActiveTrainFileName, expression2.getTrainInfoFileName());
        assertNotNull( expression2, "object exists");
        assertEquals( "My expr", expression2.getUserName(), "Username matches");
        assertEquals( "Train \"MyActiveTrainFile.xml\" is -  Train Mode  -", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> {
                var ed = new ExpressionDispatcher("IQE55:12:XY11", null);
                fail("did not thrown when creating " + ed);
            }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class,
            () -> {
                var ed = new ExpressionDispatcher("IQE55:12:XY11", "A name");
                fail("did not thrown when creating " + ed);
            }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionDispatcher.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class,
            () -> expressionDispatcher.getChild(0),
            "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertEquals( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

//        expressionDispatcher.setTrainInfoFileName(null);
//        assertEquals("Dispatcher", expressionDispatcher.getShortDescription());
//        assertEquals("Train \"null\" is Automatic mode", expressionDispatcher.getLongDescription());
        expressionDispatcher.setTrainInfoFileName(_myActiveTrainFileName);
        expressionDispatcher.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionDispatcher.getSelectEnum().setEnum(ExpressionDispatcher.DispatcherState.Dispatched);
        assertEquals("Train \"MyActiveTrainFile.xml\" is Dispatched mode", expressionDispatcher.getLongDescription());
        expressionDispatcher.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        assertEquals("Train \"MyActiveTrainFile.xml\" is not Dispatched mode", expressionDispatcher.getLongDescription());
        expressionDispatcher.getSelectEnum().setEnum(ExpressionDispatcher.DispatcherState.Manual);
        assertEquals("Train \"MyActiveTrainFile.xml\" is not Manual mode", expressionDispatcher.getLongDescription());
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException, IOException, JDOMException {
        // Clear flag
        atomicBoolean.set(false);
        // Set active train to manual
        _myActiveTrain.setMode(ActiveTrain.MANUAL);
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Set active train to automatic. This should not execute the conditional.
        _myActiveTrain.setMode(ActiveTrain.AUTOMATIC);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Close the switch. This should not execute the conditional.
        _myActiveTrain.setMode(ActiveTrain.MANUAL);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Turn the light on. This should execute the conditional.
        _myActiveTrain.setMode(ActiveTrain.AUTOMATIC);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Turn the light off. This should not execute the conditional.
        _myActiveTrain.setMode(ActiveTrain.MANUAL);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Test IS_NOT
        expressionDispatcher.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Turn the light on. This should not execute the conditional.
        _myActiveTrain.setMode(ActiveTrain.AUTOMATIC);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Turn the light off. This should not execute the conditional.
        _myActiveTrain.setMode(ActiveTrain.MANUAL);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
    }

    private void setupActiveTrain() throws IOException, JDOMException {
        Transit transit = InstanceManager.getDefault(jmri.TransitManager.class).createNewTransit("MyTransit");
        Section section1 = new DefaultSection("Section1");
        section1.addBlock(InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("StartBlock"));
        Section section2 = new DefaultSection("Section2");
        section2.addBlock(InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("Block2"));
        Section section3 = new DefaultSection("Section3");
        section3.addBlock(InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("Block3"));
        Section section4 = new DefaultSection("Section4");
        section4.addBlock(InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("EndBlock"));


        transit.addTransitSection(new TransitSection(section1, 1, Section.FORWARD, false));
        transit.addTransitSection(new TransitSection(section2, 2, Section.FORWARD, false));
        transit.addTransitSection(new TransitSection(section3, 3, Section.FORWARD, false));
        transit.addTransitSection(new TransitSection(section4, 4, Section.FORWARD, false));

        TrainInfo info = new TrainInfo();
        info.setTrainName("1");
        info.setTransitId("MyTransit");
        info.setTransitName("MyTransit");
        info.setStartBlockId("StartBlock");
        info.setStartBlockSeq(1);
        info.setDestinationBlockId("EndBlock");
        info.setDestinationBlockSeq(4);
        info.setDccAddress("1");
        info.setTrainFromUser(true);
        info.setAutoRun(false);

        TrainInfoFile tif = new TrainInfoFile();
        tif.writeTrainInfo(info, _myActiveTrainFileName);
        TrainInfo ti2 = tif.readTrainInfo(_myActiveTrainFileName);
        assertNotNull(ti2);

        _myActiveTrain =
                InstanceManager.getDefault(DispatcherActiveTrainManager.class)
                        .createActiveTrain(_myActiveTrainFileName);

        _myActiveTrain.setMode(ActiveTrain.AUTOMATIC);

        expressionDispatcher.setTrainInfoFileName(_myActiveTrainFileName);
        expressionDispatcher.propertyChange(new PropertyChangeEvent(this, "ActiveTrain", "", _myActiveTrainFileName));
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException, IOException, JDOMException {

        org.junit.Assume.assumeFalse( "This test class currently fails on Windows CI", true);   // This test class currently fails on Windows CI. Disable it entirely

        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initSectionManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        MaleSocket socketIfThenElse =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(socketIfThenElse);

        expressionDispatcher = new ExpressionDispatcher("IQDE321", null);
        expressionDispatcher.getSelectEnum().setEnum(ExpressionDispatcher.DispatcherState.Automatic);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionDispatcher);
        ifThenElse.getChild(0).connect(maleSocket2);

        _base = expressionDispatcher;
        _baseMaleSocket = maleSocket2;

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        expressionDispatcher.setTrainInfoFileName(_myActiveTrainFileName);
        expressionDispatcher.propertyChange(new PropertyChangeEvent(this, "ActiveTrain", "", _myActiveTrainFileName));

        setupActiveTrain();

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
//        JUnitAppender.clearBacklog();   // REMOVE THIS!!!
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
