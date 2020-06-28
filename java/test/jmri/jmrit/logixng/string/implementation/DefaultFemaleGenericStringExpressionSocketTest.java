package jmri.jmrit.logixng.string.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket;
import jmri.jmrit.logixng.string.expressions.StringExpressionMemory;
import jmri.util.JUnitUtil;

import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Test DefaultFemaleGenericExpressionSocket.getStringSocket()
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleGenericStringExpressionSocketTest extends FemaleSocketTestBase {

    private String _memorySystemName;
    private Memory _memory;
    private MyStringExpressionMemory _expression;
    
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(StringExpressionManager.class);
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
    public void testReset() throws SocketAlreadyConnectedException {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", _femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", _femaleSocket.isConnected());
        // Test reset() when not connected
        ((DefaultFemaleStringExpressionSocket)_femaleSocket).reset();
        // Test reset() when connected
        _femaleSocket.connect(maleSocket);
        ((DefaultFemaleStringExpressionSocket)_femaleSocket).reset();
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        flag = new AtomicBoolean();
        errorFlag = new AtomicBoolean();
        _memorySystemName = "IM1";
        _memory = InstanceManager.getDefault(MemoryManager.class).provide(_memorySystemName);
        _expression = new MyStringExpressionMemory("IQSE321");
        _expression.setMemory(_memory);
        StringExpressionMemory otherExpression = new StringExpressionMemory("IQSE322", null);
        maleSocket = new DefaultMaleStringExpressionSocket(_expression);
        otherMaleSocket = new DefaultMaleStringExpressionSocket(otherExpression);
        _femaleSocket = new DefaultFemaleGenericExpressionSocket(
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
                }, "E1").getStringSocket();
    }

    @After
    public void tearDown() {
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
