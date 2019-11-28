package jmri.jmrix.nce;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the NceNetworkPortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class NceNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       NceSystemConnectionMemo memo = new NceSystemConnectionMemo();
       memo.setNceTrafficController(new NceTrafficController());
       apc = new NceNetworkPortController(memo){
            @Override
            public void configure(){
            }
       };
    }

    @Override
    @After
    public void tearDown(){
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
       JUnitUtil.tearDown();
    }
}
