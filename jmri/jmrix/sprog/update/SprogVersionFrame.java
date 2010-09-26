// SprogVersionFrame.java

package jmri.jmrix.sprog.update;

import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;

import javax.swing.*;

/**
 * Get the firmware version of the attached SPROG
 * @author			Andrew Crosland   Copyright (C) 2008
 * @version			$Revision: 1.7 $
 */
public class SprogVersionFrame
        extends SprogUpdateFrame
        implements SprogListener {
    
    public SprogVersionFrame() {
        super();
    }
    
    String sprogUSB = "";
    
    public void initComponents() throws Exception {
        setTitle(title());

        // connect to data source
        super.init();
        
        // Send a blank message
        msg = new SprogMessage(1);
        msg.setOpCode(' ');
        tc.sendSprogMessage(msg, this);
        bootState = CRSENT;
        startShortTimer();

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.sprog.update.SprogVersionFrame", true);

    }
    
    public void notifyMessage(SprogMessage m) {}   // Ignore

    synchronized public void notifyReply(SprogReply m) {
        replyString = m.toString();
        if (bootState == IDLE) {
            if (log.isDebugEnabled()) {
                log.debug("reply in IDLE state");
            }
            return;
        } else if (bootState == CRSENT) {
            stopTimer();
            if (log.isDebugEnabled()) {
                log.debug("reply in CRSENT state");
            }
            if ( (replyString.indexOf("P>")) >= 0) {
                msg = new SprogMessage(1);
                msg.setOpCode('?');
                tc.sendSprogMessage(msg, this);
                bootState = QUERYSENT;
            } else {
                JOptionPane.showMessageDialog(null, "SPROG prompt not found",
                        "SPROG Version", JOptionPane.ERROR_MESSAGE);
                setVisible(false);
                dispose();
            }
        } else if (bootState == QUERYSENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in QUERYSENT state");
            }
            // see if reply is from a SPROG
            if (replyString.indexOf("SPROG") < 0) {
                JOptionPane.showMessageDialog(null, "Not connected to a SPROG",
                        "SPROG Version", JOptionPane.ERROR_MESSAGE);
                setVisible(false);
                dispose();
            } else {
                sprogVersion = replyString.substring(replyString.indexOf(".") -
                        1, replyString.indexOf(".") + 2);
                if (replyString.indexOf("II") >= 0) {
                    sprogType = "SPROG II ";
                } else {
                    sprogType = "SPROG ";
                }
                if (replyString.indexOf("USB") >= 0) {
                    sprogUSB = "USB ";
                }
                JOptionPane.showMessageDialog(null, sprogType + sprogUSB + "v" + sprogVersion,
                        "SPROG Version", JOptionPane.INFORMATION_MESSAGE);
                setVisible(false);
                dispose();
            }
        }
    }

/**
   * Internal routine to handle a timeout
   */
    synchronized protected void timeout() {
        if (bootState == CRSENT) {
            JOptionPane.showMessageDialog(null, "No reply",
                    "SPROG Version", JOptionPane.ERROR_MESSAGE);
        }
    }

//    protected int SHORT_TIMEOUT=500;
//    
//    javax.swing.Timer timer = null;
//    
//    /**
//     * Internal routine to start timer to protect the mode-change.
//     */
//    protected void startShortTimer() {
//        restartTimer(SHORT_TIMEOUT);
//    }
//    
//    /**
//     * Internal routine to stop timer, as all is well
//     */
//    protected void stopTimer() {
//        if (timer!=null) timer.stop();
//    }
//    
//    /**
//     * Internal routine to handle timer starts & restarts
//     */
//    protected void restartTimer(int delay) {
//        if (timer==null) {
//            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
//                public void actionPerformed(java.awt.event.ActionEvent e) {
//                    timeout();
//                }
//            });
//        }
//        timer.stop();
//        timer.setInitialDelay(delay);
//        timer.setRepeats(false);
//        timer.start();
//    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger
    .getLogger(SprogVersionFrame.class.getName());
    
}
