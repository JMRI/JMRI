package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Group operations to a UCC CTC column
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
@net.jcip.annotations.Immutable
public class Column {

    /**
     * Nobody can build anonymous object
     */
    private Column() {}
    
    /**
     * Create and configure 
     *
     * @param startTO  Name for turnout that starts operation on the layout
     * @param output1TO  Turnout name for 1st channel of code information
     * @param output2TO  Turnout name for 2nd channel of code information
     * @param output3TO  Turnout name for 3rd channel of code information
     * @param output4TO  Turnout name for 4th channel of code information
     */
    public Column(String name, CodeLine line, CodeButton button, TurnoutSection turnout) {
        this.name = name;
        this.line = line;
        this.button = button;
        this.turnout = turnout;
    }

    String name;
    CodeLine line;
    CodeButton button;
    TurnoutSection turnout;
    
    void requestSendCode() {
        turnout.codeSendStart();
        
        if (!turnout.codeSendOK()) return;
        
        line.requestSendCode();
    }

    void codeSendComplete () {
        button.codeSendComplete();
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Column.class.getName());
}
