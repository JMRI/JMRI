package jmri.jmrit.withrottle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of MultiThrottle
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MultiThrottleTest {

    private ControllerInterfaceScaffold cis = null; 
    private ThrottleControllerListenerScaffold tcls = null;
    private MultiThrottle throttle = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", throttle );
    }

    @Test
    public void testSetShortAddress(){
       // tests setting the address from the input.  
       // Does not include the prefix.
       throttle.handleMessage("+S1<;>S1");
       Assert.assertEquals("outgoing message after throttle request", "MAAS1<;>s1",cis.getLastPacket() );
       Assert.assertTrue("Address Found",tcls.hasAddressBeenFound());
    }

    @Test
    public void testSetLongAddress(){
       // tests setting the address from the input.  
       // Does not include the prefix.
       throttle.handleMessage("+L1234<;>L1234");
       Assert.assertEquals("outgoing message after throttle request", "MAAL1234<;>s1",cis.getLastPacket() );
       Assert.assertTrue("Address Found",tcls.hasAddressBeenFound());
    }

    @Test
    public void testSetAndReleaseLongAddress(){
       // set the address
       throttle.handleMessage("+L1234<;>L1234");
       Assert.assertTrue("Address Found",tcls.hasAddressBeenFound());
       // then release it.
       throttle.handleMessage("-L1234<;>r");
       Assert.assertEquals("outgoing message after throttle release", "MA-L1234<;>",cis.getLastPacket() );
       Assert.assertTrue("Address Released",tcls.hasAddressBeenReleased());
    }

    @Test
    public void testSetAndDispatchLongAddress(){
       // set the address
       throttle.handleMessage("+L1234<;>L1234");
       Assert.assertTrue("Address Found",tcls.hasAddressBeenFound());
       // then dispatch it.
       throttle.handleMessage("-L1234<;>d");
       Assert.assertEquals("outgoing message after throttle release", "MA-L1234<;>",cis.getLastPacket() );
       Assert.assertTrue("Address Released",tcls.hasAddressBeenReleased());
    }

    @Test
    public void testSetVelocityChange() {
       throttle.handleMessage("+L1234<;>L1234");
       // then send a speed change.
       throttle.handleMessage("AL1234<;>V42");
       // query the velocity.
       throttle.handleMessage("AL1234<;>qV");
       Assert.assertEquals("outgoing message after speed change", "MAAL1234<;>V42",cis.getLastPacket() );
    }

    @Test
    public void testSetEStop() {
       throttle.handleMessage("+L1234<;>L1234");
       // then send EStop.
       throttle.handleMessage("AL1234<;>X");
       Assert.assertEquals("outgoing message after throttle EStop", "MAAL1234<;>V-126",cis.getLastPacket() );
    }

    @Test
    public void testSetIdle() {
       throttle.handleMessage("+L1234<;>L1234");
       // then send EStop.
       throttle.handleMessage("AL1234<;>I");
       throttle.handleMessage("AL1234<;>qV");
       Assert.assertEquals("outgoing message after throttle EStop", "MAAL1234<;>V0",cis.getLastPacket() );
    }

    @Test
    public void testSetFunction() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>F11");
       Assert.assertEquals("outgoing message after function on", "MAAL1234<;>F11",cis.getLastPacket() );
       throttle.handleMessage("AL1234<;>F11");
       Assert.assertEquals("outgoing message after function off", "MAAL1234<;>F01",cis.getLastPacket() );
    }

    @Test
    public void testForceFunction() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>f11");
       Assert.assertEquals("outgoing message after function on", "MAAL1234<;>F11",cis.getLastPacket() );
       throttle.handleMessage("AL1234<;>f01");
       Assert.assertEquals("outgoing message after function off", "MAAL1234<;>F01",cis.getLastPacket() );
    }

    @Test
    public void testMomentaryFunction() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>m128");
       throttle.handleMessage("AL1234<;>qm");
       Assert.assertEquals("outgoing message after function on", "MAAL1234<;>m128",cis.getLastPacket() );
       throttle.handleMessage("AL1234<;>m028");
       throttle.handleMessage("AL1234<;>qm");
       Assert.assertEquals("outgoing message after function off", "MAAL1234<;>m028",cis.getLastPacket() );
    }

    @Test
    public void testSetDirection() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>R0");
       Assert.assertEquals("outgoing message after direction forward", "MAAL1234<;>R0",cis.getLastPacket() );
       throttle.handleMessage("AL1234<;>R1");
       Assert.assertEquals("outgoing message after direction reverse", "MAAL1234<;>R1",cis.getLastPacket() );
    }

    @Test
    public void testSetSpeedStepMode() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>s1");
       Assert.assertEquals("outgoing message after direction forward", "MAAL1234<;>s1",cis.getLastPacket() );
       throttle.handleMessage("AL1234<;>s8");
       Assert.assertEquals("outgoing message after direction reverse", "MAAL1234<;>s8",cis.getLastPacket() );
    }

    @Test
    public void testQuit() {
       throttle.handleMessage("+L1234<;>L1234");
       // function "on" from withrottle represents a button click event.
       throttle.handleMessage("AL1234<;>Q");
       Assert.assertEquals("outgoing message after quit", "MA-L1234<;>",cis.getLastPacket() );
    }
    
    @Test
    public void testIsValidAddress() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method m = throttle.getClass().getDeclaredMethod("isValidAddr", String.class);
        m.setAccessible(true);
        Assert.assertFalse((Boolean)m.invoke(throttle, "hjs"));
        JUnitAppender.assertWarnMessage("JMRI: address 'hjs' must have a letter S or L and then digit(s)");
        Assert.assertFalse((Boolean)m.invoke(throttle, "s13"));
        JUnitAppender.assertWarnMessage("JMRI: address 's13' must have a letter S or L and then digit(s)");
        Assert.assertFalse((Boolean)m.invoke(throttle, "l13"));
        JUnitAppender.assertWarnMessage("JMRI: address 'l13' must have a letter S or L and then digit(s)");
        Assert.assertFalse((Boolean)m.invoke(throttle, "S"));
        JUnitAppender.assertWarnMessage("JMRI: address 'S' must have a letter S or L and then digit(s)");
        Assert.assertFalse((Boolean)m.invoke(throttle, "L"));
        JUnitAppender.assertWarnMessage("JMRI: address 'L' must have a letter S or L and then digit(s)");
        Assert.assertFalse((Boolean)m.invoke(throttle, "7"));
        JUnitAppender.assertWarnMessage("JMRI: address '7' must have a letter S or L and then digit(s)");
        Assert.assertTrue((Boolean)m.invoke(throttle, "S32"));
        Assert.assertFalse((Boolean)m.invoke(throttle, "S320"));
        JUnitAppender.assertWarnMessage("JMRI: address 'S320' not allowed as Short");
        Assert.assertTrue((Boolean)m.invoke(throttle, "L32"));
        Assert.assertTrue((Boolean)m.invoke(throttle, "L320"));
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
        InstanceManager.setDefault(WiThrottlePreferences.class, new WiThrottlePreferences());
        JUnitUtil.initDebugThrottleManager();
        cis = new ControllerInterfaceScaffold();
        tcls = new ThrottleControllerListenerScaffold();
        throttle = new MultiThrottle('A',tcls,cis);
    }
    
    @After
    public void tearDown() throws Exception {
        cis = null;
        tcls = null;
        throttle = null;
        JUnitUtil.tearDown();
    }
}
