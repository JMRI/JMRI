package jmri.jmrix.can.cbus.swing.console;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * 
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusConsoleLogEntry {
    
    private final String _frame;
    private final String _decoded;
    private final int _highlighter;
    
    protected CbusConsoleLogEntry( String frame, String decoded, int highlighter ){
        _frame = frame;
        _decoded = decoded;
        _highlighter = highlighter;
    }

    protected String getFrameText(){
        return _frame;
    }

    protected String getDecodedText(){
        return _decoded;
    }

    protected int getHighlighter() {
        return _highlighter;
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusConsoleLogEntry.class);
}
