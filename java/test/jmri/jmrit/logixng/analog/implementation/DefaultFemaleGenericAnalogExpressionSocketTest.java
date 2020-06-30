package jmri.jmrit.logixng.analog.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.implementation.AbstractFemaleSocket;
import jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket;
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
    
    @Ignore("Different types of beans may be able to connect to a generic socket, which makes this test difficult")
    @Test
    @Override
    public void testSWISystemName() {
    }
    
    @Test
    public void testGetName() {
        Assert.assertTrue("String matches", "E1".equals(_femaleSocket.getName()));
    }
    
    @Test
    public void testGetDescription() {
        Assert.assertTrue("String matches", "?".equals(_femaleSocket.getShortDescription()));
        Assert.assertTrue("String matches", "? E1".equals(_femaleSocket.getLongDescription()));
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
    public void testReset() throws SocketAlreadyConnectedException {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", _femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", _femaleSocket.isConnected());
        // Test reset() when not connected
        ((DefaultFemaleAnalogExpressionSocket)_femaleSocket).reset();
        // Test reset() when connected
        _femaleSocket.connect(maleSocket);
        ((DefaultFemaleAnalogExpressionSocket)_femaleSocket).reset();
    }
    
    @Test
    public void testParent() {
        Base base = Mockito.mock(Base.class);
        _femaleSocket.setParent(base);
        Assert.assertNotNull("_femaleSocket.getParent() is not null", base);
        Assert.assertEquals("femaleGenericSocket has the same parent as _femaleSocket",
                base, femaleGenericSocket.getParent());
        
        _femaleSocket.setParent(null);
        Assert.assertNull("_femaleSocket.getParent() is null", _femaleSocket.getParent());
        Assert.assertNull("femaleGenericSocket is null", femaleGenericSocket.getParent());
        
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
        
        flag = new AtomicBoolean();
        errorFlag = new AtomicBoolean();
        _memorySystemName = "IM1";
        _memory = InstanceManager.getDefault(MemoryManager.class).provide(_memorySystemName);
        _expression = new MyAnalogExpressionMemory("IQAE321");
        _expression.setMemory(_memory);
        AnalogExpressionBean otherExpression = new AnalogExpressionMemory("IQAE322", null);
        maleSocket = new DefaultMaleAnalogExpressionSocket(_expression);
        otherMaleSocket = new DefaultMaleAnalogExpressionSocket(otherExpression);
        femaleGenericSocket = new DefaultFemaleGenericExpressionSocket(
                FemaleGenericExpressionSocket.SocketType.GENERIC,
                null,
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
        
        _femaleSocket  = femaleGenericSocket.getAnalogSocket();
    }

    @After
    public void tearDown() {
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
