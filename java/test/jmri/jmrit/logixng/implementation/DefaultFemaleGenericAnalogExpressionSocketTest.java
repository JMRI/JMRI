package jmri.jmrit.logixng.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.AnalogExpressionMemory;
import jmri.util.JUnitUtil;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * Test DefaultFemaleGenericExpressionSocket.getAnalogSocket()
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleGenericAnalogExpressionSocketTest extends FemaleSocketTestBase {

    private ConditionalNG _conditionalNG;
    private String _memorySystemName;
    private Memory _memory;
    private MyAnalogExpressionMemory _expression;
    private DefaultFemaleGenericExpressionSocket femaleGenericSocket;
    private final AtomicBoolean _listenersAreRegistered = new AtomicBoolean(false);
    private final AtomicBoolean _listenersAreUnregistered = new AtomicBoolean(false);
    
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(AnalogExpressionManager.class);
    }
    
    @Test
    @Override
    public void testSWISystemName() {
        // Different types of beans may be able to connect to a generic socket, which makes this test impossible
    }
    
    @Test
    public void testGetName() {
        Assert.assertTrue("String matches", "E1".equals(_femaleSocket.getName()));
    }
    
    @Test
    public void testGetDescription() {
        Assert.assertTrue("String matches", "?*".equals(_femaleSocket.getShortDescription()));
        Assert.assertTrue("String matches", "?* E1".equals(_femaleSocket.getLongDescription()));
    }
    
    @Override
    protected FemaleSocket getFemaleSocket(String name) {
        return new DefaultFemaleAnalogExpressionSocket(null, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
            }

            @Override
            public void disconnected(FemaleSocket socket) {
            }
        }, name);
    }
    
    @Override
    protected boolean hasSocketBeenSetup() {
        return _expression._hasBeenSetup;
    }
    
    @Test
    public void testSetValue() throws Exception {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", _femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", _femaleSocket.isConnected());
        // Test evaluate() when not connected
        Assert.assertTrue("values are equals", 0.0 == ((DefaultFemaleAnalogExpressionSocket)_femaleSocket).evaluate());
        // Test evaluate() when connected
        _femaleSocket.connect(maleSocket);
        _memory.setValue(0.0);
        Assert.assertTrue("values are equals", 0.0 == ((DefaultFemaleAnalogExpressionSocket)_femaleSocket).evaluate());
        _memory.setValue(1.2);
        Assert.assertTrue("values are equals", 1.2 == ((DefaultFemaleAnalogExpressionSocket)_femaleSocket).evaluate());
    }
    
    @Test
    public void testParent() {
        Base base = Mockito.mock(Base.class);
        _femaleSocket.setParent(base);
        Assert.assertNotNull("_femaleSocket.getParent() is not null", base);
        Assert.assertEquals("femaleGenericSocket has the same parent as _femaleSocket",
                base, femaleGenericSocket.getParent());
        
        _femaleSocket.setParent(_conditionalNG);
        Assert.assertEquals("_femaleSocket.getParent() is _conditionalNG", _conditionalNG, _femaleSocket.getParent());
        Assert.assertEquals("femaleGenericSocket is _conditionalNG", _conditionalNG, femaleGenericSocket.getParent());
        
        femaleGenericSocket.setParent(base);
        Assert.assertEquals("femaleGenericSocket.getParent() is base",
                base, _femaleSocket.getParent());
        Assert.assertEquals("_femaleSocket.getParent() is base",
                base, femaleGenericSocket.getParent());
    }
    
    @Test
    public void testListeners() {
        _listenersAreRegistered.set(false);
        ((AbstractFemaleSocket)_femaleSocket).registerListeners();
        Assert.assertTrue("listeners are registered", _listenersAreRegistered.get());
        
        _listenersAreUnregistered.set(false);
        ((AbstractFemaleSocket)_femaleSocket).unregisterListeners();
        Assert.assertTrue("listeners are unregistered", _listenersAreUnregistered.get());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        _conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        flag = new AtomicBoolean();
        errorFlag = new AtomicBoolean();
        _memorySystemName = "IM1";
        _memory = InstanceManager.getDefault(MemoryManager.class).provide(_memorySystemName);
        _expression = new MyAnalogExpressionMemory("IQAE321");
        _expression.setMemory(_memory);
        AnalogExpressionBean otherExpression = new AnalogExpressionMemory("IQAE322", null);
        manager = InstanceManager.getDefault(AnalogExpressionManager.class);
        maleSocket = ((AnalogExpressionManager)manager).registerExpression(_expression);
        otherMaleSocket = ((AnalogExpressionManager)manager).registerExpression(otherExpression);
        femaleGenericSocket = new DefaultFemaleGenericExpressionSocket(
                FemaleGenericExpressionSocket.SocketType.GENERIC,
                _conditionalNG,
                new FemaleSocketListener() {
                    @Override
                    public void connected(FemaleSocket socket) {
                        flag.set(true);
                    }

                    @Override
                    public void disconnected(FemaleSocket socket) {
                        flag.set(true);
                    }
                }, "E1") {
                    @Override
                    public void registerListeners() {
                        _listenersAreRegistered.set(true);
                    }

                    /**
                     * Register listeners if this object needs that.
                     */
                    @Override
                    public void unregisterListeners() {
                        _listenersAreUnregistered.set(true);
                    }
                };
        
        _femaleSocket  = femaleGenericSocket.getAnalogSocket(_conditionalNG);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyAnalogExpressionMemory extends AnalogExpressionMemory {
        
        private boolean _hasBeenSetup = false;
        
        public MyAnalogExpressionMemory(String systemName) {
            super(systemName, null);
        }
        
        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }
    
}
