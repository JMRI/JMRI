package jmri.jmrit.logixng.analog.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.FemaleSocketTestBase;
import jmri.jmrit.logixng.analog.actions.AnalogActionMemory;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.AnalogActionBean;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleAnalogActionSocketTest extends FemaleSocketTestBase {

    private String _memorySystemName;
    private Memory _memory;
    private MyAnalogActionMemory _action;
    
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testBundleClass() {
        Assert.assertEquals("bundle is correct", "Test Bundle bb aa cc", Bundle.getMessage("TestBundle", "aa", "bb", "cc"));
        Assert.assertEquals("bundle is correct", "Generic", Bundle.getMessage(Locale.US, "SocketTypeGeneric"));
        Assert.assertEquals("bundle is correct", "Test Bundle bb aa cc", Bundle.getMessage(Locale.US, "TestBundle", "aa", "bb", "cc"));
    }
    
    @Test
    public void testGetName() {
        Assert.assertTrue("String matches", "A1".equals(femaleSocket.getName()));
    }
    
    @Test
    public void testGetDescription() {
        Assert.assertEquals("String matches", "!~", femaleSocket.getShortDescription());
        Assert.assertEquals("String matches", "!~ A1", femaleSocket.getLongDescription());
    }
    
    @Override
    protected boolean hasSocketBeenSetup() {
        return _action._hasBeenSetup;
    }
    
    @Test
    public void testSystemName() {
        Assert.assertEquals("String matches", "IQAA10", femaleSocket.getExampleSystemName());
        Assert.assertEquals("String matches", "IQAA:0001", femaleSocket.getNewSystemName());
    }
    
    @Test
    public void testSetValue() {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", femaleSocket.isConnected());
        // Test setValue() when not connected
        ((DefaultFemaleAnalogActionSocket)femaleSocket).setValue(0.0);
    }
    
    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();
        
        List<Class<? extends Base>> classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.analog.actions.AnalogActionMemory.class);
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
        _action = new MyAnalogActionMemory("IQAA321");
        _action.setMemory(_memory);
        AnalogActionBean otherAction = new AnalogActionMemory("IQAA322", null);
        maleSocket = new DefaultMaleAnalogActionSocket(_action);
        otherMaleSocket = new DefaultMaleAnalogActionSocket(otherAction);
        femaleSocket = new DefaultFemaleAnalogActionSocket(null, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
                flag.set(true);
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                flag.set(true);
            }
        }, "A1");
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyAnalogActionMemory extends AnalogActionMemory {
        
        private boolean _hasBeenSetup = false;
        
        public MyAnalogActionMemory(String systemName) {
            super(systemName, null);
        }
        
        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }
    
}
