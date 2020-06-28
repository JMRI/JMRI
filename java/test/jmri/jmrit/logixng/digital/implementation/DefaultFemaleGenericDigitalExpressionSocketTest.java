package jmri.jmrit.logixng.digital.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.digital.expressions.ExpressionTurnout;
import jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket;
import jmri.util.JUnitUtil;

import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Test DefaultFemaleGenericExpressionSocket.getDigitalSocket()
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleGenericDigitalExpressionSocketTest extends FemaleSocketTestBase {

    private MyExpressionTurnout _expression;
    
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return InstanceManager.getDefault(DigitalExpressionManager.class);
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
    public void testSetValue() throws Exception {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", _femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", _femaleSocket.isConnected());
        // Test evaluate() when not connected
        Assert.assertFalse("result is false", ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).evaluate());
        // Test evaluate() when connected
        _femaleSocket.connect(maleSocket);
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        _expression.setTurnout(t);
        _expression.setTurnoutState(ExpressionTurnout.TurnoutState.THROWN);
        t.setState(Turnout.CLOSED);
        Assert.assertFalse("turnout is not thrown", ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).evaluate());
        t.setState(Turnout.THROWN);
        Assert.assertTrue("turnout is thrown", ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).evaluate());
    }
    
    @Test
    public void testReset() throws SocketAlreadyConnectedException {
        // Every test method should have an assertion
        Assert.assertNotNull("femaleSocket is not null", _femaleSocket);
        Assert.assertFalse("femaleSocket is not connected", _femaleSocket.isConnected());
        // Test reset() when not connected
        ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).reset();
        // Test reset() when connected
        _femaleSocket.connect(maleSocket);
        ((DefaultFemaleDigitalExpressionSocket)_femaleSocket).reset();
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
        _expression = new MyExpressionTurnout("IQDE321");
        ExpressionTurnout otherExpression = new ExpressionTurnout("IQDE322", null);
        maleSocket = new DefaultMaleDigitalExpressionSocket(_expression);
        otherMaleSocket = new DefaultMaleDigitalExpressionSocket(otherExpression);
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
                }, "E1").getDigitalSocket();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyExpressionTurnout extends ExpressionTurnout {
        
        private boolean _hasBeenSetup = false;
        
        public MyExpressionTurnout(String systemName) {
            super(systemName, null);
        }
        
        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
    }
    
}
