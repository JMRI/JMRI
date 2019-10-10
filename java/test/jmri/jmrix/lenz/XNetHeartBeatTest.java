package jmri.jmrix.lenz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetHeartBeat.java
 *
 * Description: tests for the jmri.jmrix.lenz.XNetHeartBeat class
 *
 * @author  Paul Bender Copyright (C) 2019
 */
public class XNetHeartBeatTest {

    private XNetHeartBeat hb = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull(hb);
    }

    @Before
    public void setUp(){

       jmri.util.JUnitUtil.setUp();

       XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo(
                                             new XNetInterfaceScaffold(
                                             new LenzCommandStation()));

       hb = new XNetHeartBeat(memo);

    }
  
    @After
    public void tearDown(){
        hb.dispose();
        hb = null;
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();
    }

}
