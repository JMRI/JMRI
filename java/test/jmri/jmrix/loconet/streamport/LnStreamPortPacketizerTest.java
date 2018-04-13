package jmri.jmrix.loconet.streamport;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringBufferInputStream;

/**
 * Tests for jmri.jmrix.loconet.streamport.StreamPortPacketizer
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2018
 */
public class LnStreamPortPacketizerTest extends jmri.jmrix.loconet.LnPacketizerTest {

    private LocoNetSystemConnectionMemo memo;
    private LnStreamPortController apc;

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnp = new LnStreamPortPacketizer();
        memo = new LocoNetSystemConnectionMemo();
        memo.setLnTrafficController(lnp);
        DataInputStream input = new DataInputStream(new StringBufferInputStream(""));
        DataOutputStream output = new DataOutputStream(new ByteArrayOutputStream());
        apc = new LnStreamPortController(memo,input,output,"Test Stream Port");
    }

    @Override
    @After
    public void tearDown() {
        memo.dispose();
        lnp = null;
        apc = null;
        memo = null;
        JUnitUtil.tearDown();
    }

    @Override
    @Test
    public void testStartThreads() {
       ((LnStreamPortPacketizer)lnp).connectPort(apc);
       lnp.startThreads();
    }
}
