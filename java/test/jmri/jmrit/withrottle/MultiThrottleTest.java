package jmri.jmrit.withrottle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MultiThrottle
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MultiThrottleTest {

    private ControllerInterfaceScaffold cis = null; 
    private ThrottleControllerListenerScaffold tcls = null;
    private MultiThrottle throttle = null;

    @Test
    public void testCtor() {
        assertNotNull( throttle, "exists");
    }

    @Test
    public void testSetShortAddress(){
       // tests setting the address from the input.  
       // Does not include the prefix.
       throttle.handleMessage("+S1<;>S1");
       assertEquals( "MAAS1<;>s1",cis.getLastPacket(), "outgoing message after throttle request");
       assertTrue( tcls.hasAddressBeenFound(), "Address Found");
    }

    @Test
    public void testSetLongAddress(){
       // tests setting the address from the input.  
       // Does not include the prefix.
       throttle.handleMessage("+L1234<;>L1234");
       assertEquals( "MAAL1234<;>s1",cis.getLastPacket(), "outgoing message after throttle request");
       assertTrue( tcls.hasAddressBeenFound(), "Address Found");
    }

    @Test
    public void testSetAndReleaseLongAddress(){
       // set the address
       throttle.handleMessage("+L1234<;>L1234");
       assertTrue( tcls.hasAddressBeenFound(), "Address Found");
       // then release it.
       throttle.handleMessage("-L1234<;>r");
       assertEquals( "MA-L1234<;>",cis.getLastPacket(), "outgoing message after throttle release");
       assertTrue( tcls.hasAddressBeenReleased(), "Address Released");
    }

    @Test
    public void testSetAndDispatchLongAddress(){
       // set the address
       throttle.handleMessage("+L1234<;>L1234");
       assertTrue( tcls.hasAddressBeenFound(), "Address Found");
       // then dispatch it.
       throttle.handleMessage("-L1234<;>d");
       assertEquals( "MA-L1234<;>",cis.getLastPacket(), "outgoing message after throttle release");
       assertTrue( tcls.hasAddressBeenReleased(), "Address Released");
    }

    @Test
    public void testSetVelocityChange() {
       throttle.handleMessage("+L1234<;>L1234");
       // then send a speed change.
       throttle.handleMessage("AL1234<;>V42");
       // query the velocity.
       throttle.handleMessage("AL1234<;>qV");
       assertEquals( "MAAL1234<;>V42",cis.getLastPacket(), "outgoing message after speed change");
    }

    @Test
    public void testSetEStop() {
       throttle.handleMessage("+L1234<;>L1234");
       // then send EStop.
       throttle.handleMessage("AL1234<;>X");
       assertEquals( "MAAL1234<;>V-126",cis.getLastPacket(), "outgoing message after throttle EStop");
    }

    @Test
    public void testSetIdle() {
       throttle.handleMessage("+L1234<;>L1234");
       // then send EStop.
       throttle.handleMessage("AL1234<;>I");
       throttle.handleMessage("AL1234<;>qV");
       assertEquals( "MAAL1234<;>V0",cis.getLastPacket(), "outgoing message after throttle EStop");
    }

    @Test
    public void testSetFunction() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>F11");
       assertEquals( "MAAL1234<;>F11",cis.getLastPacket(), "outgoing message after function on");
       throttle.handleMessage("AL1234<;>F11");
       assertEquals( "MAAL1234<;>F01",cis.getLastPacket(), "outgoing message after function off");
    }

    @Test
    public void testForceFunction() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>f11");
       assertEquals( "MAAL1234<;>F11",cis.getLastPacket(), "outgoing message after function on");
       throttle.handleMessage("AL1234<;>f01");
       assertEquals( "MAAL1234<;>F01",cis.getLastPacket(), "outgoing message after function off");
    }

    @Test
    public void testMomentaryFunction() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>m128");
       throttle.handleMessage("AL1234<;>qm");
       assertEquals( "MAAL1234<;>m128",cis.getLastPacket(), "outgoing message after function on");
       throttle.handleMessage("AL1234<;>m028");
       throttle.handleMessage("AL1234<;>qm");
       assertEquals( "MAAL1234<;>m028",cis.getLastPacket(), "outgoing message after function off");
    }

    @Test
    public void testSetDirection() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>R0");
       assertEquals( "MAAL1234<;>R0",cis.getLastPacket(), "outgoing message after direction forward");
       throttle.handleMessage("AL1234<;>R1");
       assertEquals( "MAAL1234<;>R1",cis.getLastPacket(), "outgoing message after direction reverse");
    }

    @Test
    public void testSetSpeedStepMode() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>s1");
       assertEquals( "MAAL1234<;>s1",cis.getLastPacket(), "outgoing message after direction forward");
       throttle.handleMessage("AL1234<;>s8");
       assertEquals( "MAAL1234<;>s8",cis.getLastPacket(), "outgoing message after direction reverse");
    }

    @Test
    public void testQuit() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>Q");
       assertEquals( "MA-L1234<;>",cis.getLastPacket(), "outgoing message after quit");
    }
    
    @Test
    public void testIsValidAddress() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = throttle.getClass().getDeclaredMethod("isValidAddr", String.class);
        m.setAccessible(true);
        assertFalse((Boolean)m.invoke(throttle, "hjs"));
        JUnitAppender.assertWarnMessage("JMRI: address 'hjs' must have a letter S or L and then digit(s)");
        assertFalse((Boolean)m.invoke(throttle, "s13"));
        JUnitAppender.assertWarnMessage("JMRI: address 's13' must have a letter S or L and then digit(s)");
        assertFalse((Boolean)m.invoke(throttle, "l13"));
        JUnitAppender.assertWarnMessage("JMRI: address 'l13' must have a letter S or L and then digit(s)");
        assertFalse((Boolean)m.invoke(throttle, "S"));
        JUnitAppender.assertWarnMessage("JMRI: address 'S' must have a letter S or L and then digit(s)");
        assertFalse((Boolean)m.invoke(throttle, "L"));
        JUnitAppender.assertWarnMessage("JMRI: address 'L' must have a letter S or L and then digit(s)");
        assertFalse((Boolean)m.invoke(throttle, "7"));
        JUnitAppender.assertWarnMessage("JMRI: address '7' must have a letter S or L and then digit(s)");
        assertTrue((Boolean)m.invoke(throttle, "S32"));
        assertFalse((Boolean)m.invoke(throttle, "S320"));
        JUnitAppender.assertWarnMessage("JMRI: address 'S320' not allowed as Short");
        assertTrue((Boolean)m.invoke(throttle, "L32"));
        assertTrue((Boolean)m.invoke(throttle, "L320"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
        InstanceManager.setDefault(WiThrottlePreferences.class, new WiThrottlePreferences());
        JUnitUtil.initDebugThrottleManager();
        cis = new ControllerInterfaceScaffold();
        tcls = new ThrottleControllerListenerScaffold();
        throttle = new MultiThrottle('A',tcls,cis);
    }
    
    @AfterEach
    public void tearDown() {
        cis = null;
        tcls = null;
        throttle = null;
        JUnitUtil.tearDown();
    }
}
