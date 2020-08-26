package jmri.jmrix.can.adapters.gridconnect;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the GcPortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class GcPortControllerTest extends jmri.jmrix.AbstractSerialPortControllerTestBase {

    @Override
    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
       CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
       apc = new GcPortController(memo){
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
       JUnitUtil.tearDown();
    }

}
