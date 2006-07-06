// LocoBufferStatsFrame.java

package jmri.jmrix.loconet.locobuffer;

import jmri.jmrix.loconet.*;

import jmri.util.StringUtil;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Frame displaying the LocoBuffer Version Number, Bad Frame Counter and Break Counter.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 *
 * @author			Alex Shepherd   Copyright (C) 2003
 * @version			$Revision: 1.5 $
 */
public class LocoBufferStatsFrame extends JFrame implements LocoNetListener {

    JPanel lb2Panel;
    JPanel rawPanel;
    JPanel pr2Panel;
    
    public LocoBufferStatsFrame() {
        super("LocoNet Stats Monitor");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add GUI items
        rawPanel = new JPanel();
        rawPanel.setLayout(new BoxLayout(rawPanel, BoxLayout.X_AXIS));
        rawPanel.add(new JLabel(" Raw data:"));
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
        lb2Panel.add(new JLabel(" Version:"));
        lb2Panel.add(version);
        lb2Panel.add(new JLabel(" Breaks:"));
        lb2Panel.add(breaks);
        lb2Panel.add(new JLabel(" Errors:"));
        lb2Panel.add(errors);

        pr2Panel = new JPanel();
        pr2Panel.setLayout(new BoxLayout(pr2Panel, BoxLayout.X_AXIS));
        pr2Panel.add(new JLabel(" Serial number:"));
        pr2Panel.add(serial);
        pr2Panel.add(new JLabel(" Status:"));
        pr2Panel.add(status);
        pr2Panel.add(new JLabel(" Current:"));
        pr2Panel.add(current);
        pr2Panel.add(new JLabel(" Hardware Version:"));
        pr2Panel.add(hardware);
        pr2Panel.add(new JLabel(" Software Version:"));
        pr2Panel.add(software);
        
       
        getContentPane().add(rawPanel);
        getContentPane().add(lb2Panel);
        getContentPane().add(pr2Panel);

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

        // notice the window is closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });

          // listen for LocoNet messages
        if (LnTrafficController.instance()!=null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.error("No LocoNet connection available, can't function");

        // and prep for display
        pack();
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
        
        updatePending = false ;

        } else if (updatePending &&
              ( msg.getOpCode() == LnConstants.OPC_PEER_XFER ) &&
              ( msg.getElement( 1 ) == 0x10 ) &&
              ( msg.getElement( 2 ) == 0x22 ) &&
              ( msg.getElement( 3 ) == 0x22 ) &&
              ( msg.getElement( 4 ) == 0x01 ) ) 
            {  // PR2 form
            serial.setText(Integer.toString(data[1]*256+data[0]));
            status.setText(StringUtil.twoHexFromInt(data[2]));
            current.setText(Integer.toString( data[3]) );
            hardware.setText(Integer.toString( data[4]) );
            software.setText(Integer.toString( data[5]) );

        } else if (updatePending) {
            r1.setText(StringUtil.twoHexFromInt(data[0]));
            r2.setText(StringUtil.twoHexFromInt(data[1]));
            r3.setText(StringUtil.twoHexFromInt(data[2]));
            r4.setText(StringUtil.twoHexFromInt(data[3]));
            r5.setText(StringUtil.twoHexFromInt(data[4]));
            r6.setText(StringUtil.twoHexFromInt(data[5]));
            r7.setText(StringUtil.twoHexFromInt(data[6]));
            r8.setText(StringUtil.twoHexFromInt(data[7]));
        }
    }

    public void requestUpdate() {
        LocoNetMessage msg = new LocoNetMessage( 2 ) ;
        msg.setOpCode( LnConstants.OPC_GPBUSY );
        updatePending = true ;
        LnTrafficController.instance().sendLocoNetMessage(msg);
    }
    
    /**
     * Destroy the window when the close box is clicked, as there is no
     * way to get it to show again.
     */
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
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

    JTextField version = new JTextField("  XXXX");
    public JTextField breaks = new JTextField("     0");
    public JTextField errors = new JTextField("     0");
    boolean updatePending = false ;

    
    JButton updateButton = new JButton("Update");

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoBufferStatsFrame.class.getName());
}
