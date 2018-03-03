package jmri.jmris.simpleserver.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link jmri.jmris.simpleserver.parser.JmriServerParser} class.
 *
 * @author Paul Bender
 */
public class JmriServerParserTest {

    @Test
    public void testParseFailure() {
        boolean exceptionOccured = false;
        String code = "ON POWER\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        jmri.util.JUnitAppender.assertErrorMessage("Recovery after Parse Exception");
        // the parser now recovers from a parse exception by skipping
        // to the end of the line, so the exception should not occur.
        Assert.assertFalse(exceptionOccured);
    }

    // test valid power commands.
    @Test
    public void testSetPowerOn() {
        boolean exceptionOccured = false;
        String code = "POWER ON\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSetPowerOff() {
        boolean exceptionOccured = false;
        String code = "POWER OFF\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetPower() {
        boolean exceptionOccured = false;
        String code = "POWER\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

   // test valid Turnout related commands
   
    @Test
    public void testTurnoutProduction() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1 THROWN\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.turnout();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTurnoutDeviceProduction() {
        boolean exceptionOccured = false;
        String code = "IT1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.token_source.SwitchTo(JmriServerParserConstants.DEVICENAME);
            p.turnoutdevice();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSetTurnoutThrown() {
        boolean exceptionOccured = false;
        String code = "TURNOUTIT1 THROWN\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testTurnoutCmdClosed() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1 CLOSED\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.turnoutcmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSetTurnoutClosed() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1 CLOSED\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }
 
    @Test
    public void testGetTurnoutCmdStatus() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.turnoutcmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetTurnoutStatus() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // test valid Light related commands
   
    @Test
    public void testLightProduction() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1 ON\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.light();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testLightDeviceProduction() {
        boolean exceptionOccured = false;
        String code = "IL1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.token_source.SwitchTo(JmriServerParserConstants.DEVICENAME);
            p.lightdevice();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSetLightOn() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1 ON\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testLightCmdOff() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1 OFF\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.lightcmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSetLightOff() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1 OFF\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetLightCmdStatus() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.lightcmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetLightStatus() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // test valid Reporter related commands
   
    @Test
    public void testReporterProduction() {
        boolean exceptionOccured = false;
        String code = "REPORTER IR1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.reporter();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testReporterDeviceProduction() {
        boolean exceptionOccured = false;
        String code = "IR1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.token_source.SwitchTo(JmriServerParserConstants.DEVICENAME);
            p.reporterdevice();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetReporterCmd() {
        boolean exceptionOccured = false;
        String code = "REPORTER IR1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.reportercmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetReporterStatus() {
        boolean exceptionOccured = false;
        String code = "REPORTER IR1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    // test valid Sensor related commands
   
    @Test
    public void testSensorProduction() {
        boolean exceptionOccured = false;
        String code = "Sensor IS1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.sensor();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSensorDeviceProduction() {
        boolean exceptionOccured = false;
        String code = "IS1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.token_source.SwitchTo(JmriServerParserConstants.DEVICENAME);
            p.sensordevice();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSensorCmd() {
        boolean exceptionOccured = false;
        String code = "Sensor IS1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.sensorcmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetSensorStatus() {
        boolean exceptionOccured = false;
        String code = "Sensor IS1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }


    // test operations related commands.
    @Test
    public void testGetOperationsTrains() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAINS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetOperationsLocations() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS LOCATIONS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetTrainLocation() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINLOCATION\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testSetTrainLocation() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINLOCATION=ABCD\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetTrainWeight() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINWEIGHT\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetTrainCars() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINCARS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetTrainLeadLoco() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINLEADLOCO\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetTrainCaboose() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINCABOOSE\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetTrainStatus() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINSTATUS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Test
    public void testGetTerminanteTrain() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TERMINATE TRAIN=ABC1234\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        Assert.assertFalse(exceptionOccured);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
