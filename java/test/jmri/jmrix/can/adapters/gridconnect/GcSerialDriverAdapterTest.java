package jmri.jmrix.can.adapters.gridconnect;

import java.io.*;
import java.util.*;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.ArgumentMatchers.any;

import purejavacomm.*;

/**
 *
 * @author Paul Bender       Copyright (C) 2017
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class GcSerialDriverAdapterTest {

    CommPortIdentifier cpi;
    private final MyInputStream _inputStream = new MyInputStream();
    private final MyOutputStream _outputStream = new MyOutputStream();

    @Test
    public void testCTor() throws PortInUseException {
        // Instantiate a MockedStatic in a try-with-resources block
        try (MockedStatic<CommPortIdentifier> mb = Mockito.mockStatic(CommPortIdentifier.class)) {

            mb.when(() -> { CommPortIdentifier.getPortIdentifier(any(String.class)); })
                    .thenReturn(cpi);

            GcSerialDriverAdapter t = new GcSerialDriverAdapter();
            Assert.assertNotNull("exists",t);

            t.openPort("my port", "JMRI app");
//            t.configure();
            
            // This should terminate the TC threads and deregister the shutdown manager but causes an NPE in the input stream
//            t.getSystemConnectionMemo().getTrafficController().terminateThreads();
        }
        // the mock is not visible outside the block above
    }

    @BeforeEach
    public void setUp() throws PortInUseException {
        JUnitUtil.setUp();

        cpi = Mockito.mock(CommPortIdentifier.class);

        Mockito.when(cpi.open(any(String.class), any(Integer.class)))
                .thenReturn(new MySerialPort(_inputStream, _outputStream));
    }

    @AfterEach
    public void tearDown() {
        // Temp fix to remove the TC registered shutdown manager
        JUnitUtil.clearShutDownManager();
       
        JUnitAppender.assertErrorMessage("no match to (null) in currentBaudNumber");
        JUnitUtil.tearDown();
    }


    private static class MySerialPort extends SerialPort {

        private final MyInputStream _inputStream;
        private final MyOutputStream _outputStream;

        public MySerialPort(MyInputStream inputStream, MyOutputStream outputStream) {
            this._inputStream = inputStream;
            this._outputStream = outputStream;
        }

        @Override
        public void addEventListener(SerialPortEventListener sl) throws TooManyListenersException {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public int getBaudRate() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public int getDataBits() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public int getFlowControlMode() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public int getParity() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public int getStopBits() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public boolean isCD() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public boolean isCTS() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public boolean isDSR() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public boolean isDTR() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public boolean isRI() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public boolean isRTS() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void notifyOnBreakInterrupt(boolean bln) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void notifyOnCarrierDetect(boolean bln) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void notifyOnCTS(boolean bln) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void notifyOnDataAvailable(boolean bln) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void notifyOnDSR(boolean bln) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void notifyOnFramingError(boolean bln) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void notifyOnOutputEmpty(boolean bln) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void notifyOnOverrunError(boolean bln) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void notifyOnParityError(boolean bln) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void notifyOnRingIndicator(boolean bln) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void removeEventListener() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void sendBreak(int i) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void setDTR(boolean bln) {
            // Do nothing
        }

        @Override
        public void setFlowControlMode(int i) throws UnsupportedCommOperationException {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void setRTS(boolean bln) {
            // Do nothing
        }

        @Override
        public void setSerialPortParams(int i, int i1, int i2, int i3) throws UnsupportedCommOperationException {
            // Do nothing
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return _inputStream;
        }

        @Override
        public void disableReceiveFraming() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void disableReceiveThreshold() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void disableReceiveTimeout() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void enableReceiveFraming(int i) throws UnsupportedCommOperationException {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void enableReceiveThreshold(int i) throws UnsupportedCommOperationException {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void enableReceiveTimeout(int i) throws UnsupportedCommOperationException {
            // Do nothing
        }

        @Override
        public int getInputBufferSize() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public int getOutputBufferSize() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return _outputStream;
        }

        @Override
        public int getReceiveFramingByte() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public int getReceiveThreshold() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public int getReceiveTimeout() {
            return 100;
        }

        @Override
        public boolean isReceiveFramingEnabled() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public boolean isReceiveThresholdEnabled() {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public boolean isReceiveTimeoutEnabled() {
            return false;
        }

        @Override
        public void setInputBufferSize(int i) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

        @Override
        public void setOutputBufferSize(int i) {
            throw new UnsupportedOperationException("Not supported in this mocked class");
        }

    }


    private static class MyInputStream extends InputStream {

        @Override
        public int read()  {
            return -1;
        }

    }


    private static class MyOutputStream extends OutputStream {
        @Override
        public void write(int c) {
        }
    }


    // private final static Logger log = LoggerFactory.getLogger(GcSerialDriverAdapterTest.class);

}
