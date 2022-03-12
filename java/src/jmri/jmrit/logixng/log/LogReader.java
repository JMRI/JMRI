package jmri.jmrit.logixng.log;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * A reader that reads from a LogixNG log.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class LogReader {

    private final Log _newLogixLog;
    private final LogHeader _logHeader;
    LogReaderDecoder decoder;
    
    /**
     * Creates a LogWriter object.
     * 
     * @param log the log
     * @param input the input stream
     * 
     * @throws java.io.IOException if an I/O error occurs
     * @throws java.lang.NoSuchMethodException if an instance of the decoder class cannot be instansiated
     * @throws java.lang.InstantiationException if an instance of the decoder class cannot be instansiated
     * @throws java.lang.IllegalAccessException if an instance of the decoder class cannot be instansiated
     * @throws java.lang.reflect.InvocationTargetException if a method on the instance of the decoder class cannot be called
     * @throws Log.InvalidFormatException invalid format of the header
     * @throws Log.UnsupportedVersionException invalid version of the file
     */
    public LogReader(Log log, InputStream input) throws IOException,
            NoSuchMethodException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            Log.InvalidFormatException, Log.UnsupportedVersionException {
        _newLogixLog = log;
        
        _logHeader = new LogHeader(log);
        _logHeader.readHeader(input);
        
        decoder = _logHeader.getEncoding().getDecoderClass()
                .getDeclaredConstructor().newInstance();
        decoder.init(_newLogixLog, input);
    }
    
    /**
     * Get the name of the log.
     * @return the name
     */
    public String getName() {
        return _logHeader.getName();
    }
    
    /**
     * Try to read one more row of data. Returns null if end of data.
     * @return a row of data or null if end of data
     * @throws java.io.IOException if an I/O error occurs
     * @throws Log.InvalidFormatException if the log has invalid format
     */
    public LogRow read() throws IOException, Log.InvalidFormatException {
        return decoder.read();
    }
    
}
