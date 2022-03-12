package jmri.jmrit.logixng.digital.log;

import jmri.jmrit.logixng.log.LogReader;
import jmri.jmrit.logixng.log.Log;
import jmri.jmrit.logixng.log.LogWriter;
import jmri.jmrit.logixng.log.LogRowArray;
import jmri.jmrit.logixng.log.DefaultLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import jmri.jmrit.logixng.log.Log.InvalidFormatException;
import jmri.jmrit.logixng.log.Log.UnsupportedVersionException;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Log
 */
public class LogTest {

    @Test
    public void testLog() {
        try {
            throw new InvalidFormatException("Invalid format");
        } catch (InvalidFormatException e) {
            // Do nothing
        }
        
        try {
            throw new UnsupportedVersionException("Unsupported version");
        } catch (UnsupportedVersionException e) {
            // Do nothing
        }
    }
    
    @Test
    public void testWriteLog() throws IOException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        Log _logixNGLog = new DefaultLog();
        _logixNGLog.addItem("IQ121:E52");
        _logixNGLog.addItem("IQ121:E3");
        _logixNGLog.addItem("IQ121:A12");
        _logixNGLog.addItem("IQ121:E1321");
        
        LogWriter logWriter = new LogWriter(_logixNGLog, output, "Log something");
        
        logWriter.write(new LogRowArray("0000"));
        logWriter.write(new LogRowArray("0100"));
        logWriter.write(new LogRowArray("0011"));
        logWriter.write(new LogRowArray("0110"));
        logWriter.write(new LogRowArray("1010"));
        logWriter.write(new LogRowArray("1110"));
        
        String outputString = output.toString("UTF-8");
        
        Assert.assertTrue("Strings matches",
                           ("[header]\n" +
                            "version=1\n" +
                            "encoding=ascii_1_bit_per_char\n" +
                            "name=Log something\n" +
                            "[items]\n" +
                            "IQ121:E52\n" +
                            "IQ121:E3\n" +
                            "IQ121:A12\n" +
                            "IQ121:E1321\n" +
                            "[data]\n" +
                            "0000\n" +
                            "0100\n" +
                            "0011\n" +
                            "0110\n" +
                            "1010\n" +
                            "1110\n").equals(outputString));
        
        output.close();
    }
    
    @Test
    public void testReadLog() throws IOException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InvalidFormatException, Log.UnsupportedVersionException {
        
        String inputString = 
                "[header]\n" +
                "version=1\n" +
                "encoding=ascii_1_bit_per_char\n" +
                "name=Log something\n" +
                "[items]\n" +
                "IQ121:E52\n" +
                "IQ121:E3\n" +
                "IQ121:A12\n" +
                "IQ121:E1321\n" +
                "[data]\n" +
                "0000\n" +
                "0100\n" +
                "0011\n" +
                "0110\n" +
                "1010\n" +
                "1110\n" +
                "\n";
        
        InputStream input = new ByteArrayInputStream(
                inputString.getBytes(StandardCharsets.UTF_8));
        
        Log log = new DefaultLog();
        LogReader logReader = new LogReader(log, input);
        Assert.assertTrue("Name matches", "Log something".equals(logReader.getName()));
        
        Assert.assertTrue(String.format("Log row matches: '%s'", "0000"), "0000".equals(logReader.read().getDataString()));
        Assert.assertTrue(String.format("Log row matches: '%s'", "0100"), "0100".equals(logReader.read().getDataString()));
        Assert.assertTrue(String.format("Log row matches: '%s'", "0011"), "0011".equals(logReader.read().getDataString()));
        Assert.assertTrue(String.format("Log row matches: '%s'", "0110"), "0110".equals(logReader.read().getDataString()));
        Assert.assertTrue(String.format("Log row matches: '%s'", "1010"), "1010".equals(logReader.read().getDataString()));
        Assert.assertTrue(String.format("Log row matches: '%s'", "1110"), "1110".equals(logReader.read().getDataString()));
        Assert.assertTrue(String.format("decoder.read() returns 'null'", "0000"), logReader.read() == null);
        
        input.close();
    }
/*    
    @Test
    public void testReadLog() throws IOException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InvalidFormatException, Log.UnsupportedVersionException {
        
        String inputString = 
                "[header]\n" +
                "version=1\n" +
                "encoding=ascii_1_bit_per_char\n" +
                "name=Log something\n" +
                "[items]\n" +
                "IQ121:E52\n" +
                "IQ121:E3\n" +
                "IQ121:A12\n" +
                "IQ121:E1321\n" +
                "[data]\n" +
                "0000\n" +
                "0100\n" +
                "0011\n" +
                "0110\n" +
                "1010\n" +
                "1110\n" +
                "\n";
        
        InputStream input = new ByteArrayInputStream(
                inputString.getBytes(StandardCharsets.UTF_8));
        
        Log log = new DefaultLog();
        LogHeader logHeader = new LogHeader(log);
        logHeader.readHeader(input);
        Assert.assertTrue("Name matches", "Log something".equals(logHeader.getName()));
        
        LogReaderDecoder decoder =
                logHeader.getEncoding()
                        .getDecoderClass()
                        .getDeclaredConstructor(Log.class, InputStream.class)
                        .newInstance(log, input);
        
        Assert.assertTrue(String.format("Log row matches: '%s'", "0000"), "0000".equals(decoder.read().getDataString()));
        Assert.assertTrue(String.format("Log row matches: '%s'", "0100"), "0100".equals(decoder.read().getDataString()));
        Assert.assertTrue(String.format("Log row matches: '%s'", "0011"), "0011".equals(decoder.read().getDataString()));
        Assert.assertTrue(String.format("Log row matches: '%s'", "0110"), "0110".equals(decoder.read().getDataString()));
        Assert.assertTrue(String.format("Log row matches: '%s'", "1010"), "1010".equals(decoder.read().getDataString()));
        Assert.assertTrue(String.format("Log row matches: '%s'", "1110"), "1110".equals(decoder.read().getDataString()));
        Assert.assertTrue(String.format("decoder.read() returns 'null'", "0000"), decoder.read() == null);
        
        input.close();
    }
*/    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
