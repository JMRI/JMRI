package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the XNetNetworkPortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class XNetNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       apc = new XNetNetworkPortController(){
           @Override
           public boolean status(){
              return true;
           }
           @Override
           public boolean okToSend(){
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
