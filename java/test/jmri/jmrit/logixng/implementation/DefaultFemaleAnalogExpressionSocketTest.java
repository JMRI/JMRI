package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.AnalogExpressionMemory;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;

/**
 * Test DefaultFemaleAnalogExpressionSocket
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleAnalogExpressionSocketTest extends FemaleSocketTestBase {

    private ConditionalNG _conditionalNG;
    private String _memorySystemName;
    private Memory _memory;
    private MyAnalogExpressionMemory _expression;

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(AnalogExpressionManager.class);
    }

    @Test
    public void testGetName() {
        assertTrue( "E1".equals(_femaleSocket.getName()), "String matches");
    }

    @Test
    public void testGetDescription() {
        assertTrue( "?~".equals(_femaleSocket.getShortDescription()), "String matches");
        assertTrue( "?~ E1".equals(_femaleSocket.getLongDescription()), "String matches 2");
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
    public void testSetValue() throws JmriException {
        // Every test method should have an assertion
        assertNotNull( _femaleSocket, "femaleSocket is not null");
        assertFalse( _femaleSocket.isConnected(), "femaleSocket is not connected");
        // Test evaluate() when not connected
        assertEquals( 0.0,  ((DefaultFemaleAnalogExpressionSocket)_femaleSocket).evaluate(),
            "values are equals");
        // Test evaluate() when connected
        _femaleSocket.connect(maleSocket);
        _memory.setValue(0.0);
        assertEquals( 0.0, ((DefaultFemaleAnalogExpressionSocket)_femaleSocket).evaluate(),
            "values are equals");
        _memory.setValue(1.2);
        assertEquals( 1.2, ((DefaultFemaleAnalogExpressionSocket)_femaleSocket).evaluate(),
            "values are equals");
    }

    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();

        List<Class<? extends Base>> classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.expressions.AnalogExpressionAnalogIO.class);
        classes.add(jmri.jmrit.logixng.expressions.AnalogExpressionConstant.class);
        classes.add(jmri.jmrit.logixng.expressions.AnalogExpressionLocalVariable.class);
        classes.add(jmri.jmrit.logixng.expressions.AnalogExpressionMemory.class);
        classes.add(jmri.jmrit.logixng.expressions.TimeSinceMidnight.class);
        map.put(LogixNG_Category.ITEM, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.expressions.AnalogFormula.class);
        map.put(LogixNG_Category.COMMON, classes);

        classes = new ArrayList<>();
        map.put(LogixNG_Category.OTHER, classes);

        assertTrue( isConnectionClassesEquals(map, _femaleSocket.getConnectableClasses()),
            "maps are equal");
    }

    // The minimal setup for log4J
    @Before
    @BeforeEach
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
        _expression.getSelectNamedBean().setNamedBean(_memory);
        AnalogExpressionBean otherExpression = new AnalogExpressionMemory("IQAE322", null);
        manager = InstanceManager.getDefault(AnalogExpressionManager.class);
        maleSocket = ((AnalogExpressionManager)manager).registerExpression(_expression);
        otherMaleSocket = ((AnalogExpressionManager)manager).registerExpression(otherExpression);
        _femaleSocket = new DefaultFemaleAnalogExpressionSocket(_conditionalNG, new FemaleSocketListener() {
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
    @AfterEach
    public void tearDown() {
//        JUnitAppender.clearBacklog();   // REMOVE THIS!!!!
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }



    private static class MyAnalogExpressionMemory extends AnalogExpressionMemory {

        boolean _hasBeenSetup = false;

        MyAnalogExpressionMemory(String systemName) {
            super(systemName, null);
        }

        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }

}
