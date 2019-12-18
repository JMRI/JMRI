package jmri.jmrix.lenz;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * JUnit tests for the XNetSimulatorPortController class.
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class XNetSimulatorPortControllerTest extends jmri.jmrix.AbstractSerialPortControllerTestBase {

    @Override
    @Before
    public void setUp(){
       JUnitUtil.setUp();
       XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());
       new XNetSystemConnectionMemo(tc);
       apc = new XNetSimulatorPortController(){
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
             * Open a specified port. The appName argument is to be provided to the
             * underlying OS during startup so that it can show on status displays, etc
             */
            @Override
            public String openPort(String portName, String appName){
               return "";
            }

            @Override
            public void setOutputBufferEmpty(boolean s){
            }
            
            @Override
            public boolean okToSend(){
                  return true;
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
