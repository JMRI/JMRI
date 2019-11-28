package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the Dcc4PcPortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class Dcc4PcPortControllerTest extends jmri.jmrix.AbstractSerialPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       Dcc4PcTrafficController tc = new Dcc4PcTrafficController(){
          @Override
          public void sendDcc4PcMessage(Dcc4PcMessage m,Dcc4PcListener reply) {
          }
       };
       Dcc4PcSystemConnectionMemo memo = new Dcc4PcSystemConnectionMemo(tc);
       apc = new Dcc4PcPortController(memo){
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
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
