package jmri.jmrit.logixng.analog.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.FemaleSocketTestBase;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionMemory;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.AnalogExpressionBean;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.analog.actions.AnalogActionMemory;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * Test DefaultFemaleAnalogExpressionSocket
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleAnalogExpressionSocketTest extends FemaleSocketTestBase {

    private String _memorySystemName;
    private Memory _memory;
    private MyAnalogExpressionMemory _expression;
    
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetName() {
        Assert.assertTrue("String matches", "E1".equals(femaleSocket.getName()));
    }
    
    @Test
    public void testGetDescription() {
        Assert.assertTrue("String matches", "?~".equals(femaleSocket.getShortDescription()));
        Assert.assertTrue("String matches", "?~ E1".equals(femaleSocket.getLongDescription()));
    }
    
    @Override
    protected boolean hasSocketBeenSetup() {
        return _expression._hasBeenSetup;
    }
    
    @Test
    public void testSystemName() {
        Assert.assertEquals("String matches", "IQAE10", femaleSocket.getExampleSystemName());
        Assert.assertEquals("String matches", "IQAE:0001", femaleSocket.getNewSystemName());
    }
    
    @Test
    public void testSetValue() throws SocketAlreadyConnectedException {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", femaleSocket.isConnected());
        // Test evaluate() when not connected
        Assert.assertTrue("values are equals", 0.0 == ((DefaultFemaleAnalogExpressionSocket)femaleSocket).evaluate());
        // Test evaluate() when connected
        femaleSocket.connect(maleSocket);
        _memory.setValue(0.0);
        Assert.assertTrue("values are equals", 0.0 == ((DefaultFemaleAnalogExpressionSocket)femaleSocket).evaluate());
        _memory.setValue(1.2);
        Assert.assertTrue("values are equals", 1.2 == ((DefaultFemaleAnalogExpressionSocket)femaleSocket).evaluate());
    }
    
    @Test
    public void testReset() throws SocketAlreadyConnectedException {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", femaleSocket.isConnected());
        // Test reset() when not connected
        ((DefaultFemaleAnalogExpressionSocket)femaleSocket).reset();
        // Test reset() when connected
        femaleSocket.connect(maleSocket);
        ((DefaultFemaleAnalogExpressionSocket)femaleSocket).reset();
    }
    
    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();
        
        List<Class<? extends Base>> classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.analog.expressions.AnalogExpressionMemory.class);
        map.put(Category.ITEM, classes);
        
        classes = new ArrayList<>();
        map.put(Category.COMMON, classes);
        
        classes = new ArrayList<>();
        map.put(Category.OTHER, classes);
        
        classes = new ArrayList<>();
        map.put(Category.EXRAVAGANZA, classes);
        
        Assert.assertTrue("maps are equal",
                isConnectionClassesEquals(map, femaleSocket.getConnectableClasses()));
    }
    
    @Test
    public void testGetNewObjectBasedOnTemplate() {
        thrown.expect(UnsupportedOperationException.class);
        femaleSocket.getNewObjectBasedOnTemplate();
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
        _expression = new MyAnalogExpressionMemory("IQAE321");
        _expression.setMemory(_memory);
        AnalogExpressionBean otherExpression = new AnalogExpressionMemory("IQAE322", null);
        maleSocket = new DefaultMaleAnalogExpressionSocket(_expression);
        otherMaleSocket = new DefaultMaleAnalogExpressionSocket(otherExpression);
        femaleSocket = new DefaultFemaleAnalogExpressionSocket(null, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
                flag.set(true);
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                flag.set(true);
            }
        }, "E1");
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
