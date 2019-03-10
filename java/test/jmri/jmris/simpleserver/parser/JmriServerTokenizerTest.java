package jmri.jmris.simpleserver.parser;

import java.io.StringReader;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertEquals("Wrong token kind for ADDR", JmriServerParserConstants.ADDR, t.kind);
        Assert.assertEquals("Wrong image for ADDR", "1234", t.image);
    }

    // constants.
    @Test 
    public void testTokenizeONOFF() {
        String cmd = "ON OFF\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertEquals("Wrong token kind for ON", JmriServerParserConstants.ONOFF,t.kind);
        t = stm.getNextToken();
        Assert.assertEquals("Wrong token kind for OFF", JmriServerParserConstants.ONOFF,t.kind);
    }

    @Test 
    public void testTokenizeTHROWNCLOSED() {
        String cmd = "THROWN CLOSED\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertEquals("Wrong token kind for THROWN", JmriServerParserConstants.THROWNCLOSED,t.kind);
        t = stm.getNextToken();
        Assert.assertEquals("Wrong token kind for CLOSED", JmriServerParserConstants.THROWNCLOSED,t.kind);
    }

    // command types
    @Test 
    public void testTokenizePower() {
        String cmd = "POWER ON\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertEquals("Wrong token kind for POWER", JmriServerParserConstants.POWER,t.kind);
        t = stm.getNextToken();
        Assert.assertEquals("Wrong token kind for ON", JmriServerParserConstants.ONOFF,t.kind);
    }

    @Test 
    public void testTokenizeTurnout() {
        String cmd = "TURNOUT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TURNOUT", JmriServerParserConstants.TURNOUT == t.kind);
    }

    @Test 
    public void testTokenizeReporeter() {
        String cmd = "REPORTER\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for REPORTER", JmriServerParserConstants.REPORTER == t.kind);
    }

    @Test 
    public void testTokenizeLight() {
        String cmd = "LIGHT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for LIGHT", JmriServerParserConstants.LIGHT == t.kind);
    }

    @Test 
    public void testTokenizeSENSOR() {
        String cmd = "SENSOR\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for SENSOR", JmriServerParserConstants.SENSOR == t.kind);
    }

    @Test 
    public void testTokenizeThrottle() {
        String cmd = "THROTTLE\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for THROTTLE", JmriServerParserConstants.THROTTLE == t.kind);
    }

    @Test 
    public void testTokenizeOperations() {
        String cmd = "OPERATIONS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for OPERATIONS", JmriServerParserConstants.OPERATIONS == t.kind);
    }

    @Test 
    public void testTokenizeTrain() {
        String cmd = "TRAIN\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TRAIN", JmriServerParserConstants.TRAIN == t.kind);
    }
    
    @Test 
    public void testTokenizeTrains() {
        String cmd = "TRAINS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TRAINS", JmriServerParserConstants.TRAINS == t.kind);
    }

    @Test 
    public void testTokenizeTrainWeight() {
        String cmd = "TRAINWEIGHT\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TRAINWEIGHT", JmriServerParserConstants.TRAINWEIGHT == t.kind);
    }

    @Test 
    public void testTokenizeTrainCars() {
        String cmd = "TRAINCARS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TRAINCARS", JmriServerParserConstants.TRAINCARS == t.kind);
    }

    @Test 
    public void testTokenizeTrainLeadLoco() {
        String cmd = "TRAINLEADLOCO\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TRAINLEADLOCO", JmriServerParserConstants.TRAINLEADLOCO == t.kind);
    }

    @Test 
    public void testTokenizeTrainCaboose() {
        String cmd = "TRAINCABOOSE\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TRAINCABOOSE", JmriServerParserConstants.TRAINCABOOSE == t.kind);
    }

    @Test 
    public void testTokenizeTrainStatus() {
        String cmd = "TRAINSTATUS\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TRAINSTATUS", JmriServerParserConstants.TRAINSTATUS == t.kind);
    }

    @Test 
    public void testTokenizeTrainLocation() {
        String cmd = "TRAINLOCATION\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TRAINLOCATION", JmriServerParserConstants.TRAINLOCATION == t.kind);
    }

    @Test 
    public void testTokenizeTerminate() {
        String cmd = "TERMINATE\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for TERMINANTE", JmriServerParserConstants.TERMINATE == t.kind);
    }


// device names
    @Test 
    public void testTurnoutDevice() {
        String cmd = "IT1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for Turnout Name", JmriServerParserConstants.JMRITURNOUT == t.kind);
    }

    @Test 
    public void testSensorDevice() {
        String cmd = "IS1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for Sensor Name", JmriServerParserConstants.JMRISENSOR == t.kind);
    }

    @Test 
    public void testLightDevice() {
        String cmd = "IL1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for Light Name", JmriServerParserConstants.JMRILIGHT == t.kind);
    }

    @Test 
    public void testReporterDevice() {
        String cmd = "IR1\n\r";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.SwitchTo(JmriServerParserConstants.DEVICENAME);
        Token t = stm.getNextToken();
        Assert.assertTrue("Wrong token kind for Reporter Name", JmriServerParserConstants.JMRIREPORTER == t.kind);
    }

    // This used to be an error.
    // now should check to see that the token produced
    // is the BADTOKEN token.
    @Test(expected=TokenMgrError.class)
    public void testTokenizeFailure() throws TokenMgrError {
        String cmd = "this should fail";
        SimpleCharStream cs = new SimpleCharStream(new StringReader(cmd));
        JmriServerParserTokenManager stm = new JmriServerParserTokenManager(cs);
        stm.getNextToken(); // called to provoke TokenMgrError
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
