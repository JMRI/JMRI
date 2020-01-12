package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Z21XNetMessageTest extends jmri.jmrix.lenz.XNetMessageTest {

    // Test the string constructor.
    @Override
    @Test
    public void testStringCtor() {
        msg = new Z21XNetMessage("12 34 AB 3 19 6 B B1");
        Assert.assertEquals("length", 8, msg.getNumDataElements());
        Assert.assertEquals("0th byte", 0x12, msg.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x34, msg.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0xAB, msg.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x03, msg.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x19, msg.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x06, msg.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0x0B, msg.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0xB1, msg.getElement(7) & 0xFF);
    }

    @Test
    @Override
    public void testStringCtorEmptyString() {
        msg= new Z21XNetMessage("");
        Assert.assertEquals("length", 0, msg.getNumDataElements());
        Assert.assertTrue("empty reply",msg.toString().equals(""));
    }

    @Test
    @Override
    public void testCtorXNetReply(){
        Z21XNetReply x = new Z21XNetReply("12 34 AB 03 19 06 0B B1");
        msg = new Z21XNetMessage(x);
        Assert.assertEquals("length", x.getNumDataElements(), msg.getNumDataElements());
        Assert.assertEquals("0th byte", x.getElement(0)& 0xFF, msg.getElement(0)& 0xFF);
        Assert.assertEquals("1st byte", x.getElement(1)& 0xFF, msg.getElement(1)& 0xFF);
        Assert.assertEquals("2nd byte", x.getElement(2)& 0xFF, msg.getElement(2)& 0xFF);
        Assert.assertEquals("3rd byte", x.getElement(3)& 0xFF, msg.getElement(3)& 0xFF);
        Assert.assertEquals("4th byte", x.getElement(4)& 0xFF, msg.getElement(4)& 0xFF);
        Assert.assertEquals("5th byte", x.getElement(5)& 0xFF, msg.getElement(5)& 0xFF);
        Assert.assertEquals("6th byte", x.getElement(6)& 0xFF, msg.getElement(6)& 0xFF);
        Assert.assertEquals("7th byte", x.getElement(7)& 0xFF, msg.getElement(7)& 0xFF);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = msg = new Z21XNetMessage(3);
    }

    @After
    @Override
    public void tearDown() {
        m = msg = null;
        JUnitUtil.tearDown();
        // make sure the message timeouts and retries are set to
        // the defaults.
        jmri.jmrix.lenz.XNetMessage.setXNetMessageTimeout(5000); 
        jmri.jmrix.lenz.XNetMessage.setXNetMessageRetries(5); 
    }

    // private final static Logger log = LoggerFactory.getLogger(Z21XNetMessageTest.class);

}
