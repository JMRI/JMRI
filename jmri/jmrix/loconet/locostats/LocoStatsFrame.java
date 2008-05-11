// LocoStatsFrame.java

package jmri.jmrix.loconet.locostats;

import jmri.jmrix.loconet.*;
import jmri.util.StringUtil;

import java.util.ResourceBundle;
import java.awt.event.*;

import javax.swing.*;

/**
 * Frame displaying LocoNet interface status information.
 * <P>
 * The LocoBuffer family from RR-CirKits and the PRn family from Digitrax use
 * different formats for the status message.  This class detects this
 * from the reply contents, and displays different panes depending on which
 * message was received. If the format is not recognised, a raw display
 * format is used.
 * <p>
 * Moved from loconet.locobuffer.LocoBufferStatsFrame
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 *
 * @author			Alex Shepherd   Copyright (C) 2003
 * @author			Bob Jacobsen   Copyright (C) 2008
 * @version			$Revision: 1.1 $
 * @since 2.1.5
 */
public class LocoStatsFrame extends jmri.util.JmriJFrame implements LocoNetListener {

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.loconet.LocoNetBundle");
    
    JPanel lb2Panel;
    JPanel rawPanel;
    JPanel pr2Panel;
    JPanel ms100Panel;

    public LocoStatsFrame() {
        super((rb = ResourceBundle.getBundle("jmri.jmrix.loconet.locostats.LocoStatsBundle")).getString("Title"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add GUI items
        rawPanel = new JPanel();
        rawPanel.setLayout(new BoxLayout(rawPanel, BoxLayout.X_AXIS));
        rawPanel.add(new JLabel(rb.getString("LabelRawData")));
        rawPanel.add(r1);
        rawPanel.add(r2);
        rawPanel.add(r3);
        rawPanel.add(r4);
        rawPanel.add(r5);
        rawPanel.add(r6);
        rawPanel.add(r7);
        rawPanel.add(r8);

        lb2Panel = new JPanel();
        lb2Panel.setLayout(new BoxLayout(lb2Panel, BoxLayout.X_AXIS));
        lb2Panel.add(new JLabel(rb.getString("LabelVersion")));
        lb2Panel.add(version);
        lb2Panel.add(new JLabel(" Breaks:"));
        breaks.setPreferredSize(version.getPreferredSize());
        lb2Panel.add(breaks);
        lb2Panel.add(new JLabel(" Errors:"));
        errors.setPreferredSize(version.getPreferredSize());
        lb2Panel.add(errors);

        pr2Panel = new JPanel();
        pr2Panel.setLayout(new BoxLayout(pr2Panel, BoxLayout.X_AXIS));
        pr2Panel.add(new JLabel(rb.getString("LabelSerialNumber")));
        pr2Panel.add(serial);
        pr2Panel.add(new JLabel(" PR2 Status:"));
        pr2Panel.add(status);
        pr2Panel.add(new JLabel(" Current:"));
        pr2Panel.add(current);
        pr2Panel.add(new JLabel(" Hardware Version:"));
        pr2Panel.add(hardware);
        pr2Panel.add(new JLabel(" Software Version:"));
        pr2Panel.add(software);

        ms100Panel = new JPanel();
        ms100Panel.setLayout(new BoxLayout(ms100Panel, BoxLayout.X_AXIS));
        ms100Panel.add(new JLabel(rb.getString("LabelGoodCnt")));
        ms100Panel.add(goodMsgCnt);
        ms100Panel.add(new JLabel(rb.getString("LabelBadCnt")));
        ms100Panel.add(badMsgCnt);
        ms100Panel.add(new JLabel(rb.getString("LabelMS100Status")));
        ms100Panel.add(ms100status);
        
        getContentPane().add(rawPanel);
        getContentPane().add(lb2Panel);
        getContentPane().add(pr2Panel);
        getContentPane().add(ms100Panel);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        getContentPane().add(updateButton);
        getContentPane().add(panel);

        // install "update" button handler
        updateButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    requestUpdate();
                }
            }
        );

        // add window help
        addHelpMenu("package.jmri.jmrix.loconet.locostats.LocoStatsFrame", true);

        // listen for LocoNet messages
        if (LnTrafficController.instance()!=null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            report("No LocoNet connection available, can't function");

        // and prep for display
        pack();
        lb2Panel.setVisible(false);
        rawPanel.setVisible(false);
        pr2Panel.setVisible(false);
        ms100Panel.setVisible(false);

        // finally, request data
        requestUpdate();

    }

    void report(String msg) {
        log.error(msg);
    }

    public void message(LocoNetMessage msg){
      if( updatePending &&
          ( msg.getOpCode() == LnConstants.OPC_PEER_XFER ) &&
          ( msg.getElement( 1 ) == 0x10 ) &&
          ( msg.getElement( 2 ) == 0x50 ) &&
          ( msg.getElement( 3 ) == 0x50 ) &&
          ( msg.getElement( 4 ) == 0x01 ) &&
          ( ( msg.getElement( 5 ) & 0xF0 ) == 0x0 ) &&
          ( ( msg.getElement( 10 ) & 0xF0 ) == 0x0 ) ) {
            // LocoBuffer II form
            int[] data = msg.getPeerXfrData() ;

            version.setText( StringUtil.twoHexFromInt( data[0]) + StringUtil.twoHexFromInt(data[4] ) );
            breaks.setText( Integer.toString( (data[5] << 16) + (data[6] << 8) + data[7] ) );
            errors.setText( Integer.toString( (data[1] << 16) + (data[2] << 8) + data[3] ) );

            lb2Panel.setVisible(true);
            rawPanel.setVisible(false);
            pr2Panel.setVisible(false);
            ms100Panel.setVisible(false);
            pack();

            updatePending = false ;

        } else if (updatePending &&
              ( msg.getOpCode() == LnConstants.OPC_PEER_XFER ) &&
              ( msg.getElement( 1 ) == 0x10 ) &&
              ( msg.getElement( 2 ) == 0x22 ) &&
              ( msg.getElement( 3 ) == 0x22 ) &&
              ( msg.getElement( 4 ) == 0x01 ) )
            {  // Digitrax form, check PR2/PR3 or MS100/PR3 mode
            
            if ((msg.getElement(8)&0x20) == 0) {
                // PR2 format
                int[] data = msg.getPeerXfrData() ;
                serial.setText(Integer.toString(data[1]*256+data[0]));
                status.setText(StringUtil.twoHexFromInt(data[2]));
                current.setText(Integer.toString( data[3]) );
                hardware.setText(Integer.toString( data[4]) );
                software.setText(Integer.toString( data[5]) );
                
                pr2Panel.setVisible(true);
            } else {
                // MS100 format
                int[] data = msg.getPeerXfrData();
                goodMsgCnt.setText(Integer.toString(data[1]*256+data[0]));
                badMsgCnt.setText(Integer.toString(data[5]*256+data[4]));
                ms100status.setText(StringUtil.twoHexFromInt(data[2]));

                ms100Panel.setVisible(true);
            }
            lb2Panel.setVisible(false);
            rawPanel.setVisible(false);
            pack();

            updatePending = false ;

        } else if (updatePending &&
              ( msg.getOpCode() == LnConstants.OPC_PEER_XFER ) ) {
            try {
                int[] data = msg.getPeerXfrData() ;
                r1.setText(StringUtil.twoHexFromInt(data[0]));
                r2.setText(StringUtil.twoHexFromInt(data[1]));
                r3.setText(StringUtil.twoHexFromInt(data[2]));
                r4.setText(StringUtil.twoHexFromInt(data[3]));
                r5.setText(StringUtil.twoHexFromInt(data[4]));
                r6.setText(StringUtil.twoHexFromInt(data[5]));
                r7.setText(StringUtil.twoHexFromInt(data[6]));
                r8.setText(StringUtil.twoHexFromInt(data[7]));

                lb2Panel.setVisible(false);
                rawPanel.setVisible(true);
                pr2Panel.setVisible(false);
                ms100Panel.setVisible(false);
                pack();

                updatePending = false ;
            } catch ( Exception e ) {
                log.error("Error parsing update: "+msg);
            }
        } else if (!updatePending && (msg.getOpCode() == LnConstants.OPC_GPBUSY)) {
            updatePending = true;
        }
    }

    public void requestUpdate() {
        LocoNetMessage msg = new LocoNetMessage( 2 ) ;
        msg.setOpCode( LnConstants.OPC_GPBUSY );
        updatePending = true ;
        LnTrafficController.instance().sendLocoNetMessage(msg);
    }

    public void dispose() {
        // disconnect from the LnTrafficController
        LnTrafficController.instance().removeLocoNetListener(~0,this);

        // take apart the JFrame
        super.dispose();
    }

    JTextField r1 = new JTextField(3);
    JTextField r2 = new JTextField(3);
    JTextField r3 = new JTextField(3);
    JTextField r4 = new JTextField(3);
    JTextField r5 = new JTextField(3);
    JTextField r6 = new JTextField(3);
    JTextField r7 = new JTextField(3);
    JTextField r8 = new JTextField(3);

    JTextField serial = new JTextField(6);
    JTextField status = new JTextField(5);
    JTextField current = new JTextField(4);
    JTextField hardware = new JTextField(2);
    JTextField software = new JTextField(3);

    JTextField goodMsgCnt = new JTextField(5);
    JTextField badMsgCnt = new JTextField(5);
    JTextField ms100status = new JTextField(6);
    
    JTextField version = new JTextField(8);
    JTextField breaks = new JTextField(6);
    JTextField errors = new JTextField(6);
    
    boolean updatePending = false ;

    JButton updateButton = new JButton("Update");

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoStatsFrame.class.getName());
}
