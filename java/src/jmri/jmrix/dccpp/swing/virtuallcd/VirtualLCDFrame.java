package jmri.jmrix.dccpp.swing.virtuallcd;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;

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
            lines.get(msg.getLCDLineNumInt()).setText(msg.getLCDTextString()+"   ");
            pack();
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void notifyTimeout(DCCppMessage msg) {
    }
    
    final static int TOTALLINES = 64;  // max DCC-EX will reference
    ArrayList<JLabel> lines;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        super.initComponents();
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        Font font = null;
        // load the custom 5x8 found
        try { 
            InputStream stream = new FileInputStream(new File("resources/fonts/5x8_lcd_hd44780u_a02.ttf"));
            font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(16f).deriveFont(Font.BOLD);
        } catch (IOException e1) { log.error("failed to find or open font file");
        } catch (FontFormatException e2) { log.error("font file not valid");
        }
        
        var pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        // initialize the list of display lines
        lines = new ArrayList<>(9);
        for (int i = 0; i<TOTALLINES; i++) {
            var label = new JLabel();
            if (font != null) label.setFont(font);
            label.setOpaque(true);
            label.setBackground(Color.BLACK);
            label.setForeground(Color.WHITE);
            lines.add(label);
            pane.add(lines.get(i));
        }
        pane.setOpaque(true);
        pane.setBackground(Color.BLACK);
        this.add(pane);
        
        // set the title, include prefix in event of multiple connections 
        setTitle(Bundle.getMessage("VirtualLCDFrameTitle") + " (" + _memo.getSystemPrefix() + ")");
        
        // pack to layout display
        pack();
    }
   
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLCDFrame.class);

}
