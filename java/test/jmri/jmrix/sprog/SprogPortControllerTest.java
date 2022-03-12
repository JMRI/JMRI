package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SprogPortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class SprogPortControllerTest extends jmri.jmrix.AbstractSerialPortControllerTestBase {

    private SprogTrafficControlScaffold stcs;

    @Override
    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       SprogSystemConnectionMemo memo = new SprogSystemConnectionMemo();
       stcs = new SprogTrafficControlScaffold(memo);
       apc = new SprogPortController(memo){
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

            /**
             * Get an array of valid baud rates; used to display valid options.
             */
            @Override
            public String[] validBaudRates(){
                return new String[]{"9600"};
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
       stcs.dispose();
       JUnitUtil.tearDown();
    }

}
