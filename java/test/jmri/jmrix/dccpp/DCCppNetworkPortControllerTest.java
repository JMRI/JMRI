package jmri.jmrix.dccpp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the DCCppNetworkPortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class DCCppNetworkPortControllerTest extends jmri.jmrix.AbstractNetworkPortControllerTestBase {

    @Override
    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

       DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);
       apc = new DCCppNetworkPortController(memo){
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
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
