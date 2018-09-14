package jmri.jmrix.powerline.cm11;

import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the cm11.SpecficReply class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008, 2010 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
  */
public class SpecificReplyTest extends jmri.jmrix.AbstractMessageTestBase {

    SerialTrafficController t = null;
    SerialSystemConnectionMemo memo = null;
    SpecificReply msg = null;

    @Override
    @Test
    public void testToString() {
        msg.setOpCode(0x81);
        msg.setElement(1, (byte) 0x02);
        msg.setElement(2, (byte) 0xA2);
        msg.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", msg.toString());
    }

    @Override
    @Before
    public void setUp(){
	JUnitUtil.setUp();
        memo = new SpecificSystemConnectionMemo();
        t = new SpecificTrafficController(memo);
        m = msg = new SpecificReply(t);
    }

    @After
    public void tearDown() {
	memo = null;
	t = null;
	m = msg = null;
	JUnitUtil.tearDown();
    }

}
