// ClockMonFrame.java

package jmri.jmrix.loconet.clockmon;

import jmri.jmrix.loconet.*;
import jmri.*;

import java.util.Date;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Frame displaying and programming a LocoNet clock monitor.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 *
 * @author			Bob Jacobsen   Copyright (C) 2003, 2004
 * @version			$Revision: 1.2 $
 */
public class ClockMonFrame extends JFrame implements SlotListener {

    public ClockMonFrame() {
        super("LocoNet clock monitor");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add GUI items
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(" Day:"));
        panel.add(days);
        panel.add(new JLabel(" Time:"));
        panel.add(hours);
        panel.add(new JLabel(":"));
        panel.add(minutes);
        getContentPane().add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(" Rate:"));
        panel.add(rate);
        getContentPane().add(panel);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        getContentPane().add(readButton);
        getContentPane().add(setButton);
        getContentPane().add(panel);

        getContentPane().add(setInternal);
        
        // Load GUI element contents with current slot contents
        notifyChangedSlot(SlotManager.instance().slot(LnConstants.FC_SLOT));

        // install "read" button handler
        readButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	SlotManager.instance().sendReadSlot(LnConstants.FC_SLOT);
                }
            }
        );
        // install "set" button handler
        setButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                	setContents();
                }
            }
        );

        // notice the window is closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                thisWindowClosing(e);
            }
        });

        // listen for updated slot contents
        if (SlotManager.instance()!=null)
            SlotManager.instance().addSlotListener(this);
        else
            log.error("No LocoNet connection available, can't function");

        // and prep for display
        pack();
    }

    /**
     * Handle changed slot contents, due to clock changes.
     * @param s
     */
    public void notifyChangedSlot(LocoNetSlot s) {
        if (s.getSlot()!= LnConstants.FC_SLOT ) return; // only watch clock slot
        if (log.isDebugEnabled()) log.debug("slot update "+s);

        // update GUI from the new slot contents
        days.setText(""+s.getFcDays());
        hours.setText(""+s.getFcHours());
        minutes.setText(""+s.getFcMinutes());
        rate.setText(""+s.getFcRate());
        
        // if needed, update internal clock & rate
        if (setInternal.isSelected()) {
            // set the internal timebase
            // we calculate a new msec value for a specific hour/minute
            // in the current day, then set that.
            long mSecPerHour = 3600000;
            long mSecPerMinute = 60000;
            Date tem = InstanceManager.timebaseInstance().getTime();
            int cHours = tem.getHours();
            long cNumMSec = tem.getTime();
            
            long nNumMSec = ((cNumMSec/mSecPerHour)*mSecPerHour) - (cHours*mSecPerHour) +
                    (s.getFcHours()*mSecPerHour) + (s.getFcMinutes()*mSecPerMinute);
            
            InstanceManager.timebaseInstance().setTime(new Date(nNumMSec));
            try {
                InstanceManager.timebaseInstance().setRate(s.getFcRate());
            } catch (TimebaseRateException e) { 
                if (!timebaseErrorReported) {
                    timebaseErrorReported = true;
                    log.warn("Time base exception on setting rate from LocoNet");
                }
            }          
        }
    }

    static boolean timebaseErrorReported = false;
    
    /**
     * Push GUI contents out to LocoNet slot.
     */
    void setContents() {
        LocoNetSlot s = SlotManager.instance().slot(LnConstants.FC_SLOT);
        s.setFcDays(Integer.parseInt(days.getText()));
        s.setFcHours(Integer.parseInt(hours.getText()));
        s.setFcMinutes(Integer.parseInt(minutes.getText()));
        s.setFcRate(Integer.parseInt(rate.getText()));
        LnTrafficController.instance().sendLocoNetMessage(s.writeSlot());
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
        // Drop loconet connection
        if (SlotManager.instance()!=null)
            SlotManager.instance().removeSlotListener(this);

        // take apart the JFrame
        super.dispose();
    }

    JTextField days = new JTextField("00");
    JTextField hours = new JTextField("00");
    JTextField minutes = new JTextField("00");

    JTextField rate = new JTextField(4);

    JCheckBox setInternal = new JCheckBox("LocoNet clock sets internal clock");
    
    JButton setButton = new JButton("Set");
    JButton readButton = new JButton("Read");

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ClockMonFrame.class.getName());

}
