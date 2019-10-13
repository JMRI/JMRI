package jmri.jmrit.logixng.template;

// import java.awt.GraphicsEnvironment;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.Audio;
import jmri.AudioManager;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.LogixNG_InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test TemplateTest
 * 
 * @author Daniel Bergqvist 2018
 */
public class TemplateTest {

    @Test
    public void testBundleClass() {
        Assert.assertEquals("bundle is correct", "Test Bundle bb aa cc", Bundle.getMessage("TestBundle", "aa", "bb", "cc"));
        Assert.assertEquals("bundle is correct", "Generic", Bundle.getMessage(Locale.US, "SocketTypeGeneric"));
        Assert.assertEquals("bundle is correct", "Test Bundle bb aa cc", Bundle.getMessage(Locale.US, "TestBundle", "aa", "bb", "cc"));
    }
    
    @Test
    public void testInstanceManager() {
        InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        
        InstanceManager.setDefault(LogixNG_InstanceManager.class, new TemplateInstanceManager());
        LogixNG_InstanceManager manager = InstanceManager.getDefault(LogixNG_InstanceManager.class);
        Turnout turnout1 = manager.provide(TurnoutManager.class, Turnout.class, "IT1");
        Assert.assertEquals("Names matches", "IT1", turnout1.getSystemName());
        Turnout turnout2 = manager.provide(TurnoutManager.class, Turnout.class, "IT2");
        Assert.assertEquals("Names matches", "IT2", turnout2.getSystemName());
    }
    
    @Test
    public void testNullNamedBeansCtor() {
        
        NamedBean b = new NullAudio("AB1");
        Assert.assertNotNull("exists", b);
        b = new NullIdTag("AB1");
        Assert.assertNotNull("exists", b);
        b = new NullLight("AB1");
        Assert.assertNotNull("exists", b);
        b = new NullLogix("AB1");
        Assert.assertNotNull("exists", b);
        b = new NullMemory("AB1");
        Assert.assertNotNull("exists", b);
        b = new NullReporter("AB1");
        Assert.assertNotNull("exists", b);
        b = new NullSensor("AB1");
        Assert.assertNotNull("exists", b);
        b = new NullSignalHead("AB1");
        Assert.assertNotNull("exists", b);
        b = new NullSignalMast("AB1");
        Assert.assertNotNull("exists", b);
        b = new NullTurnout("AB1");
        Assert.assertNotNull("exists", b);
    }
    
    @Test
    public void testCtor() {
//        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
    }
    
    @Test
    public void testTemplateInstanceManager() {
        TemplateInstanceManager tim = new TemplateInstanceManager();
        Audio a = tim.provide(AudioManager.class, NullAudio.class, "IA1");
//        IdTag i = tim.provide(IdTagManager.class, NullIdTag.class, "ID1");
        Light l = tim.provide(LightManager.class, NullLight.class, "IL1");
        Logix x = tim.provide(LogixManager.class, NullLogix.class, "IX1");
        Memory m = tim.provide(MemoryManager.class, NullMemory.class, "IM1");
        OBlock ob = tim.provide(OBlockManager.class, OBlock.class, "OB1");
        Reporter r = tim.provide(ReporterManager.class, NullReporter.class, "IR1");
        Sensor s = tim.provide(SensorManager.class, NullSensor.class, "IS1");
        SignalHead sh = tim.provide(SignalHeadManager.class, NullSignalHead.class, "IH1");
        SignalMast sm = tim.provide(SignalMastManager.class, NullSignalMast.class, "IF$shsm:AAR-1946:CPL(IH1)");
//        SignalMast sm = tim.provide(SignalMastManager.class, NullSignalMast.class, "IF1");
        Turnout t = tim.provide(TurnoutManager.class, NullTurnout.class, "IT1");
        Assert.assertNotNull("Not null", a);
//        Assert.assertNotNull("Not null", i);
        Assert.assertNotNull("Not null", l);
        Assert.assertNotNull("Not null", x);
        Assert.assertNotNull("Not null", m);
        Assert.assertNotNull("Not null", ob);
        Assert.assertNotNull("Not null", r);
        Assert.assertNotNull("Not null", s);
        Assert.assertNotNull("Not null", sh);
        Assert.assertNotNull("Not null", sm);
        Assert.assertNotNull("Not null", t);
        Assert.assertTrue("Objects are the same", a.equals(tim.get(AudioManager.class, NullAudio.class, "IA1")));
//        Assert.assertTrue("Objects are the same", i.equals(tim.get(IdTagManager.class, NullIdTag.class, "ID1")));
        Assert.assertTrue("Objects are the same", l.equals(tim.get(LightManager.class, NullLight.class, "IL1")));
        Assert.assertTrue("Objects are the same", x.equals(tim.get(LogixManager.class, NullLogix.class, "IX1")));
        Assert.assertTrue("Objects are the same", m.equals(tim.get(MemoryManager.class, NullMemory.class, "IM1")));
        Assert.assertTrue("Objects are the same", ob.equals(tim.get(OBlockManager.class, OBlock.class, "OB1")));
        Assert.assertTrue("Objects are the same", r.equals(tim.get(ReporterManager.class, NullReporter.class, "IR1")));
        Assert.assertTrue("Objects are the same", s.equals(tim.get(SensorManager.class, NullSensor.class, "IS1")));
        Assert.assertTrue("Objects are the same", sh.equals(tim.get(SignalHeadManager.class, NullSignalHead.class, "IH1")));
        Assert.assertTrue("Objects are the same", sm.equals(tim.get(SignalMastManager.class, NullSignalMast.class, "IF$shsm:AAR-1946:CPL(IH1)")));
        Assert.assertTrue("Objects are the same", t.equals(tim.get(TurnoutManager.class, NullTurnout.class, "IT1")));
    }
    
    private boolean hasThrownException(Run r) {
        AtomicBoolean b = new AtomicBoolean(false);
        try {
            r.run();
        } catch (JmriException | UnsupportedOperationException e) {
            b.set(true);
        }
        return b.get();
    }
    
    private boolean hasThrownException(RunWithReturn r) {
        AtomicBoolean b = new AtomicBoolean(false);
        try {
            r.run();
        } catch (JmriException | UnsupportedOperationException e) {
            b.set(true);
        }
        return b.get();
    }
    
    @Test
    public void testNullAudio() {
        NullAudio b = new NullAudio("AB1");
        Assert.assertEquals("return value is correct", 0, b.getState());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setState(NamedBean.UNKNOWN); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.cleanup(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.stateChanged(0); }));
    }
    
    @Test
    public void testNullIdTag() {
        NullIdTag b = new NullIdTag("AB1");
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { return b.getState(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setState(NamedBean.UNKNOWN); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.load(null); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setWhereLastSeen(null); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.store(false); }));
    }
    
    @Test
    public void testNullLogix() {
        NullLogix b = new NullLogix("AB1");
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { return b.getState(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setState(NamedBean.UNKNOWN); }));
        
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.activateLogix(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.addConditional("", null); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.addConditional("", 0); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.calculateConditionals(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.deActivateLogix(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.deleteConditional(""); }));
        Assert.assertEquals("Strings matches", "Logix", b.getBeanType());
        Assert.assertEquals("Same enum", Category.ITEM, b.getCategory());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getChild(0); }));
        Assert.assertEquals("Values matches", 0, b.getChildCount());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getConditional(""); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getConditionalByNumberOrder(0); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getEnabled(); }));
        Assert.assertEquals("Same enum", Base.Lock.NONE, b.getLock());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getLongDescription(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getNewObjectBasedOnTemplate(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getNumConditionals(); }));
        Assert.assertEquals("Same value", null, b.getParent());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getShortDescription(); }));
        Assert.assertFalse("is not external", b.isExternal());
        b.registerListenersForThisClass();
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setEnabled(false); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setGuiNames(); }));
        b.setLock(Base.Lock.NONE);
        b.setParent(null);
        b.setup();
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.swapConditional(0,1); }));
        b.unregisterListenersForThisClass();
        
        b.disposeMe();
    }
    
    @Test
    public void testNullMemory() {
        NullMemory b = new NullMemory("AB1");
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { return b.getState(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setState(NamedBean.UNKNOWN); }));
    }
    
    @Test
    public void testNullReporter() {
        NullReporter b = new NullReporter("AB1");
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { return b.getState(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setState(NamedBean.UNKNOWN); }));
    }
    
    @Test
    public void testNullSensor() {
        NullSensor b = new NullSensor("AB1");
        Assert.assertEquals("return value is correct", NamedBean.UNKNOWN, b.getState());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.requestUpdateFromLayout(); }));
    }
    
    @Test
    public void testNullSignalHead() {
        NullSignalHead b = new NullSignalHead("AB1");
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { return b.getState(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setState(NamedBean.UNKNOWN); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getAppearance(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getAppearanceName(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getAppearanceName(0); }));
        Assert.assertEquals("Strings matches", "Signal Head", b.getBeanType());
        Assert.assertEquals("Same enum", Category.ITEM, b.getCategory());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getChild(0); }));
        Assert.assertEquals("Values matches", 0, b.getChildCount());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getHeld(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getLit(); }));
        Assert.assertEquals("Same enum", Base.Lock.NONE, b.getLock());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getLongDescription(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getNewObjectBasedOnTemplate(); }));
        Assert.assertEquals("Same value", null, b.getParent());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getShortDescription(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getValidStateNames(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.getValidStates(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.isAtStop(); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.isCleared(); }));
        Assert.assertFalse("is not external", b.isExternal());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.isShowingRestricting(); }));
        b.registerListenersForThisClass();
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setAppearance(0); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setHeld(false); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setLit(false); }));
        b.setLock(Base.Lock.NONE);
        b.setParent(null);
        b.setup();
        b.unregisterListenersForThisClass();
        
        b.disposeMe();
    }
    
    @Test
    public void testNullTurnout() {
        NullTurnout b = new NullTurnout("AB1");
        Assert.assertEquals("return value is correct", NamedBean.UNKNOWN, b.getState());
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.setState(NamedBean.UNKNOWN); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.forwardCommandChangeToLayout(0); }));
        Assert.assertTrue("Exception thrown", hasThrownException(() -> { b.turnoutPushbuttonLockout(false); }));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
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
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private interface Run {
        public void run() throws JmriException;
    }
    
    private interface RunWithReturn {
        public Object run() throws JmriException;
    }
    
}
