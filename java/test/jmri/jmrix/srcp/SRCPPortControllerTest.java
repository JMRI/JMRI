package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SRCPPortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class SRCPPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       SRCPSystemConnectionMemo memo = new SRCPSystemConnectionMemo();
       apc = new SRCPPortController(memo){
           @Override
           public boolean status(){
              return true;
           }
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
