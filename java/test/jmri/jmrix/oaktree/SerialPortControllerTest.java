package jmri.jmrix.oaktree;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SerialPortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class SerialPortControllerTest extends jmri.jmrix.AbstractSerialPortControllerTestBase {

    @Override
    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       OakTreeSystemConnectionMemo memo = new OakTreeSystemConnectionMemo();
       new SerialTrafficController(memo){
          @Override
          public void sendSerialMessage(SerialMessage m,SerialListener reply) {
          }
       };
       apc = new SerialPortController(memo){
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
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
