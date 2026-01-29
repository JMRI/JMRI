package jmri.jmrit.logixng.implementation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import jmri.util.junit.annotations.NotApplicable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    @NotApplicable("Different types of beans may be able to connect to a generic socket, which makes this test impossible")
    public void testSWISystemName() {
    }

    @Test
    @Override
    public void testSetParentForAllChildren() throws SocketAlreadyConnectedException {
        // This female socket has child female sockets, which requires special treatment
        assertFalse( _femaleSocket.isConnected(), "femaleSocket is not connected");
        assertTrue( _femaleSocket.setParentForAllChildren(new ArrayList<>()));
        assertNull( maleSocket.getParent(), "malesocket.getParent() is null");
        _femaleSocket.connect(maleSocket);
        assertTrue( _femaleSocket.setParentForAllChildren(new ArrayList<>()));
        assertEquals( _femaleGenericSocket, maleSocket.getParent(),
            "malesocket.getParent() is femaleSocket");
    }

    @Test
    public void testSocketType() {
        assertEquals( Bundle.getMessage("SocketTypeDigital"),
                FemaleGenericExpressionSocket.SocketType.DIGITAL.toString(),
                "strings are equal");
        assertEquals( Bundle.getMessage("SocketTypeAnalog"),
                FemaleGenericExpressionSocket.SocketType.ANALOG.toString(),
                "strings are equal");
        assertEquals( Bundle.getMessage("SocketTypeString"),
                FemaleGenericExpressionSocket.SocketType.STRING.toString(),
                "strings are equal");
        assertEquals( Bundle.getMessage("SocketTypeGeneric"),
                FemaleGenericExpressionSocket.SocketType.GENERIC.toString(),
                "strings are equal");
    }

    @Test
    public void testGetName() {
        assertTrue( "E".equals(_femaleSocket.getName()), "String matches");
    }

    @Test
    public void testGetDescription() {
        assertEquals( "?*", _femaleSocket.getShortDescription(), "String matches");
        assertEquals( "?* E", _femaleSocket.getLongDescription(), "String matches");


        DefaultFemaleGenericExpressionSocket socket;

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        assertEquals( "?*", socket.getShortDescription(), "String matches");
        assertEquals( "?* E", socket.getLongDescription(), "String matches");
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

        assertDoesNotThrow( () -> _femaleSocket.connect(maleSocket) );

        // Try to connect twice. This should fail.
        SocketAlreadyConnectedException e2 = assertThrows( SocketAlreadyConnectedException.class,
            () -> _femaleSocket.connect(maleSocket),
            "Exception thrown");
        assertEquals("Socket is already connected", e2.getMessage());

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

        assertNull( femaleSocket.getCurrentActiveSocket(),
            "_currentActiveSocket is null");

        // Test disconnect() without connected socket
        femaleSocket.disconnect();

        assertNull( femaleSocket.getCurrentActiveSocket(),
            "_currentActiveSocket is null");

        // Test disconnect() without connected socket
        femaleSocket.connect(digitalMaleSocket);

        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                femaleSocket.getCurrentActiveSocket().getClass().getName(),
                "_currentActiveSocket is has correct class");

        femaleSocket.disconnect();


        // Test female digital socket
        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");

        // When the SocketType is not GENERIC, _currentActiveSocket is assigned to a socket
        assertNotNull( femaleSocket.getCurrentActiveSocket(),
            "_currentActiveSocket is not null");

        // Test disconnect() without connected socket
        femaleSocket.disconnect();

        // When the SocketType is not GENERIC, _currentActiveSocket is assigned to a socket
        assertNotNull( femaleSocket.getCurrentActiveSocket(),
            "_currentActiveSocket is not null");

        // Test disconnect() without connected socket
        femaleSocket.connect(digitalMaleSocket);

        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                femaleSocket.getCurrentActiveSocket().getClass().getName(),
                "_currentActiveSocket is has correct class");

        femaleSocket.disconnect();
    }

    private void checkConnectableClasses(FemaleSocket femaleSocket) {
        Map<Category, List<Class<? extends Base>>> classes = femaleSocket.getConnectableClasses();
        assertNotNull( classes, "classes is not null");
        assertFalse( classes.isEmpty(), "classes is not empty");
    }

    @Test
    public void testConnectableClasses() throws SocketAlreadyConnectedException {
        MyExpressionTurnout digitalExpression = new MyExpressionTurnout("IQDE351");
        MaleSocket digitalMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(digitalExpression);

        DefaultFemaleGenericExpressionSocket socket;

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        socket.connect(digitalMaleSocket);
        checkConnectableClasses(socket);
        socket.disconnect();
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

        femaleSocket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        assertTrue( femaleSocket.isCompatible(analogMaleSocket),
            "Socket is compatible");
        assertTrue( femaleSocket.isCompatible(digitalMaleSocket),
            "Socket is compatible");
        assertTrue( femaleSocket.isCompatible(stringMaleSocket),
            "Socket is compatible");
    }

    @Test
    public void testGetAndSetSocketType() throws SocketAlreadyConnectedException {

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
        assertEquals( SocketType.ANALOG, _femaleGenericSocket.getSocketType(),
            "Socket type is correct");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
            _femaleGenericSocket.getCurrentActiveSocket().getClass().getName(),
            "Active socket is correct");

        // We can't change socket type if it's connected
        _femaleGenericSocket.connect(analogMaleSocket);
        SocketAlreadyConnectedException e = assertThrows( SocketAlreadyConnectedException.class,
            () -> _femaleGenericSocket.setSocketType(SocketType.DIGITAL),
            "Exception thrown");
        assertEquals( "Socket is already connected", e.getMessage(),
            "Error message is correct");

        _femaleGenericSocket.disconnect();


        // This should work
        _femaleGenericSocket.setSocketType(SocketType.DIGITAL);
        _femaleGenericSocket.setSocketType(SocketType.DIGITAL);   // Test calling setSocketType() twice
        assertEquals( SocketType.DIGITAL, _femaleGenericSocket.getSocketType(), "Socket type is correct");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
            _femaleGenericSocket.getCurrentActiveSocket().getClass().getName(),
            "Active socket is correct");

        // We can't change socket type if it's connected
        _femaleGenericSocket.connect(digitalMaleSocket);

        e = assertThrows( SocketAlreadyConnectedException.class,
            () -> _femaleGenericSocket.setSocketType(SocketType.GENERIC),
            "Exception thrown");
        assertEquals( "Socket is already connected", e.getMessage(),
            "Error message is correct");

        _femaleGenericSocket.disconnect();


        // This should work
        _femaleGenericSocket.setSocketType(SocketType.GENERIC);
        _femaleGenericSocket.setSocketType(SocketType.GENERIC);   // Test calling setSocketType() twice
        assertEquals( SocketType.GENERIC, _femaleGenericSocket.getSocketType(),
            "Socket type is correct");
        assertNull( _femaleGenericSocket.getCurrentActiveSocket(), "Active socket is null");

        // We can't change socket type if it's connected
        _femaleGenericSocket.connect(stringMaleSocket);

        e = assertThrows( SocketAlreadyConnectedException.class,
            () -> _femaleGenericSocket.setSocketType(SocketType.STRING),
            "Exception thrown");
        assertEquals( "Socket is already connected", e.getMessage(),
            "Error message is correct");

        _femaleGenericSocket.disconnect();


        // This should work
        _femaleGenericSocket.setSocketType(SocketType.STRING);
        _femaleGenericSocket.setSocketType(SocketType.STRING);   // Test calling setSocketType() twice
        assertEquals( SocketType.STRING, _femaleGenericSocket.getSocketType(),
            "Socket type is correct");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleStringExpressionSocket",
            _femaleGenericSocket.getCurrentActiveSocket().getClass().getName(),
            "Active socket is correct");

        // We can't change socket type if it's connected
        _femaleGenericSocket.connect(stringMaleSocket);
        e = assertThrows( SocketAlreadyConnectedException.class,
            () -> _femaleGenericSocket.setSocketType(SocketType.DIGITAL),
            "Exception thrown");
        assertEquals( "Socket is already connected", e.getMessage(),
            "Error message is correct");
        _femaleGenericSocket.disconnect();
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
        assertTrue( socket.isCompatible(analogMaleSocket),
            "Analog male socket is compatible");
        assertTrue( socket.isCompatible(digitalMaleSocket),
            "Digital male socket is compatible");
        assertTrue( socket.isCompatible(stringMaleSocket),
            "String male socket is compatible");

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.DIGITAL, _conditionalNG, _listener, "E");
        assertTrue( socket.isCompatible(analogMaleSocket),
            "Analog male socket is compatible");
        assertTrue( socket.isCompatible(digitalMaleSocket),
            "Digital male socket is compatible");
        assertTrue( socket.isCompatible(stringMaleSocket),
            "String male socket is compatible");

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.GENERIC, _conditionalNG, _listener, "E");
        assertTrue( socket.isCompatible(analogMaleSocket),
            "Analog male socket is compatible");
        assertTrue( socket.isCompatible(digitalMaleSocket),
            "Digital male socket is compatible");
        assertTrue( socket.isCompatible(stringMaleSocket),
            "String male socket is compatible");

        socket = new DefaultFemaleGenericExpressionSocket(SocketType.STRING, _conditionalNG, _listener, "E");
        assertTrue( socket.isCompatible(analogMaleSocket),
            "Analog male socket is compatible");
        assertTrue( socket.isCompatible(digitalMaleSocket),
            "Digital male socket is compatible");
        assertTrue( socket.isCompatible(stringMaleSocket),
            "String male socket is compatible");
    }

    @Test
    public void testDoI18N() {
        DefaultFemaleGenericExpressionSocket socket =
                new DefaultFemaleGenericExpressionSocket(SocketType.ANALOG, _conditionalNG, _listener, "E");
        assertFalse( socket.getDoI18N(), "do_i18n is false");
        socket.setDoI18N(true);
        assertTrue( socket.getDoI18N(), "do_i18n is true");
        socket.setDoI18N(false);
        assertFalse( socket.getDoI18N(), "do_i18n is false");
    }

    @BeforeEach
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

    @AfterEach
    public void tearDown() {
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

//    private final static org.slf4j.Logger log =
//            org.slf4j.LoggerFactory.getLogger(DefaultFemaleGenericExpressionSocket1_Test.class);
}
