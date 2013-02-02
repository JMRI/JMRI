// LnDplxGrpInfoImpl.java

package jmri.jmrix.loconet.duplexgroup.swing;

import org.apache.log4j.Logger;
import jmri.jmrix.loconet.duplexgroup.*;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Provides a low-level interface to Digitrax Duplex Group Identity information
 *
 * Implements the following "Property Change" events, which are defined as
 * static strings in DuplexGropuIdentityImplConstants:
 *      DPLX_PC_STAT_LN_UPDATE    - Indicates that a GUI status line could be
 *              updated using provided string
 *
 *      DPLX_PC_STAT_LN_UPDATE_IF_NOT_CURRENTLY_ERROR    - Indicates that a GUI
 *              status line could be updated using the provided string UNLESS
 *              the status line is currently showing an error
 *
 *      NumberOfUr92sUpdate     - Indicates that the class has counted the
 *              number of UR92 devices
 *
 *      DPLX_PC_NAME_UPDATE   - Indicates that a LocoNet message has reported
 *              the Duplex Group Name
 *
 *      DPLX_PC_CHANNEL_UPDATE    - Indicates that a LocoNet message has
 *              reported the Duplex Group Channel
 *
 *      DPLX_PC_PASSWORD_UPDATE   - Indicates that a LocoNet message has
 *              reported the Duplex Group Password
 *
 *      DPLX_PC_ID_UPDATE     - Indicates that a LocoNet message has reported
 *              the Duplex Group Id
 *
 *      DPLX_PC_NAME_VALIDITY     - Indicates that the validity of GUI field
 *              showing the Duplex Group Name should be changed.  NewValue() is
 *              true if a valid Duplex Group Name is available; is false if the
 *              Duplex Group Name should be considered invalid.
 *
 *      DPLX_PC_CHANNEL_VALIDITY      - Indicates that the validity of GUI
 *              field showing the Duplex Group Channel should be changed.
 *              NewValue() is true if a valid Duplex Group Channel is available;
 *              is false if the Duplex Group Channel should be considered invalid.
 *
 *      DPLX_PC_PASSWORD_VALIDITY     - Indicates that the validity of GUI
 *              field showing the Duplex Group Password should be changed.
 *              NewValue() is true if a valid Duplex Group Password is available;
 *              is false if the Duplex Group Password should be considered invalid.
 *
 *      DPLX_PC_ID_VALIDITY       - Indicates that the validity of GUI field
 *              showing the Duplex Group Id should be changed.  NewValue() is
 *              true if a valid Duplex Group Id is available; is false if the
 *              Duplex Group Id should be considered invalid.
 *
 *      DPLX_PC_RCD_DPLX_IDENTITY_QUERY    - Indicates that a LocoNet message
 *              which queries the Duplex Group identity has been received.
 *
 *      DPLX_PC_RCD_DPLX_IDENTITY_REPORT   - Indicates that a LocoNet message
 *              which reports the Duplex Group identity has been received.
 *
 * @author B. Milhaupt Copyright 2011
 */
public class LnDplxGrpInfoImpl extends javax.swing.JComponent implements jmri.jmrix.loconet.LocoNetListener {

    private static final boolean limitPasswordToNumericCharacters = false;
    private LocoNetSystemConnectionMemo memo;
    private Integer numUr92;
    private javax.swing.Timer   swingTmrIplQuery;
    private javax.swing.Timer   swingTmrDuplexInfoQuery;
    private boolean waitingForIplReply;
    private boolean gotQueryReply;

    LnDplxGrpInfoImpl thisone;

    public LnDplxGrpInfoImpl() {
        super();
        thisone = this;

        // figure out a LnConnectionMemo to use - get the first  one available in the list...
        java.util.List<Object> listOfLocoNetSystemConnectionMemos;
        listOfLocoNetSystemConnectionMemos = jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
        boolean foundMemo = false;
        LocoNetSystemConnectionMemo thisMemo = null;
        for (Object listObj : listOfLocoNetSystemConnectionMemos) {
            if (foundMemo == false) {
                thisMemo = (LocoNetSystemConnectionMemo)listObj;
                foundMemo = true;
            }
        }
        if (foundMemo == false) {
            // did not find a connection memo.  Abort.
            log.error("No LocoNet Connection Memo available - cannot initialize DuplexGropuIdentityImplementation class.");
            return;
        }

        finishInitialization(thisMemo);
    }

    public LnDplxGrpInfoImpl(LocoNetSystemConnectionMemo LNCMemo) {
        super();
        thisone = this;

        finishInitialization(LNCMemo);
    }

    private void finishInitialization(LocoNetSystemConnectionMemo LNCMemo) {

        memo = LNCMemo;

        // connect to the LnTrafficController
        connect(memo.getLnTrafficController());

        numUr92 = 0;        // assume 0 UR92 devices available
        waitingForIplReply = false;

        swingTmrIplQuery = new javax.swing.Timer(LnDplxGrpInfoImplConstants.IPL_QUERY_DELAY, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                swingTmrIplQuery.stop();
                waitingForIplReply = false;
                int oldvalue = 9999;
                int newvalue = 0;
                if (numUr92 > 0) {
                    newvalue = numUr92;
                    thisone.firePropertyChange("NumberOfUr92sUpdate", oldvalue, newvalue);
                    InvalidateDataAndQueryDuplexInfo();
                }
                else {
                    thisone.firePropertyChange("NumberOfUr92sUpdate", oldvalue, newvalue);
                    thisone.firePropertyChange(DPLX_PC_STAT_LN_UPDATE, " ", "ErrorNoUR92Found");
                }
            }
        });
        swingTmrDuplexInfoQuery = new javax.swing.Timer(LnDplxGrpInfoImplConstants.DPLX_QUERY_DELAY, new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                swingTmrDuplexInfoQuery.stop();
                waitingForIplReply = false;
                if (gotQueryReply == true) {
                    // do not want to erase any status message other than the "Processing" message.
                    thisone.firePropertyChange(DPLX_PC_STAT_LN_UPDATE_IF_NOT_CURRENTLY_ERROR, "", " ");
                    gotQueryReply = false;
                }
                else {
                    thisone.firePropertyChange(DPLX_PC_STAT_LN_UPDATE, " ", "ErrorNoQueryResponse");
                    numUr92 = 0;
                    int oldvalue = 9999;
                    int newvalue = 0;
                    thisone.firePropertyChange("NumberOfUr92sUpdate", oldvalue, newvalue);
                }
            }
        });

        acceptedGroupName = "";
        acceptedGroupChannel = "";
        acceptedGroupPassword = "";
        acceptedGroupId = "";

    }

    /**
     * Reports whether Duplex Group Password must only be numeric,
     * or if Password is allowed to include characters 'A', 'B', and/or 'C'.
     * @return true if Password may only include digits.
     */
    public static final boolean getLimitPasswordToNumericOnly() {
        return limitPasswordToNumericCharacters;
    }

    /**
     * Validates a Duplex Group Name.
     * <p>
     * A valid Duplex Group Name is an 8 character string.  The calling method
     * should append spaces or truncate to give correct length if necessary.
     * <p>
     * @param sGroupName
     * @return true if and only if groupName is a valid Duplex Group Name
     */
    public static final boolean validateGroupName(String sGroupName) {
        if (sGroupName.length() == 0) return false;
        return sGroupName.matches("^.{8}$");
    }

    /**
     * Validates a Duplex Group Password.
     * <p>
     * Note that the password must be four digits if only numeric values are allowed,
     * or must be four characters, each of pattern [0-9A-C] if alphanumeric values
     * are allowed.  (See private field limitPasswordToNumericCharacters.)
     * <p>
     * @param sGroupPassword
     * @return true if and only if sGroupPassword is a valid Duplex Group Password.
     */
    public static final boolean validateGroupPassword(String sGroupPassword)     {
        // force the value to uppercase
        if (sGroupPassword.length() == 0) return false;
       if (limitPasswordToNumericCharacters == true) {
            return sGroupPassword.matches("^[0-9][0-9][0-9][0-9]$");
        } else {
            return sGroupPassword.matches("^[0-9A-C][0-9A-C][0-9A-C][0-9A-C]$");
        }
    }

    /**
     * Validates a Duplex Group Channel Number.
     *
     * @param iGroupChannel
     * @return true if and only if iGroupChannel is a valid Duplex Group Channel.
     */
    public static final boolean validateGroupChannel(Integer iGroupChannel)     {
        if ((iGroupChannel < LnDplxGrpInfoImplConstants.DPLX_MIN_CH ) ||
                (iGroupChannel > LnDplxGrpInfoImplConstants.DPLX_MAX_CH )) {
            return false;
        } else {
            return true;
        }
    }

     /**
      *  Validates the parameter as a Duplex Group ID number.
      *
      * @param iGroupId
      * @return true if and only if iGroupId is a valid Duplex Group ID.
      */
    public static final boolean validateGroupID(Integer iGroupId) {

        if ((iGroupId < LnDplxGrpInfoImplConstants.DPLX_MIN_ID) ||
                (iGroupId > LnDplxGrpInfoImplConstants.DPLX_MAX_ID)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Creates a LocoNet packet which queries UR92(s) for Duplex group
     * identification information.  The invoking method is responsible for
     * sending the message to LocoNet.
     * @return LocoNetMessage containing IPL query of UR92s
     */
    public static final LocoNetMessage createUr92GroupIdentityQueryPacket() {
       // format packet
        LocoNetMessage m = new LocoNetMessage(LnConstants.RE_DPLX_OP_LEN);
        Integer i;
        i = 0;
        m.setElement(i++, LnConstants.OPC_PEER_XFER);
        m.setElement(i++, LnConstants.RE_DPLX_OP_LEN);   // 20-byte message
        m.setElement(i++, LnConstants.RE_DPLX_GP_NAME_TYPE);   // Group Name Operation
        m.setElement(i++, LnConstants.RE_DPLX_OP_TYPE_QUERY);   // Query Operation
        for (;i < (LnConstants.RE_DPLX_OP_LEN - 1); i++) {
            m.setElement(i, 0);   // always 0 for duplex group name write
        }
        return m;
    }


    /**
     * Create a LocoNet packet to set the Duplex group name.
     * <p>
     * Throws an exception if s provides a 0-length group name string.
     * If s is too short, it is padded with spaces at the end of the string.
     * <p>
     * @param sGroupName is the desired group name value as a string
     * @return  The LocoNet packet which writes the Group Name to the UR92 device(s)
     * @throws jmri.jmrix.loconet.LocoNetException if sGroupName is not a valid Duplex Group Name
     */
    public static final LocoNetMessage createSetUr92GroupNamePacket(String sGroupName) throws jmri.jmrix.loconet.LocoNetException {
       int gr_msb1 = 0;
       int gr_msb2 = 0;
       int i;

       if (validateGroupName(sGroupName) == false) {
            throw new jmri.jmrix.loconet.LocoNetException("Invalid Duplex Group Name - must be exactly 8 characters");
       }

       // format packet
        LocoNetMessage m = new LocoNetMessage(LnConstants.RE_DPLX_OP_LEN);

        // update extended data storage for most-significant bits of each character
        gr_msb1 += (Character.valueOf(sGroupName.charAt(0)) > LnConstants.RE_DPLX_7BITS_MAX) ? LnConstants.RE_DPLX_MSB1_BIT : 0;
        gr_msb1 += (Character.valueOf(sGroupName.charAt(1)) > LnConstants.RE_DPLX_7BITS_MAX) ? LnConstants.RE_DPLX_MSB2_BIT : 0;
        gr_msb1 += (Character.valueOf(sGroupName.charAt(2)) > LnConstants.RE_DPLX_7BITS_MAX) ? LnConstants.RE_DPLX_MSB3_BIT : 0;
        gr_msb1 += (Character.valueOf(sGroupName.charAt(3)) > LnConstants.RE_DPLX_7BITS_MAX) ? LnConstants.RE_DPLX_MSB4_BIT : 0;
        gr_msb2 += (Character.valueOf(sGroupName.charAt(4)) > LnConstants.RE_DPLX_7BITS_MAX) ? LnConstants.RE_DPLX_MSB1_BIT : 0;
        gr_msb2 += (Character.valueOf(sGroupName.charAt(5)) > LnConstants.RE_DPLX_7BITS_MAX) ? LnConstants.RE_DPLX_MSB2_BIT : 0;
        gr_msb2 += (Character.valueOf(sGroupName.charAt(6)) > LnConstants.RE_DPLX_7BITS_MAX) ? LnConstants.RE_DPLX_MSB3_BIT : 0;
        gr_msb2 += (Character.valueOf(sGroupName.charAt(7)) > LnConstants.RE_DPLX_7BITS_MAX) ? LnConstants.RE_DPLX_MSB4_BIT : 0;

        i = 0;
        m.setElement(i++, LnConstants.OPC_PEER_XFER);
        m.setElement(i++, LnConstants.RE_DPLX_OP_LEN);   // 20-byte message
        m.setElement(i++, LnConstants.RE_DPLX_GP_NAME_TYPE);   // Group Name Operation
        m.setElement(i++, LnConstants.RE_DPLX_OP_TYPE_WRITE);   // Write Operation
        m.setElement(i++, gr_msb1);   // MSB1
        m.setElement(i++, Character.valueOf(sGroupName.charAt(0)) &
                LnConstants.RE_DPLX_MAX_NOT_OPC);   // 7 LSBs of leftmost character
        m.setElement(i++, Character.valueOf(sGroupName.charAt(1)) &
                LnConstants.RE_DPLX_MAX_NOT_OPC);   // 7 LSBs of next character
        m.setElement(i++, Character.valueOf(sGroupName.charAt(2)) &
                LnConstants.RE_DPLX_MAX_NOT_OPC);   // 7 LSBs of next character
        m.setElement(i++, Character.valueOf(sGroupName.charAt(3)) &
                LnConstants.RE_DPLX_MAX_NOT_OPC);   //  7 LSBs of next character
        m.setElement(i++, gr_msb2);   // MSB2
        m.setElement(i++, Character.valueOf(sGroupName.charAt(4)) &
                LnConstants.RE_DPLX_MAX_NOT_OPC);   //  7 LSBs of next character
        m.setElement(i++, Character.valueOf(sGroupName.charAt(5)) &
                LnConstants.RE_DPLX_MAX_NOT_OPC);   //  7 LSBs of next character
        m.setElement(i++, Character.valueOf(sGroupName.charAt(6)) &
                LnConstants.RE_DPLX_MAX_NOT_OPC);   //  7 LSBs of next character
        m.setElement(i++, Character.valueOf(sGroupName.charAt(7)) &
                LnConstants.RE_DPLX_MAX_NOT_OPC);   // 7 LSBs of rightmost character
        for (;i < (LnConstants.RE_DPLX_OP_LEN - 1); i++) {
            m.setElement(i, 0);   // always 0 for duplex group name write
        }
        // Note: LocoNet send process will compute and add checksum byte in correct location

        return m;
    }

    /**
     * Create a LocoNet packet to set the Duplex group channel number.
     * <p>
     * If s provides a 0-length group name, a bogus LocoNet message is
     * returned.  If s does not define an integer is too short, it is padded with spaces at the
     * end of the string.
     * <p>
     * @param iChannelNumber The desired group channel number value as an integer
     * @return  The packet which writes the Group Channel Number to the UR92 device(s)
     * @throws jmri.jmrix.loconet.LocoNetException if sGroupName is not a valid Duplex Group Name
     */
    public static final LocoNetMessage createSetUr92GroupChannelPacket(Integer iChannelNumber) throws jmri.jmrix.loconet.LocoNetException {
        int i;

       if (validateGroupChannel(iChannelNumber) == false) {
            throw new jmri.jmrix.loconet.LocoNetException("Invalid Duplex Group Channel - must be between 11 and 26, inclusive");
       }

        // format packet
        LocoNetMessage m = new LocoNetMessage(LnConstants.RE_DPLX_OP_LEN);

        i = 0;
        m.setElement(i++, LnConstants.OPC_PEER_XFER);
        m.setElement(i++, LnConstants.RE_DPLX_OP_LEN);   // 20-byte message
        m.setElement(i++, LnConstants.RE_DPLX_GP_CHAN_TYPE);   // Group Channel Operation
        m.setElement(i++, LnConstants.RE_DPLX_OP_TYPE_WRITE);   // Write Operation
        m.setElement(i++, 0);   // always 0 for duplex group channel write
        m.setElement(i++, iChannelNumber);  // Group Channel Number
        for (;i < (LnConstants.RE_DPLX_OP_LEN - 1); i++) {
            m.setElement(i, 0);   // always 0 for duplex group channel write
        }
        // Note: LocoNet send process will compute and add checksum byte in correct location

        return m;
    }

    /**
     * Create a LocoNet packet to set the Duplex group password.
     * <p>
     * If s provides anything other than a 4 character length group password
     * which uses only valid group ID characters (0-9, A-C, a-c), a bogus
     * LocoNet message is returned.
     * <p>
     * @param sGroupPassword The desired group password as a string
     * @return  The packet which writes the Group Password to the UR92 device(s)
     */
    public static final LocoNetMessage createSetUr92GroupPasswordPacket(String sGroupPassword) throws jmri.jmrix.loconet.LocoNetException {

        int gr_p1 = sGroupPassword.charAt(0);
        int gr_p2 = sGroupPassword.charAt(1);
        int gr_p3 = sGroupPassword.charAt(2);
        int gr_p4 = sGroupPassword.charAt(3);
        int i;

       if (validateGroupPassword(sGroupPassword) == false) {
           if (getLimitPasswordToNumericOnly() == true) {
                throw new jmri.jmrix.loconet.LocoNetException("Invalid Duplex Group Password - must be a 4 digit number between 0000 and 9999, inclusive");
           } else {
                throw new jmri.jmrix.loconet.LocoNetException("Invalid Duplex Group Password - must be a 4 character value using only digits, 'A', 'B', and/or 'C'");
           }
       }

        // re-code individual characters when an alphabetic character is used
        gr_p1 -= (gr_p1 > '9') ? ('A'-'9'-1) : 0;
        gr_p2 -= (gr_p2 > '9') ? ('A'-'9'-1) : 0;
        gr_p3 -= (gr_p3 > '9') ? ('A'-'9'-1) : 0;
        gr_p4 -= (gr_p4 > '9') ? ('A'-'9'-1) : 0;

        // format packet
        LocoNetMessage m = new LocoNetMessage(LnConstants.RE_DPLX_OP_LEN);

        i = 0;
        m.setElement(i++, LnConstants.OPC_PEER_XFER);
        m.setElement(i++, LnConstants.RE_DPLX_OP_LEN);   // 20-byte message
        m.setElement(i++, LnConstants.RE_DPLX_GP_PW_TYPE);   // Group password Operation
        m.setElement(i++, LnConstants.RE_DPLX_OP_TYPE_WRITE);   // Write Operation
        m.setElement(i++, 0);   // always 0 for duplex group password write
        m.setElement(i++, gr_p1);   // Group password leftmost value + 0x30
        m.setElement(i++, gr_p2);   // Group password next value + 0x30
        m.setElement(i++, gr_p3);   // Group password next value + 0x30
        m.setElement(i++, gr_p4);   // Group password rightmost value + 0x30
        for (;i < (LnConstants.RE_DPLX_OP_LEN - 1); i++) {
            m.setElement(i, 0);   // always 0 for duplex group password write
        }
        // Note: LocoNet send process will compute and add checksum byte in correct location

        return m;
    }

    /**
     * Create a LocoNet packet to set the Duplex group ID number.
     * <p>
     * If s provides anything other than a numeric value between 0 and 127, a
     * bogus LocoNet message is returned.
     * <p>
     * @param s The desired group ID number as a string
     * @return  The packet which writes the Group ID Number to the UR92 device(s)
     */
    public static final LocoNetMessage createSetUr92GroupIDPacket(String s) throws jmri.jmrix.loconet.LocoNetException {
        int gr_id = Integer.parseInt(s,10);

        if ((gr_id >= LnDplxGrpInfoImplConstants.DPLX_MIN_ID) && (gr_id <= LnDplxGrpInfoImplConstants.DPLX_MAX_ID)) {
            // format packet
            int i = 0;
            LocoNetMessage m = new LocoNetMessage(LnConstants.RE_DPLX_OP_LEN);

            m.setElement(i++, LnConstants.OPC_PEER_XFER);
            m.setElement(i++, LnConstants.RE_DPLX_OP_LEN);   // 20-byte message
            m.setElement(i++, LnConstants.RE_DPLX_GP_ID_TYPE);   // Group ID Operation
            m.setElement(i++, LnConstants.RE_DPLX_OP_TYPE_WRITE);   // Write Operation
            m.setElement(i++, 0);   // always 0 for duplex group ID write
            m.setElement(i++, gr_id);  // Group ID Number
            for (;i < (LnConstants.RE_DPLX_OP_LEN - 1); i++) {
                m.setElement(i, 0);   // always 0 for duplex group ID write
            }
            // Note: LocoNet send process will compute and add checksum byte in correct location
            return m;
        }
        else {
           /* in case param s encodes something other than a valid Duplex
            * ID number, throw an exception.
            */
            throw new jmri.jmrix.loconet.LocoNetException("Illegal Duplex Group ID number");
        }
    }

    /**
     * Checks message m to determine if it contains a Duplex Group Identity
     * message, including queries, reports, and writes, for Name, Channel,
     * Password, and ID.
     * @param m
     * @return true if message is query, report, or write of Duplex Group Name,
     * Channel, Password or ID
     */
    public static final boolean isDuplexGroupMessage(LocoNetMessage m) {
        if ((m.getElement(0) == LnConstants.OPC_PEER_XFER) &&
                (m.getElement(1) == LnConstants.RE_DPLX_OP_LEN)) {
            // Message is a peer-to-peer message of appropriate length for
            // Duplex Group operations.  Check the individual message type
            Integer byte2 = m.getElement(2);
            if ((byte2 == LnConstants.RE_DPLX_GP_CHAN_TYPE) ||
                    (byte2 == LnConstants.RE_DPLX_GP_NAME_TYPE) ||
                    (byte2 == LnConstants.RE_DPLX_GP_ID_TYPE) ||
                    (byte2 == LnConstants.RE_DPLX_GP_PW_TYPE)) {
                // To be sure the message is a duplex operation, check the
                // operation type.
                Integer byte3 = m.getElement(3);
                if ((byte3 == LnConstants.RE_DPLX_OP_TYPE_QUERY) ||
                        (byte3 == LnConstants.RE_DPLX_OP_TYPE_REPORT) ||
                        (byte3 == LnConstants.RE_DPLX_OP_TYPE_WRITE)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Classifies a LocoNet Message to see if it is a Duplex Group Identity message
     *
     * @param m - a LocoNetMessage
     * @return DuplexGroupMessageType, encoded as one of the following
     *      NOT_A_DUPLEX_GROUP_MESSAGE
     *      DUPLEX_GROUP_CHANNEL_QUERY_MESSAGE
     *      DUPLEX_GROUP_CHANNEL_REPORT_MESSAGE
     *      DUPLEX_GROUP_CHANNEL_WRITE_MESSAGE
     *      DUPLEX_GROUP_NAME_QUERY_MESSAGE
     *      DUPLEX_GROUP_NAME_ETC_REPORT_MESSAGE
     *      DUPLEX_GROUP_NAME_WRITE_MESSAGE
     *      DUPLEX_GROUP_PASSWORD_QUERY_MESSAGE
     *      DUPLEX_GROUP_PASSWORD_REPORT_MESSAGE
     *      DUPLEX_GROUP_PASSWORD_WRITE_MESSAGE
     *      DUPLEX_GROUP_ID_QUERY_MESSAGE
     *      DUPLEX_GROUP_ID_REPORT_MESSAGE
     *      DUPLEX_GROUP_ID_WRITE_MESSAGE
     */
    public static final DuplexGroupMessageType getDuplexGroupIdentityMessageType(LocoNetMessage m) {
        if ((m.getElement(0) == LnConstants.OPC_PEER_XFER) &&
                (m.getElement(1) == LnConstants.RE_DPLX_OP_LEN)) {
            // Message is a peer-to-peer message of appropriate length for
            // Duplex Group operations.  Check the individual message type
            Integer byte3 = m.getElement(3);
            switch (m.getElement(2)) {
                case LnConstants.RE_DPLX_GP_CHAN_TYPE:
                    return (byte3 == LnConstants.RE_DPLX_OP_TYPE_QUERY)?DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_QUERY_MESSAGE:
                        (byte3 == LnConstants.RE_DPLX_OP_TYPE_REPORT)?DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_REPORT_MESSAGE:
                            (byte3 == LnConstants.RE_DPLX_OP_TYPE_WRITE)?DuplexGroupMessageType.DUPLEX_GROUP_CHANNEL_WRITE_MESSAGE:
                                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE;
                case LnConstants.RE_DPLX_GP_NAME_TYPE:
                    return (byte3 == LnConstants.RE_DPLX_OP_TYPE_QUERY)?DuplexGroupMessageType.DUPLEX_GROUP_NAME_QUERY_MESSAGE:
                        (byte3 == LnConstants.RE_DPLX_OP_TYPE_REPORT)?DuplexGroupMessageType.DUPLEX_GROUP_NAME_ETC_REPORT_MESSAGE:
                            (byte3 == LnConstants.RE_DPLX_OP_TYPE_WRITE)?DuplexGroupMessageType.DUPLEX_GROUP_NAME_WRITE_MESSAGE:
                                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE;
                case LnConstants.RE_DPLX_GP_PW_TYPE:
                    return (byte3 == LnConstants.RE_DPLX_OP_TYPE_QUERY)?DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_QUERY_MESSAGE:
                        (byte3 == LnConstants.RE_DPLX_OP_TYPE_REPORT)?DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_REPORT_MESSAGE:
                            (byte3 == LnConstants.RE_DPLX_OP_TYPE_WRITE)?DuplexGroupMessageType.DUPLEX_GROUP_PASSWORD_WRITE_MESSAGE:
                                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE;
                case LnConstants.RE_DPLX_GP_ID_TYPE:
                    return (byte3 == LnConstants.RE_DPLX_OP_TYPE_QUERY)?DuplexGroupMessageType.DUPLEX_GROUP_ID_QUERY_MESSAGE:
                        (byte3 == LnConstants.RE_DPLX_OP_TYPE_REPORT)?DuplexGroupMessageType.DUPLEX_GROUP_ID_REPORT_MESSAGE:
                            (byte3 == LnConstants.RE_DPLX_OP_TYPE_WRITE)?DuplexGroupMessageType.DUPLEX_GROUP_ID_WRITE_MESSAGE:
                                DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE;
                default:
                    return DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE;
            }
        }
        return DuplexGroupMessageType.NOT_A_DUPLEX_GROUP_MESSAGE;
    }

    /**
     * Checks that m is a message with a Duplex Group Name encoded inside, then
     * extracts the Duplex Group Name.  Note that the returned string is always
     * 8 characters long.
     *
     * If m does not contain a Duplex Group Name, returns null.
     *
     * @param m
     * @return String containing Duplex Group Name as extracted from m
     */
    public static String extractDuplexGroupName (LocoNetMessage m) {
        switch (getDuplexGroupIdentityMessageType(m)) {
            case DUPLEX_GROUP_NAME_ETC_REPORT_MESSAGE:
            case DUPLEX_GROUP_NAME_WRITE_MESSAGE:
                return extractGroupName(m);
            default:
                return null;
        }
    }

    /**
     * Assumes that m is a message with a Duplex Group Name encoded inside.
     * Extracts the Duplex Group Name and returns it as an 8 character String.
     *
     * @param m
     * @return String containing Duplex Group Name as extracted from m
     */
    private static String extractGroupName(LocoNetMessage m) {
        StringBuilder gr_name = new StringBuilder();
        int gr_msb1;
        int gr_msb2;

        gr_msb1 = m.getElement(4) & LnConstants.RE_DPLX_MAX_NOT_OPC;
        gr_msb2 = m.getElement(9) & LnConstants.RE_DPLX_MAX_NOT_OPC;
        gr_name.append( Character.toString((char) ((m.getElement(5) & LnConstants.RE_DPLX_MAX_NOT_OPC) +
                ((gr_msb1 & LnConstants.RE_DPLX_MSB1_BIT) << LnConstants.RE_DPLX_BUMP_MSB1_BIT))) );
        gr_name.append( Character.toString((char) ((m.getElement(6) & LnConstants.RE_DPLX_MAX_NOT_OPC) +
                ((gr_msb1 & LnConstants.RE_DPLX_MSB2_BIT) << LnConstants.RE_DPLX_BUMP_MSB2_BIT))) );
        gr_name.append( Character.toString((char) ((m.getElement(7) & LnConstants.RE_DPLX_MAX_NOT_OPC) +
                ((gr_msb1 & LnConstants.RE_DPLX_MSB3_BIT) << LnConstants.RE_DPLX_BUMP_MSB3_BIT))) );
        gr_name.append( Character.toString((char) ((m.getElement(8) & LnConstants.RE_DPLX_MAX_NOT_OPC) +
                ((gr_msb1 & LnConstants.RE_DPLX_MSB4_BIT) << LnConstants.RE_DPLX_BUMP_MSB4_BIT))) );
        gr_name.append( Character.toString((char) ((m.getElement(10) & LnConstants.RE_DPLX_MAX_NOT_OPC) +
                ((gr_msb2 & LnConstants.RE_DPLX_MSB1_BIT) << LnConstants.RE_DPLX_BUMP_MSB1_BIT))) );
        gr_name.append( Character.toString((char) ((m.getElement(11) & LnConstants.RE_DPLX_MAX_NOT_OPC) +
                ((gr_msb2 & LnConstants.RE_DPLX_MSB2_BIT) << LnConstants.RE_DPLX_BUMP_MSB2_BIT))) );
        gr_name.append( Character.toString((char) ((m.getElement(12) & LnConstants.RE_DPLX_MAX_NOT_OPC) +
                ((gr_msb2 & LnConstants.RE_DPLX_MSB3_BIT) << LnConstants.RE_DPLX_BUMP_MSB3_BIT))) );
        gr_name.append( Character.toString((char) ((m.getElement(13) & LnConstants.RE_DPLX_MAX_NOT_OPC) +
                ((gr_msb2 & LnConstants.RE_DPLX_MSB4_BIT) << LnConstants.RE_DPLX_BUMP_MSB4_BIT))) );
    return gr_name.toString();
    }

    /**
     * Checks that m is a message with a Duplex Group Channel encoded inside, then
     * extracts and returns the Duplex Group Channel.
     *
     * Returns -1 if the m does not contain a Duplex Group Channel.
     *
     * @param m
     * @return Integer containing Duplex Group Name as extracted from m
     */
    public static Integer extractDuplexGroupChannel (LocoNetMessage m) {
        switch (getDuplexGroupIdentityMessageType(m)) {
            case DUPLEX_GROUP_NAME_ETC_REPORT_MESSAGE:
               return m.getElement(17) +
                       (((m.getElement(14) & 0x4)== 0x4)? 128 : 0);
           case DUPLEX_GROUP_CHANNEL_REPORT_MESSAGE:
           case DUPLEX_GROUP_CHANNEL_WRITE_MESSAGE:
                return m.getElement(5)+
                        (((m.getElement(4) & 0x1)== 0x1)? 128 : 0);
            default:
                return -1;
        }
    }
    /**
     * Checks that m is a message with a Duplex Group ID encoded inside, then
     * extracts and returns the Duplex Group ID.
     *
     * Returns -1 if the m does not contain a Duplex Group ID.
     *
     * @param m
     * @return Integer containing Duplex Group Name as extracted from m
     */
    public static Integer extractDuplexGroupID (LocoNetMessage m) {
        switch (getDuplexGroupIdentityMessageType(m)) {
            case DUPLEX_GROUP_NAME_ETC_REPORT_MESSAGE:
               return m.getElement(18) +
                       (((m.getElement(14) & 0x8)== 0x8)? 128 : 0);
           case DUPLEX_GROUP_ID_REPORT_MESSAGE:
           case DUPLEX_GROUP_ID_WRITE_MESSAGE:
                return m.getElement(5)+
                        (((m.getElement(4) & 0x1)== 0x1)? 128 : 0);
            default:
                return -1;
        }
    }

    /**
     * Checks that m is a message with a Duplex Group Password encoded inside, then
     * extracts and returns the Duplex Group Password.
     *
     * Returns null if the m does not contain a Duplex Group Password.
     *
     * @param m
     * @return String containing the Duplex Group Password as extracted from m
     */
    public static String extractDuplexGroupPassword (LocoNetMessage m) {
        switch (getDuplexGroupIdentityMessageType(m)) {
            case DUPLEX_GROUP_NAME_ETC_REPORT_MESSAGE:
               return extractDuplexGroupPasswordSimplified(
                       (m.getElement(15) & 0x70) >>4,
                       (m.getElement(15) & 0x0f),
                       (m.getElement(16) & 0x70) >> 4,
                       (m.getElement(16) & 0x0f),
                       (((m.getElement(14) & 0x1) == 0x1)?true:false),
                       false,
                       (((m.getElement(14) & 0x2) == 0x2)?true:false),
                       false);
           case DUPLEX_GROUP_PASSWORD_REPORT_MESSAGE:
           case DUPLEX_GROUP_PASSWORD_WRITE_MESSAGE:
               return extractDuplexGroupPasswordSimplified(
                       (m.getElement(5) & 0x0f),
                       (m.getElement(6) & 0x0f),
                       (m.getElement(7) & 0x0f),
                       (m.getElement(8) & 0x0f),
                       false,
                       false,
                       false,
                       false
                       );
            default:
                return null;
        }
    }

    private static String extractDuplexGroupPasswordSimplified(Integer byte1, Integer byte2, Integer byte3, Integer byte4,
            boolean x1, boolean x2, boolean x3, boolean x4) {
        Integer b1;
        Integer b2;
        Integer b3;
        Integer b4;
        char gr_p1;
        char gr_p2;
        char gr_p3;
        char gr_p4;

        b1 = byte1 + (x1?8:0);
        b2 = byte2 + (x2?8:0);
        b3 = byte3 + (x3?8:0);
        b4 = byte4 + (x4?8:0);

        // extract reported password characters and convert to displayable character
        gr_p1 = (char) ('0' + b1);
        gr_p1 += (gr_p1 > '9') ? ('A'-'9'-1) : 0;

        gr_p2 = (char) ('0' + b2);
        gr_p2 += (gr_p2 > '9') ? ('A'-'9'-1) : 0;

        gr_p3 = (char) ('0' + b3);
        gr_p3 += (gr_p3 > '9') ? ('A'-'9'-1) : 0;

        gr_p4 = (char) ('0' + b4);
        gr_p4 += (gr_p4 > '9') ? ('A'-'9'-1) : 0;

        return ""+gr_p1 + gr_p2 + gr_p3 + gr_p4;

    }

    /**
     * Process all incoming LocoNet messages to look for Duplex Group
     * information operations.  Only pays attention to LocoNet report
     * of Duplex Group Name/password/channel/groupID, and ignores all other
     * LocoNet messages.
     * <p>
     * If tool has sent a query for Duplex group information and has not
     * yet received a Duplex group report, the method updates the GUI with
     * the received information.
     * <p>
     * If the tool is not currently waiting for a
     * response to a query, then the method compares the received information
     * against the information currently displayed in the GUI.  If the received
     * information does not match, a message is displayed on the status line
     * in the GUI, else nothing is displayed in the GUI status line.
     * <p>
     * @param m
     */
    public void message(LocoNetMessage m) {

        if (handleMessageIplResult(m)) {
            return;
        }

        if (handleMessageDuplexInfoQuery(m)) {
            gotQueryReply = true;
            thisone.firePropertyChange(DPLX_PC_RCD_DPLX_IDENTITY_QUERY, false, true);
            return;
        }

        if (handleMessageDuplexInfoReport(m)) {
            gotQueryReply = true;
            thisone.firePropertyChange(DPLX_PC_RCD_DPLX_IDENTITY_REPORT, false, true);
            return;
        }

        return;
    }

    private boolean awaitingGroupReadReport;
    private String  acceptedGroupName;
    private String  acceptedGroupChannel;
    private String  acceptedGroupPassword;
    private String  acceptedGroupId;

    /**
     *
     * @return String containing reported Duplex Group Name
     */
    public String getFetchedDuplexGroupName() {
        return acceptedGroupName;
    }

    /**
     *
     * @return String containing reported Duplex Group Name
     */
    public String getFetchedDuplexGroupChannel() {
        return acceptedGroupChannel;
    }

    /**
     *
     * @return String containing reported Duplex Group Name
     */
    public String getFetchedDuplexGroupPassword() {
        return acceptedGroupPassword;
    }

    /**
     *
     * @return String containing reported Duplex Group Name
     */
    public String getFetchedDuplexGroupId() {
        return acceptedGroupId;
    }

    /**
     * Interprets a received LocoNet message.  If message is an IPL report of
     * attached IPL-capable equipment, check to see if it reports a UR92
     * device as attached.  If so, increment count of UR92 devices.  Else
     * ignore.
     * @param m
     * @return true if message is an IPL device report indicating a UR92 present,
     * else return false.
     */
    private boolean handleMessageIplResult(LocoNetMessage m) {
        if (LnIPLImplementation.isIplUr92IdentityReportMessage(m)) {
            numUr92++;
            thisone.firePropertyChange(DPLX_PC_STAT_LN_UPDATE, "", " ");
            waitingForIplReply = false;

            return true;
        } else {
            return false;
        }
    }

    private boolean handleMessageDuplexInfoQuery(LocoNetMessage m) {
        if (getDuplexGroupIdentityMessageType(m) == DuplexGroupMessageType.DUPLEX_GROUP_NAME_QUERY_MESSAGE)
            return true;
        else
            return false;
    }

    /**
     * Interprets a received LocoNet message.  If message is a report of Duplex
     * Group Identity information, extract it to local variables.  Else ignore.
     * @param m
     * @return true if message is a Duplex Group Identity Report, else return false.
     */
    private boolean handleMessageDuplexInfoReport(LocoNetMessage m) {

        String gr_name = "";
        String gr_password = "";
        int gr_ch;
        int gr_id;
        int i;
        if (getDuplexGroupIdentityMessageType(m) == DuplexGroupMessageType.DUPLEX_GROUP_NAME_ETC_REPORT_MESSAGE) {
            gr_name = extractDuplexGroupName(m);
            // remove trailing spaces from name
            i = (gr_name.length()-1);
            while ((gr_name.charAt(i) == ' ') && (i > 0)) {
                if (i > 0) {
                    gr_name = gr_name.substring(0,i);
                }
                i--;
            }
            gr_password = extractDuplexGroupPassword(m);
            gr_ch = extractDuplexGroupChannel(m);
            gr_id = extractDuplexGroupID(m);

            if (awaitingGroupReadReport == true) {
                awaitingGroupReadReport = false;
                acceptedGroupName = gr_name;
                acceptedGroupChannel = Integer.toString(gr_ch, LnDplxGrpInfoImplConstants.GENERAL_DECIMAL_RADIX);
                acceptedGroupPassword = gr_password;
                acceptedGroupId = Integer.toString(gr_id, LnDplxGrpInfoImplConstants.GENERAL_DECIMAL_RADIX);

                thisone.firePropertyChange(DPLX_PC_NAME_UPDATE, false, true);
                thisone.firePropertyChange(DPLX_PC_CHANNEL_UPDATE, false, true);
                thisone.firePropertyChange(DPLX_PC_PASSWORD_UPDATE, false, true);
                thisone.firePropertyChange(DPLX_PC_ID_UPDATE, false, true);

                thisone.firePropertyChange(DPLX_PC_NAME_VALIDITY, false, true);
                thisone.firePropertyChange(DPLX_PC_CHANNEL_VALIDITY, false, true);
                thisone.firePropertyChange(DPLX_PC_PASSWORD_VALIDITY, false, true);
                thisone.firePropertyChange(DPLX_PC_ID_VALIDITY, false, true);

                thisone.firePropertyChange(DPLX_PC_STAT_LN_UPDATE, "", " ");
            }
            else {
                // if not expecting a group read, compare new read data
                // versus first returned data
                String c = ""+Integer.toString(gr_ch, LnDplxGrpInfoImplConstants.GENERAL_DECIMAL_RADIX);
                String p = ""+gr_password;
                String d = ""+Integer.toString(gr_id, LnDplxGrpInfoImplConstants.GENERAL_DECIMAL_RADIX);

                if (( ! acceptedGroupName.equals(gr_name)) ||
                        ( ! acceptedGroupChannel.equals(c)) ||
                        ( ! acceptedGroupPassword.equals(p)) ||
                        ( ! acceptedGroupId.equals(d))) {
                    // show that a Duplex Group Identification information hapened.
                    // Show message as as red text on status line
                    thisone.firePropertyChange(DPLX_PC_STAT_LN_UPDATE, " ", "ErrorGroupMismatch");
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Sends a LocoNet Message to query the Duplex Group Identity.  Starts
     * a timer to monitor completion.
     *
     */
    public void queryDuplexGroupIdentity () {

        awaitingGroupReadReport = true;
        gotQueryReply = false;

        memo.getLnTrafficController().sendLocoNetMessage(createUr92GroupIdentityQueryPacket());
        invalidateDuplexGroupIdentityInfo();

        if (swingTmrDuplexInfoQuery != null) {
            if (swingTmrDuplexInfoQuery.isRunning()) {
                swingTmrDuplexInfoQuery.restart();
            }
            else
                swingTmrDuplexInfoQuery.start();
        }
    }

    /**
     * Creates and sends a LocoNet message which sets the Duplex Group Name.
     * @param dgn - String containing the new Duplex Group Name
     * @throws jmri.jmrix.loconet.LocoNetException if dgn is not a valid Duplex
     * Group Name.
     */
    public void setDuplexGroupName(String dgn) throws jmri.jmrix.loconet.LocoNetException {
        memo.getLnTrafficController().sendLocoNetMessage(
                createSetUr92GroupNamePacket(
                dgn));
    }

    /**
     * Creates and sends a LocoNet message which sets the Duplex Group Channel.
     * @param dgc - Integer containing the new Duplex Group Channel
     * @throws jmri.jmrix.loconet.LocoNetException if dgc is not a valid Duplex
     * Group Channel number.
     */
    public void setDuplexGroupChannel(Integer dgc) throws jmri.jmrix.loconet.LocoNetException {
        memo.getLnTrafficController().sendLocoNetMessage(createSetUr92GroupChannelPacket(
                dgc));
    }

    /**
     * Creates and sends a LocoNet message which sets the Duplex Group Password.
     * @param dgp - String containing the new Duplex Group Password
     * @throws jmri.jmrix.loconet.LocoNetException if dgp is not a valid Duplex
     * Group Password.
     */
    public void setDuplexGroupPassword(String dgp) throws jmri.jmrix.loconet.LocoNetException {
        memo.getLnTrafficController().sendLocoNetMessage(createSetUr92GroupPasswordPacket(
                dgp));
    }

    /**
     * Creates and sends a LocoNet message which sets the Duplex Group Id.
     * @param dgi - String containing the new Duplex Group Id
     * @throws jmri.jmrix.loconet.LocoNetException if dgi is not a valid Duplex
     * Group Id.
     */
    public void setDuplexGroupId(String dgi) throws jmri.jmrix.loconet.LocoNetException {
        memo.getLnTrafficController().sendLocoNetMessage(createSetUr92GroupIDPacket(
                dgi));
    }

    private void InvalidateDataAndQueryDuplexInfo() {
        if (numUr92 > 0) {
            thisone.firePropertyChange(DPLX_PC_STAT_LN_UPDATE, " ", "ProcessingReadingInfo");
            queryDuplexGroupIdentity();
        }
    }

    private void sendUr92IplQuery() {
        waitingForIplReply = true;
        memo.getLnTrafficController().sendLocoNetMessage(
                LnIPLImplementation.createIplSpecificHostQueryPacket(
                        LnConstants.RE_IPL_DIGITRAX_HOST_ALL,
                        LnConstants.RE_IPL_DIGITRAX_HOST_UR92));
        int oldvalue = 9999;
        int newvalue = 0;
        thisone.firePropertyChange("NumberOfUr92sUpdate", oldvalue, newvalue);
        invalidateDuplexGroupIdentityInfo();
        if (swingTmrIplQuery != null) {
            if (swingTmrIplQuery.isRunning()) {
                swingTmrIplQuery.restart();
            }
            else swingTmrIplQuery.start();
        }
    }

    private void invalidateDuplexGroupIdentityInfo() {
        acceptedGroupName = "";
        acceptedGroupChannel = "";
        acceptedGroupPassword = "";
        acceptedGroupId = "";
        thisone.firePropertyChange(DPLX_PC_NAME_UPDATE, true, false);
        thisone.firePropertyChange(DPLX_PC_CHANNEL_UPDATE, true, false);
        thisone.firePropertyChange(DPLX_PC_PASSWORD_UPDATE, true, false);
        thisone.firePropertyChange(DPLX_PC_ID_UPDATE, true, false);
        thisone.firePropertyChange(DPLX_PC_NAME_VALIDITY, true, false);
        thisone.firePropertyChange(DPLX_PC_CHANNEL_VALIDITY, true, false);
        thisone.firePropertyChange(DPLX_PC_PASSWORD_VALIDITY, true, false);
        thisone.firePropertyChange(DPLX_PC_ID_VALIDITY, true, false);
    }

    /**
     * Begins a sequence which includes counting available UR92s and, if at least
     * one UR92 is present, reads the Duplex Group Identity Info.
     */
    public void countUr92sAndQueryDuplexIdentityInfo() {
        if (thisone == null) {
            log.error("called countUR92sAndQueryDuplexInfo before thisone is initialized");
        }
        if ((waitingForIplReply == true) ||
                (swingTmrIplQuery == null) ||
                (swingTmrDuplexInfoQuery == null) ||
                (swingTmrIplQuery.isRunning()) ||
                (swingTmrDuplexInfoQuery.isRunning())) {
            thisone.firePropertyChange(DPLX_PC_STAT_LN_UPDATE, " ", "ErrorReadingTooSoon");
            return;
        }
        invalidateDuplexGroupIdentityInfo();
        numUr92 = 0; 

        // configure timer for delay between UR92 query request and begin of duplex info query

        sendUr92IplQuery();
        thisone.firePropertyChange(DPLX_PC_STAT_LN_UPDATE, " ", "ProcessingInitialStatusMessage");
        swingTmrIplQuery.stop();
        swingTmrIplQuery.setInitialDelay(LnDplxGrpInfoImplConstants.IPL_QUERY_DELAY);
        swingTmrIplQuery.setRepeats(false);
        swingTmrIplQuery.restart();
    }
    
    // the following code may be used to create a LocoNet message that follows the
    // form of the message sent by a UR92 in response to a Duplex Group Name query 
    // LocoNet message.
    public static final LocoNetMessage createUr92GroupNameReportPacket(
            String  dupName,
            String  dupPass,
            int     dupChan,
            int     dupId) {
       // format packet
        LocoNetMessage m = new LocoNetMessage(LnConstants.RE_DPLX_OP_LEN);
        int i=0;
        dupName +="        ";
        dupName = dupName.substring(0,8); // get first 8 chars of space-padded name
        m.setElement(i++, LnConstants.OPC_PEER_XFER);
        m.setElement(i++, LnConstants.RE_DPLX_OP_LEN);   // 20-byte message
        m.setElement(i++, LnConstants.RE_DPLX_GP_NAME_TYPE);   // Group Name Operation
        m.setElement(i++, LnConstants.RE_DPLX_OP_TYPE_REPORT);   // Report Operation
        
        m.setElement(i++, 
                (((dupName.charAt(0)&0x80) == 0x80) ? 1:0) + 
                (((dupName.charAt(1)&0x80) == 0x80) ? 2:0) + 
                (((dupName.charAt(2)&0x80) == 0x80) ? 4:0) + 
                (((dupName.charAt(3)&0x80) == 0x80) ? 8:0) );
        m.setElement(i++, dupName.charAt(0) & 0x7f);
        m.setElement(i++, dupName.charAt(1) & 0x7f);
        m.setElement(i++, dupName.charAt(2) & 0x7f);
        m.setElement(i++, dupName.charAt(3) & 0x7f);
        
        m.setElement(i++, 
                (((dupName.charAt(4)&0x80) == 0x80) ? 1:0) + 
                (((dupName.charAt(5)&0x80) == 0x80) ? 2:0) + 
                (((dupName.charAt(6)&0x80) == 0x80) ? 4:0) + 
                (((dupName.charAt(7)&0x80) == 0x80) ? 8:0) );
        m.setElement(i++, dupName.charAt(4) & 0x7f);
        m.setElement(i++, dupName.charAt(5) & 0x7f);
        m.setElement(i++, dupName.charAt(6) & 0x7f);
        m.setElement(i++, dupName.charAt(7) & 0x7f);
        dupPass += "0000";
        dupPass = dupPass.substring(0,4);
        int gr_p1 = dupPass.charAt(0);
        int gr_p2 = dupPass.charAt(1);
        int gr_p3 = dupPass.charAt(2);
        int gr_p4 = dupPass.charAt(3);

        // re-code individual characters when an alphabetic character is used
        gr_p1 -= (gr_p1 > '9') ? ('A'-'9'-1) : 0;
        gr_p2 -= (gr_p2 > '9') ? ('A'-'9'-1) : 0;
        gr_p3 -= (gr_p3 > '9') ? ('A'-'9'-1) : 0;
        gr_p4 -= (gr_p4 > '9') ? ('A'-'9'-1) : 0;
        int passLo = ((gr_p1 & 0x0f) << 4) + (gr_p2 & 0x0f);
        int passHi = ((gr_p3 & 0x0f) << 4) + (gr_p4 & 0x0f);
        m.setElement(i++, 
                (((passLo & 0x80) == 0x80) ? 1: 0) +
                (((passHi  & 0x80) == 0x80) ? 2: 0) +
                (((dupChan & 0x80) == 0x80) ? 4: 0) +
                (((dupId   & 0x80) == 0x80) ? 8: 0) );
        m.setElement(i++, passLo & 0x7f);
        m.setElement(i++, passHi & 0x7f);
        m.setElement(i++, dupChan & 0x7f);
        m.setElement(i++, dupId & 0x7f);

        return m;
    }

    // the following code may be used to create a LocoNet message that follows the
    // form of the message sent by a UR92 in response to a Duplex Group Channel query 
    // LocoNet message.
    public static final LocoNetMessage createUr92GroupChannelReportPacket(
            int     dupChan) {
       // format packet
        LocoNetMessage m = new LocoNetMessage(LnConstants.RE_DPLX_OP_LEN);
        int i=0;
        m.setElement(i++, LnConstants.OPC_PEER_XFER);
        m.setElement(i++, LnConstants.RE_DPLX_OP_LEN);   // 20-byte message
        m.setElement(i++, LnConstants.RE_DPLX_GP_CHAN_TYPE);   // Group Channel Operation
        m.setElement(i++, LnConstants.RE_DPLX_OP_TYPE_REPORT);   // Report Operation
        
        m.setElement(i++, (dupChan & 0x80) >> 7);
        m.setElement(i++, dupChan & 0x7f);
        
        for (;i < LnConstants.RE_DPLX_OP_LEN; i++) { m.setElement(i, 0); }

        return m;
    }

  // the following code may be used to create a LocoNet message that follows the
    // form of the message sent by a UR92 in response to a Duplex Group Password query 
    // LocoNet message.
    // No attempt is made to check the validity of the dupPass argument.
    public static final LocoNetMessage createUr92GroupPasswordReportPacket(
            String  dupPass) {
       // format packet
        LocoNetMessage m = new LocoNetMessage(LnConstants.RE_DPLX_OP_LEN);
        int i=0;
        m.setElement(i++, LnConstants.OPC_PEER_XFER);
        m.setElement(i++, LnConstants.RE_DPLX_OP_LEN);   // 20-byte message
        m.setElement(i++, LnConstants.RE_DPLX_GP_PW_TYPE);   // Group Password Operation
        m.setElement(i++, LnConstants.RE_DPLX_OP_TYPE_REPORT);   // Report Operation
        
        dupPass += "0000";
        
        m.setElement(i++, 
                ((dupPass.charAt(0) & 0x80) == 0x80 ? 8:0) +
                ((dupPass.charAt(1) & 0x80) == 0x80 ? 3:0) +
                ((dupPass.charAt(2) & 0x80) == 0x80 ? 2:0) +
                ((dupPass.charAt(3) & 0x80) == 0x80 ? 1:0) 
                );
        m.setElement(i++, dupPass.charAt(0) & 0x7f);
        m.setElement(i++, dupPass.charAt(1) & 0x7f);
        m.setElement(i++, dupPass.charAt(2) & 0x7f);
        m.setElement(i++, dupPass.charAt(3) & 0x7f);
        
        for (;i < LnConstants.RE_DPLX_OP_LEN; i++) { m.setElement(i, 0); }

        return m;
    }

    // the following code may be used to create a LocoNet message that follows the
    // form of the message sent by a UR92 in response to a Duplex Group Id query 
    // LocoNet message.
    public static final LocoNetMessage createUr92GroupIdReportPacket(
            int     dupId) {
       // format packet
        LocoNetMessage m = new LocoNetMessage(LnConstants.RE_DPLX_OP_LEN);
        int i=0;
        m.setElement(i++, LnConstants.OPC_PEER_XFER);
        m.setElement(i++, LnConstants.RE_DPLX_OP_LEN);   // 20-byte message
        m.setElement(i++, LnConstants.RE_DPLX_GP_ID_TYPE);   // Group Id Operation
        m.setElement(i++, LnConstants.RE_DPLX_OP_TYPE_REPORT);   // Report Operation
        
        m.setElement(i++, (dupId & 0x80) >> 7);
        m.setElement(i++, dupId & 0x7f);
        
        for (;i < LnConstants.RE_DPLX_OP_LEN; i++) { m.setElement(i, 0); }

        return m;
    }

    // Property Change keys relating to GUI status line
    public final static String DPLX_PC_STAT_LN_UPDATE = "DPLXPCK_STAT_LN_UPDATE";
    public final static String DPLX_PC_STAT_LN_UPDATE_IF_NOT_CURRENTLY_ERROR = "DPLXPCK_STAT_LN_ON_OVER_UPDATE";

    // Property Change keys relating to validity of identity info
    public final static String DPLX_PC_NAME_VALIDITY = "DPLXPCK_NAME_VALID";
    public final static String DPLX_PC_CHANNEL_VALIDITY = "DPLXPCK_CH_VALID";
    public final static String DPLX_PC_PASSWORD_VALIDITY = "DPLXPCK_PW_VALID";
    public final static String DPLX_PC_ID_VALIDITY = "DPLXPCK_ID_VALID";

    // Property Change keys relating to identity info value changes
    public final static String DPLX_PC_NAME_UPDATE = "DPLXPCK_NAME_UPDATE";
    public final static String DPLX_PC_CHANNEL_UPDATE = "DPLXPCK_CH_UPDATE";
    public final static String DPLX_PC_PASSWORD_UPDATE = "DPLXPCK_PW_UPDATE";
    public final static String DPLX_PC_ID_UPDATE = "DPLXPCK_ID_UPDATE";

    // Property Change keys relating to Duplex Group Identity LocoNet messages
    public final static String DPLX_PC_RCD_DPLX_IDENTITY_QUERY = "DPLXPCK_IDENTITY_QUERY";
    public final static String DPLX_PC_RCD_DPLX_IDENTITY_REPORT = "DPLXPCK_IDENTITY_REPORT";

    /**
     * Connect this instance's LocoNetListener to the LocoNet Traffic Controller
     */
    public void connect(jmri.jmrix.loconet.LnTrafficController t) {
        if (t != null) {
            // connect to the LnTrafficController
            t.addLocoNetListener(~0, this);
        }
    }

    /**
     * Break connection with the LnTrafficController and stop timers
     */
    public void dispose() {
        if (swingTmrIplQuery != null) swingTmrIplQuery.stop();
        if (swingTmrDuplexInfoQuery != null) swingTmrDuplexInfoQuery.stop();
        if (memo.getLnTrafficController() != null) {
            memo.getLnTrafficController().removeLocoNetListener(~0, this);
        }
    }
    static Logger log = Logger.getLogger(LnDplxGrpInfoImpl.class.getName());

}
