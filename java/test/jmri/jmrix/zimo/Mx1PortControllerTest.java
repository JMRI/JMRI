package jmri.jmrix.zimo;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the Mx1PortController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class Mx1PortControllerTest extends jmri.jmrix.AbstractSerialPortControllerTest {

    @Override
    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       JUnitUtil.resetInstanceManager();
       Mx1SystemConnectionMemo memo = new Mx1SystemConnectionMemo();
       Mx1TrafficController tc = new Mx1TrafficController(){
          @Override
          public boolean status(){
            return true;
          }
          @Override
          public void sendMx1Message(Mx1Message m,Mx1Listener reply) {
          }
       };
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
             * Open a specified port. The appname argument is to be provided to the
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
       JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
    }

}
