package jmri.jmrix.dccpp.simulator;

import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author mstevetodd Copyright (C) 2022
 */
public class DCCppSimulatorAdapterTest {

    private DCCppSimulatorAdapter a = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",a);
    }

    private DCCppReply getReplyForMessage(DCCppMessage m){
        DCCppReply r = null;
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method generateReplyMethod = null;
        try {
            generateReplyMethod = a.getClass().getDeclaredMethod("generateReply", DCCppMessage.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method generateReply in DCCppSimulatorAdapter class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(generateReplyMethod);
        generateReplyMethod.setAccessible(true);

        try {
           r = (DCCppReply) generateReplyMethod.invoke(a,m);
        } catch(java.lang.IllegalAccessException ite){
             Assertions.fail("could not access method generateReply in DCCppSimulatoradapter class ", ite);
        } catch(java.lang.reflect.InvocationTargetException ite){
             Assertions.fail("generateReply execution failed reason: ", ite);
        }
        return r;
    }

    // tests of generation of specific replies.
    @Test
    public void testThrottleReplies() {
        DCCppReply r = getReplyForMessage(new DCCppMessage("t 1 1234 22 1"));
        Assertions.assertEquals( "Throttle Reply: Register: 1, Speed: 22, Direction: Forward", r.toMonitorString());
        r = getReplyForMessage(new DCCppMessage("t 1234 22 1")); //<t locoId speed dir>
        Assertions.assertEquals("Loco State: LocoId:1234 Dir:Forward Speed:22 F0-28:00000000000000000000000000000", r.toMonitorString());
        r = getReplyForMessage(new DCCppMessage("t 1234 44 0"));
        Assertions.assertEquals("Loco State: LocoId:1234 Dir:Reverse Speed:44 F0-28:00000000000000000000000000000", r.toMonitorString());
    }

    @Test
    //note that these tests preserve states from line to line
    public void testFunctionReplies() {
        DCCppReply r = getReplyForMessage(new DCCppMessage("F 1234 0 1")); //<F locoId func 1|0>
        Assert.assertEquals("Loco State: LocoId:1234 Dir:Reverse Speed:0 F0-28:10000000000000000000000000000", r.toMonitorString());
        r = getReplyForMessage(new DCCppMessage("F 1234 1 1"));
        Assert.assertEquals("Loco State: LocoId:1234 Dir:Reverse Speed:0 F0-28:11000000000000000000000000000", r.toMonitorString());
        r = getReplyForMessage(new DCCppMessage("F 1234 0 0"));
        Assert.assertEquals("Loco State: LocoId:1234 Dir:Reverse Speed:0 F0-28:01000000000000000000000000000", r.toMonitorString());
        r = getReplyForMessage(new DCCppMessage("F 1234 1 0"));
        Assert.assertEquals("Loco State: LocoId:1234 Dir:Reverse Speed:0 F0-28:00000000000000000000000000000", r.toMonitorString());
        r = getReplyForMessage(new DCCppMessage("F 1234 12 1"));
        Assert.assertEquals("Loco State: LocoId:1234 Dir:Reverse Speed:0 F0-28:00000000000010000000000000000", r.toMonitorString());
    }

    // setup and teardown    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        a = new DCCppSimulatorAdapter();        
    }

    @AfterEach
    public void tearDown() {
        a.dispose();
        a = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppSimulatorAdapterTest.class);

}
