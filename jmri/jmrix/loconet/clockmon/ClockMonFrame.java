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
 * @version			$Revision: 1.3 $
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
        panel.add(new JLabel("."));
        panel.add(frac_mins);
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
        getContentPane().add(correctFastClockMaster);

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

        // install "Correct Fast Clock Master handler
        correctFastClockMaster.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                        correctFastClockMasterAction();
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

          // Get an instance of the internal timebase
        clock = InstanceManager.timebaseInstance();

          // Create a Timebase listner for the Minute change events
        minuteChangeListener = new java.beans.PropertyChangeListener() {
          public void propertyChange(java.beans.PropertyChangeEvent e) {
            newMinute();
          }
        } ;
    }

    void correctFastClockMasterAction() {
      if( correctFastClockMaster.isSelected() )
      {
          // Set a flag to say we are not in sync
        inSyncWithFastClockMaster = false ;

          // Now enable the setting of the internal clock from the LocoNet Fast Clock Master
          // as this is the basis of us correcting the Fast Clock Master
        setInternal.setSelected( true );

          // Request Fast Clock Read
        SlotManager.instance().sendReadSlot(LnConstants.FC_SLOT);
        InstanceManager.timebaseInstance().addMinuteChangeListener( minuteChangeListener );
      }
      else
      {
        log.debug( "correctExternalAction: Correction: Disabled" );
        InstanceManager.timebaseInstance().removeMinuteChangeListener( minuteChangeListener );
      }
    }

    public void newMinute()
    {
      if( correctFastClockMaster.isSelected() && inSyncWithFastClockMaster )
      {
        Date now = clock.getTime();

        LocoNetSlot s = SlotManager.instance().slot(LnConstants.FC_SLOT);
          // Set the Fast Clock Day to the current Day of the month 1-31
        s.setFcDays(now.getDate());

        s.setFcHours(now.getHours());
        s.setFcMinutes(now.getMinutes());

        long millis = now.getTime() ;
          // How many ms are we into the fast minute as we want to sync the
          // Fast Clock Master Frac_Mins to the right 65.535 ms tick
        long elapsedMS = millis % 60000 ;
        double frac_min = elapsedMS / 60000.0 ;
        int ticks = 915 - (int)( 915 * frac_min ) ;

        s.setFcFracMins( ticks );
        LnTrafficController.instance().sendLocoNetMessage(s.writeSlot());
      }
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
        frac_mins.setText( ""+s.getFcFracMins());

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
                    (s.getFcHours()*mSecPerHour) + (s.getFcMinutes()*mSecPerMinute) ;

              // Work out how far through the current fast minute we are
              // and add that on to the time.
            nNumMSec += (long) ( ( ( 915 - s.getFcFracMins() ) / 915.0 * 60000) ) ;

            InstanceManager.timebaseInstance().setTime(new Date(nNumMSec));
            try {
                InstanceManager.timebaseInstance().setRate(s.getFcRate());

                  // Once we have done everything else set the flag to say we
                  // are in sync with the master
                inSyncWithFastClockMaster = true;

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
        s.setFcFracMins(Integer.parseInt(frac_mins.getText()));
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

          // Remove ourselves from the Timebase minute rollover event
        InstanceManager.timebaseInstance().removeMinuteChangeListener( minuteChangeListener );
        minuteChangeListener = null ;

        // take apart the JFrame
        super.dispose();
    }

    JTextField days = new JTextField("00");
    JTextField hours = new JTextField("00");
    JTextField minutes = new JTextField("00");
    JTextField frac_mins = new JTextField("00");

    JTextField rate = new JTextField(4);

    JCheckBox setInternal = new JCheckBox("LocoNet Fast Clock sets Internal Clock");

    Timebase clock ;

    JCheckBox correctFastClockMaster = new JCheckBox("Correct LocoNet Fast Clock Master");
    java.beans.PropertyChangeListener minuteChangeListener ;

    JButton setButton = new JButton("Set");
    JButton readButton = new JButton("Read");

    boolean inSyncWithFastClockMaster = false ;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ClockMonFrame.class.getName());

}
