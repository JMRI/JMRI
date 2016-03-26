package jmri.jmrix.powerline.cm11;

import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialTrafficController;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the cm11.SpecficReply class.
 *
 * @author	Bob Jacobsen Copyright 2003, 2007, 2008, 2010 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 * @version	$Revision$
 */
public class SpecificReplyTest extends TestCase {

    SerialTrafficController t = null;
    SerialSystemConnectionMemo memo = null;

    public void testCreate() {
        memo = new jmri.jmrix.powerline.cm11.SpecificSystemConnectionMemo();
        t = new SpecificTrafficController(memo);
        SpecificReply m = new SpecificReply(t);
        Assert.assertNotNull("exists", m);
    }

    public void testBytesToString() {
        SpecificReply m = new SpecificReply(t);
        m.setOpCode(0x81);
        m.setElement(1, (byte) 0x02);
        m.setElement(2, (byte) 0xA2);
        m.setElement(3, (byte) 0x00);
        Assert.assertEquals("string compare ", "81 02 A2 00", m.toString());
    }

    // from here down is testing infrastructure
    public SpecificReplyTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SpecificReplyTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SpecificReplyTest.class);
        return suite;
    }

}
