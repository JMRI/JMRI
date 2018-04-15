package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringBufferInputStream;

/**
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2018
 */
public class LnPacketizerTest {

    protected LnPacketizer lnp;

    @Test
    public void testCtor() {
       Assert.assertNotNull("exists", lnp );
    }

    @Test
    public void testStatusWithoutInit() {
       Assert.assertFalse("not connected", lnp.status() );
    }

    @Test
    @Ignore("may be causing hang on travis and appveyor")
    public void testStartThreads() {
       LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
       lnp.connectPort(new LnPortController(memo){
            @Override
            public boolean status(){
              return true;
            }
            @Override
            public void configure(){
            }
            @Override
            public java.io.DataInputStream getInputStream(){
                return new DataInputStream(new StringBufferInputStream(""));
            }
            @Override
            public java.io.DataOutputStream getOutputStream(){
                return new DataOutputStream(new ByteArrayOutputStream());
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

         });
       lnp.startThreads();
       memo.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnp = new LnPacketizer();
    }

    @After
    public void tearDown() {
        lnp = null;
        JUnitUtil.tearDown();
    }
}
