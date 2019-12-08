package jmri.jmrit.logixng.digital.expressions;

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
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.jmrit.logixng.digital.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
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
    public String getExpectedPrintedTree() {
        return String.format("Reference \"Not selected\" is Nothing%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         If E then A1 else A2%n" +
                "            ? E%n" +
                "               Reference \"Not selected\" is Nothing%n" +
                "            ! A1%n" +
                "               Set the atomic boolean to true%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionReference(systemName, null);
    }
    
    @Test
    public void testCtor() {
        ExpressionReference t = new ExpressionReference("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        ExpressionReference expressionReference = new ExpressionReference("IQDE321", null);
        Assert.assertEquals("Reference", expressionReference.getShortDescription());
        Assert.assertEquals("Reference \"Not selected\" is Nothing", expressionReference.getLongDescription());
    }
    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        
        Memory m = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        ExpressionReference expressionReference = new ExpressionReference("IQDE321", null);
        
        expressionReference.setReference("{IM1}");
        
        // Test IS
        expressionReference.set_Is_IsNot(Is_IsNot_Enum.IS);
        
        m.setValue("Turnout 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.NOTHING);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        m.setValue("");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Nothing", expressionReference.getLongDescription());
        
        m.setValue("Table 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.TABLE);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(NamedTableManager.class).newTable("IQT1", "Table 1", 2, 3);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Table", expressionReference.getLongDescription());
        
        m.setValue("Audio 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.AUDIO);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(AudioManager.class).newAudio("IAB1", "Audio 1");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Audio", expressionReference.getLongDescription());
        
        m.setValue("Light 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.LIGHT);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(LightManager.class).newLight("IL1", "Light 1");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Light", expressionReference.getLongDescription());
        
        m.setValue("Memory 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.MEMORY);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(MemoryManager.class).newMemory("IM5", "Memory 1");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Memory", expressionReference.getLongDescription());
        
        m.setValue("Sensor 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SENSOR);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(SensorManager.class).newSensor("IS1", "Sensor 1");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Sensor", expressionReference.getLongDescription());
        
        m.setValue("Signal Head 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SIGNAL_HEAD);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        SignalHead signalHeadIH1 = new VirtualSignalHead("IH1", "Signal Head 1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH1);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is SignalHead", expressionReference.getLongDescription());
        
        m.setValue("IF$shsm:AAR-1946:CPL(IH1)");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SIGNAL_MAST);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is SignalMast", expressionReference.getLongDescription());
        
        m.setValue("Turnout 1");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.TURNOUT);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT1", "Turnout 1");
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Turnout", expressionReference.getLongDescription());
        
        
        // Test IS_NOT
        expressionReference.set_Is_IsNot(Is_IsNot_Enum.IS_NOT);
        
        m.setValue("Turnout 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.NOTHING);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        m.setValue("");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Nothing", expressionReference.getLongDescription());
        
        m.setValue("Table 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.TABLE);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(NamedTableManager.class).newTable("IQT2", "Table 2", 2, 3);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Table", expressionReference.getLongDescription());
        
        m.setValue("Audio 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.AUDIO);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(AudioManager.class).newAudio("IAB2", "Audio 2");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Audio", expressionReference.getLongDescription());
        
        m.setValue("Light 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.LIGHT);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(LightManager.class).newLight("IL2", "Light 2");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Light", expressionReference.getLongDescription());
        
        m.setValue("Memory 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.MEMORY);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(MemoryManager.class).newMemory("IM6", "Memory 2");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Memory", expressionReference.getLongDescription());
        
        m.setValue("Sensor 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SENSOR);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(SensorManager.class).newSensor("IS2", "Sensor 2");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Sensor", expressionReference.getLongDescription());
        
        m.setValue("Signal Head 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SIGNAL_HEAD);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        SignalHead signalHeadIH2 = new VirtualSignalHead("IH2", "Signal Head 2");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH2);
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not SignalHead", expressionReference.getLongDescription());
        
        m.setValue("IF$shsm:AAR-1946:CPL(IH2)");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.SIGNAL_MAST);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH2)");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not SignalMast", expressionReference.getLongDescription());
        
        m.setValue("Turnout 2");
        expressionReference.setPointsTo(ExpressionReference.PointsTo.TURNOUT);
        Assert.assertTrue("evaluate returns true",expressionReference.evaluate());
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT2", "Turnout 2");
        Assert.assertFalse("evaluate returns false",expressionReference.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Turnout", expressionReference.getLongDescription());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initRouteManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initReporterManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initWarrantManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initSectionManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initIdTagManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
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
    }

    @After
    public void tearDown() {
        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
        
        JUnitUtil.tearDown();
    }
    
}
