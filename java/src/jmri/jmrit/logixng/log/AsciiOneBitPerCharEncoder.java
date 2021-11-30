package jmri.jmrit.logixng.log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This encoder writes a log where each status bit is a '0' or '1' character.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class AsciiOneBitPerCharEncoder implements LogWriterEncoder {

    OutputStream _output;
    
    /** {@inheritDoc} */
    @Override
    public void init(OutputStream output) {
        _output = output;
    }
    
    /** {@inheritDoc} */
    @Override
    public Encodings getEncoding() {
        return Encodings.ASCII_ONE_BIT_PER_CHAR;
    }
    
    /** {@inheritDoc} */
    @Override
    public void write(LogRow row) throws IOException {
        for (int i=0; i < row.getNumStates(); i++) {
            _output.write(row.getState(i) ? '1' : '0');
        }
        _output.write('\n');
    }

}
