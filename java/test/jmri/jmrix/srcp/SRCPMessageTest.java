package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SRCPMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPMessage class
 *
 * @author	Bob Jacobsen
 * @author  Paul Bender Copyright (C) 2017
 */
public class SRCPMessageTest extends jmri.jmrix.AbstractMessageTestBase {

    private SRCPMessage msg = null;

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        String s = "100 OK REASON GOES HERE\n\r";
        msg = new SRCPMessage(s);
        Assert.assertNotNull(m);
        Assert.assertTrue("String Constructor Correct", s.equals(msg.toString()));
    }

    // check validation methods.
    @Test
    public void checkIsKillMain(){
       SRCPMessage m1 = new SRCPMessage("SET 1 POWER ON\n");
       SRCPMessage m2 = new SRCPMessage("SET 1 POWER OFF\n");
       SRCPMessage m3 = new SRCPMessage("SET 1 PWOER ON\n");
       Assert.assertFalse("POWER ON",m1.isKillMain());
       Assert.assertTrue("POWER OFF",m2.isKillMain());
       Assert.assertFalse("Bad POWER message",m3.isKillMain());
    }

    @Test
    public void checkIsEnableMain(){
       SRCPMessage m1 = new SRCPMessage("SET 1 POWER ON\n");
       SRCPMessage m2 = new SRCPMessage("SET 1 POWER OFF\n");
       SRCPMessage m3 = new SRCPMessage("SET 1 PWOER ON\n");
       Assert.assertTrue("POWER ON",m1.isEnableMain());
       Assert.assertFalse("POWER OFF",m2.isEnableMain());
       Assert.assertFalse("Bad POWER message",m3.isEnableMain());
    } 

    //Test canned message formats
    @Test
    public void checkGetEnableMain(){
      msg = SRCPMessage.getEnableMain();
      Assert.assertEquals("Enable Main Message",new SRCPMessage("SET 1 POWER ON\n"),msg);
      Assert.assertFalse("not binary",msg.isBinary());
    }

    @Test
    public void checkGetKillMain(){
      msg = SRCPMessage.getKillMain();
      Assert.assertEquals("Kill Main Message",new SRCPMessage("SET 1 POWER OFF\n"),msg);
      Assert.assertFalse("not binary",msg.isBinary());
    }

    @Test
    public void checkGetProgMode(){
      Assert.assertEquals("Init Prog Mode Message",new SRCPMessage("INIT 2 SM NMRA\n"),SRCPMessage.getProgMode(2));
    }

    @Test
    public void checkGetExitProgMode(){
      Assert.assertEquals("Exit Prog Mode Message",new SRCPMessage("TERM 2 SM\n"),SRCPMessage.getExitProgMode(2));
    }

    @Test
    public void checkGetReadDirectCV(){
      msg = SRCPMessage.getReadDirectCV(2,19);
      Assert.assertEquals("Read CV in Direct Mode Message",new SRCPMessage("GET 2 SM 0 CV 19\n"),msg);
      Assert.assertEquals("Timeout",180000,msg.getTimeout());
    }

    @Test
    public void checkGetConfirmDirectCV(){
      msg = SRCPMessage.getConfirmDirectCV(2,19,20);
      Assert.assertEquals("Confirm CV in Direct Mode Message",new SRCPMessage("VERIFY 2 SM 0 CV 19 20\n"),msg);
      Assert.assertEquals("Timeout",180000,msg.getTimeout());
    }

    @Test
    public void checkGetWriteDirectCV(){
      msg = SRCPMessage.getWriteDirectCV(2,19,20);
      Assert.assertEquals("Write CV in Direct Mode Message",new SRCPMessage("SET 2 SM 0 CV 19 20\n"),msg);
      Assert.assertEquals("Timeout",180000,msg.getTimeout());
    }

    @Test
    public void checkGetReadBitDirectCV(){
      msg = SRCPMessage.getReadDirectBitCV(2,19,1);
      Assert.assertEquals("Read CV Bit in Direct Mode Message",new SRCPMessage("GET 2 SM 0 CVBIT 19 1\n"),msg);
      Assert.assertEquals("Timeout",180000,msg.getTimeout());
    }

    @Test
    public void checkGetConfirmBitDirectCV(){
      msg = SRCPMessage.getConfirmDirectBitCV(2,19,1,0);
      Assert.assertEquals("Confirm CV in Direct Mode Message",new SRCPMessage("VERIFY 2 SM 0 CVBIT 19 1 0\n"),msg);
      Assert.assertEquals("Timeout",180000,msg.getTimeout());
    }

    @Test
    public void checkGetWriteBitDirectCV(){
      msg = SRCPMessage.getWriteDirectBitCV(2,19,1,0);
      Assert.assertEquals("Write CV in Direct Mode Message",new SRCPMessage("SET 2 SM 0 CVBIT 19 1 0\n"),msg);
      Assert.assertEquals("Timeout",180000,msg.getTimeout());
    }

    @Test
    public void checkGetReadRegister(){
      msg = SRCPMessage.getReadRegister(2,2);
      Assert.assertEquals("Read Register Message",new SRCPMessage("GET 2 SM 0 REG 2\n"),msg);
      Assert.assertEquals("Timeout",180000,msg.getTimeout());
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkGetReadRegisterIlegalArgument(){
      SRCPMessage.getReadRegister(2,10);
    }

    @Test
    public void checkGetConfirmRegister(){
      msg = SRCPMessage.getConfirmRegister(2,2,5);
      Assert.assertEquals("Confirm Register Message",new SRCPMessage("VERIFY 2 SM 0 REG 2 5\n"),msg);
      Assert.assertEquals("Timeout",180000,msg.getTimeout());
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkGetConfirmRegisterIlegalArgument(){
      SRCPMessage.getConfirmRegister(2,10,5);
    }

    @Test
    public void checkGetWriteRegister(){
      msg = SRCPMessage.getWriteRegister(2,2,5);
      Assert.assertEquals("Write Register Message",new SRCPMessage("SET 2 SM 0 REG 2 5\n"),msg);
      Assert.assertEquals("Timeout",180000,msg.getTimeout());
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkGetWriteRegisterIlegalArgument(){
      SRCPMessage.getWriteRegister(2,10,5);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new SRCPMessage();
    }

    @After
    public void tearDown() {
        m = msg = null;
        JUnitUtil.tearDown();
    }
}
