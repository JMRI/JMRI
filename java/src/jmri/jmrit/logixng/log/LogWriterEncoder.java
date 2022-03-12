package jmri.jmrit.logixng.log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes the data part of the log to a stream and encodes it.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface LogWriterEncoder {

    /**
     * Init the encoder.
     * @param output the output stream
     */
    public void init(OutputStream output);
    
    /**
     * Get the encoding this encoder implements.
     * @return the encoding
     */
    public Encodings getEncoding();
    
    /**
     * Writes one row of data.
     * @param row the row to write
     * @throws java.io.IOException if an I/O error occurs
     */
    public void write(LogRow row) throws IOException;
    
}
