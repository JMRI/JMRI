package jmri.jmris.simpleserver;

import jmri.InstanceManager;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleOperationsServer class
 *
 * @author Paul Bender
 */
public class SimpleOperationsServerTest {

    private StringBuilder sb = null;
    private java.io.DataOutputStream output = null;
    private java.io.DataInputStream input = null;

    @Test
    public void testCtor() {
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        Assert.assertNotNull(a);
    }

    @Test
    public void testConnectionCtor() {
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleOperationsServer a = new SimpleOperationsServer(jcs);
        Assert.assertNotNull(a);
    }

    // test sending a message.
    @Test
    public void testSendMessage() {
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod = null;
        try {
            sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method sendMessage in SimpleOperationsServer class. ");
        }

        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
            sendMessageMethod.invoke(a, "Hello World");
            Assert.assertEquals("SendMessage Check", "Hello World", sb.toString());
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method sendMessage in SimpleOperationsServer class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
        }
    }

    // test sending a message.
    @Test
    public void testSendMessageWithConnection() {
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleOperationsServer a = new SimpleOperationsServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod = null;
        try {
            sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method sendMessage in SimpleOperationsServer class. ");
        }

        // override the default permissions.
        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
            sendMessageMethod.invoke(a, "Hello World");
            Assert.assertEquals("SendMessage Check", "Hello World", jcs.getOutput());
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method sendMessage in SimpleOperationsServer class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
        }
    }

    // test sending the train list.
    @Test
    public void testSendTrainList() {
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.sendTrainList();
        Assert.assertEquals("SendTrainList Check", "OPERATIONS , TRAINS=SFF\nOPERATIONS , TRAINS=STF\n", sb.toString());
    }

    // test sending the locations list.
    @Test
    public void testSendLocationList() {
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.sendLocationList();
        Assert.assertEquals("SendLocationList Check", "OPERATIONS , LOCATIONS=North End Staging\nOPERATIONS , LOCATIONS=North Industries\nOPERATIONS , LOCATIONS=South End Staging\n", sb.toString());
    }

    // test sending the full status of a train.
    @Test
    public void testSendFullStatus() throws java.io.IOException {
        new jmri.jmrit.operations.trains.TrainBuilder().build(InstanceManager.getDefault(TrainManager.class).getTrainById("1"));
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.sendFullStatus(InstanceManager.getDefault(TrainManager.class).getTrainByName("STF"));
        Assert.assertEquals("SendFullStatus Check", "OPERATIONS , TRAIN=STF , TRAINLOCATION=North End Staging , TRAINLENGTH=160 , TRAINWEIGHT=56 , TRAINCARS=4 , TRAINLEADLOCO , TRAINCABOOSE=CP C10099\n", sb.toString());
    }

    // test sending the full status of a train.
    @Test
    public void testTrainPropertyChangeListener() {
        new SimpleOperationsServer(input, output);
        // Building a train causes the property change listener to send
        // full status of the train.
        new jmri.jmrit.operations.trains.TrainBuilder().build(InstanceManager.getDefault(TrainManager.class).getTrainById("1"));
        Assert.assertEquals("SendFullStatus Check", "OPERATIONS , TRAIN=STF , TRAINLOCATION=North End Staging , TRAINLENGTH=160 , TRAINWEIGHT=56 , TRAINCARS=4 , TRAINLEADLOCO , TRAINCABOOSE=CP C10099\n", sb.toString());
    }

    //test parsing an message from the client.
    @Test
    public void testParseTrainRequestStatus() throws jmri.JmriException, java.io.IOException {
        String inputString = "OPERATIONS , TRAIN=STF , TRAINLENGTH , TRAINWEIGHT , TRAINCARS , TRAINLEADLOCO , TRAINCABOOSE , TRAINLOCATION";

        new jmri.jmrit.operations.trains.TrainBuilder().build(InstanceManager.getDefault(TrainManager.class).getTrainById("1"));
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.parseStatus(inputString);
        // parsing the input causes a status report to be generated.
        Assert.assertEquals("Train Command Response Check", "OPERATIONS , TRAIN=STF , TRAINLENGTH=160 , TRAINWEIGHT=56 , TRAINCARS=4 , TRAINCABOOSE=CP C10099 , TRAINLOCATION=North End Staging\n", sb.toString());
    }

    @Test
    public void testParseTrainTerminateRequestStatus() throws jmri.JmriException, java.io.IOException {
        String inputString = "OPERATIONS , TRAIN=STF , TERMINATE";

        new jmri.jmrit.operations.trains.TrainBuilder().build(InstanceManager.getDefault(TrainManager.class).getTrainById("1"));
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.parseStatus(inputString);
        // parsing the input causes a status report to be generated.
        // in this case, we're checking the report contents using startsWith
        // instead of an assertEquals because the report ends in a date, which
        // changes with each run of the test.  We could generate the date, but
        // that is more work than required to verify the parsing was correct.
        Assert.assertTrue("Terminate Command Response Check " + sb.toString(), sb.toString().startsWith("OPERATIONS , TRAIN=STF , TRAINLOCATION= , TRAINLENGTH=0 , TRAINWEIGHT=0 , TRAINCARS=0 , TRAINLEADLOCO , TRAINCABOOSE=\nOPERATIONS , TRAIN=STF , TERMINATE=Terminated"));
    }

    @Test
    public void testParseTrainsRequestStatus() throws jmri.JmriException, java.io.IOException {
        String inputString = "OPERATIONS , TRAINS";

        new jmri.jmrit.operations.trains.TrainBuilder().build(InstanceManager.getDefault(TrainManager.class).getTrainById("1"));
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.parseStatus(inputString);
        // parsing the input causes a status report to be generated.
        Assert.assertEquals("Trains Command Response Check", "OPERATIONS , TRAINS=SFF\nOPERATIONS , TRAINS=STF\n", sb.toString());
    }

    @Test
    public void testParseLocationRequestStatus() throws jmri.JmriException, java.io.IOException {
        String inputString = "OPERATIONS , LOCATIONS";

        new jmri.jmrit.operations.trains.TrainBuilder().build(InstanceManager.getDefault(TrainManager.class).getTrainById("1"));
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.parseStatus(inputString);
        // parsing the input causes a status report to be generated.
        Assert.assertEquals("Locations Command Response Check", "OPERATIONS , LOCATIONS=North End Staging\nOPERATIONS , LOCATIONS=North Industries\nOPERATIONS , LOCATIONS=South End Staging\n", sb.toString());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
        jmri.util.JUnitUtil.initDebugThrottleManager();
        jmri.util.JUnitOperationsUtil.setupOperationsTests();
        jmri.util.JUnitOperationsUtil.initOperationsData();

        sb = new StringBuilder();
        output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
            @Override
            public void write(int b) throws java.io.IOException {
                sb.append((char) b);
            }
        });
        input = new java.io.DataInputStream(System.in);
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
