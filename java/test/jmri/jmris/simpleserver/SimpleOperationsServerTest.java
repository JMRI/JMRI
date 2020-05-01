package jmri.jmris.simpleserver;

import jmri.InstanceManager;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

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
        assertThat(a).isNotNull();
    }

    @Test
    public void testConnectionCtor() {
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleOperationsServer a = new SimpleOperationsServer(jcs);
        assertThat(a).isNotNull();
    }

    // test sending a message.
    @Test
    public void testSendMessage() {
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        // NOTE: this test uses reflection to test a private method.
        Throwable thrown = catchThrowable( () -> {
                java.lang.reflect.Method sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
                sendMessageMethod.setAccessible(true);
                sendMessageMethod.invoke(a, "Hello World");
        });
        assertThat(thrown).withFailMessage("Could not execute sendMessage via reflection {}",thrown).isNull();
        assertThat(sb.toString()).withFailMessage("SendMessage Check").isEqualTo("Hello World");
    }

    // test sending a message.
    @Test
    public void testSendMessageWithConnection() {
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleOperationsServer a = new SimpleOperationsServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        Throwable thrown = catchThrowable( () -> {
            java.lang.reflect.Method sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
            // override the default permissions.
            sendMessageMethod.setAccessible(true);
            sendMessageMethod.invoke(a, "Hello World");
        });
        assertThat(thrown).withFailMessage("Could not execute sendMessage via reflection {}",thrown).isNull();
        assertThat(jcs.getOutput()).withFailMessage("SendMessage Check").isEqualTo("Hello World");
    }

    // test sending the train list.
    @Test
    public void testSendTrainList() {
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.sendTrainList();
        assertThat(sb.toString()).withFailMessage("SendTrainList Check").isEqualTo("OPERATIONS , TRAINS=SFF\nOPERATIONS , TRAINS=STF\n");
    }

    // test sending the locations list.
    @Test
    public void testSendLocationList() {
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.sendLocationList();
        assertThat(sb.toString()).withFailMessage("SendLocationList Check").isEqualTo("OPERATIONS , LOCATIONS=North End Staging\nOPERATIONS , LOCATIONS=North Industries\nOPERATIONS , LOCATIONS=South End Staging\n");
    }

    // test sending the full status of a train.
    @Test
    public void testSendFullStatus() throws java.io.IOException {
        new jmri.jmrit.operations.trains.TrainBuilder().build(InstanceManager.getDefault(TrainManager.class).getTrainById("1"));
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.sendFullStatus(InstanceManager.getDefault(TrainManager.class).getTrainByName("STF"));
        assertThat(sb.toString()).withFailMessage("SendFullStatus Check").isEqualTo("OPERATIONS , TRAIN=STF , TRAINLOCATION=North End Staging , TRAINLENGTH=160 , TRAINWEIGHT=56 , TRAINCARS=4 , TRAINLEADLOCO , TRAINCABOOSE=CP C10099\n");
    }

    // test sending the full status of a train.
    @Test
    public void testTrainPropertyChangeListener() {
        new SimpleOperationsServer(input, output);
        // Building a train causes the property change listener to send
        // full status of the train.
        new jmri.jmrit.operations.trains.TrainBuilder().build(InstanceManager.getDefault(TrainManager.class).getTrainById("1"));
        assertThat(sb.toString()).withFailMessage("SendFullStatus Check").isEqualTo("OPERATIONS , TRAIN=STF , TRAINLOCATION=North End Staging , TRAINLENGTH=160 , TRAINWEIGHT=56 , TRAINCARS=4 , TRAINLEADLOCO , TRAINCABOOSE=CP C10099\n");
    }

    //test parsing an message from the client.
    @Test
    public void testParseTrainRequestStatus() throws jmri.JmriException, java.io.IOException {
        String inputString = "OPERATIONS , TRAIN=STF , TRAINLENGTH , TRAINWEIGHT , TRAINCARS , TRAINLEADLOCO , TRAINCABOOSE , TRAINLOCATION";

        new jmri.jmrit.operations.trains.TrainBuilder().build(InstanceManager.getDefault(TrainManager.class).getTrainById("1"));
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.parseStatus(inputString);
        // parsing the input causes a status report to be generated.
        assertThat(sb.toString()).withFailMessage("Train Command Response Check").isEqualTo("OPERATIONS , TRAIN=STF , TRAINLENGTH=160 , TRAINWEIGHT=56 , TRAINCARS=4 , TRAINCABOOSE=CP C10099 , TRAINLOCATION=North End Staging\n");
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
        assertThat(sb.toString())
                .withFailMessage("Terminate Command Response Check {}",sb)
                .startsWith("OPERATIONS , TRAIN=STF , TRAINLOCATION= , TRAINLENGTH=0 , TRAINWEIGHT=0 , TRAINCARS=0 , TRAINLEADLOCO , TRAINCABOOSE=\nOPERATIONS , TRAIN=STF , TERMINATE=Terminated");
    }

    @Test
    public void testParseTrainsRequestStatus() throws jmri.JmriException, java.io.IOException {
        String inputString = "OPERATIONS , TRAINS";

        new jmri.jmrit.operations.trains.TrainBuilder().build(InstanceManager.getDefault(TrainManager.class).getTrainById("1"));
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.parseStatus(inputString);
        // parsing the input causes a status report to be generated.
        assertThat(sb.toString()).withFailMessage("Trains Command Response Check").isEqualTo("OPERATIONS , TRAINS=SFF\nOPERATIONS , TRAINS=STF\n");
    }

    @Test
    public void testParseLocationRequestStatus() throws jmri.JmriException, java.io.IOException {
        String inputString = "OPERATIONS , LOCATIONS";

        new jmri.jmrit.operations.trains.TrainBuilder().build(InstanceManager.getDefault(TrainManager.class).getTrainById("1"));
        SimpleOperationsServer a = new SimpleOperationsServer(input, output);
        a.parseStatus(inputString);
        // parsing the input causes a status report to be generated.
        assertThat(sb.toString()).withFailMessage("Locations Command Response Check").isEqualTo("OPERATIONS , LOCATIONS=North End Staging\nOPERATIONS , LOCATIONS=North Industries\nOPERATIONS , LOCATIONS=South End Staging\n");
    }

    @BeforeEach
    public void setUp() {
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
            public void write(int b) {
                sb.append((char) b);
            }
        });
        input = new java.io.DataInputStream(System.in);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
