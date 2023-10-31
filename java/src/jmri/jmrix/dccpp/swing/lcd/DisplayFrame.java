package jmri.jmrix.dccpp.swing.lcd;

import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JTextField;

import jmri.jmrix.dccpp.*;
import jmri.util.JmriJFrame;

/**
 * Frame to image the DCC-EX command station's OLED display
 *
 * @author Bob Jacobsen Copyright (C) 2023
 */
public class DisplayFrame extends JmriJFrame implements DCCppListener  {

    // private data
    private DCCppTrafficController _tc = null;
    private DCCppSystemConnectionMemo _memo;

    public DisplayFrame(DCCppSystemConnectionMemo memo) {
        super();
        _tc = memo.getDCCppTrafficController();
        _memo = memo;
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
        // TODO: This needs to parse the message, decide whether to display, and put on right lines
        lines.get(0).setText(msg.toString());
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void notifyTimeout(DCCppMessage msg) {
    }
    
    final static int TEXTFIELDLENGTH = 40;
    final static int TOTALLINES = 5;
    ArrayList<JTextField> lines;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // initialize the list of display lines
        lines = new ArrayList<>(5);
        for (int i = 0; i<TOTALLINES; i++) {
            lines.add(new JTextField(TEXTFIELDLENGTH));
            this.add(lines.get(i));
        }
        
        // set the title, include prefix in event of multiple connections 
        setTitle(Bundle.getMessage("DisplayFrameTitle") + " (" + _memo.getSystemPrefix() + ")");
        
        // pack to layout display
        pack();
    }
   
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DisplayFrame.class);

}
