package jmri.jmrit.logixng.implementation;

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
import jmri.jmrit.logixng.expressions.ExpressionTurnout;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;

/**
 * Test DefaultFemaleDigitalExpressionSocket
 *
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleDigitalExpressionSocketTest extends FemaleSocketTestBase {

    private ConditionalNG _conditionalNG;
    private MyExpressionTurnout _expression;

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(DigitalExpressionManager.class);
    }

    @Test
    public void testGetName() {
        assertTrue( "E1".equals(_femaleSocket.getName()), "String matches");
    }

    @Test
    public void testGetDescription() {
        assertTrue( "?".equals(_femaleSocket.getShortDescription()), "String matches");
        assertTrue( "? E1".equals(_femaleSocket.getLongDescription()), "String matches");
    }

    @Override
    protected FemaleSocket getFemaleSocket(String name) {
        return new DefaultFemaleDigitalExpressionSocket(null, new FemaleSocketListener() {
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
        assertFalse( ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).evaluate(), "result is false");
        // Test evaluate() when connected
        _femaleSocket.connect(maleSocket);
        assertTrue( _conditionalNG.setParentForAllChildren(new ArrayList<>()));
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        _expression.getSelectNamedBean().setNamedBean(t);
        _expression.setBeanState(ExpressionTurnout.TurnoutState.Thrown);
        t.setState(Turnout.CLOSED);
        assertFalse( ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).evaluate(), "turnout is not thrown");
        t.setState(Turnout.THROWN);
        assertTrue( ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).evaluate(), "turnout is thrown");
    }

    @Test
    public void testGetConnectableClasses() {
        Map<Category, List<Class<? extends Base>>> map = new HashMap<>();

        List<Class<? extends Base>> classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.expressions.ExpressionAudio.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionBlock.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionClock.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionConditional.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionDispatcher.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionEntryExit.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionLight.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionLocalVariable.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionMemory.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionOBlock.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionPower.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionReference.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionReporter.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionScript.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionSection.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionSensor.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionSensorEdge.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionSignalHead.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionSignalMast.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionTransit.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionTurnout.class);
        classes.add(jmri.jmrit.logixng.expressions.ExpressionWarrant.class);
        map.put(LogixNG_Category.ITEM, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.expressions.And.class);
        classes.add(jmri.jmrit.logixng.expressions.Antecedent.class);
        classes.add(jmri.jmrit.logixng.expressions.DigitalFormula.class);
        classes.add(jmri.jmrit.logixng.expressions.Not.class);
        classes.add(jmri.jmrit.logixng.expressions.Or.class);
        classes.add(jmri.jmrit.logixng.expressions.Timer.class);
        map.put(LogixNG_Category.COMMON, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.expressions.DigitalCallModule.class);
        map.put(LogixNG_Category.FLOW_CONTROL, classes);

        classes = new ArrayList<>();
        classes.add(jmri.jmrit.logixng.expressions.ConnectionName.class);
        classes.add(jmri.jmrit.logixng.expressions.False.class);
        classes.add(jmri.jmrit.logixng.expressions.FileAsFlag.class);
        classes.add(jmri.jmrit.logixng.expressions.Hold.class);
        classes.add(jmri.jmrit.logixng.expressions.LastResultOfDigitalExpression.class);
        classes.add(jmri.jmrit.logixng.expressions.LogData.class);
        classes.add(jmri.jmrit.logixng.expressions.TriggerOnce.class);
        classes.add(jmri.jmrit.logixng.expressions.True.class);
        map.put(LogixNG_Category.OTHER, classes);

        if (jmri.util.SystemType.isLinux()) {
            classes = new ArrayList<>();
            classes.add(jmri.jmrit.logixng.expressions.ExpressionLinuxLinePower.class);
            map.put(LogixNG_Category.LINUX, classes);
        }

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
        _expression = new MyExpressionTurnout("IQDE321");
        ExpressionTurnout otherExpression = new ExpressionTurnout("IQDE322", null);
        manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        maleSocket = ((DigitalExpressionManager)manager).registerExpression(_expression);
        otherMaleSocket = ((DigitalExpressionManager)manager).registerExpression(otherExpression);
        _femaleSocket = new DefaultFemaleDigitalExpressionSocket(_conditionalNG, new FemaleSocketListener() {
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
//        JUnitAppender.clearBacklog();   // REMOVE THIS!!!
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }



    private static class MyExpressionTurnout extends ExpressionTurnout {

        boolean _hasBeenSetup = false;

        MyExpressionTurnout(String systemName) {
            super(systemName, null);
        }

        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }

}
