package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the LnPortController class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LnPortControllerTest extends jmri.jmrix.AbstractSerialPortControllerTestBase {

    private LocoNetSystemConnectionMemo memo;

    @Override
    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       memo = new LocoNetSystemConnectionMemo();
       new LocoNetInterfaceScaffold(memo);
       apc = new LnPortController(memo){
            @Override
            public boolean status(){
              return true;
            }

            @Override
            public void configure(){
            }

            @Override
            public java.io.DataInputStream getInputStream(){
                return null;
            }

            @Override
            public java.io.DataOutputStream getOutputStream(){
                return null;
            }

            @Override
            public String[] validBaudRates(){
               String[] retval = {"9600"};
               return retval;
            }

            /**
             * Open a specified port. The appName argument is to be provided to the
             * underlying OS during startup so that it can show on status displays, etc
             */
            @Override
            public String openPort(String portName, String appName){
               return "";
            }
       };
    }

    @Override
    @AfterEach
    public void tearDown(){
       memo.dispose();
       JUnitUtil.tearDown();
    }

}
