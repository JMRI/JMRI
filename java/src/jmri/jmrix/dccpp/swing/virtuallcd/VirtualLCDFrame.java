package jmri.jmrix.dccpp.swing.virtuallcd;

import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JTextField;

import jmri.jmrix.dccpp.*;
import jmri.util.JmriJFrame;

/**
 * Frame to image the DCC-EX command station's OLED display
 *   Also sends request to DCC-EX to send copies of all LCD messages to this instance of JMRI
 *
 * @author Bob Jacobsen Copyright (C) 2023
 */
public class VirtualLCDFrame extends JmriJFrame implements DCCppListener  {

    // private data
    private DCCppTrafficController _tc = null;
    private DCCppSystemConnectionMemo _memo;

    public VirtualLCDFrame(DCCppSystemConnectionMemo memo) {
        super();
        _tc = memo.getDCCppTrafficController();
        _memo = memo;
        _tc.sendDCCppMessage(DCCppMessage.makeLCDRequestMsg(), null);        
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void message(DCCppMessage msg) {
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void message(DCCppReply msg) {
        if (msg.isLCDTextReply()) {
            lines.get(msg.getLCDLineNumInt()).setText(msg.getLCDTextString());
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void notifyTimeout(DCCppMessage msg) {
    }
    
    final static int TEXTFIELDLENGTH = 40;
    final static int TOTALLINES = 8;
    ArrayList<JTextField> lines;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // initialize the list of display lines
        lines = new ArrayList<>(9);
        for (int i = 0; i<TOTALLINES; i++) {
            lines.add(new JTextField(TEXTFIELDLENGTH));
            this.add(lines.get(i));
        }
        
        // set the title, include prefix in event of multiple connections 
        setTitle(Bundle.getMessage("VirtualLCDFrameTitle") + " (" + _memo.getSystemPrefix() + ")");
        
        // pack to layout display
        pack();
    }
   
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLCDFrame.class);

}
