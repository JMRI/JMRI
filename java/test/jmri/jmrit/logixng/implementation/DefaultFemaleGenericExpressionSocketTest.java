package jmri.jmrit.logixng.implementation;

import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.FemaleSocketTestBase;
import jmri.jmrit.logixng.FemaleGenericExpressionSocket;
import jmri.jmrit.logixng.FemaleGenericExpressionSocket.SocketType;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.implementation.DefaultMaleDigitalExpressionSocket;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import jmri.jmrit.logixng.digital.expressions.ExpressionTurnout;

/**
 * Test DefaultFemaleGenericExpressionSocket
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleGenericExpressionSocketTest extends FemaleSocketTestBase {

    private MyExpressionTurnout _expression;
    private FemaleGenericExpressionSocket femaleGenericSocket;
    
    @Test
    @Override
    public void testSetParentForAllChildren() throws SocketAlreadyConnectedException {
        // This female socket has child female sockets, which requires special treatment
        Assert.assertFalse("femaleSocket is not connected", femaleSocket.isConnected());
        femaleSocket.setParentForAllChildren();
        Assert.assertNull("malesocket.getParent() is null", maleSocket.getParent());
        femaleSocket.connect(maleSocket);
        femaleSocket.setParentForAllChildren();
        Assert.assertEquals("malesocket.getParent() is femaleSocket",
                femaleGenericSocket.getCurrentActiveSocket(),
                maleSocket.getParent());
    }
    
    @Test
    public void testSocketType() {
        Assert.assertEquals("strings are equal",
                Bundle.getMessage("SocketTypeDigital"),
                FemaleGenericExpressionSocket.SocketType.DIGITAL.toString());
        Assert.assertEquals("strings are equal",
                Bundle.getMessage("SocketTypeAnalog"),
                FemaleGenericExpressionSocket.SocketType.ANALOG.toString());
        Assert.assertEquals("strings are equal",
                Bundle.getMessage("SocketTypeString"),
                FemaleGenericExpressionSocket.SocketType.STRING.toString());
        Assert.assertEquals("strings are equal",
                Bundle.getMessage("SocketTypeGeneric"),
                FemaleGenericExpressionSocket.SocketType.GENERIC.toString());
    }
    
    @Test
    public void testGetName() {
        Assert.assertTrue("String matches", "E1".equals(femaleSocket.getName()));
    }
    
    @Test
    public void testGetDescription() {
        Assert.assertTrue("String matches", "?".equals(femaleSocket.getShortDescription()));
        Assert.assertTrue("String matches", "? E1".equals(femaleSocket.getLongDescription()));
    }
    
    @Override
    protected boolean hasSocketBeenSetup() {
        return _expression._hasBeenSetup;
    }
    
    // DefaultFemaleGenericExpressionSocket is a virtual female socket that
    // is not used directly, but has inner private classes that implements the
    // female sockets. See the classes AnalogSocket, DigitalSocket and StringSocket.
    @Ignore("DefaultFemaleGenericExpressionSocket does not tell the listeners")
    @Test
    @Override
    public void testConnect() {
        // Do nothing
    }
    
    // DefaultFemaleGenericExpressionSocket is a virtual female socket that
    // is not used directly, but has inner private classes that implements the
    // female sockets. See the classes AnalogSocket, DigitalSocket and StringSocket.
    @Ignore("DefaultFemaleGenericExpressionSocket does not tell the listeners")
    @Test
    @Override
    public void testDisconnect() throws SocketAlreadyConnectedException {
        // Do nothing
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
        femaleGenericSocket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
                flag.set(true);
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                flag.set(true);
            }
        }, "E1");
        femaleSocket = femaleGenericSocket;
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
