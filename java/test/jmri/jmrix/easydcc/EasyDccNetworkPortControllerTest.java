package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the EasyDccNetworkPortController class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EasyDccNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @BeforeEach
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
    @AfterEach
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
