// JmriServerParserTest.java
package jmri.jmris.simpleserver.parser;

import java.io.StringReader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the {@link jmri.jmris.simpleserver.parser.JmriServerParser} class.
 *
 * @author Paul Bender
 */
public class JmriServerParserTest extends TestCase {

    public void testParseFailure() {
        boolean exceptionOccured = false;
        String code = "POWER SET\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertTrue(exceptionOccured);
    }

    // test valid power commands.
    public void testSetPowerOn() {
        boolean exceptionOccured = false;
        String code = "POWER ON\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSetPowerOff() {
        boolean exceptionOccured = false;
        String code = "POWER OFF\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetPower() {
        boolean exceptionOccured = false;
        String code = "POWER\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

   // test valid Turnout related commands
   
    public void testTurnoutProduction() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1 THROWN\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.turnout();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

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
        assertFalse(exceptionOccured);
    }

    public void testSetTurnoutThrown() {
        boolean exceptionOccured = false;
        String code = "TURNOUTIT1 THROWN\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testTurnoutCmdClosed() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1 CLOSED\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.turnoutcmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSetTurnoutClosed() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1 CLOSED\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }
 
   public void testGetTurnoutCmdStatus() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.turnoutcmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetTurnoutStatus() {
        boolean exceptionOccured = false;
        String code = "TURNOUT IT1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

   // test valid Light related commands
   
    public void testLightProduction() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1 ON\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.light();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

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
        assertFalse(exceptionOccured);
    }

    public void testSetLightOn() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1 ON\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testLightCmdOff() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1 OFF\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.lightcmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testSetLightOff() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1 OFF\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetLightCmdStatus() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.lightcmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetLightStatus() {
        boolean exceptionOccured = false;
        String code = "LIGHT IL1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

   // test valid Reporter related commands
   
    public void testReporterProduction() {
        boolean exceptionOccured = false;
        String code = "REPORTER IR1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.reporter();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

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
        assertFalse(exceptionOccured);
    }

    public void testGetReporterCmd() {
        boolean exceptionOccured = false;
        String code = "REPORTER IR1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.reportercmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetReporterStatus() {
        boolean exceptionOccured = false;
        String code = "REPORTER IR1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

   // test valid Sensor related commands
   
    public void testSensorProduction() {
        boolean exceptionOccured = false;
        String code = "Sensor IS1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.sensor();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

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
        assertFalse(exceptionOccured);
    }

    public void testSensorCmd() {
        boolean exceptionOccured = false;
        String code = "Sensor IS1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.sensorcmd();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetSensorStatus() {
        boolean exceptionOccured = false;
        String code = "Sensor IS1\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }


// test operations related commands.
    public void testGetOperationsTrains() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAINS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetOperationsLocations() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS LOCATIONS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    public void testGetTrainLocation() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINLOCATION\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }
    public void testSetTrainLocation() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINLOCATION=ABCD\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }
    public void testGetTrainWeight() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINWEIGHT\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }
    public void testGetTrainCars() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINCARS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }
    public void testGetTrainLeadLoco() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINLEADLOCO\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }
    public void testGetTrainCaboose() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINCABOOSE\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }
    public void testGetTrainStatus() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TRAIN=ABC1234 , TRAINSTATUS\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }
    public void testGetTerminanteTrain() {
        boolean exceptionOccured = false;
        String code = "OPERATIONS TERMINATE TRAIN=ABC1234\n\r";
        JmriServerParser p = new JmriServerParser(new StringReader(code));
        try {
            p.command();
        } catch (ParseException pe) {
            exceptionOccured = true;
        }
        assertFalse(exceptionOccured);
    }

    // Main entry point

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JmriServerParserTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(JmriServerParserTest.class);
        return suite;

    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
