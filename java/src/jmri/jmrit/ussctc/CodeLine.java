package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Drive the code line communications on a USS CTC panel
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
@net.jcip.annotations.Immutable
public class CodeLine {

    /**
     * Nobody can build anonymous object
     */
    private CodeLine() {}
    
    /**
     * Create and configure 
     *
     * @param startTO  Name for turnout that starts operation on the layout
     * @param output1TO  Turnout name for 1st channel of code information
     * @param output2TO  Turnout name for 2nd channel of code information
     * @param output3TO  Turnout name for 3rd channel of code information
     * @param output4TO  Turnout name for 4th channel of code information
     */
    public CodeLine(String startTO, String output1TO, String output2TO, String output3TO, String output4TO) {
        NamedBeanHandleManager hm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
        
        hStartTO = hm.getNamedBeanHandle(startTO, tm.provideTurnout(startTO));

        hOutput1TO = hm.getNamedBeanHandle(output1TO, tm.provideTurnout(output1TO));
        hOutput2TO = hm.getNamedBeanHandle(output2TO, tm.provideTurnout(output2TO));
        hOutput3TO = hm.getNamedBeanHandle(output3TO, tm.provideTurnout(output3TO));
        hOutput4TO = hm.getNamedBeanHandle(output4TO, tm.provideTurnout(output4TO));
    }

    NamedBeanHandle<Turnout> hStartTO;

    NamedBeanHandle<Turnout> hOutput1TO;
    NamedBeanHandle<Turnout> hOutput2TO;
    NamedBeanHandle<Turnout> hOutput3TO;
    NamedBeanHandle<Turnout> hOutput4TO;
    
    void codeSendStart() {
    }

    void codeSendComplete () {
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CodeLine.class.getName());
}
