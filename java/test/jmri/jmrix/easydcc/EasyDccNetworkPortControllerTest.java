package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the EasyDccNetworkPortController class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EasyDccNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       EasyDccSystemConnectionMemo memo = new EasyDccSystemConnectionMemo();
       apc = new EasyDccNetworkPortController(memo){
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
