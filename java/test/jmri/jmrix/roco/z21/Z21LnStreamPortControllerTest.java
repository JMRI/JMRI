package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * JUnit tests for the Z21LnStreamPortController class.
 *
 * @author Paul Bender Copyright (C) 2016, 2018
 */
public class Z21LnStreamPortControllerTest extends jmri.jmrix.AbstractStreamPortControllerTestBase {
       
    private LocoNetSystemConnectionMemo memo;

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       memo = new LocoNetSystemConnectionMemo();
       memo.setLnTrafficController(new Z21LnStreamPortPacketizer(memo));
       apc = new Z21LnStreamPortController(memo, null, null, "Test Stream Port");
    }

    @Override
    @After
    public void tearDown(){
       memo.dispose();
       memo=null;
       apc=null;
       JUnitUtil.tearDown();
    }

}
