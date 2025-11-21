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
import jmri.jmrit.logixng.expressions.StringExpressionMemory;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;

/**
 * Test DefaultFemaleStringExpressionSocket
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleStringExpressionSocketTest extends FemaleSocketTestBase {

    private ConditionalNG _conditionalNG;
    private String _memorySystemName;
    private Memory _memory;
    private MyStringExpressionMemory _expression;

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(StringExpressionManager.class);
    }

    @Test
    public void testGetName() {
        assertTrue( "E1".equals(_femaleSocket.getName()), "String matches");
    }

    @Test
    public void testGetDescription() {
        assertTrue( "?s".equals(_femaleSocket.getShortDescription()), "String matches");
        assertTrue( "?s E1".equals(_femaleSocket.getLongDescription()), "String matches 2");
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
    public void testSetValue() throws JmriException {
        // Every test method should have an assertion
        assertNotNull( _femaleSocket, "femaleSocket is not null");
        assertFalse( _femaleSocket.isConnected(), "femaleSocket is not connected");
        // Test evaluate() when not connected
        assertEquals( "", ((DefaultFemaleStringExpressionSocket)_femaleSocket).evaluate(), "strings are equals");
        // Test evaluate() when connected
        _femaleSocket.connect(maleSocket);
        _memory.setValue("");
        assertEquals( "", ((DefaultFemaleStringExpressionSocket)_femaleSocket).evaluate(), "strings are equals 2");
        _memory.setValue("Test");
        assertEquals( "Test", ((DefaultFemaleStringExpressionSocket)_femaleSocket).evaluate(), "strings are equals 3");
    }

    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();

        List<Class<? extends Base>> classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.expressions.StringExpressionConstant.class);
        classes.add(jmri.jmrit.logixng.expressions.StringExpressionMemory.class);
        classes.add(jmri.jmrit.logixng.expressions.StringExpressionStringIO.class);
        map.put(LogixNG_Category.ITEM, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.expressions.StringFormula.class);
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
        _expression = new MyStringExpressionMemory("IQSE321");
        _expression.getSelectNamedBean().setNamedBean(_memory);
        StringExpressionMemory otherExpression = new StringExpressionMemory("IQSE322", null);
        manager = InstanceManager.getDefault(StringExpressionManager.class);
        maleSocket = ((StringExpressionManager)manager).registerExpression(_expression);
        otherMaleSocket = ((StringExpressionManager)manager).registerExpression(otherExpression);
        _femaleSocket = new DefaultFemaleStringExpressionSocket(_conditionalNG, new FemaleSocketListener() {
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
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }



    private static class MyStringExpressionMemory extends StringExpressionMemory {

        boolean _hasBeenSetup = false;

        MyStringExpressionMemory(String systemName) {
            super(systemName, null);
        }

        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }

}
