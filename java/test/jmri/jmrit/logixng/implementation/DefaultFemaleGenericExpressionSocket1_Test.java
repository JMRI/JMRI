package jmri.jmrit.logixng.implementation;

import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.FemaleGenericExpressionSocket.SocketType;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.digital.expressions.ExpressionMemory;
import jmri.jmrit.logixng.digital.expressions.ExpressionTurnout;
import jmri.jmrit.logixng.string.expressions.StringExpressionConstant;
import jmri.jmrit.logixng.string.expressions.StringExpressionMemory;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test DefaultFemaleGenericExpressionSocket
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultFemaleGenericExpressionSocket1_Test extends FemaleSocketTestBase {

    private MyExpressionTurnout _expression;
    private FemaleGenericExpressionSocket femaleGenericSocket;
    
    @Override
    protected Manager<? extends NamedBean> getManager() {
        return null;
    }
    
    @Ignore("Different types of beans may be able to connect to a generic socket, which makes this test difficult")
    @Test
    @Override
    public void testSWISystemName() {
    }
    
    @Test
    @Override
    public void testSetParentForAllChildren() throws SocketAlreadyConnectedException {
        // This female socket has child female sockets, which requires special treatment
        Assert.assertFalse("femaleSocket is not connected", _femaleSocket.isConnected());
        _femaleSocket.setParentForAllChildren();
        Assert.assertNull("malesocket.getParent() is null", maleSocket.getParent());
        _femaleSocket.connect(maleSocket);
        _femaleSocket.setParentForAllChildren();
        Assert.assertEquals("malesocket.getParent() is femaleSocket",
                femaleGenericSocket,
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
        Assert.assertTrue("String matches", "E1".equals(_femaleSocket.getName()));
    }
    
    @Test
    public void testGetDescription() {
        Assert.assertTrue("String matches", "?".equals(_femaleSocket.getShortDescription()));
        Assert.assertTrue("String matches", "? E1".equals(_femaleSocket.getLongDescription()));
    }
    
    @Override
    protected FemaleSocket getFemaleSocket(String name) {
        return new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, new FemaleSocketListener() {
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
    
    // DefaultFemaleGenericExpressionSocket is a virtual female socket that
    // is not used directly, but has inner private classes that implements the
    // female sockets. See the classes AnalogSocket, DigitalSocket and StringSocket.
    @Test
    @Override
    public void testConnect() {
        try {
            _femaleSocket.connect(maleSocket);
            
            // Try to connect twice. This should fail.
            boolean hasThrown = false;
            try {
                _femaleSocket.connect(maleSocket);
            } catch (SocketAlreadyConnectedException e2) {
                hasThrown = true;
                Assert.assertEquals("Socket is already connected", e2.getMessage());
            }
            Assert.assertTrue("Exception thrown", hasThrown);
        } catch (SocketAlreadyConnectedException e) {
            throw new RuntimeException(e);
        }
    }
    
    // DefaultFemaleGenericExpressionSocket is a virtual female socket that
    // is not used directly, but has inner private classes that implements the
    // female sockets. See the classes AnalogSocket, DigitalSocket and StringSocket.
    @Test
    @Override
    public void testDisconnect() throws SocketAlreadyConnectedException {
        // Test female generic socket
        ExpressionMemory digitalExpression = new ExpressionMemory("IQDE351", null);
        MaleSocket digitalMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(digitalExpression);
        
        DefaultFemaleGenericExpressionSocket femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        
        Assert.assertNull("_currentActiveSocket is null",
                femaleSocket.getCurrentActiveSocket());
        
        // Test disconnect() without connected socket
        femaleSocket.disconnect();
        
        Assert.assertNull("_currentActiveSocket is null",
                femaleSocket.getCurrentActiveSocket());
        
        // Test disconnect() without connected socket
        femaleSocket.connect(digitalMaleSocket);
        
        Assert.assertEquals("_currentActiveSocket is has correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalExpressionSocket",
                femaleSocket.getCurrentActiveSocket().getClass().getName());
        
        femaleSocket.disconnect();
        
        
        // Test female digital socket
        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        
        // When the SocketType is not GENERIC, _currentActiveSocket is assigned to a socket
        Assert.assertNotNull("_currentActiveSocket is not null",
                femaleSocket.getCurrentActiveSocket());
        
        // Test disconnect() without connected socket
        femaleSocket.disconnect();
        
        // When the SocketType is not GENERIC, _currentActiveSocket is assigned to a socket
        Assert.assertNotNull("_currentActiveSocket is not null",
                femaleSocket.getCurrentActiveSocket());
        
        // Test disconnect() without connected socket
        femaleSocket.connect(digitalMaleSocket);
        
        Assert.assertEquals("_currentActiveSocket is has correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalExpressionSocket",
                femaleSocket.getCurrentActiveSocket().getClass().getName());
        
        femaleSocket.disconnect();
    }
    
    private void checkConnectableClasses(FemaleSocket femaleSocket) {
        Map<Category, List<Class<? extends Base>>> classes = femaleSocket.getConnectableClasses();
        Assert.assertNotNull("classes is not null", classes);
        Assert.assertFalse("classes is not empty", classes.isEmpty());
    }
    
    @Test
    public void testReset() throws SocketAlreadyConnectedException {
        MyExpressionTurnout digitalExpression = new MyExpressionTurnout("IQDE351");
        MaleSocket digitalMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(digitalExpression);
        
        DefaultFemaleGenericExpressionSocket socket;
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        socket.connect(digitalMaleSocket);
        digitalExpression._hasBeenReset = false;
        FemaleAnalogExpressionSocket analogSocket = socket.getAnalogSocket();
        analogSocket.reset();
        Assert.assertTrue("Expression has been reset", digitalExpression._hasBeenReset);
        checkConnectableClasses(analogSocket);
        socket.disconnect();
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        socket.connect(digitalMaleSocket);
        digitalExpression._hasBeenReset = false;
        FemaleDigitalExpressionSocket digitalSocket = socket.getDigitalSocket();
        digitalSocket.reset();
        Assert.assertTrue("Expression has been reset", digitalExpression._hasBeenReset);
        checkConnectableClasses(digitalSocket);
        socket.disconnect();
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        socket.connect(digitalMaleSocket);
        digitalExpression._hasBeenReset = false;
        FemaleGenericExpressionSocket genericSocket = socket.getGenericSocket();
        genericSocket.reset();
        Assert.assertTrue("Expression has been reset", digitalExpression._hasBeenReset);
        checkConnectableClasses(genericSocket);
        socket.disconnect();
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        socket.connect(digitalMaleSocket);
        digitalExpression._hasBeenReset = false;
        FemaleStringExpressionSocket stringSocket = socket.getStringSocket();
        stringSocket.reset();
        Assert.assertTrue("Expression has been reset", digitalExpression._hasBeenReset);
        checkConnectableClasses(stringSocket);
        socket.disconnect();
    }
    
    private void testLockUnlock(FemaleSocket socket1, FemaleSocket socket2) {
        socket1.setLock(Base.Lock.NONE);
        Assert.assertEquals("Lock is correct", Base.Lock.NONE, socket2.getLock());
        socket1.setLock(Base.Lock.USER_LOCK);
        Assert.assertEquals("Lock is correct", Base.Lock.USER_LOCK, socket2.getLock());
        socket1.setLock(Base.Lock.HARD_LOCK);
        Assert.assertEquals("Lock is correct", Base.Lock.HARD_LOCK, socket2.getLock());
        
        socket2.setLock(Base.Lock.NONE);
        Assert.assertEquals("Lock is correct", Base.Lock.NONE, socket1.getLock());
        socket2.setLock(Base.Lock.USER_LOCK);
        Assert.assertEquals("Lock is correct", Base.Lock.USER_LOCK, socket1.getLock());
        socket2.setLock(Base.Lock.HARD_LOCK);
        Assert.assertEquals("Lock is correct", Base.Lock.HARD_LOCK, socket1.getLock());
    }
    
    @Test
    public void testLockUnlock() throws SocketAlreadyConnectedException {
        MyExpressionTurnout digitalExpression = new MyExpressionTurnout("IQDE351");
        MaleSocket digitalMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(digitalExpression);
        
        DefaultFemaleGenericExpressionSocket socket;
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        socket.connect(digitalMaleSocket);
        FemaleAnalogExpressionSocket analogSocket = socket.getAnalogSocket();
        testLockUnlock(socket, analogSocket);
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        socket.connect(digitalMaleSocket);
        FemaleDigitalExpressionSocket digitalSocket = socket.getDigitalSocket();
        testLockUnlock(socket, digitalSocket);
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        socket.connect(digitalMaleSocket);
        FemaleGenericExpressionSocket genericSocket = socket.getGenericSocket();
        testLockUnlock(socket, genericSocket);
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        socket.connect(digitalMaleSocket);
        FemaleStringExpressionSocket stringSocket = socket.getStringSocket();
        testLockUnlock(socket, stringSocket);
    }
    
    @Test
    public void testIsCompatibleSocket() {
        AnalogExpressionConstant analogExpression = new AnalogExpressionConstant("IQAE351", null);
        MaleSocket analogMaleSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(analogExpression);
        
        // Test female generic socket
        ExpressionMemory digitalExpression = new ExpressionMemory("IQDE351", null);
        MaleSocket digitalMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(digitalExpression);
        
        StringExpressionConstant stringExpression = new StringExpressionConstant("IQSE351", null);
        MaleSocket stringMaleSocket =
                InstanceManager.getDefault(StringExpressionManager.class).registerExpression(stringExpression);
        
        DefaultFemaleGenericExpressionSocket femaleSocket;
        
        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        FemaleAnalogExpressionSocket analogSocket = femaleSocket.getAnalogSocket();
        Assert.assertTrue("Socket is compatible",
                analogSocket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Socket is compatible",
                analogSocket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("Socket is compatible",
                analogSocket.isCompatible(stringMaleSocket));
        
        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        FemaleDigitalExpressionSocket digitalSocket = femaleSocket.getDigitalSocket();
        Assert.assertTrue("Socket is compatible",
                digitalSocket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Socket is compatible",
                digitalSocket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("Socket is compatible",
                digitalSocket.isCompatible(stringMaleSocket));
        
        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        FemaleGenericExpressionSocket genericSocket = femaleSocket.getGenericSocket();
        Assert.assertTrue("Socket is compatible",
                genericSocket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Socket is compatible",
                genericSocket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("Socket is compatible",
                genericSocket.isCompatible(stringMaleSocket));
        
        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        FemaleStringExpressionSocket stringSocket = femaleSocket.getStringSocket();
        Assert.assertTrue("Socket is compatible",
                stringSocket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Socket is compatible",
                stringSocket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("Socket is compatible",
                stringSocket.isCompatible(stringMaleSocket));
    }
    
    @Test
    public void testGetAndSetSocketType() throws SocketAlreadyConnectedException {
        boolean exceptionThrown;
        
        AnalogExpressionMemory analogExpression = new AnalogExpressionMemory("IQAE351", null);
        MaleSocket analogMaleSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(analogExpression);
        
        ExpressionMemory digitalExpression = new ExpressionMemory("IQDE351", null);
        MaleSocket digitalMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(digitalExpression);
        
        StringExpressionMemory stringExpression = new StringExpressionMemory("IQSE351", null);
        MaleSocket stringMaleSocket =
                InstanceManager.getDefault(StringExpressionManager.class).registerExpression(stringExpression);
        
        // This should work
        femaleGenericSocket.setSocketType(SocketType.ANALOG);
        femaleGenericSocket.setSocketType(SocketType.ANALOG);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.ANALOG, femaleGenericSocket.getSocketType());
        Assert.assertEquals("Active socket is correct", "jmri.jmrit.logixng.analog.implementation.DefaultFemaleAnalogExpressionSocket", femaleGenericSocket.getCurrentActiveSocket().getClass().getName());
        
        // We can't change socket type if it's connected
        femaleGenericSocket.connect(analogMaleSocket);
        exceptionThrown = false;
        try {
            femaleGenericSocket.setSocketType(SocketType.DIGITAL);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        femaleGenericSocket.disconnect();
        
        
        // This should work
        femaleGenericSocket.setSocketType(SocketType.DIGITAL);
        femaleGenericSocket.setSocketType(SocketType.DIGITAL);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.DIGITAL, femaleGenericSocket.getSocketType());
        Assert.assertEquals("Active socket is correct", "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalExpressionSocket", femaleGenericSocket.getCurrentActiveSocket().getClass().getName());
        
        // We can't change socket type if it's connected
        femaleGenericSocket.connect(digitalMaleSocket);
        exceptionThrown = false;
        try {
            femaleGenericSocket.setSocketType(SocketType.GENERIC);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        femaleGenericSocket.disconnect();
        
        
        // This should work
        femaleGenericSocket.setSocketType(SocketType.GENERIC);
        femaleGenericSocket.setSocketType(SocketType.GENERIC);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.GENERIC, femaleGenericSocket.getSocketType());
        Assert.assertNull("Active socket is null", femaleGenericSocket.getCurrentActiveSocket());
        
        // We can't change socket type if it's connected
        femaleGenericSocket.connect(stringMaleSocket);
        exceptionThrown = false;
        try {
            femaleGenericSocket.setSocketType(SocketType.STRING);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        femaleGenericSocket.disconnect();
        
        
        // This should work
        femaleGenericSocket.setSocketType(SocketType.STRING);
        femaleGenericSocket.setSocketType(SocketType.STRING);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.STRING, femaleGenericSocket.getSocketType());
        Assert.assertEquals("Active socket is correct", "jmri.jmrit.logixng.string.implementation.DefaultFemaleStringExpressionSocket", femaleGenericSocket.getCurrentActiveSocket().getClass().getName());
        
        // We can't change socket type if it's connected
        femaleGenericSocket.connect(stringMaleSocket);
        exceptionThrown = false;
        try {
            femaleGenericSocket.setSocketType(SocketType.DIGITAL);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        femaleGenericSocket.disconnect();
    }
    
    private void testGetSocketException(
            DefaultFemaleGenericExpressionSocket socket) {
        
        boolean exceptionThrown;
        
        exceptionThrown = false;
        try {
            socket.getAnalogSocket();
        } catch (RuntimeException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "internal socket cannot be set more than once", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        
        exceptionThrown = false;
        try {
            socket.getDigitalSocket();
        } catch (RuntimeException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "internal socket cannot be set more than once", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        
        exceptionThrown = false;
        try {
            socket.getGenericSocket();
        } catch (RuntimeException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "internal socket cannot be set more than once", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        
        exceptionThrown = false;
        try {
            socket.getStringSocket();
        } catch (RuntimeException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "internal socket cannot be set more than once", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
    }
    
    @Test
    public void testGetSocket() {
        DefaultFemaleGenericExpressionSocket socket;
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.ANALOG, null, null, "E");
        socket.getAnalogSocket();
        testGetSocketException(socket);
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        socket.getDigitalSocket();
        testGetSocketException(socket);
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        socket.getGenericSocket();
        testGetSocketException(socket);
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.STRING, null, null, "E");
        socket.getStringSocket();
        testGetSocketException(socket);
    }
    
    @Test
    public void testIsCompatible() {
        AnalogExpressionMemory analogExpression = new AnalogExpressionMemory("IQAE351", null);
        MaleSocket analogMaleSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(analogExpression);
        
        ExpressionMemory digitalExpression = new ExpressionMemory("IQDE351", null);
        MaleSocket digitalMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(digitalExpression);
        
        StringExpressionMemory stringExpression = new StringExpressionMemory("IQSE351", null);
        MaleSocket stringMaleSocket =
                InstanceManager.getDefault(StringExpressionManager.class).registerExpression(stringExpression);
        
        DefaultFemaleGenericExpressionSocket socket;
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.ANALOG, null, null, "E");
        Assert.assertTrue("Analog male socket is compatible", socket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Digital male socket is compatible", socket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("String male socket is compatible", socket.isCompatible(stringMaleSocket));
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, null, null, "E");
        Assert.assertTrue("Analog male socket is compatible", socket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Digital male socket is compatible", socket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("String male socket is compatible", socket.isCompatible(stringMaleSocket));
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, null, null, "E");
        Assert.assertTrue("Analog male socket is compatible", socket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Digital male socket is compatible", socket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("String male socket is compatible", socket.isCompatible(stringMaleSocket));
        
        socket = new DefaultFemaleGenericExpressionSocket(SocketType.STRING, null, null, "E");
        Assert.assertTrue("Analog male socket is compatible", socket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Digital male socket is compatible", socket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("String male socket is compatible", socket.isCompatible(stringMaleSocket));
    }
    
    @Test
    public void testDoI18N() {
        DefaultFemaleGenericExpressionSocket socket =
                new DefaultFemaleGenericExpressionSocket(SocketType.ANALOG, null, null, "E");
        Assert.assertFalse("do_i18n is false", socket.getDoI18N());
        socket.setDoI18N(true);
        Assert.assertTrue("do_i18n is true", socket.getDoI18N());
        socket.setDoI18N(false);
        Assert.assertFalse("do_i18n is false", socket.getDoI18N());
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
        maleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(_expression);
        otherMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(otherExpression);
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
        _femaleSocket = femaleGenericSocket;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyExpressionTurnout extends ExpressionTurnout {
        
        private boolean _hasBeenSetup = false;
        private boolean _hasBeenReset = false;
        
        public MyExpressionTurnout(String systemName) {
            super(systemName, null);
        }
        
        /** {@inheritDoc} */
        @Override
        public void setup() {
            _hasBeenSetup = true;
        }
        
        @Override
        public void reset() {
            _hasBeenReset = true;
        }
        
    }
    
//    private final static org.slf4j.Logger log =
//            org.slf4j.LoggerFactory.getLogger(DefaultFemaleGenericExpressionSocket1_Test.class);
}
