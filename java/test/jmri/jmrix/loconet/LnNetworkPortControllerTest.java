package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the LnNetworkPortController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class LnNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
       apc = new LnNetworkPortController(memo){
            @Override
            public void configure(){
            }
       };
    }

    @Override
    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }
}
