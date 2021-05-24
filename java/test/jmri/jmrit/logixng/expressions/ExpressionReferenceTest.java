package jmri.jmrit.logixng.expressions;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Reference '' is Nothing", expression2.getLongDescription());
        
        expression2 = new ExpressionReference("IQDE321", "My expression");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My expression", expression2.getUserName());
        Assert.assertEquals("String matches", "Reference '' is Nothing", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionReference("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ExpressionReference("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionReference.getChildCount());
        
        boolean hasThrown = false;
        try {
            expressionReference.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.ITEM == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testDescription() {
        Assert.assertEquals("Reference", expressionReference.getShortDescription());
        Assert.assertEquals("Reference '' is Nothing", expressionReference.getLongDescription());
    }
    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        
        Memory m = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        
        expressionReference.setReference("{IM1}");
        
        // Test IS
        expressionReference.set_Is_IsNot(Is_IsNot_Enum.Is);
        
        m.setValue("Turnout 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Nothing);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        m.setValue("");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is Nothing", expressionReference.getLongDescription());
        
        m.setValue("Table 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.LogixNGTable);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(NamedTableManager.class).newInternalTable("IQT1", "Table 1", 2, 3);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is LogixNG Table", expressionReference.getLongDescription());
        
        m.setValue("Audio 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Audio);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(AudioManager.class).newAudio("IAB1", "Audio 1");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is Audio", expressionReference.getLongDescription());
        
        m.setValue("Light 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Light);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(LightManager.class).newLight("IL1", "Light 1");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is Light", expressionReference.getLongDescription());
        
        m.setValue("Memory 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Memory);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(MemoryManager.class).newMemory("IM5", "Memory 1");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is Memory", expressionReference.getLongDescription());
        
        m.setValue("Sensor 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Sensor);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(SensorManager.class).newSensor("IS1", "Sensor 1");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is Sensor", expressionReference.getLongDescription());
        
        m.setValue("Signal Head 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SignalHead);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        SignalHead signalHeadIH1 = new VirtualSignalHead("IH1", "Signal Head 1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH1);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is SignalHead", expressionReference.getLongDescription());
        
        m.setValue("IF$shsm:AAR-1946:CPL(IH1)");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SignalMast);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is SignalMast", expressionReference.getLongDescription());
        
        m.setValue("Turnout 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Turnout);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT1", "Turnout 1");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is Turnout", expressionReference.getLongDescription());
        
        
        // Test IS_NOT
        expressionReference.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        
        m.setValue("Turnout 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Nothing);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        m.setValue("");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is not Nothing", expressionReference.getLongDescription());
        
        m.setValue("Table 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.LogixNGTable);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(NamedTableManager.class).newInternalTable("IQT2", "Table 2", 2, 3);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is not LogixNG Table", expressionReference.getLongDescription());
        
        m.setValue("Audio 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Audio);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(AudioManager.class).newAudio("IAB2", "Audio 2");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is not Audio", expressionReference.getLongDescription());
        
        m.setValue("Light 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Light);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(LightManager.class).newLight("IL2", "Light 2");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is not Light", expressionReference.getLongDescription());
        
        m.setValue("Memory 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Memory);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(MemoryManager.class).newMemory("IM6", "Memory 2");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is not Memory", expressionReference.getLongDescription());
        
        m.setValue("Sensor 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Sensor);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(SensorManager.class).newSensor("IS2", "Sensor 2");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is not Sensor", expressionReference.getLongDescription());
        
        m.setValue("Signal Head 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SignalHead);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        SignalHead signalHeadIH2 = new VirtualSignalHead("IH2", "Signal Head 2");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH2);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is not SignalHead", expressionReference.getLongDescription());
        
        m.setValue("IF$shsm:AAR-1946:CPL(IH2)");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SignalMast);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH2)");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is not SignalMast", expressionReference.getLongDescription());
        
        m.setValue("Turnout 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.Turnout);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT2", "Turnout 2");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference {IM1} is not Turnout", expressionReference.getLongDescription());
    }
    
    @Test
    public void testSetReferenceException() {
        String reference = "{IM1}";
        // Test setScript() when listeners are registered
        Assert.assertNotNull("Reference is not null", reference);
        expressionReference.setReference(reference);
        Assert.assertNotNull("Reference is not null", expressionReference.getReference());
        expressionReference.registerListeners();
        boolean thrown = false;
        try {
            expressionReference.setReference((String)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
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
    public void testEnableAndEvaluate() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }
    
    @Test
    @Override
    public void testDebugConfig() {
        // Not implemented.
        // This method is implemented for other digital expressions so no need
        // to add support here. It doesn't need to be tested for every digital
        // expression.
    }
    
    // The minimal setup for log4J
    @Before
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
        
        _category = Category.ITEM;
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
        
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        
        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
        
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
}
