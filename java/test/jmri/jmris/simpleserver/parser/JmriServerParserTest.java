package jmri.jmris.simpleserver.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests for the {@link jmri.jmris.simpleserver.parser.JmriServerParser} class.
 *
 * @author Paul Bender
 */
public class JmriServerParserTest {

    @Test
    public void testParseFailure() {
        String code = "ON POWER\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
        jmri.util.JUnitAppender.assertErrorMessage("Recovery after Parse Exception");
        // the parser now recovers from a parse exception by skipping
        // to the end of the line, so the exception should not occur.
        assertThat(thrown).isNull();
    }

    // test valid power commands.
    @Test
    public void testSetPowerOn() {
        String code = "POWER ON\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testSetPowerOff() {
        String code = "POWER OFF\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetPower() {
        String code = "POWER\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

   // test valid Turnout related commands
   
    @Test
    public void testTurnoutProduction() {
        String code = "TURNOUT IT1 THROWN\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::turnout);
assertThat(thrown).isNull();
    }

    @Test
    public void testTurnoutDeviceProduction() {
        String code = "IT1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        p.token_source.SwitchTo(JmriServerParserConstants.DEVICENAME);
Throwable thrown = catchThrowable(p::turnoutdevice);
assertThat(thrown).isNull();
    }

    @Test
    public void testSetTurnoutThrown() {
        String code = "TURNOUTIT1 THROWN\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testTurnoutCmdClosed() {
        String code = "TURNOUT IT1 CLOSED\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::turnoutcmd);
assertThat(thrown).isNull();
    }

    @Test
    public void testSetTurnoutClosed() {
        String code = "TURNOUT IT1 CLOSED\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }
 
    @Test
    public void testGetTurnoutCmdStatus() {
        String code = "TURNOUT IT1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::turnoutcmd);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetTurnoutStatus() {
        String code = "TURNOUT IT1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    // test valid Light related commands
   
    @Test
    public void testLightProduction() {
        String code = "LIGHT IL1 ON\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::light);
assertThat(thrown).isNull();
    }

    @Test
    public void testLightDeviceProduction() {
        String code = "IL1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        p.token_source.SwitchTo(JmriServerParserConstants.DEVICENAME);
Throwable thrown = catchThrowable(p::lightdevice);
assertThat(thrown).isNull();
    }

    @Test
    public void testSetLightOn() {
        String code = "LIGHT IL1 ON\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testLightCmdOff() {
        String code = "LIGHT IL1 OFF\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::lightcmd);
assertThat(thrown).isNull();
    }

    @Test
    public void testSetLightOff() {
        String code = "LIGHT IL1 OFF\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetLightCmdStatus() {
        String code = "LIGHT IL1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::lightcmd);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetLightStatus() {
        String code = "LIGHT IL1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    // test valid Reporter related commands
   
    @Test
    public void testReporterProduction() {
        String code = "REPORTER IR1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::reporter);
assertThat(thrown).isNull();
    }

    @Test
    public void testReporterDeviceProduction() {
        String code = "IR1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        p.token_source.SwitchTo(JmriServerParserConstants.DEVICENAME);
Throwable thrown = catchThrowable(p::reporterdevice);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetReporterCmd() {
        String code = "REPORTER IR1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::reportercmd);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetReporterStatus() {
        String code = "REPORTER IR1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    // test valid Sensor related commands
   
    @Test
    public void testSensorProduction() {
        String code = "Sensor IS1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::sensor);
assertThat(thrown).isNull();
    }

    @Test
    public void testSensorDeviceProduction() {
        String code = "IS1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        p.token_source.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Throwable thrown = catchThrowable(p::sensordevice);
        assertThat(thrown).isNull();
    }

    @Test
    public void testSensorCmd() {
        String code = "Sensor IS1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::sensorcmd);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetSensorStatus() {
        String code = "Sensor IS1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }


    // test operations related commands.
    @Test
    public void testGetOperationsTrains() {
        String code = "OPERATIONS TRAINS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetOperationsLocations() {
        String code = "OPERATIONS LOCATIONS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetTrainLocation() {
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINLOCATION\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testSetTrainLocation() {
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINLOCATION=ABCD\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetTrainWeight() {
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINWEIGHT\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetTrainCars() {
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINCARS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetTrainLeadLoco() {
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINLEADLOCO\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetTrainCaboose() {
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINCABOOSE\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetTrainStatus() {
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINSTATUS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @Test
    public void testGetTerminanteTrain() {
        String code = "OPERATIONS TERMINATE TRAIN=ABC1234\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        Throwable thrown = catchThrowable(p::command);
assertThat(thrown).isNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
