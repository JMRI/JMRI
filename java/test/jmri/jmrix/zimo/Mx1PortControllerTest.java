package jmri.jmrix.zimo;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the Mx1PortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class Mx1PortControllerTest extends jmri.jmrix.AbstractSerialPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       Mx1TrafficController tc = new Mx1TrafficController(){
          @Override
          public boolean status(){
            return true;
          }
          @Override
          public void sendMx1Message(Mx1Message m,Mx1Listener reply) {
          }
       };
       Mx1SystemConnectionMemo memo = new Mx1SystemConnectionMemo(tc);
       apc = new Mx1PortController(memo){
            @Override
            public boolean status(){
              return true;
            }
            @Override
            public void configure(){
            }
            @Override
            public boolean okToSend(){
               return true;
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
    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
