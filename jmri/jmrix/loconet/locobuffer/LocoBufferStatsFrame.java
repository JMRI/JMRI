// LocoBufferStatsFrame.java

package jmri.jmrix.loconet.locobuffer;

import jmri.jmrix.loconet.*;

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
 * @version			$Revision: 1.2 $
 */
public class LocoBufferStatsFrame extends JFrame implements LocoNetListener {

    public LocoBufferStatsFrame() {
        super("LocoBuffer Stats Monitor");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add GUI items
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(" Version:"));
        panel.add(version);
        panel.add(new JLabel(" Breaks:"));
        panel.add(breaks);
        panel.add(new JLabel(" Errors:"));
        panel.add(errors);
        getContentPane().add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        getContentPane().add(updateButton);
        getContentPane().add(panel);

        // install "update" button handler
        updateButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                  LocoNetMessage msg = new LocoNetMessage( 2 ) ;
                  msg.setOpCode( LnConstants.OPC_GPBUSY );
                  LnTrafficController.instance().sendLocoNetMessage(msg);
                  updatePending = true ;
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
          ( ( msg.getElement( 10 ) & 0xF0 ) == 0x0 ) )
      {
        int[] data = msg.getPeerXfrData() ;
        version.setText( Integer.toHexString( ( data[0] << 8 ) + data[4] ) );
        breaks.setText( Integer.toString( (data[5] << 16) + (data[6] << 8) + data[7] ) );
        errors.setText( Integer.toString( (data[1] << 16) + (data[2] << 8) + data[3] ) );
        updatePending = false ;
      }
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

    JTextField version = new JTextField("  XXXX");
    JTextField breaks = new JTextField("     0");
    JTextField errors = new JTextField("     0");
    boolean updatePending = false ;

    JButton updateButton = new JButton("Update");

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoBufferStatsFrame.class.getName());
}
