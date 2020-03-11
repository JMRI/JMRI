package jmri.jmris.simpleserver.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests for the jmri.jmris.simpleserver.parser.JmriServerTokenizer class.
 *
 * @author Paul Bender
 */
public class JmriServerTokenizerTest {

    // numeric values
    @Test 
    public void testTokenizeAddr() {
        String cmd = "1234\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.ADDR).withFailMessage("Wrong token kind for ADDR");
        assertThat(t.image).isEqualTo("1234").withFailMessage("Wrong image for ADDR");
    }

    // constants.
    @Test 
    public void testTokenizeONOFF() {
        String cmd = "ON OFF\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.ONOFF).withFailMessage("Wrong token kind for ON");
        t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.ONOFF).withFailMessage("Wrong token kind for OFF");
    }

    @Test 
    public void testTokenizeTHROWNCLOSED() {
        String cmd = "THROWN CLOSED\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.THROWNCLOSED).withFailMessage("Wrong token kind for THROWN");
        t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.THROWNCLOSED).withFailMessage("Wrong token kind for CLOSED");
    }

    // command types
    @Test 
    public void testTokenizePower() {
        String cmd = "POWER ON\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.POWER).withFailMessage("Wrong token kind for POWER");
        t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.ONOFF).withFailMessage("Wrong token kind for ON");
    }

    @Test 
    public void testTokenizeTurnout() {
        String cmd = "TURNOUT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.TURNOUT).withFailMessage("Wrong token kind for TURNOUT");
    }

    @Test 
    public void testTokenizeReporeter() {
        String cmd = "REPORTER\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.REPORTER).withFailMessage("Wrong token kind for REPORTER");
    }

    @Test 
    public void testTokenizeLight() {
        String cmd = "LIGHT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.LIGHT).withFailMessage("Wrong token kind for LIGHT");
    }

    @Test 
    public void testTokenizeSENSOR() {
        String cmd = "SENSOR\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.SENSOR).withFailMessage("Wrong token kind for SENSOR");
    }

    @Test 
    public void testTokenizeThrottle() {
        String cmd = "THROTTLE\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.THROTTLE).withFailMessage("Wrong token kind for THROTTLE");
    }

    @Test 
    public void testTokenizeOperations() {
        String cmd = "OPERATIONS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.OPERATIONS).withFailMessage("Wrong token kind for OPERATIONS");
    }

    @Test 
    public void testTokenizeTrain() {
        String cmd = "TRAIN\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.TRAIN).withFailMessage("Wrong token kind for TRAIN");
    }
    
    @Test 
    public void testTokenizeTrains() {
        String cmd = "TRAINS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.TRAINS).withFailMessage("Wrong token kind for TRAINS");
    }

    @Test 
    public void testTokenizeTrainWeight() {
        String cmd = "TRAINWEIGHT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.TRAINWEIGHT).withFailMessage("Wrong token kind for TRAINWEIGHT");
    }

    @Test 
    public void testTokenizeTrainCars() {
        String cmd = "TRAINCARS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.TRAINCARS).withFailMessage("Wrong token kind for TRAINCARS");
    }

    @Test 
    public void testTokenizeTrainLeadLoco() {
        String cmd = "TRAINLEADLOCO\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.TRAINLEADLOCO).withFailMessage("Wrong token kind for TRAINLEADLOCO");
    }

    @Test 
    public void testTokenizeTrainCaboose() {
        String cmd = "TRAINCABOOSE\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.TRAINCABOOSE).withFailMessage("Wrong token kind for TRAINCABOOSE");
    }

    @Test 
    public void testTokenizeTrainStatus() {
        String cmd = "TRAINSTATUS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.TRAINSTATUS).withFailMessage("Wrong token kind for TRAINSTATUS");
    }

    @Test 
    public void testTokenizeTrainLocation() {
        String cmd = "TRAINLOCATION\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.TRAINLOCATION).withFailMessage("Wrong token kind for TRAINLOCATION");
    }

    @Test 
    public void testTokenizeTerminate() {
        String cmd = "TERMINATE\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.TERMINATE).withFailMessage("Wrong token kind for TERMINANTE");
    }


// device names
    @Test 
    public void testTurnoutDevice() {
        String cmd = "IT1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.JMRITURNOUT).withFailMessage("Wrong token kind for Turnout Name");
    }

    @Test 
    public void testSensorDevice() {
        String cmd = "IS1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.JMRISENSOR).withFailMessage("Wrong token kind for Sensor Name");
    }

    @Test 
    public void testLightDevice() {
        String cmd = "IL1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.JMRILIGHT).withFailMessage("Wrong token kind for Light Name");
    }

    @Test 
    public void testReporterDevice() {
        String cmd = "IR1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        assertThat(t.kind).isEqualTo(JmriServerParserConstants.JMRIREPORTER).withFailMessage("Wrong token kind for Reporter Name");
    }

    // This used to be an error.
    // now should check to see that the token produced
    // is the BADTOKEN token.
    @Test
    public void testTokenizeFailure() {
        Throwable thrown = catchThrowable( () -> {
            String cmd = "this should fail";
            SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
            JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
            stm.getNextToken(); // called to provoke TokenMgrError
        });
        assertThat(thrown).isNotNull();
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
