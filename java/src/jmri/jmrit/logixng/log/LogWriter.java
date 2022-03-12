package jmri.jmrit.logixng.log;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * A writer that writes to a LogixNG log.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class LogWriter {
    
    private final LogHeader logHeader;
    LogWriterEncoder encoder;
    
    /**
     * Creates a LogWriter object.
     * 
     * @param log the log
     * @param output the output stream
     * @param name the name of the log
     * 
     * @throws java.io.IOException if an I/O error occurs
     * @throws java.lang.NoSuchMethodException if an instance of the decoder class cannot be instansiated
     * @throws java.lang.InstantiationException if an instance of the decoder class cannot be instansiated
     * @throws java.lang.IllegalAccessException if an instance of the decoder class cannot be instansiated
     * @throws java.lang.reflect.InvocationTargetException if a method on the instance of the decoder class cannot be called
     */
    public LogWriter(Log log, OutputStream output, String name) throws IOException,
            NoSuchMethodException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        
        logHeader = new LogHeader(log);
        logHeader.setName(name);
        logHeader.writeHeader(output);
        
        encoder = logHeader.getEncoding().getEncoderClass()
                .getDeclaredConstructor().newInstance();
        encoder.init(output);
    }
    
    /**
     * Writes one row of data.
     * @param row the row to write
     * @throws java.io.IOException if an I/O error occurs
     */
    public void write(LogRow row) throws IOException {
        encoder.write(row);
    }
    
}
