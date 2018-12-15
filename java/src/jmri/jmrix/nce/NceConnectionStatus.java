package jmri.jmrix.nce;

import javax.swing.JOptionPane;
import jmri.jmrix.ConnectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
/**
 * Continuously checks and confirms that the communication link to the NCE
 * Command Station is operational by reading the revision number of the EPROM.
 * Only invokes the EPROM read when the interface experiences a timeout.
 *
 * Checks revision of NCE CS by reading the 3 byte revision. Sends a warning
 * message NCE EPROM found & preferences are not correct for revision selected.
 *
 * Also checks for March 2007 EPROM and warns user about Monitoring feedback.
 *
 * @author Daniel Boudreau (C) 2007, 2010, 2012
 *
 */
public class NceConnectionStatus implements NceListener {

    private static final boolean JOptPane_ERROR_MESSAGES_ENABLED = true;
    private static final boolean JOptPane_WARNING_MESSAGES_ENABLED = false; // Disabled for headless operations!

    private static final int REPLY_LEN = 3; // number of bytes read

    // EPROM Checker states
    private static final int INIT_STATE = 0; // Initial state
    private static final int WAIT_STATE = 1; // Waiting for reply
    private static final int CHECK_OK = 2; // Valid response
    private static final int NORMAL_STATE = 4; // Normal state

    private static final int WARN1_STATE = 8; // Serial interface is not functioning properly
    private static final int WARN2_STATE = 9; // Detected 2007 March EPROM

    // all of the error states below display a JOptionPane error message
    private static final int ERROR1_STATE = 16; // Wrong revision EPROM, 2004 or earlier
    private static final int ERROR2_STATE = 17; // Wrong revision EPROM, 2006 or later
    private static final int ERROR4_STATE = 19; // Wrong NCE System
    private static final int ERROR5_STATE = 20; // Wrong NCE System, detected Power Cab
    private static final int ERROR6_STATE = 21; // Wrong NCE System, detected Smart Booster SB3
    private static final int ERROR7_STATE = 22; // Wrong NCE System, detected Power Pro
    private static final int ERROR8_STATE = 23; // Wrong NCE System, detected SB5

    private int epromState = INIT_STATE; // Eprom state
    private boolean epromChecked = false;

    private static boolean nceEpromMarch2007 = false; // flag to allow JMRI to be bug for bug compatible

    // Our current knowledge of NCE Command Station EPROMs
    private static final int VV_1999 = 4; // Revision of Apr 1999 EPROM VV.MM.mm = 4.0.1
    private static final int MM_1999 = 0;
    private static final int mm_1999 = 1;

    private static final int VV_2004 = 6; // Revision of Dec 2004 EPROM VV.MM.mm = 6.0.0
    private static final int MM_2004 = 0;
    private static final int mm_2004 = 0;

    private static final int VV_2007 = 6; // Revision of Mar 2007 EPROM VV.MM.mm = 6.2.0
    private static final int MM_2007 = 2;
    private static final int mm_2007 = 0;

    private static final int mm_2007a = 1; // Revision of May 2007 EPROM VV.MM.mm = 6.2.1
    private static final int mm_2008 = 2; // Revision of 2008 EPROM VV.MM.mm = 6.2.2

    private static final int VV_2012 = 7; // Revision 2012 EPROM VV.MM.mm = 7.2.0
    private static final int MM_2012 = 2;

    // USB -> Cab bus adapter:
    // When used with PowerCab V1.28 - 6.3.0
    // When used with SB3 V1.28 - 6.3.1 (No program track on an SB3)
    // When used with PH-Pro or PH-10 - 6.3.2 (limited set of features available
    // through cab bus)
    //
    // Future version of PowerCab V1.61 - 6.3.4
    // Future version of SB3 V1.61 - 6.3.5
    //
    // NOTE: The USB port can not read CS memory, unless greater than 7.* version
    private static final int VV_USB_V6 = 6; // Revision of USB EPROM VV.MM.mm = 6.3.x
    private static final int VV_USB_V7 = 7; // 2012 revision of USB EPROM VV.MM.mm = 7.3.x
    private static final int MM_USB = 3;
    // V6 flavors
    private static final int mm_USB_V6_PwrCab = 0; // PowerCab
    private static final int mm_USB_V6_SB3 = 1; // SB3
    private static final int mm_USB_V6_PH = 2;  // PH-Pro or PH-10
    // Future releases by NCE (Not used by JMRI yet!)
    private static final int mm_USB_V6_ALL = 3;  // All systems, not currently used
    private static final int mm_USB_V6_PC161 = 4; // Future use, PowerCab 1.61, not currently used
    private static final int mm_USB_V6_SB161 = 5; // Future use, SB3 1.61, not currently used
    // V7 flavors
    private static final int mm_USB_V7_PC_128_A = 0; // PowerCab with 1.28c
    private static final int mm_USB_V7_SB5_165_A = 1; // SB5 with 1.65
    private static final int mm_USB_V7_SB5_165_B = 2; // SB5 with 1.65
    private static final int mm_USB_V7_PC_165 = 3;  // PowerCab with 1.65
    private static final int mm_USB_V7_PC_128_B = 4; // PowerCab with 1.28c
    private static final int mm_USB_V7_SB3 = 5;   // SB3 with 1.28c
    private static final int mm_USB_V7_PH = 6;   // PowerPro with 3.1.2007
    private static final int mm_USB_V7_ALL = 7;   // All systems

    private NceTrafficController tc = null;

    public NceConnectionStatus(NceTrafficController tc) {
        super();
        this.tc = tc;
    }

    public NceMessage nceEpromPoll() {

        if (tc.getCommandOptions() <= NceTrafficController.OPTION_1999) {
            return null;
        }

        // normal state for this routine?
        if (epromState == NORMAL_STATE) {
            // are there interface timeouts?
            if (tc.hasTimeouts()) {
                epromState = INIT_STATE;
            } else {
                return null;
            }
        }

        if (epromState == CHECK_OK) {
            ConnectionStatus.instance().setConnectionState(tc.getUserName(), tc.getPortName(), ConnectionStatus.CONNECTION_UP);
            epromState = NORMAL_STATE;
            return null;
        }

        if (epromState != INIT_STATE) {
            ConnectionStatus.instance().setConnectionState(tc.getUserName(), tc.getPortName(), ConnectionStatus.CONNECTION_DOWN);
        }

        // no response from command station?
        if (epromState == WAIT_STATE) {
            log.warn("Incorrect or no response from NCE command station");

            if (JOptPane_WARNING_MESSAGES_ENABLED) {
                JOptionPane.showMessageDialog(null,
                        "JMRI could not establish communication with NCE command station. \n"
                        + "Check the \"Serial port:\" and \"Baud rate:\" in Edit -> Preferences. \n"
                        + "Confirm cabling and that the NCE system is powered up.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }

            epromState = WARN1_STATE;
        } // still no response from command station?
        else if (epromState == WARN1_STATE) {
            log.warn("No response from NCE command station");
        }

        if (epromState == ERROR1_STATE) {
            if (JOptPane_ERROR_MESSAGES_ENABLED) {
                JOptionPane.showMessageDialog(null,
                        "Wrong revision of Command Station EPROM selected in Preferences \n"
                        + "Change the Command Station EPROM selection to \"2004 or earlier\"", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            epromState = NORMAL_STATE;
            return null;
        }

        if (epromState == ERROR2_STATE) {
            if (JOptPane_ERROR_MESSAGES_ENABLED) {
                JOptionPane.showMessageDialog(null,
                        "Wrong revision of Command Station EPROM selected in Preferences \n"
                        + "Change the Command Station EPROM selection to \"2006 or later\"", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            epromState = NORMAL_STATE;
            return null;
        }

        if (epromState == WARN2_STATE) {
            log.warn("Detected 2007 March EPROM which doesn't provide reliable MONITORING feedback for turnouts");
            if (JOptPane_WARNING_MESSAGES_ENABLED) // Need to add checkbox "Do not show this message again" otherwise
            // the message can be a pain.
            {
                JOptionPane.showMessageDialog(null, "The 2007 March EPROM doesn't provide reliable feedback,"
                        + " contact NCE if you want to use MONITORING feedback ", "Warning",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            ConnectionStatus.instance().setConnectionState(tc.getUserName(), tc.getPortName(), ConnectionStatus.CONNECTION_UP);
            epromState = NORMAL_STATE;
            return null;
        }

        if (epromState == ERROR4_STATE) {
            if (JOptPane_ERROR_MESSAGES_ENABLED) {
                JOptionPane.showMessageDialog(null, "Wrong NCE System Connection selected in Preferences. "
                        + "Change the System Connection to \"" + jmri.jmrix.nce.serialdriver.ConnectionConfig.NAME
                        + "\" or \"" + jmri.jmrix.nce.networkdriver.ConnectionConfig.NAME + "\".", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            epromState = NORMAL_STATE;
            return null;
        }

        if (epromState == ERROR5_STATE) {
            if (JOptPane_ERROR_MESSAGES_ENABLED) {
                JOptionPane.showMessageDialog(null, "Wrong NCE System Connection selected in Preferences. "
                        + "The System Connection \"" + jmri.jmrix.nce.usbdriver.ConnectionConfig.NAME
                        + "\" should change the system to \"Power Cab\".", "Error", JOptionPane.ERROR_MESSAGE);
            }
            epromState = NORMAL_STATE;
            return null;
        }

        if (epromState == ERROR6_STATE) {
            if (JOptPane_ERROR_MESSAGES_ENABLED) {
                JOptionPane.showMessageDialog(null, "Wrong NCE System Connection selected in Preferences. "
                        + "The System Connection \"" + jmri.jmrix.nce.usbdriver.ConnectionConfig.NAME
                        + "\" should change the system to \"Smart Booster SB3\".", "Error", JOptionPane.ERROR_MESSAGE);
            }
            epromState = NORMAL_STATE;
            return null;
        }

        if (epromState == ERROR7_STATE) {
            if (JOptPane_ERROR_MESSAGES_ENABLED) {
                JOptionPane.showMessageDialog(null, "Wrong NCE System Connection selected in Preferences. "
                        + "The System Connection \"" + jmri.jmrix.nce.usbdriver.ConnectionConfig.NAME
                        + "\" should change the system to \"Power Pro\".", "Error", JOptionPane.ERROR_MESSAGE);
            }
            epromState = NORMAL_STATE;
            return null;
        }

        if (epromState == ERROR8_STATE) {
            if (JOptPane_ERROR_MESSAGES_ENABLED) {
                JOptionPane.showMessageDialog(null, "Wrong NCE System Connection selected in Preferences. "
                        + "The System Connection \"" + jmri.jmrix.nce.usbdriver.ConnectionConfig.NAME
                        + "\" should change the system to \"SB5\".", "Error", JOptionPane.ERROR_MESSAGE);
            }
            epromState = NORMAL_STATE;
            return null;
        }

        // go ahead and read the EPROM revision
        if (epromState != WARN1_STATE) // stay in error state until reply
        {
            epromState = WAIT_STATE;
        }

        byte[] bl = NceBinaryCommand.getNceEpromRev();
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_LEN);
        return m;

    }

    @Override
    public void message(NceMessage m) {
        if (log.isDebugEnabled()) {
            log.debug("unexpected message");
        }
    }

    @Override
    public void reply(NceReply r) {
        if (r.getNumDataElements() == REPLY_LEN) {

            byte VV = (byte) r.getElement(0);
            byte MM = (byte) r.getElement(1);
            byte mm = (byte) r.getElement(2);

            // Is the reply valid? Check major revision, there are only three valid responses
            // note that VV_2004 = VV_2007 = VV_USB
            if (VV != VV_2012 && VV != VV_2004 && VV != VV_1999) {
                log.error("Wrong major revision: " + Integer.toHexString(VV & 0xFF));
                // show the entire revision number
                log.info("NCE EPROM revision = " + Integer.toHexString(VV & 0xFF) + "."
                        + Integer.toHexString(MM & 0xFF) + "." + Integer.toHexString(mm & 0xFF));
                return;
            }

            // We got a valid reply so we're done!
            epromState = CHECK_OK;

            // Have we already done the error checking?
            if (epromChecked) {
                return;
            }
            epromChecked = true;

            // Send to log file the NCE EPROM revision
            log.info("NCE EPROM revision = " + Integer.toHexString(VV & 0xFF) + "." + Integer.toHexString(MM & 0xFF)
                    + "." + Integer.toHexString(mm & 0xFF));

            // Warn about the March 2007 CS EPROM
            if (VV == VV_2007 && MM == MM_2007 && mm == mm_2007) {
                setNceEpromMarch2007(true);
                epromState = WARN2_STATE;
            }

            // Confirm that user selected correct revision of EPROM, check for old EPROM installed, new EPROM
            // preferences
            if ((VV <= VV_2007 && MM < MM_2007) && (tc.getCommandOptions() >= NceTrafficController.OPTION_2006)) {
                log.error("Wrong revision (" + Integer.toHexString(VV & 0xFF) + "." + Integer.toHexString(MM & 0xFF)
                        + "." + Integer.toHexString(mm & 0xFF)
                        + ") of the NCE Command Station EPROM selected in Preferences");
                epromState = ERROR1_STATE;
            }

            // Confirm that user selected correct revision of EPROM, check for new EPROM installed, old EPROM
            // preferences
            boolean eprom2007orNewer = ((VV == VV_2007) && (MM >= MM_2007));
            if (((VV > VV_2007) || eprom2007orNewer) && (tc.getCommandOptions() < NceTrafficController.OPTION_2006)) {
                log.error("Wrong revision (" + Integer.toHexString(VV & 0xFF) + "." + Integer.toHexString(MM & 0xFF)
                        + "." + Integer.toHexString(mm & 0xFF)
                        + ") of the NCE Command Station EPROM selected in Preferences");
                epromState = ERROR2_STATE;
            }

            // Check that layout connection is correct
            // PowerPro? 4 cases for PH, 1999, 2004, 2007, & 2012
            if (VV == VV_1999 || (VV == VV_2004 && MM == MM_2004) || (VV == VV_2007 && MM == MM_2007)
                    || (VV == VV_2012 && MM == MM_2012)) // make sure system connection is not NCE USB
            {
                if (tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE) {
                    log.error("System Connection is incorrect, detected Power Pro");
                    epromState = ERROR4_STATE;
                }
            }

            // Check for USB 6.3.x
            if (VV == VV_USB_V6 && MM == MM_USB) {
                // USB detected, check to see if user preferences are correct
                if (mm == mm_USB_V6_PwrCab && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_POWERCAB) {
                    log.error("System Connection is incorrect, detected USB connected to a PowerCab");
                    epromState = ERROR5_STATE;
                }
                if (mm == mm_USB_V6_SB3 && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_SB3) {
                    log.error("System Connection is incorrect, detected USB connected to a Smart Booster SB3");
                    epromState = ERROR6_STATE;
                }
                if (mm == mm_USB_V6_PH && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_POWERPRO) {
                    log.error("System Connection is incorrect, detected USB connected to a Power Pro");
                    epromState = ERROR7_STATE;
                }
            }
            // Check for USB 7.3.x
            if (VV == VV_USB_V7 && MM == MM_USB) {
                // USB V7 detected, check to see if user preferences are correct
                if (((mm == mm_USB_V7_PC_128_A) || (mm == mm_USB_V7_PC_128_B)) && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_POWERCAB) {
                    log.error("System Connection is incorrect, detected USB connected to a PowerCab");
                    epromState = ERROR5_STATE;
                }
                if (mm == mm_USB_V7_SB3 && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_SB3) {
                    log.error("System Connection is incorrect, detected USB connected to a Smart Booster SB3");
                    epromState = ERROR6_STATE;
                }
                if (mm == mm_USB_V7_PH && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_POWERPRO) {
                    log.error("System Connection is incorrect, detected USB connected to a Power Pro");
                    epromState = ERROR7_STATE;
                }
                if (((mm == mm_USB_V7_SB5_165_A) || (mm == mm_USB_V7_SB5_165_B)) && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_SB5) {
                    log.error("System Connection is incorrect, detected USB connected to a Smart Booster SB5");
                    epromState = ERROR8_STATE;
                }
            }

        } else {
            log.warn("wrong number of read bytes for revision check");
        }
    }

    public static boolean isNceEpromMarch2007() {
        return nceEpromMarch2007;
    }

    private static void setNceEpromMarch2007(boolean b) {
        nceEpromMarch2007 = b;
    }

    private final static Logger log = LoggerFactory.getLogger(NceConnectionStatus.class);

}

