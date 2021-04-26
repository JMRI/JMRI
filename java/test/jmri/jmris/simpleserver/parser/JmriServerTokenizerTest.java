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
        assertThat(t.kind).withFailMessage("Wrong token kind for ADDR").isEqualTo(JmriServerParserConstants.ADDR);
        assertThat(t.image).withFailMessage("Wrong image for ADDR").isEqualTo("1234");
    }

    // constants.
    @Test 
    public void testTokenizeONOFF() {
        String cmd = "ON OFF\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for ON").isEqualTo(JmriServerParserConstants.ONOFF);
        t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for OFF").isEqualTo(JmriServerParserConstants.ONOFF);
    }

    @Test 
    public void testTokenizeTHROWNCLOSED() {
        String cmd = "THROWN CLOSED\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for THROWN").isEqualTo(JmriServerParserConstants.THROWNCLOSED);
        t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for CLOSED").isEqualTo(JmriServerParserConstants.THROWNCLOSED);
    }

    // command types
    @Test 
    public void testTokenizePower() {
        String cmd = "POWER ON\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for POWER").isEqualTo(JmriServerParserConstants.POWER);
        t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for ON").isEqualTo(JmriServerParserConstants.ONOFF);
    }

    @Test 
    public void testTokenizeTurnout() {
        String cmd = "TURNOUT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TURNOUT").isEqualTo(JmriServerParserConstants.TURNOUT);
    }

    @Test 
    public void testTokenizeReporeter() {
        String cmd = "REPORTER\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for REPORTER").isEqualTo(JmriServerParserConstants.REPORTER);
    }

    @Test 
    public void testTokenizeLight() {
        String cmd = "LIGHT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for LIGHT").isEqualTo(JmriServerParserConstants.LIGHT);
    }

    @Test 
    public void testTokenizeSENSOR() {
        String cmd = "SENSOR\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for SENSOR").isEqualTo(JmriServerParserConstants.SENSOR);
    }

    @Test 
    public void testTokenizeThrottle() {
        String cmd = "THROTTLE\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for THROTTLE").isEqualTo(JmriServerParserConstants.THROTTLE);
    }

    @Test 
    public void testTokenizeOperations() {
        String cmd = "OPERATIONS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for OPERATIONS").isEqualTo(JmriServerParserConstants.OPERATIONS);
    }

    @Test 
    public void testTokenizeTrain() {
        String cmd = "TRAIN\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TRAIN").isEqualTo(JmriServerParserConstants.TRAIN);
    }
    
    @Test 
    public void testTokenizeTrains() {
        String cmd = "TRAINS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TRAINS").isEqualTo(JmriServerParserConstants.TRAINS);
    }

    @Test 
    public void testTokenizeTrainWeight() {
        String cmd = "TRAINWEIGHT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TRAINWEIGHT").isEqualTo(JmriServerParserConstants.TRAINWEIGHT);
    }

    @Test 
    public void testTokenizeTrainCars() {
        String cmd = "TRAINCARS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TRAINCARS").isEqualTo(JmriServerParserConstants.TRAINCARS);
    }

    @Test 
    public void testTokenizeTrainLeadLoco() {
        String cmd = "TRAINLEADLOCO\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TRAINLEADLOCO").isEqualTo(JmriServerParserConstants.TRAINLEADLOCO);
    }

    @Test 
    public void testTokenizeTrainCaboose() {
        String cmd = "TRAINCABOOSE\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TRAINCABOOSE").isEqualTo(JmriServerParserConstants.TRAINCABOOSE);
    }

    @Test 
    public void testTokenizeTrainStatus() {
        String cmd = "TRAINSTATUS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TRAINSTATUS").isEqualTo(JmriServerParserConstants.TRAINSTATUS);
    }

    @Test 
    public void testTokenizeTrainLocation() {
        String cmd = "TRAINLOCATION\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TRAINLOCATION").isEqualTo(JmriServerParserConstants.TRAINLOCATION);
    }

    @Test 
    public void testTokenizeTerminate() {
        String cmd = "TERMINATE\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for TERMINANTE").isEqualTo(JmriServerParserConstants.TERMINATE);
    }


// device names
    @Test 
    public void testTurnoutDevice() {
        String cmd = "IT1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for Turnout Name").isEqualTo(JmriServerParserConstants.JMRITURNOUT);
    }

    @Test 
    public void testSensorDevice() {
        String cmd = "IS1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for Sensor Name").isEqualTo(JmriServerParserConstants.JMRISENSOR);
    }

    @Test 
    public void testLightDevice() {
        String cmd = "IL1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for Light Name").isEqualTo(JmriServerParserConstants.JMRILIGHT);
    }

    @Test 
    public void testReporterDevice() {
        String cmd = "IR1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        assertThat(t.kind).withFailMessage("Wrong token kind for Reporter Name").isEqualTo(JmriServerParserConstants.JMRIREPORTER);
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
