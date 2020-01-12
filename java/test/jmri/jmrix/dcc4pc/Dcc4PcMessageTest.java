package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Dcc4PcMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    // check contents of canned messages
    @Test
    public void checkProgModeMessage(){
       Assert.assertNotNull(Dcc4PcMessage.getProgMode());
       // source says the message is not supported, so no
       // content is added to the message for us to test at this
       // point in time.
    } 

    @Test
    public void checkExitProgModeMessage(){
       Assert.assertNotNull(Dcc4PcMessage.getExitProgMode());
       // source says the message is not supported, so no
       // content is added to the message for us to test at this
       // point in time.
    } 

    @Test
    public void checkGetInfo(){
       Dcc4PcMessage m = Dcc4PcMessage.getInfo(1);
       Assert.assertNotNull(m);
       Assert.assertTrue("for child",m.isForChildBoard());
       Assert.assertFalse("get response",m.isGetResponse());
       Assert.assertEquals("board",1,m.getBoard());
       Assert.assertEquals("message type",Dcc4PcMessage.INFO,m.getMessageType());
       Assert.assertEquals("retries",2,m.getRetries());
       Assert.assertEquals(0x0b,m.getElement(0));
       Assert.assertEquals(1,m.getElement(1));
       Assert.assertEquals(Dcc4PcMessage.INFO,m.getElement(2));
    } 

    @Test
    public void checkGetDescription(){
       Dcc4PcMessage m = Dcc4PcMessage.getDescription(1);
       Assert.assertNotNull(m);
       Assert.assertTrue("for child",m.isForChildBoard());
       Assert.assertFalse("get response",m.isGetResponse());
       Assert.assertEquals("board",1,m.getBoard());
       Assert.assertEquals("message type",Dcc4PcMessage.DESC,m.getMessageType());
       Assert.assertEquals("retries",1,m.getRetries());
       Assert.assertEquals(0x0b,m.getElement(0));
       Assert.assertEquals(1,m.getElement(1));
       Assert.assertEquals(Dcc4PcMessage.DESC,m.getElement(2));
    } 

    @Test
    public void checkGetSerialNumber(){
       Dcc4PcMessage m = Dcc4PcMessage.getSerialNumber(1);
       Assert.assertNotNull(m);
       Assert.assertTrue("for child",m.isForChildBoard());
       Assert.assertFalse("get response",m.isGetResponse());
       Assert.assertEquals("board",1,m.getBoard());
       Assert.assertEquals("message type",Dcc4PcMessage.SERIAL,m.getMessageType());
       Assert.assertEquals("retries",1,m.getRetries());
       Assert.assertEquals(0x0b,m.getElement(0));
       Assert.assertEquals(1,m.getElement(1));
       Assert.assertEquals(Dcc4PcMessage.SERIAL,m.getElement(2));
    } 

    @Test
    public void checkResetBoardData(){
       Dcc4PcMessage m = Dcc4PcMessage.resetBoardData(1);
       Assert.assertNotNull(m);
       Assert.assertTrue("for child",m.isForChildBoard());
       Assert.assertFalse("get response",m.isGetResponse());
       Assert.assertEquals("board",1,m.getBoard());
       Assert.assertEquals("message type",Dcc4PcMessage.CHILDRESET,m.getMessageType());
       Assert.assertEquals("retries",1,m.getRetries());
       Assert.assertEquals(0x0b,m.getElement(0));
       Assert.assertEquals(1,m.getElement(1));
       Assert.assertEquals(Dcc4PcMessage.CHILDRESET,m.getElement(2));
    } 

    @Test
    public void checkPollBoard(){
       Dcc4PcMessage m = Dcc4PcMessage.pollBoard(1);
       Assert.assertNotNull(m);
       Assert.assertTrue("for child",m.isForChildBoard());
       Assert.assertFalse("get response",m.isGetResponse());
       Assert.assertEquals("board",1,m.getBoard());
       Assert.assertEquals("message type",Dcc4PcMessage.CHILDPOLL,m.getMessageType());
       Assert.assertEquals("retries",1,m.getRetries());
       Assert.assertEquals("timeout",500,m.getTimeout());
       Assert.assertEquals(0x0b,m.getElement(0));
       Assert.assertEquals(1,m.getElement(1));
       Assert.assertEquals(Dcc4PcMessage.CHILDPOLL,m.getElement(2));
    }
 
    @Test
    public void checkGetEnabledInputs(){
       Dcc4PcMessage m = Dcc4PcMessage.getEnabledInputs(1);
       Assert.assertNotNull(m);
       Assert.assertTrue("for child",m.isForChildBoard());
       Assert.assertFalse("get response",m.isGetResponse());
       Assert.assertEquals("board",1,m.getBoard());
       Assert.assertEquals("message type",Dcc4PcMessage.CHILDENABLEDINPUTS,m.getMessageType());
       Assert.assertEquals("retries",1,m.getRetries());
       Assert.assertEquals(0x0b,m.getElement(0));
       Assert.assertEquals(1,m.getElement(1));
       Assert.assertEquals(Dcc4PcMessage.CHILDENABLEDINPUTS,m.getElement(2));
    } 

    @Test
    public void checkParentInfo(){
       Dcc4PcMessage m = Dcc4PcMessage.getInfo();
       Assert.assertNotNull(m);
       Assert.assertFalse("for child",m.isForChildBoard());
       Assert.assertFalse("get response",m.isGetResponse());
       Assert.assertEquals("board",-1,m.getBoard());
       Assert.assertEquals("message type",Dcc4PcMessage.INFO,m.getMessageType());
       Assert.assertEquals("retries",1,m.getRetries());
       Assert.assertEquals(Dcc4PcMessage.INFO,m.getElement(0));
    } 

    @Test
    public void checkParentResponse(){
       Dcc4PcMessage m = Dcc4PcMessage.getResponse();
       Assert.assertNotNull(m);
       Assert.assertFalse("for child",m.isForChildBoard());
       Assert.assertTrue("get response",m.isGetResponse());
       Assert.assertEquals("board",-1,m.getBoard());
       Assert.assertEquals("message type",Dcc4PcMessage.RESPONSE,m.getMessageType());
       Assert.assertEquals("retries",1,m.getRetries());
       Assert.assertEquals(Dcc4PcMessage.RESPONSE,m.getElement(0));
    } 

    @Test
    public void checkParentDescription(){
       Dcc4PcMessage m = Dcc4PcMessage.getDescription();
       Assert.assertNotNull(m);
       Assert.assertFalse("for child",m.isForChildBoard());
       Assert.assertFalse("get response",m.isGetResponse());
       Assert.assertEquals("board",-1,m.getBoard());
       Assert.assertEquals("message type",Dcc4PcMessage.DESC,m.getMessageType());
       Assert.assertEquals("retries",1,m.getRetries());
       Assert.assertEquals(Dcc4PcMessage.DESC,m.getElement(0));
    } 

    @Test
    public void checkParentSerialNumber(){
       Dcc4PcMessage m = Dcc4PcMessage.getSerialNumber();
       Assert.assertNotNull(m);
       Assert.assertFalse("for child",m.isForChildBoard());
       Assert.assertFalse("get response",m.isGetResponse());
       Assert.assertEquals("board",-1,m.getBoard());
       Assert.assertEquals("message type",Dcc4PcMessage.SERIAL,m.getMessageType());
       Assert.assertEquals("retries",1,m.getRetries());
       Assert.assertEquals(Dcc4PcMessage.SERIAL,m.getElement(0));
    } 

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new Dcc4PcMessage(5);
    }

    @After
    public void tearDown() {
	m = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcMessageTest.class);

}
