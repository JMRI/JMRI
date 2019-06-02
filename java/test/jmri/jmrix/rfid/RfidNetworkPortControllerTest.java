package jmri.jmrix.rfid;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the RfidNetworkPortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class RfidNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       RfidSystemConnectionMemo memo = new RfidSystemConnectionMemo();
       apc = new RfidNetworkPortController(memo){
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
