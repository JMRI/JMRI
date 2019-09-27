package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * JUnit tests for the LnPacketizerTest class.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2018
 */
public class LnPacketizerTest {

    protected LnPacketizer lnp;
    protected LocoNetSystemConnectionMemo memo;

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
                return new DataInputStream(new ByteArrayInputStream(new byte[0]));
            }
            @Override
            public java.io.DataOutputStream getOutputStream(){
                return new DataOutputStream(new ByteArrayOutputStream());
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

         });
       lnp.startThreads();
       memo.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
        lnp = new LnPacketizer(memo);
    }

    @After
    public void tearDown() {
        lnp = null;
        memo = null;
        JUnitUtil.tearDown();
    }

}
