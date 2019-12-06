package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.AudioManager;
import jmri.Light;
import jmri.LightManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.implementation.VirtualSignalHead;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.NamedTableManager;
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
public class ExpressionReferenceTest {

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
        ExpressionReference expression = new ExpressionReference("IQDE321", null);
        
        expression.setReference("{IM1}");
        
        // Test IS
        expression.set_Is_IsNot(Is_IsNot_Enum.IS);
        
        m.setValue("Turnout 1");
        expression.setPointsTo(ExpressionReference.PointsTo.NOTHING);
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        m.setValue("");
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Nothing", expression.getLongDescription());
        
        m.setValue("Table 1");
        expression.setPointsTo(ExpressionReference.PointsTo.TABLE);
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        InstanceManager.getDefault(NamedTableManager.class).newTable("IQT1", "Table 1", 2, 3);
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Table", expression.getLongDescription());
        
        m.setValue("Audio 1");
        expression.setPointsTo(ExpressionReference.PointsTo.AUDIO);
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        InstanceManager.getDefault(AudioManager.class).newAudio("IAB1", "Audio 1");
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Audio", expression.getLongDescription());
        
        m.setValue("Light 1");
        expression.setPointsTo(ExpressionReference.PointsTo.LIGHT);
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        InstanceManager.getDefault(LightManager.class).newLight("IL1", "Light 1");
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Light", expression.getLongDescription());
        
        m.setValue("Memory 1");
        expression.setPointsTo(ExpressionReference.PointsTo.MEMORY);
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        InstanceManager.getDefault(MemoryManager.class).newMemory("IM5", "Memory 1");
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Memory", expression.getLongDescription());
        
        m.setValue("Sensor 1");
        expression.setPointsTo(ExpressionReference.PointsTo.SENSOR);
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        InstanceManager.getDefault(SensorManager.class).newSensor("IS1", "Sensor 1");
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Sensor", expression.getLongDescription());
        
        m.setValue("Signal Head 1");
        expression.setPointsTo(ExpressionReference.PointsTo.SIGNAL_HEAD);
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        SignalHead signalHeadIH1 = new VirtualSignalHead("IH1", "Signal Head 1");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH1);
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is SignalHead", expression.getLongDescription());
        
        m.setValue("IF$shsm:AAR-1946:CPL(IH1)");
        expression.setPointsTo(ExpressionReference.PointsTo.SIGNAL_MAST);
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH1)");
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is SignalMast", expression.getLongDescription());
        
        m.setValue("Turnout 1");
        expression.setPointsTo(ExpressionReference.PointsTo.TURNOUT);
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT1", "Turnout 1");
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is Turnout", expression.getLongDescription());
        
        
        // Test IS_NOT
        expression.set_Is_IsNot(Is_IsNot_Enum.IS_NOT);
        
        m.setValue("Turnout 2");
        expression.setPointsTo(ExpressionReference.PointsTo.NOTHING);
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        m.setValue("");
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Nothing", expression.getLongDescription());
        
        m.setValue("Table 2");
        expression.setPointsTo(ExpressionReference.PointsTo.TABLE);
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        InstanceManager.getDefault(NamedTableManager.class).newTable("IQT2", "Table 2", 2, 3);
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Table", expression.getLongDescription());
        
        m.setValue("Audio 2");
        expression.setPointsTo(ExpressionReference.PointsTo.AUDIO);
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        InstanceManager.getDefault(AudioManager.class).newAudio("IAB2", "Audio 2");
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Audio", expression.getLongDescription());
        
        m.setValue("Light 2");
        expression.setPointsTo(ExpressionReference.PointsTo.LIGHT);
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        InstanceManager.getDefault(LightManager.class).newLight("IL2", "Light 2");
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Light", expression.getLongDescription());
        
        m.setValue("Memory 2");
        expression.setPointsTo(ExpressionReference.PointsTo.MEMORY);
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        InstanceManager.getDefault(MemoryManager.class).newMemory("IM6", "Memory 2");
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Memory", expression.getLongDescription());
        
        m.setValue("Sensor 2");
        expression.setPointsTo(ExpressionReference.PointsTo.SENSOR);
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        InstanceManager.getDefault(SensorManager.class).newSensor("IS2", "Sensor 2");
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Sensor", expression.getLongDescription());
        
        m.setValue("Signal Head 2");
        expression.setPointsTo(ExpressionReference.PointsTo.SIGNAL_HEAD);
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        SignalHead signalHeadIH2 = new VirtualSignalHead("IH2", "Signal Head 2");
        InstanceManager.getDefault(SignalHeadManager.class).register(signalHeadIH2);
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not SignalHead", expression.getLongDescription());
        
        m.setValue("IF$shsm:AAR-1946:CPL(IH2)");
        expression.setPointsTo(ExpressionReference.PointsTo.SIGNAL_MAST);
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        InstanceManager.getDefault(SignalMastManager.class).provideSignalMast("IF$shsm:AAR-1946:CPL(IH2)");
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not SignalMast", expression.getLongDescription());
        
        m.setValue("Turnout 2");
        expression.setPointsTo(ExpressionReference.PointsTo.TURNOUT);
        Assert.assertTrue("evaluate returns true",expression.evaluate());
        InstanceManager.getDefault(TurnoutManager.class).newTurnout("IT2", "Turnout 2");
        Assert.assertFalse("evaluate returns false",expression.evaluate());
        Assert.assertEquals("Reference \"{IM1}\" is not Turnout", expression.getLongDescription());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
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
    }

    @After
    public void tearDown() {
        // this created an audio manager, clean that up
        InstanceManager.getDefault(jmri.AudioManager.class).cleanup();
        
        JUnitUtil.tearDown();
    }
    
}
