package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.AudioManager;
import jmri.LightManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test ExpressionReference
 *
 * @author Daniel Bergqvist 2019
 */
public class ExpressionReferenceTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionReference expressionReference;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;


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
        DigitalExpressionBean childExpression = new True("IQDE999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format("Reference '' is Nothing ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Reference '' is Nothing ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionReference(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        ExpressionReference expression2;

        expression2 = new ExpressionReference("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Reference '' is Nothing", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionReference("IQDE321", "My expression");
        assertNotNull( expression2, "object exists");
        assertEquals( "My expression", expression2.getUserName(), "Username matches");
        assertEquals( "Reference '' is Nothing", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            // Illegal system name
            ExpressionReference eRef = new ExpressionReference("IQE55:12:XY11", null);
            fail("eref created: " + eRef.toString() );
        });
        assertNotNull(ex, "Expected exception thrown");

        ex = assertThrows( IllegalArgumentException.class, () -> {
            // Illegal system name
            ExpressionReference eRef = new ExpressionReference("IQE55:12:XY11", "A name");
            fail("eref created: " + eRef.toString() );
        });
        assertNotNull(ex, "Expected exception thrown");
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionReference.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionReference.getChild(0), "Exception is thrown");
        assertNotNull(ex);
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        assertEquals("Reference", expressionReference.getShortDescription());
        assertEquals("Reference '' is Nothing", expressionReference.getLongDescription());
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {

        Memory m = InstanceManager.getDefault(MemoryManager.class).provide("IM1");

        expressionReference.setReference("{IM1}");

        // Test IS
        expressionReference.set_Is_IsNot(Is_IsNot_Enum.Is);

        m.setValue("Turnout 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Nothing);
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        m.setValue("");
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        assertEquals("Reference {IM1} is Nothing", expressionReference.getLongDescription());

        m.setValue("Table 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.LogixNGTable);
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        InstanceManager.getDefault(NamedTableManager.class).newInternalTable("IQT1", "Table 1", 2, 3);
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        assertEquals("Reference {IM1} is LogixNG Table", expressionReference.getLongDescription());

        m.setValue("Audio 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Audio);
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        InstanceManager.getDefault(AudioManager.class).newAudio("IAB1", "Audio 1");
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        assertEquals("Reference {IM1} is Audio", expressionReference.getLongDescription());

        m.setValue("Light 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Light);
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        InstanceManager.getDefault(LightManager.class).newLight("IL1", "Light 1");
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        assertEquals("Reference {IM1} is Light", expressionReference.getLongDescription());

        m.setValue("Memory 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Memory);
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        InstanceManager.getDefault(MemoryManager.class).newMemory("IM5", "Memory 1");
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        assertEquals("Reference {IM1} is Memory", expressionReference.getLongDescription());

        m.setValue("Sensor 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Sensor);
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        InstanceManager.getDefault(SensorManager.class).newSensor("IS1", "Sensor 1");
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        assertEquals("Reference {IM1} is Sensor", expressionReference.getLongDescription());

        m.setValue("Signal Head 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SignalHead);
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        SignalHead signalHeadIH1 = new VirtualSignalHead("IH1", "Signal Head 1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH1);
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        assertEquals("Reference {IM1} is SignalHead", expressionReference.getLongDescription());

        m.setValue("IF$shsm:AAR-1946:CPL(IH1)");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SignalMast);
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        assertEquals("Reference {IM1} is SignalMast", expressionReference.getLongDescription());

        m.setValue("Turnout 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Turnout);
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT1", "Turnout 1");
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        assertEquals("Reference {IM1} is Turnout", expressionReference.getLongDescription());


        // Test IS_NOT
        expressionReference.set_Is_IsNot(Is_IsNot_Enum.IsNot);

        m.setValue("Turnout 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Nothing);
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        m.setValue("");
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        assertEquals("Reference {IM1} is not Nothing", expressionReference.getLongDescription());

        m.setValue("Table 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.LogixNGTable);
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        InstanceManager.getDefault(NamedTableManager.class).newInternalTable("IQT2", "Table 2", 2, 3);
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        assertEquals("Reference {IM1} is not LogixNG Table", expressionReference.getLongDescription());

        m.setValue("Audio 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Audio);
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        InstanceManager.getDefault(AudioManager.class).newAudio("IAB2", "Audio 2");
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        assertEquals("Reference {IM1} is not Audio", expressionReference.getLongDescription());

        m.setValue("Light 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Light);
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        InstanceManager.getDefault(LightManager.class).newLight("IL2", "Light 2");
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        assertEquals("Reference {IM1} is not Light", expressionReference.getLongDescription());

        m.setValue("Memory 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Memory);
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        InstanceManager.getDefault(MemoryManager.class).newMemory("IM6", "Memory 2");
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        assertEquals("Reference {IM1} is not Memory", expressionReference.getLongDescription());

        m.setValue("Sensor 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Sensor);
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        InstanceManager.getDefault(SensorManager.class).newSensor("IS2", "Sensor 2");
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        assertEquals("Reference {IM1} is not Sensor", expressionReference.getLongDescription());

        m.setValue("Signal Head 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SignalHead);
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        SignalHead signalHeadIH2 = new VirtualSignalHead("IH2", "Signal Head 2");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH2);
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        assertEquals("Reference {IM1} is not SignalHead", expressionReference.getLongDescription());

        m.setValue("IF$shsm:AAR-1946:CPL(IH2)");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SignalMast);
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH2)");
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        assertEquals("Reference {IM1} is not SignalMast", expressionReference.getLongDescription());

        m.setValue("Turnout 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Turnout);
        assertTrue( expressionReference.evaluate(), "evaluate returns true");
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT2", "Turnout 2");
        assertFalse( expressionReference.evaluate(), "evaluate returns false");
        assertEquals("Reference {IM1} is not Turnout", expressionReference.getLongDescription());
        
        // Potentially no Audio Device installed
        JUnitAppender.suppressWarnMessageStartsWith("Error initialising JOAL");
    }

    @Test
    public void testSetReferenceException() {
        String reference = "{IM1}";
        // Test setScript() when listeners are registered
        expressionReference.setReference(reference);
        assertNotNull( expressionReference.getReference(), "Reference is not null");
        expressionReference.registerListeners();
        RuntimeException ex = assertThrows( RuntimeException.class, () ->
            expressionReference.setReference(null), "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setReference must not be called when listeners are registered");
    }

    @Test
    public void testRegisterListeners() {
        // Test registerListeners() when the ExpressionReference has no reference
        conditionalNG.setEnabled(false);
        expressionReference.setReference(null);
        conditionalNG.setEnabled(true);
    }

    @Test
    @Override
    @Disabled("Not implemented")
    public void testEnableAndEvaluate() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }

    @Test
    @Override
    @Disabled("Not implemented")
    public void testDebugConfig() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }

    @Before
    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initReporterManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initSectionManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDefaultSignalMastManager();
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
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);

        expressionReference = new ExpressionReference("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionReference);
        ifThenElse.getChild(0).connect(maleSocket2);

        _base = expressionReference;
        _baseMaleSocket = maleSocket2;

        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);

        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();

        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
