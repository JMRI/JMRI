/**
 * Tests for the jmri.jmrix.nce.EasyDccConsistManager class
 *
 * @author Paul Bender Copyright (C) 2012,2017
 */
package jmri.jmrix.easydcc;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.util.JUnitUtil;

public class EasyDccConsistManagerTest extends jmri.implementation.AbstractConsistManagerTestBase{

    private EasyDccTrafficControlScaffold t = null;

    // test the initialization loop
    @Test
    public void testInitSequence() {
        EasyDccConsistManager m = (EasyDccConsistManager) cm;
        // we need to call requestUpdateFromLayout() to trigger the 
        // init sequence.
        m.requestUpdateFromLayout();

        for (int i = 1; i < 255; i++) {
            // check "display consist" message sent
            Assert.assertEquals("display consist", i, t.outbound.size());
            if (i < 16) {
                Assert.assertEquals("read message contents", "GD 0" + Integer.toHexString(i).toUpperCase(),
                        ((t.outbound.elementAt(i - 1))).toString());
            } else {
                Assert.assertEquals("read message contents", "GD " + Integer.toHexString(i).toUpperCase(),
                        ((t.outbound.elementAt(i - 1))).toString());
            }
            // reply from programmer arrives
            EasyDccReply r = new EasyDccReply();

            r.setElement(0, 'G');
            r.setElement(1, i < 16 ? '0' : Integer.toHexString(i).charAt(0)); // first hex digit of i
            r.setElement(2, i < 16 ? Integer.toHexString(i).charAt(0) : Integer.toHexString(i).charAt(1)); // second hex digit of i
            if (i == 80) {
                // For consist 80, use data from real hardware
                // provided by Rick Beaber. PAB
                r.setElement(1, '5');
                r.setElement(2, '0');
                r.setElement(3, '0');
                r.setElement(4, '0');
                r.setElement(5, '5');
                r.setElement(6, '0');
                r.setElement(7, '0');
                r.setElement(8, '1');
                r.setElement(9, '1');
                r.setElement(10, '8');
                r.setElement(11, '8');
                r.setElement(12, '1');
                r.setElement(13, '2');
                r.setElement(14, '1');
                r.setElement(15, 0x0D);
            } else if (i < 254) {
                // for the rest of the first 254 consists, reply with 
                // an empty consist
                r.setElement(3, ' ');
                r.setElement(4, '0');
                r.setElement(5, '0');
                r.setElement(6, '0');
                r.setElement(7, '0');
                r.setElement(8, 0x0D);
            } else {
                // for the last consist, reply with a non-empty consist.
                // the data here is from the EasyDCC manual.
                r.setElement(3, ' ');
                r.setElement(4, '0');
                r.setElement(5, '5');
                r.setElement(6, '0');
                r.setElement(7, 'F');
                r.setElement(8, ' ');
                r.setElement(9, '0');
                r.setElement(10, '7');
                r.setElement(11, '3');
                r.setElement(12, '6');
                r.setElement(13, ' ');
                r.setElement(14, '1');
                r.setElement(15, '9');
                r.setElement(16, '4');
                r.setElement(17, 'A');
                r.setElement(18, 0x0D);
            }
            t.sendTestReply(r);

        }
        // check and make sure the last consist was created
        EasyDccConsist c = (EasyDccConsist) m.getConsist(new jmri.DccLocoAddress(255, true));
        Assert.assertNotNull(c);

    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        EasyDccSystemConnectionMemo memo = new EasyDccSystemConnectionMemo("E", "EasyDCC Test");
        t = new EasyDccTrafficControlScaffold(memo);
        memo.setEasyDccTrafficController(t); // important for successful getTrafficController()
        cm = new EasyDccConsistManager(memo);
    }

    @After
    @Override
    public void tearDown() {
        cm = null;
        t.terminateThreads();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EasyDccConsistManagerTest.class);

}
