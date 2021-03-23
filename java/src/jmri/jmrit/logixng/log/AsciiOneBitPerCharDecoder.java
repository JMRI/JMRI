package jmri.jmrit.logixng.log;

import java.io.IOException;
import java.io.InputStream;

import jmri.jmrit.logixng.log.Log.InvalidFormatException;

/**
 * This decoder reads a log where each status bit is a '0' or '1' character.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class AsciiOneBitPerCharDecoder implements LogReaderDecoder {

    private Log _logixNGLog;
    private InputStream _input;
    
    /** {@inheritDoc} */
    @Override
    public void init(Log logixNGLog, InputStream input) {
        _logixNGLog = logixNGLog;
        _input = input;
    }
    
    /** {@inheritDoc} */
    @Override
    public Encodings getEncoding() {
        return Encodings.ASCII_ONE_BIT_PER_CHAR;
    }
    
    /** {@inheritDoc} */
    @Override
    public LogRow read() throws IOException, InvalidFormatException {
        LogRow row = new LogRowArray(_logixNGLog.getNumItems());
        
        for (int i=0; i < row.getNumStates(); i++) {
            int ch = _input.read();
            
            // If we haven't read any data yet and we either has end of stream
            // or a new line, return 'null'.
            if ((i == 0) && ((ch == -1) || (ch == '\n'))) {
                return null;
            }
            
            if (ch == -1) {
                throw new InvalidFormatException("Unexpected end of stream");
            }
            if (ch == '\n') {
                throw new InvalidFormatException("Unexpected end of line");
            }
            
            switch (ch) {
                case '1':
                    row.setState(i, true);
                    break;
                case '0':
                    row.setState(i, false);
                    break;
                default:
                    throw new InvalidFormatException("Unknown character");
            }
        }
        
        int ch = _input.read();
        if (ch == -1) {
            throw new InvalidFormatException("Unexpected end of stream");
        }
        if (ch != '\n') {
            throw new InvalidFormatException("New line expected");
        }
        
        return row;
    }

}
