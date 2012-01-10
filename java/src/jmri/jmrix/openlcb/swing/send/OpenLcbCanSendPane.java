// OpenLcbCanSendPane.java

package jmri.jmrix.openlcb.swing.send;

import jmri.util.StringUtil;

import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusAddress;

// This makes it a bit CBUS specific
// May need refactoring one day

import java.awt.*;

import javax.swing.*;

import jmri.util.javaworld.GridLayout2;

/**
 * User interface for sending OpenLCB CAN frames to exercise the system
 * <P>
 * When  sending a sequence of operations:
 * <UL>
 * <LI>Send the next message and start a timer
 * <LI>When the timer trips, repeat if buttons still down.
 * </UL>
 * @author			Bob Jacobsen   Copyright (C) 2008
 * @version			$Revision: 19697 $
 */
public class OpenLcbCanSendPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    // member declarations
    JLabel jLabel1 = new JLabel();
    JButton sendButton = new JButton();
    JTextField packetTextField = new JTextField(12);
    
    // internal members to hold sequence widgets
    static final int MAXSEQUENCE = 4;
    JTextField mPacketField[]   = new JTextField[MAXSEQUENCE];
    JCheckBox  mUseField[]      = new JCheckBox[MAXSEQUENCE];
    JTextField mDelayField[]    = new JTextField[MAXSEQUENCE];
    JToggleButton    mRunButton = new JToggleButton("Go");

    JTextField srcAliasField = new JTextField("123");
    JTextField verifyNodeField = new JTextField("02 03 04 05 06 07 ");
    JTextField sendEventField = new JTextField("02 03 04 05 06 07 00 01 ");
    JTextField dstAliasField = new JTextField(4);
    JTextField datagramContentsField = new JTextField("20 61 00 00 00 00 08");
    JTextField configNumberField = new JTextField("40");
    JTextField configAddressField = new JTextField("00 00 00 00");
    JTextField writeDataField = new JTextField("00 00");
    JComboBox addrSpace = new JComboBox(new String[]{"CDI", "All", "Config", "None"});

    /*public OpenLcbCanSendPane() {
        super();
    }*/

    public void OpenLcbCanSendPane() {

//        setTitle("Send CAN Frames and OpenLCB Messages");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // handle single-packet part
        {
            JPanel pane1 = new JPanel();
            pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));

            jLabel1.setText("Single Frame:  (Raw input format is [123] 12 34 56) ");
            jLabel1.setVisible(true);

            sendButton.setText("Send");
            sendButton.setVisible(true);
            sendButton.setToolTipText("Send frame");

            packetTextField.setToolTipText("Frame as hex pairs, e.g. 82 7D; standard header in (), extended in []");


            pane1.add(jLabel1);
            pane1.add(packetTextField);
            pane1.add(sendButton);
            pane1.add(Box.createVerticalGlue());

            sendButton.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendButtonActionPerformed(e);
                    }
                });

            add(pane1);
        }

        add(new JSeparator());

        // Configure the sequence
        add(new JLabel("Send sequence of frames:"));
        JPanel pane2 = new JPanel();
        pane2.setLayout(new GridLayout2(MAXSEQUENCE+2, 4));
        pane2.add(new JLabel(""));
        pane2.add(new JLabel("Send"));
        pane2.add(new JLabel("packet"));
        pane2.add(new JLabel("wait (msec)"));
        for (int i=0;i<MAXSEQUENCE; i++) {
            pane2.add(new JLabel(Integer.toString(i+1)));
            mUseField[i]=new JCheckBox();
            mPacketField[i]=new JTextField(10);
            mDelayField[i]=new JTextField(10);
            pane2.add(mUseField[i]);
            pane2.add(mPacketField[i]);
            pane2.add(mDelayField[i]);
        }
        pane2.add(mRunButton); // starts a new row in layout
        add(pane2);

        mRunButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    runButtonActionPerformed(e);
                }
            });

        // special packet forms
        add(new JSeparator());
        add(new JLabel("Send special frame:"));

        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        pane2.add(new JLabel("Src Node alias:"));
        pane2.add(srcAliasField);
        pane2.add(new JLabel("Dest Node alias: "));
        pane2.add(dstAliasField);
        add(pane2);
        
        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        JButton b;
        b = new JButton("Send CIM");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendCimPerformed(e);
                    }
                });
        
        pane2.add(b);
        
        
        // send OpenLCB messages
        add(new JSeparator());
        add(new JLabel("Send OpenLCB message:"));

        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        b = new JButton("Send Verify Nodes");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendVerifyNode(e);
                    }
                });
        pane2.add(b); 
        b = new JButton("Send Request Events");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendRequestEvents(e);
                    }
                });
        pane2.add(b); 
        pane2.add(new JLabel("Node ID (6 bytes)"));
        pane2.add(verifyNodeField);

        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        b = new JButton("Send Request Consumers");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendReqConsumers(e);
                    }
                });
        pane2.add(b); 
        b = new JButton("Send Request Producers");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendReqProducers(e);
                    }
                });
        pane2.add(b); 
        b = new JButton("Send Event Produced");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendEventPerformed(e);
                    }
                });
        pane2.add(b); 
        pane2.add(new JLabel("Event ID (8 bytes):"));
        pane2.add(sendEventField);

        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        b = new JButton("Send Datagram");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendDatagramPerformed(e);
                    }
                });
        pane2.add(b); 
        pane2.add(new JLabel("Contents: "));
        pane2.add(datagramContentsField);
        b = new JButton("Send Datagram Reply");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendDatagramReply(e);
                    }
                });
        pane2.add(b); 
        
        // send OpenLCB Configuration message
        add(new JSeparator());
        add(new JLabel("Send OpenLCB Configuration Command:"));

        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        pane2.add(new JLabel("Memory Address: "));
        pane2.add(configAddressField);
        pane2.add(new JLabel("Address Space: "));
        pane2.add(addrSpace);
        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        b = new JButton("Read");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                         readPerformed(e);
                    }
                });
        pane2.add(b); 
        pane2.add(new JLabel("Byte Count: "));
        pane2.add(configNumberField);
        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        b = new JButton("Write");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                         writePerformed(e);
                    }
                });
        pane2.add(b); 
        pane2.add(new JLabel("Data: "));
        pane2.add(writeDataField);
        pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        add(pane2);
        b = new JButton("Send Confirm ");
        b.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendDatagramReply(e);
                    }
                });
        pane2.add(b); 

        // configuration
        
        // end GUI, add help

        
        // pack to cause display
        //pack();
    }
    
    public String getHelpTarget() { return "package.jmri.jmrix.openlcb.swing.send.OpenLcbCanSendPane"; }

    public String getTitle() {
        if(memo!=null) {
            return (memo.getUserName() + " Send Can Frame");
        }
        return "Send CAN Frames and OpenLCB Messages";
    }
    
    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        CanMessage m = createPacket(packetTextField.getText());
        log.debug("sendButtonActionPerformed: "+m);
        tc.sendCanMessage(m, this);
    }

    public void sendCimPerformed(java.awt.event.ActionEvent e) {
        String data = "[10700"+srcAliasField.getText()+"]";
        System.out.println("|"+data+"|");
        CanMessage m = createPacket(data);
        log.debug("sendCimPerformed");
        tc.sendCanMessage(m, this);
    }

    public void sendVerifyNode(java.awt.event.ActionEvent e) {
        String data = "[180A7"+srcAliasField.getText()+"] "+verifyNodeField.getText();
        System.out.println("|"+data+"|");
        CanMessage m = createPacket(data);
        log.debug("sendVerifyNode: "+m);
        tc.sendCanMessage(m, this);
    }

    public void sendRequestEvents(java.awt.event.ActionEvent e) {
        String data = "[182B7"+srcAliasField.getText()+"] "+verifyNodeField.getText();
        System.out.println("|"+data+"|");
        CanMessage m = createPacket(data);
        log.debug("sendVerifyNode: "+m);
        tc.sendCanMessage(m, this);
    }

    public void sendEventPerformed(java.awt.event.ActionEvent e) {
        String data = "[182DF"+srcAliasField.getText()+"] "+sendEventField.getText();
        System.out.println("|"+data+"|");
        CanMessage m = createPacket(data);
        log.debug("sendEventPerformed: "+m);
        tc.sendCanMessage(m, this);
    }

    public void sendReqConsumers(java.awt.event.ActionEvent e) {
        String data = "[1824F"+srcAliasField.getText()+"] "+sendEventField.getText();
        System.out.println("|"+data+"|");
        CanMessage m = createPacket(data);
        log.debug("sendEventPerformed: "+m);
        tc.sendCanMessage(m, this);
    }
    public void sendReqProducers(java.awt.event.ActionEvent e) {
        String data = "[1828F"+srcAliasField.getText()+"] "+sendEventField.getText();
        System.out.println("|"+data+"|");
        CanMessage m = createPacket(data);
        log.debug("sendEventPerformed: "+m);
        tc.sendCanMessage(m, this);
    }

    public void sendDatagramPerformed(java.awt.event.ActionEvent e) {
        // for now, no more than 8 bytes
        String data = "[1d"+dstAliasField.getText()+srcAliasField.getText()+"] "+datagramContentsField.getText();
        System.out.println("|"+data+"|");
        CanMessage m = createPacket(data);
        log.debug("sendDatagramPerformed: "+m);
        tc.sendCanMessage(m, this);
    }

    public void sendDatagramReply(java.awt.event.ActionEvent e) {
        String data = "[1e"+dstAliasField.getText()+srcAliasField.getText()+"] 04";
        System.out.println("|"+data+"|");
        CanMessage m = createPacket(data);
        log.debug("sendDatagramPerformed: "+m);
        tc.sendCanMessage(m, this);
    }

    public void readPerformed(java.awt.event.ActionEvent e) {
        String data = "[1d"+dstAliasField.getText()+srcAliasField.getText()+"] 20 6"+addrSpace.getSelectedIndex()+" "+configAddressField.getText()+" "+configNumberField.getText();
        System.out.println("|"+data+"|");
        CanMessage m = createPacket(data);
        log.debug("readPerformed: "+m);
        tc.sendCanMessage(m, this);
    }

    public void writePerformed(java.awt.event.ActionEvent e) {
        // for now, no more than 8 bytes
        String data = "[1d"+dstAliasField.getText()+srcAliasField.getText()+"] 20 2"+addrSpace.getSelectedIndex()
                        +" "+configAddressField.getText()+" "+writeDataField.getText();
        System.out.println("|"+data+"|");
        CanMessage m = createPacket(data);
        log.debug("writePerformed: "+m);
        tc.sendCanMessage(m, this);
    }


    // control sequence operation
    int mNextSequenceElement = 0;
    javax.swing.Timer timer = null;

    /**
     * Internal routine to handle timer starts & restarts
     */
    protected void restartTimer(int delay) {
        if (timer==null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        sendNextItem();
                    }
                });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Internal routine to handle a timeout and send next item
     */
    synchronized protected void timeout() {
        sendNextItem();
    }
    
    /**
     * Run button pressed down, start the sequence operation
     * @param e
     */
    public void runButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (!mRunButton.isSelected()) return;
        // make sure at least one is checked
        boolean ok = false;
        for (int i=0; i<MAXSEQUENCE; i++) {
            if (mUseField[i].isSelected()) ok = true;
        }
        if (!ok) {
            mRunButton.setSelected(false);
            return;
        }
        // start the operation
        mNextSequenceElement = 0;
        sendNextItem();
    }

    /**
     * Echo has been heard, start delay for next packet
     */
    void startSequenceDelay() {
        // at the start, mNextSequenceElement contains index we're
        // working on
        int delay = Integer.parseInt(mDelayField[mNextSequenceElement].getText());
        // increment to next line at completion
        mNextSequenceElement++;
        // start timer
        restartTimer(delay);
    }

    /**
     * Send next item; may be used for the first item or
     * when a delay has elapsed.
     */
    void sendNextItem() {
        // check if still running
        if (!mRunButton.isSelected()) return;
        // have we run off the end?
        if (mNextSequenceElement>=MAXSEQUENCE) {
            // past the end, go back
            mNextSequenceElement = 0;
        }
        // is this one enabled?
        if (mUseField[mNextSequenceElement].isSelected()) {
            // make the packet
            CanMessage m = createPacket(mPacketField[mNextSequenceElement].getText());
            // send it
            tc.sendCanMessage(m, this);
            startSequenceDelay();
        } else {
            // ask for the next one
            mNextSequenceElement++;
            sendNextItem();
        }
    }

    /**
     * Create a well-formed message from a String
     * String is expected to be space seperated hex bytes or CbusAddress, e.g.:
     *      12 34 56
     *      +n4e1
     * @param s
     * @return The packet, with contents filled-in
     */
    CanMessage createPacket(String s) {
        CanMessage m;
        // Try to convert using CbusAddress class
        CbusAddress a = new CbusAddress(s);
        if (a.check()) {
            m = a.makeMessage(tc.getCanid());
        } else {
            m = new CanMessage(tc.getCanid());
            // check for header
            if (s.charAt(0)=='[') {
                // extended header
                m.setExtended(true);
                int i = s.indexOf(']');
                String h = s.substring(1, i);
                m.setHeader(Integer.parseInt(h, 16));
                s = s.substring(i+1, s.length());
            } else if (s.charAt(0) == '(') {
                // standard header
                int i = s.indexOf(')');
                String h = s.substring(1, i);
                m.setHeader(Integer.parseInt(h, 16));
                s = s.substring(i+1, s.length());
            }
            // Try to get hex bytes
            byte b[] = StringUtil.bytesFromHexString(s);
            m.setNumDataElements(b.length);
            // Use &0xff to ensure signed bytes are stored as unsigned ints
            for (int i=0; i<b.length; i++) m.setElement(i, b[i]&0xff);
        }
        return m;
    }

    // connect to the CanInterface
    public void connect(TrafficController t) {
        tc = t;
        tc.addCanListener(this);
    }

    /**
     * Don't pay attention to messages
     */
    public void message(CanMessage m) {
    }

    /**
     * Don't pay attention to replies
     */
    public void reply(CanReply m) {
    }
    

    /**
     * When the window closes, 
     * stop any sequences running
     */
    public void dispose() {
        mRunButton.setSelected(false);
        super.dispose();
    }
    
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {
        public Default() {
            super("Send CAN Frames and OpenLCB Messages", 
                new jmri.util.swing.sdi.JmriJFrameInterface(), 
                OpenLcbCanSendPane.class.getName(), 
                jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
    
    // private data
    private TrafficController tc = null; //was CanInterface
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OpenLcbCanSendPane.class.getName());

}
