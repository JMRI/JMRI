package jmri.jmrix.loconet.streamport;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;


/**
 * JUnit tests for the LnStreamPortController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016,2018
 */
public class LnStreamPortControllerTest extends jmri.jmrix.AbstractStreamPortControllerTestBase {
       
    private LocoNetSystemConnectionMemo memo;

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       memo = new LocoNetSystemConnectionMemo();
       memo.setLnTrafficController(new LnStreamPortPacketizer());
       apc = new LnStreamPortController(memo,null,null,"Test Stream Port");
    }

    @Override
    @After
    public void tearDown(){
       memo.dispose();
       JUnitUtil.tearDown();
    }

}
