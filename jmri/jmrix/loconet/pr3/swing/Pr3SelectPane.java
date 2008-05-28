// Pr3SelectPane.java

package jmri.jmrix.loconet.pr3.swing;

import jmri.jmrix.loconet.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ResourceBundle;


/**
 * Pane for downloading software updates to PRICOM products
 * @author	    Bob Jacobsen   Copyright (C) 2005
 * @version	    $Revision: 1.1 $
 */
public class Pr3SelectPane extends javax.swing.JPanel implements LocoNetListener {

    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.pr3.Pr3Bundle");

    public Pr3SelectPane() {
    
        // first build GUI
        setLayout(new FlowLayout());
        
        JButton b = new JButton(res.getString("ButtonPr2Mode"));
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                    selectPR2mode();
            }
        });
        add(b);
        
        b = new JButton(res.getString("ButtonMs100Mode"));
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event) {
                    selectMS100mode();
            }
        });
        add(b);
        add(status);
        
        // listen for LocoNet messages
        if (LnTrafficController.instance()!=null)
            LnTrafficController.instance().addLocoNetListener(~0, this);
        else
            log.error("No LocoNet connection available, can't function");
        
        // request status
        LocoNetMessage msg = new LocoNetMessage( 2 ) ;
        msg.setOpCode( LnConstants.OPC_GPBUSY );
        LnTrafficController.instance().sendLocoNetMessage(msg);
    }
    
    JLabel status = new JLabel(res.getString("StatusUnknown"));
    
    void selectPR2mode() {
        // set to PR2 mode
        status.setText(res.getString("StatusPr2"));
        LocoNetMessage msg = new LocoNetMessage( 6 ) ;
        msg.setOpCode( 0xD3 );
        msg.setElement( 1, 0x10 );
        msg.setElement( 2, 1 );  // set PR2
        msg.setElement( 3, 0 );
        msg.setElement( 4, 0 );
        LnTrafficController.instance().sendLocoNetMessage(msg);
    }

    void selectMS100mode() {
        // set to MS100 mode
        status.setText(res.getString("StatusMs100"));
        LocoNetMessage msg = new LocoNetMessage( 6 ) ;
        msg.setOpCode( 0xD3 );
        msg.setElement( 1, 0x10 );
        msg.setElement( 2, 0 );  // set MS100
        msg.setElement( 3, 0 );
        msg.setElement( 4, 0 );
        LnTrafficController.instance().sendLocoNetMessage(msg);
    }
    
    public void message(LocoNetMessage msg){
        if (  ( msg.getOpCode() == LnConstants.OPC_PEER_XFER ) &&
              ( msg.getElement( 1 ) == 0x10 ) &&
              ( msg.getElement( 2 ) == 0x22 ) &&
              ( msg.getElement( 3 ) == 0x22 ) &&
              ( msg.getElement( 4 ) == 0x01 ) )
            {  // Digitrax form, check PR2/PR3 or MS100/PR3 mode
                int mode = msg.getElement(8)&0x0C;
            if (mode == 0x00) {
                // PR2 format
                status.setText(res.getString("StatusPr2"));
            } else {
                // MS100 format
                status.setText(res.getString("StatusMs100"));
            }
        }
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Pr3SelectPane.class.getName());

}
