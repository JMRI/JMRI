package jmri.jmrix.cmri.serial;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SerialNodeList class.
 *
 * @author	Bob Jacobsen Copyright 2017
 */
public class SerialNodeListTest {

    @Test
    public void testCtor() {
        new SerialNodeList();       
    }

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold stcs = null;
    private SerialNode n2 = null;
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // replace the SerialTrafficController
        stcs = new SerialTrafficControlScaffold();
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(stcs);

        new SerialNode(stcs);
        new SerialNode(1, SerialNode.SMINI,stcs);
        n2 = new SerialNode(2, SerialNode.USIC_SUSIC,stcs);
        n2.setNumBitsPerCard(24);
        n2.setCardTypeByAddress(0, SerialNode.INPUT_CARD);
        n2.setCardTypeByAddress(1, SerialNode.OUTPUT_CARD);
        n2.setCardTypeByAddress(3, SerialNode.OUTPUT_CARD);
        n2.setCardTypeByAddress(4, SerialNode.INPUT_CARD);
        n2.setCardTypeByAddress(2, SerialNode.OUTPUT_CARD);

    }

    @After
    public void tearDown() {
        if (stcs != null) stcs.terminateThreads();
        stcs = null;
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
