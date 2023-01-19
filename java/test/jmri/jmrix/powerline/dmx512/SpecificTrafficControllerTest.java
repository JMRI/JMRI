package jmri.jmrix.powerline.dmx512;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import jmri.jmrix.powerline.SerialPortController;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the SpecificTrafficController class.
 *
 * @author Bob Jacobsen Copyright 2005, 2007, 2008, 2009 Converted to multiple
 * connection
 * @author Ken Cameron Copyright (C) 2011,2023
 */
public class SpecificTrafficControllerTest extends jmri.jmrix.powerline.SerialTrafficControllerTest {

    SerialTrafficController t = null;
    SerialSystemConnectionMemo memo = null;



    @Test
    public void testScaffold() throws java.io.IOException {
        SerialPortControllerScaffold scaff = new SerialPortControllerScaffold(memo);

        Assertions.assertNotNull(scaff);
        Assertions.assertNotNull(tostream);
        Assertions.assertNotNull(ostream);

        scaff.dispose();
    }

    // internal class to simulate a PortController
    private class SerialPortControllerScaffold extends SerialPortController {

        SerialPortControllerScaffold(SerialSystemConnectionMemo memo ) throws java.io.IOException {
            super(memo);
            PipedInputStream tempPipe;
            tempPipe = new PipedInputStream();
            tostream = new DataInputStream(tempPipe);
            ostream = new DataOutputStream(new PipedOutputStream(tempPipe));
        }
        
        @Override
        public java.util.Vector<String> getPortNames() {
            return null;
        }

        @Override
        public String openPort(String portName, String appName) {
            return null;
        }

        @Override
        public void configure() {
        }

        @Override
        public String[] validBaudRates() {
            return new String[] {};
        }

        //@Override
        @Override
        public int[] validBaudNumbers() {
            return new int[] {};
        }

        @Override
        public DataInputStream getInputStream() {
            // no input for DMX, all output
            return null;
        }
        
        // returns the outputStream to the port
        @Override
        public DataOutputStream getOutputStream() {
            return ostream;
        }

        // check that this object is ready to operate
        @Override
        public boolean status() {
            return true;
        }

    }

    private DataOutputStream ostream;  // Traffic controller writes to this
    private DataInputStream tostream; // so we can read it from this

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SpecificSystemConnectionMemo();
        tc = t = new SpecificTrafficController(memo);
    }

    @Override
    @AfterEach
    public void tearDown() {
        if ( memo != null ) {
            memo.dispose();
            memo = null;
        }
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(SpecificTrafficControllerTest.class);

}
