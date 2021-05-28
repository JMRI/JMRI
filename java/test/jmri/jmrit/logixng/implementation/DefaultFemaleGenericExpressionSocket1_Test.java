package jmri.jmrit.logixng.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.FemaleGenericExpressionSocket.SocketType;
import jmri.jmrit.logixng.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.ExpressionTurnout;
import jmri.jmrit.logixng.expressions.StringExpressionConstant;
import jmri.jmrit.logixng.expressions.StringExpressionMemory;
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

    private ConditionalNG _conditionalNG;
    private MyExpressionTurnout _expression;
    private FemaleSocketListener _listener;
    private DefaultFemaleGenericExpressionSocket _femaleGenericSocket;

    @Override
    protected Manager<? extends NamedBean> getManager() {
        return null;
    }

    @Test
    @Override
    public void testSWISystemName() {
        // Different types of beans may be able to connect to a generic socket, which makes this test impossible
    }

    @Test
    @Override
    public void testSetParentForAllChildren() throws SocketAlreadyConnectedException {
        // This female socket has child female sockets, which requires special treatment
        Assert.assertFalse("femaleSocket is not connected", _femaleSocket.isConnected());
        if (! _femaleSocket.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        Assert.assertNull("malesocket.getParent() is null", maleSocket.getParent());
        _femaleSocket.connect(maleSocket);
        if (! _femaleSocket.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        Assert.assertEquals("malesocket.getParent() is femaleSocket",
                _femaleGenericSocket,
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
        Assert.assertTrue("String matches", "E".equals(_femaleSocket.getName()));
    }

    @Test
    public void testGetDescription() {
        Assert.assertEquals("String matches", "?*", _femaleSocket.getShortDescription());
        Assert.assertEquals("String matches", "?* E", _femaleSocket.getLongDescription());


        DefaultFemaleGenericExpressionSocket socket;

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        FemaleAnalogExpressionSocket analogSocket = socket.getAnalogSocket(_conditionalNG);
        Assert.assertEquals("String matches", "?*", analogSocket.getShortDescription());
        Assert.assertEquals("String matches", "?* E", analogSocket.getLongDescription());

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        FemaleDigitalExpressionSocket digitalSocket = socket.getDigitalSocket(_conditionalNG);
        Assert.assertEquals("String matches", "?*", digitalSocket.getShortDescription());
        Assert.assertEquals("String matches", "?* E", digitalSocket.getLongDescription());

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        FemaleGenericExpressionSocket genericSocket = socket.getGenericSocket(_conditionalNG);
        Assert.assertEquals("String matches", "?*", genericSocket.getShortDescription());
        Assert.assertEquals("String matches", "?* E", genericSocket.getLongDescription());

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        FemaleStringExpressionSocket stringSocket = socket.getStringSocket(_conditionalNG);
        Assert.assertEquals("String matches", "?*", stringSocket.getShortDescription());
        Assert.assertEquals("String matches", "?* E", stringSocket.getLongDescription());
    }

    @Override
    protected FemaleSocket getFemaleSocket(String name) {
        return new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, new FemaleSocketListener() {
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

        DefaultFemaleGenericExpressionSocket femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, _listener, "E");

        Assert.assertNull("_currentActiveSocket is null",
                femaleSocket.getCurrentActiveSocket());

        // Test disconnect() without connected socket
        femaleSocket.disconnect();

        Assert.assertNull("_currentActiveSocket is null",
                femaleSocket.getCurrentActiveSocket());

        // Test disconnect() without connected socket
        femaleSocket.connect(digitalMaleSocket);

        Assert.assertEquals("_currentActiveSocket is has correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                femaleSocket.getCurrentActiveSocket().getClass().getName());

        femaleSocket.disconnect();


        // Test female digital socket
        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");

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
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                femaleSocket.getCurrentActiveSocket().getClass().getName());

        femaleSocket.disconnect();
    }

    private void checkConnectableClasses(FemaleSocket femaleSocket) {
        Map<Category, List<Class<? extends Base>>> classes = femaleSocket.getConnectableClasses();
        Assert.assertNotNull("classes is not null", classes);
        Assert.assertFalse("classes is not empty", classes.isEmpty());
    }

    @Test
    public void testConnectableClasses() throws SocketAlreadyConnectedException {
        MyExpressionTurnout digitalExpression = new MyExpressionTurnout("IQDE351");
        MaleSocket digitalMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(digitalExpression);

        DefaultFemaleGenericExpressionSocket socket;

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        socket.connect(digitalMaleSocket);
        FemaleAnalogExpressionSocket analogSocket = socket.getAnalogSocket(_conditionalNG);
        checkConnectableClasses(analogSocket);
        socket.disconnect();

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        socket.connect(digitalMaleSocket);
        FemaleDigitalExpressionSocket digitalSocket = socket.getDigitalSocket(_conditionalNG);
        checkConnectableClasses(digitalSocket);
        socket.disconnect();

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        socket.connect(digitalMaleSocket);
        FemaleGenericExpressionSocket genericSocket = socket.getGenericSocket(_conditionalNG);
        checkConnectableClasses(genericSocket);
        socket.disconnect();

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        socket.connect(digitalMaleSocket);
        FemaleStringExpressionSocket stringSocket = socket.getStringSocket(_conditionalNG);
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

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        socket.connect(digitalMaleSocket);
        FemaleAnalogExpressionSocket analogSocket = socket.getAnalogSocket(_conditionalNG);
        testLockUnlock(socket, analogSocket);

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        socket.connect(digitalMaleSocket);
        FemaleDigitalExpressionSocket digitalSocket = socket.getDigitalSocket(_conditionalNG);
        testLockUnlock(socket, digitalSocket);

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        socket.connect(digitalMaleSocket);
        FemaleGenericExpressionSocket genericSocket = socket.getGenericSocket(_conditionalNG);
        testLockUnlock(socket, genericSocket);

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        socket.connect(digitalMaleSocket);
        FemaleStringExpressionSocket stringSocket = socket.getStringSocket(_conditionalNG);
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

        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, _listener, "E");
        FemaleAnalogExpressionSocket analogSocket = femaleSocket.getAnalogSocket(_conditionalNG);
        Assert.assertTrue("Socket is compatible",
                analogSocket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Socket is compatible",
                analogSocket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("Socket is compatible",
                analogSocket.isCompatible(stringMaleSocket));

        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        FemaleDigitalExpressionSocket digitalSocket = femaleSocket.getDigitalSocket(_conditionalNG);
        Assert.assertTrue("Socket is compatible",
                digitalSocket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Socket is compatible",
                digitalSocket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("Socket is compatible",
                digitalSocket.isCompatible(stringMaleSocket));

        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        FemaleGenericExpressionSocket genericSocket = femaleSocket.getGenericSocket(_conditionalNG);
        Assert.assertTrue("Socket is compatible",
                genericSocket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Socket is compatible",
                genericSocket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("Socket is compatible",
                genericSocket.isCompatible(stringMaleSocket));

        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        FemaleStringExpressionSocket stringSocket = femaleSocket.getStringSocket(_conditionalNG);
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
        _femaleGenericSocket.setSocketType(SocketType.ANALOG);
        _femaleGenericSocket.setSocketType(SocketType.ANALOG);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.ANALOG, _femaleGenericSocket.getSocketType());
        Assert.assertEquals("Active socket is correct", "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket", _femaleGenericSocket.getCurrentActiveSocket().getClass().getName());

        // We can't change socket type if it's connected
        _femaleGenericSocket.connect(analogMaleSocket);
        exceptionThrown = false;
        try {
            _femaleGenericSocket.setSocketType(SocketType.DIGITAL);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        _femaleGenericSocket.disconnect();


        // This should work
        _femaleGenericSocket.setSocketType(SocketType.DIGITAL);
        _femaleGenericSocket.setSocketType(SocketType.DIGITAL);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.DIGITAL, _femaleGenericSocket.getSocketType());
        Assert.assertEquals("Active socket is correct", "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket", _femaleGenericSocket.getCurrentActiveSocket().getClass().getName());

        // We can't change socket type if it's connected
        _femaleGenericSocket.connect(digitalMaleSocket);
        exceptionThrown = false;
        try {
            _femaleGenericSocket.setSocketType(SocketType.GENERIC);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        _femaleGenericSocket.disconnect();


        // This should work
        _femaleGenericSocket.setSocketType(SocketType.GENERIC);
        _femaleGenericSocket.setSocketType(SocketType.GENERIC);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.GENERIC, _femaleGenericSocket.getSocketType());
        Assert.assertNull("Active socket is null", _femaleGenericSocket.getCurrentActiveSocket());

        // We can't change socket type if it's connected
        _femaleGenericSocket.connect(stringMaleSocket);
        exceptionThrown = false;
        try {
            _femaleGenericSocket.setSocketType(SocketType.STRING);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        _femaleGenericSocket.disconnect();


        // This should work
        _femaleGenericSocket.setSocketType(SocketType.STRING);
        _femaleGenericSocket.setSocketType(SocketType.STRING);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.STRING, _femaleGenericSocket.getSocketType());
        Assert.assertEquals("Active socket is correct", "jmri.jmrit.logixng.implementation.DefaultFemaleStringExpressionSocket", _femaleGenericSocket.getCurrentActiveSocket().getClass().getName());

        // We can't change socket type if it's connected
        _femaleGenericSocket.connect(stringMaleSocket);
        exceptionThrown = false;
        try {
            _femaleGenericSocket.setSocketType(SocketType.DIGITAL);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        _femaleGenericSocket.disconnect();
    }

    // The same test as above, but use the _femaleGenericSocket.getGenericSocket() instead
    @Test
    public void testGetAndSetSocketTypeInternalSocket() throws SocketAlreadyConnectedException {
        FemaleGenericExpressionSocket internalSocket = _femaleGenericSocket.getGenericSocket(_conditionalNG);

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
        internalSocket.setSocketType(SocketType.ANALOG);
        internalSocket.setSocketType(SocketType.ANALOG);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.ANALOG, internalSocket.getSocketType());
        Assert.assertEquals("Active socket is correct", "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket", internalSocket.getCurrentActiveSocket().getClass().getName());

        // We can't change socket type if it's connected
        internalSocket.connect(analogMaleSocket);
        exceptionThrown = false;
        try {
            internalSocket.setSocketType(SocketType.DIGITAL);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        internalSocket.disconnect();


        // This should work
        internalSocket.setSocketType(SocketType.DIGITAL);
        internalSocket.setSocketType(SocketType.DIGITAL);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.DIGITAL, internalSocket.getSocketType());
        Assert.assertEquals("Active socket is correct", "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket", internalSocket.getCurrentActiveSocket().getClass().getName());

        // We can't change socket type if it's connected
        internalSocket.connect(digitalMaleSocket);
        exceptionThrown = false;
        try {
            internalSocket.setSocketType(SocketType.GENERIC);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        internalSocket.disconnect();


        // This should work
        internalSocket.setSocketType(SocketType.GENERIC);
        internalSocket.setSocketType(SocketType.GENERIC);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.GENERIC, internalSocket.getSocketType());
        Assert.assertNull("Active socket is null", internalSocket.getCurrentActiveSocket());

        // We can't change socket type if it's connected
        internalSocket.connect(stringMaleSocket);
        exceptionThrown = false;
        try {
            internalSocket.setSocketType(SocketType.STRING);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        internalSocket.disconnect();


        // This should work
        internalSocket.setSocketType(SocketType.STRING);
        internalSocket.setSocketType(SocketType.STRING);   // Test calling setSocketType() twice
        Assert.assertEquals("Socket type is correct", SocketType.STRING, internalSocket.getSocketType());
        Assert.assertEquals("Active socket is correct", "jmri.jmrit.logixng.implementation.DefaultFemaleStringExpressionSocket", internalSocket.getCurrentActiveSocket().getClass().getName());

        // We can't change socket type if it's connected
        internalSocket.connect(stringMaleSocket);
        exceptionThrown = false;
        try {
            internalSocket.setSocketType(SocketType.DIGITAL);
        } catch (SocketAlreadyConnectedException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "Socket is already connected", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
        internalSocket.disconnect();
    }

    private void testGetSocketException(
            DefaultFemaleGenericExpressionSocket socket) {

        boolean exceptionThrown;

        exceptionThrown = false;
        try {
            socket.getAnalogSocket(_conditionalNG);
        } catch (RuntimeException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "internal socket cannot be set more than once", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);

        exceptionThrown = false;
        try {
            socket.getDigitalSocket(_conditionalNG);
        } catch (RuntimeException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "internal socket cannot be set more than once", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);

        exceptionThrown = false;
        try {
            socket.getGenericSocket(_conditionalNG);
        } catch (RuntimeException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "internal socket cannot be set more than once", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);

        exceptionThrown = false;
        try {
            socket.getStringSocket(_conditionalNG);
        } catch (RuntimeException e) {
            exceptionThrown = true;
            Assert.assertEquals("Error message is correct", "internal socket cannot be set more than once", e.getMessage());
        }
        Assert.assertTrue("Exception thrown", exceptionThrown);
    }

    @Test
    public void testGetSocket() {
        DefaultFemaleGenericExpressionSocket socket;

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.ANALOG, _conditionalNG, _listener, "E");
        socket.getAnalogSocket(_conditionalNG);
        testGetSocketException(socket);

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        socket.getDigitalSocket(_conditionalNG);
        testGetSocketException(socket);

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, _listener, "E");
        socket.getGenericSocket(_conditionalNG);
        testGetSocketException(socket);

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.STRING, _conditionalNG, _listener, "E");
        socket.getStringSocket(_conditionalNG);
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

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.ANALOG, _conditionalNG, _listener, "E");
        Assert.assertTrue("Analog male socket is compatible", socket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Digital male socket is compatible", socket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("String male socket is compatible", socket.isCompatible(stringMaleSocket));

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        Assert.assertTrue("Analog male socket is compatible", socket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Digital male socket is compatible", socket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("String male socket is compatible", socket.isCompatible(stringMaleSocket));

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, _listener, "E");
        Assert.assertTrue("Analog male socket is compatible", socket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Digital male socket is compatible", socket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("String male socket is compatible", socket.isCompatible(stringMaleSocket));

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.STRING, _conditionalNG, _listener, "E");
        Assert.assertTrue("Analog male socket is compatible", socket.isCompatible(analogMaleSocket));
        Assert.assertTrue("Digital male socket is compatible", socket.isCompatible(digitalMaleSocket));
        Assert.assertTrue("String male socket is compatible", socket.isCompatible(stringMaleSocket));
    }

    @Test
    public void testDoI18N() {
        DefaultFemaleGenericExpressionSocket socket =
                new DefaultFemaleGenericExpressionSocket(SocketType.ANALOG, _conditionalNG, _listener, "E");
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
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        _listener = new FemaleSocketListener(){
            @Override
            public void connected(FemaleSocket socket) {
                // Do nothing
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                // Do nothing
            }
        };

        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N

        _conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        flag = new AtomicBoolean();
        errorFlag = new AtomicBoolean();
        _expression = new MyExpressionTurnout("IQDE321");
        ExpressionTurnout otherExpression = new ExpressionTurnout("IQDE322", null);
        maleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(_expression);
        otherMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(otherExpression);
        _femaleGenericSocket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, new FemaleSocketListener() {
            @Override
            public void connected(FemaleSocket socket) {
                flag.set(true);
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                flag.set(true);
            }
        }, "E");
        _femaleSocket = _femaleGenericSocket;
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
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

//    private final static org.slf4j.Logger log =
//            org.slf4j.LoggerFactory.getLogger(DefaultFemaleGenericExpressionSocket1_Test.class);
}
