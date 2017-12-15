package jmri.jmrix.grapevine;

import jmri.implementation.AbstractLightTestBase;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the jmri.jmrix.grapevine.SerialLight class, low address.
 *
 * @author	Bob Jacobsen
  */
public class SerialLightTest extends AbstractLightTestBase {

    private GrapevineSystemConnectionMemo memo = null; 
    private SerialTrafficControlScaffold tcis = null;

    @Override
    @Before
    public void setUp() {
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        tcis.registerNode(new SerialNode(1, SerialNode.NODE2002V6));
        memo = new GrapevineSystemConnectionMemo();
        memo.setTrafficController(tcis);

        t = new SerialLight("GL1104", "t4",memo);
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    @Override
    public void checkOffMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 1C 81 04", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // CLOSED message
    }

    @Override
    public void checkOnMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 18 81 0C", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROWN message
    }

}
