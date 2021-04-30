package jmri.jmrit.logixng.log;

import java.io.IOException;
import java.io.InputStream;

import jmri.jmrit.logixng.log.Log.InvalidFormatException;

/**
 * Reads the data part of the log from a stream and decodes it.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface LogReaderDecoder {

    /**
     * Init the decoder.
     * 
     * @param logixNGLog the log
     * @param input the input stream
     */
    public void init(Log logixNGLog, InputStream input);
    
    /**
     * Get the encoding this decoder implements.
     * @return the encoding
     */
    public Encodings getEncoding();
    
    /**
     * Try to read one more row of data. Returns null if end of data.
     * @return a row of data or null if end of data
     * @throws java.io.IOException if an I/O error occurs
     * @throws InvalidFormatException if the log has invalid format
     */
    public LogRow read() throws IOException, InvalidFormatException;
    
}
