package jmri.jmrit.logixng.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.StringExpressionMemory;
import jmri.util.JUnitUtil;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * Test DefaultFemaleGenericExpressionSocket.getStringSocket()
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleGenericStringExpressionSocketTest extends FemaleSocketTestBase {

    private ConditionalNG _conditionalNG;
    private String _memorySystemName;
    private Memory _memory;
    private MyStringExpressionMemory _expression;
    private DefaultFemaleGenericExpressionSocket femaleGenericSocket;
    private final AtomicBoolean _listenersAreRegistered = new AtomicBoolean(false);
    private final AtomicBoolean _listenersAreUnregistered = new AtomicBoolean(false);
    
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(StringExpressionManager.class);
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
        return new DefaultFemaleStringExpressionSocket(null, new FemaleSocketListener() {
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
        Assert.assertEquals("strings are equals", "", ((DefaultFemaleStringExpressionSocket)_femaleSocket).evaluate());
        // Test evaluate() when connected
        _femaleSocket.connect(maleSocket);
        _memory.setValue("");
        Assert.assertEquals("strings are equals", "", ((DefaultFemaleStringExpressionSocket)_femaleSocket).evaluate());
        _memory.setValue("Test");
        Assert.assertEquals("strings are equals", "Test", ((DefaultFemaleStringExpressionSocket)_femaleSocket).evaluate());
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
        Assert.assertEquals("femaleGenericSocket is null", _conditionalNG, femaleGenericSocket.getParent());
        
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
        _expression = new MyStringExpressionMemory("IQSE321");
        _expression.setMemory(_memory);
        StringExpressionMemory otherExpression = new StringExpressionMemory("IQSE322", null);
        manager = InstanceManager.getDefault(StringExpressionManager.class);
        maleSocket = ((StringExpressionManager)manager).registerExpression(_expression);
        otherMaleSocket = ((StringExpressionManager)manager).registerExpression(otherExpression);
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
        
        _femaleSocket  = femaleGenericSocket.getStringSocket(_conditionalNG);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyStringExpressionMemory extends StringExpressionMemory {
        
        private boolean _hasBeenSetup = false;
        
        public MyStringExpressionMemory(String systemName) {
            super(systemName, null);
        }
        
        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }
    
}
