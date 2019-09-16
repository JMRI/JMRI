package jmri.jmrix.loconet.duplexgroup.swing;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.duplexgroup.LnDplxGrpInfoImplConstants;

/**
 * Implements a class to handle message creation and message interpretation of
 * LocoNet messages associated with IPL. IPL is a mechanism which allows
 * identification and firmware programming of some types of Digitrax hardware.
 *
 * @author B. Milhaupt Copyright 2010, 2011, 2018
 */
public class LnIPLImplementation extends javax.swing.JComponent implements jmri.jmrix.loconet.LocoNetListener {

    /**
     * Constructor for LnIPMImplementation for a given
     * LocoNetSystemConnectionMemo as provided by the instantiating
     * method.
     *
     * @param lnMemo LocoNetSystemConnectionMemo for the LocoNet communication
     *               interface
     */
    public LnIPLImplementation(LocoNetSystemConnectionMemo lnMemo) {
        super();
        thisone = this;
        memo = lnMemo;

        moreInit();
    }

    private void moreInit() {
        waitingForIplReply = false;
        // connect to the LnTrafficController
        connect(memo.getLnTrafficController());

        swingTmrIplQuery = new javax.swing.Timer(LnDplxGrpInfoImplConstants.IPL_QUERY_DELAY, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                swingTmrIplQuery.stop();
                waitingForIplReply = false;
                int oldvalue = 9999;
                int newvalue = 0;
                thisone.firePropertyChange("LnIPLEndOfDeviceQuery", oldvalue, newvalue); // NOI18N
            }
        });
    }

    /**
     * Create a LocoNet packet which queries UR92(s) for Duplex group
     * identification information. The invoking method is responsible for
     * sending the message to LocoNet.
     *
     * @return a LocoNetMessage containing the packet required to query UR92
     *         device Duplex Group Identity information
     */
    public static final LocoNetMessage createQueryAllIplDevicesPacket() {
        LocoNetMessage m = new LocoNetMessage(LnConstants.RE_IPL_OP_LEN);
        // fill in data for an IPL Query of UR92s message
        m.setElement(0, LnConstants.OPC_PEER_XFER);
        m.setElement(1, LnConstants.RE_IPL_OP_LEN);
        m.setElement(2, LnConstants.RE_IPL_IDENTITY_OPERATION);
        m.setElement(3, LnConstants.RE_IPL_OP_QUERY);
        m.setElement(4, LnConstants.RE_IPL_MFR_ALL);
        m.setElement(5, LnConstants.RE_IPL_DIGITRAX_HOST_ALL);
        m.setElement(6, LnConstants.RE_IPL_DIGITRAX_SLAVE_ALL);
        m.setElement(7, LnConstants.RE_IPL_MFR_ALL);
        m.setElement(8, LnConstants.RE_IPL_OP_HFW_QUERY);
        m.setElement(9, LnConstants.RE_IPL_OP_HSNM_QUERY);
        m.setElement(10, LnConstants.RE_IPL_OP_SFW_QUERY);
        m.setElement(11, LnConstants.RE_IPL_OP_HSN0_QUERY);
        m.setElement(12, LnConstants.RE_IPL_OP_HSN1_QUERY);
        m.setElement(13, LnConstants.RE_IPL_OP_HSN2_QUERY);
        m.setElement(14, LnConstants.RE_IPL_OP_SSNM_QUERY);
        m.setElement(15, LnConstants.RE_IPL__OP_SSN0_QUERY);
        m.setElement(16, LnConstants.RE_IPL_OP_SSN1_QUERY);
        m.setElement(17, LnConstants.RE_IPL_OP_SSN2_QUERY);
        m.setElement(18, LnConstants.RE_IPL_OP_SSN3_QUERY);
        return m;
    }

    public void sendIplQueryAllDevices() {
        jmri.jmrix.loconet.LnTrafficController tc = memo.getLnTrafficController();
        tc.sendLocoNetMessage(createQueryAllIplDevicesPacket());
        waitingForIplReply = true;
    }

    /**
     * Create a LocoNet packet which queries IPL devices by specific host
     * manufacturer and specific host device type. The invoking method is
     * responsible for sending the message to LocoNet.
     * <p>
     * Note: Different devices may only respond to IPL Identity requests if the
     * host manufacturer and host type are defined. Others devices will respond
     * when host manufacturer and host type are left as zero.
     *
     * @param hostMfr    the host manufacturer number
     * @param hostDevice the host device type number
     * @return a LocoNetMessage containing the packet required to request IPL
     *         identity information from devices of the specified host
     *         manufacturer and host device type.
     */
    public static final LocoNetMessage createIplSpecificHostQueryPacket(
            Integer hostMfr,
            Integer hostDevice) {
        LocoNetMessage m = createQueryAllIplDevicesPacket();
        m.setElement(4, hostMfr & 0x7F);
        m.setElement(5, hostDevice & 0x7F);
        return m;
    }

    /**
     * Create a LocoNet packet which queries IPL devices by specific slave
     * manufacturer and specific slave device type. The invoking method is
     * responsible for sending the message to LocoNet.
     * <p>
     * Note: Some devices have no "slave" device and may not respond to this
     * message. Other devices may only respond if both manufacturer and device
     * type information is specified for both host and slave.
     *
     * @param slaveMfr    the slave manufacturer number
     * @param slaveDevice the slave device type number
     * @return a LocoNetMessage containing the packet required to request IPL
     *         identity information from devices of the specified slave
     *         manufacturer and slave device type.
     */
    public static final LocoNetMessage createIplSpecificSlaveQueryPacket(
            Integer slaveMfr,
            Integer slaveDevice) {
        LocoNetMessage m = createQueryAllIplDevicesPacket();
        m.setElement(7, slaveMfr & 0x7F);
        m.setElement(6, slaveDevice & 0x7F);
        return m;
    }

    /**
     * Create a LocoNet packet which queries IPL devices by specific host
     * manufacturer, specific host device type, specific slave manufacturer and
     * specific slave device type. The invoking method is responsible for
     * sending the message to LocoNet.
     * <p>
     * Note: Different devices respond differently depending on whether host
     * and/or slave manufacturer and/or device type information are provided.
     *
     * @param hostMfr     the host manufacturer number
     * @param hostDevice  the host device type number
     * @param slaveMfr    the slave manufacturer number
     * @param slaveDevice the slave device type number
     * @return a LocoNetMessage containing the packet required to request IPL
     *         identity information from devices of the specified host and slave
     *         manufacturers and host and slave device types.
     */
    public static final LocoNetMessage createIplSpecificSlaveQueryPacket(
            Integer hostMfr,
            Integer hostDevice,
            Integer slaveMfr,
            Integer slaveDevice) {
        LocoNetMessage m = createQueryAllIplDevicesPacket();
        m.setElement(4, hostMfr & 0x7F);
        m.setElement(5, hostDevice & 0x7F);
        m.setElement(7, slaveMfr & 0x7F);
        m.setElement(6, slaveDevice & 0x7F);
        return m;
    }

    /**
     * Create a LocoNet packet which queries UR92 devices for IPL
     * identification information. The invoking method is responsible for
     * sending the message to LocoNet.
     *
     * @return a LocoNetMessage containing the packet required to query UR92
     *         devices for IPL identification information
     */
    public static final LocoNetMessage createIplUr92QueryPacket() {
        return createIplSpecificHostQueryPacket(
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_UR92);
    }

    /**
     * Create a LocoNet packet which queries DT402x throttles for IPL
     * identification information. The invoking method is responsible for
     * sending the message to LocoNet.
     *
     * @return a LocoNetMessage containing the packet required to query DT402x
     *         devices for IPL identification information
     */
    public static final LocoNetMessage createIplDt402QueryPacket() {
        return createIplSpecificHostQueryPacket(
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_DT402);
    }

    /**
     * Create a LocoNet packet which queries (some) UT4 throttles for IPL
     * identification information. The invoking method is responsible for
     * sending the message to LocoNet.
     * <p>
     * Note that UT4 and UT4R devices may not respond to this query. UT4D
     * devices may respond to this query.
     *
     * @return a LocoNetMessage containing the packet required to query (some)
     *         UT4 devices for IPL identification information
     */
    public static final LocoNetMessage createIplUt4QueryPacket() {
        return createIplSpecificHostQueryPacket(
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_UT4);
    }

    /**
     * Create a LocoNet packet which queries DCS51 devices for IPL
     * identification information. The invoking method is responsible for
     * sending the message to LocoNet.
     *
     * @return a LocoNetMessage containing the packet required to query DCS51
     *         devices for IPL identification information
     */
    public static final LocoNetMessage createIplDcs51QueryPacket() {
        return createIplSpecificHostQueryPacket(
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_DCS51);
    }

    /**
     * Create a LocoNet packet which queries DCS52 devices for IPL
     * identification information. The invoking method is responsible for
     * sending the message to LocoNet.
     *
     * @return a LocoNetMessage containing the packet required to query DCS52
     *         devices for IPL identification information
     */
    public static final LocoNetMessage createIplDcs52QueryPacket() {
        return createIplSpecificHostQueryPacket(
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_DCS52);
    }

    /**
     * Create a LocoNet packet which queries PR3 devices for IPL identification
     * information. The invoking method is responsible for sending the message
     * to LocoNet.
     *
     * @return a LocoNetMessage containing the packet required to query PR3
     *         devices for IPL identification information
     */
    public static final LocoNetMessage createIplPr3QueryPacket() {
        return createIplSpecificHostQueryPacket(
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_PR3);
    }

    /**
     * Checks message m to determine if it contains a IPL Identity Report
     * message.
     *
     * @param m  LocoNetMessage to be checked for an IPL Identity Query message
     * @return true if message is report of IPL Identity
     */
    public static final boolean isIplIdentityQueryMessage(LocoNetMessage m) {
        if ((m.getOpCode() == LnConstants.OPC_PEER_XFER)
                && (m.getElement(1) == LnConstants.RE_IPL_OP_LEN)) {
            // Message is a peer-to-peer message of appropriate length for
            // IPL Report message.  Check the individual message type
            if (m.getElement(2) == LnConstants.RE_IPL_IDENTITY_OPERATION) {
                // To be sure the message is a IPL Identity Report operation, check the
                // operation type.
                if (m.getElement(3) == LnConstants.RE_IPL_OP_QUERY) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks message m to determine if it contains a IPL Identity Report
     * message.
     *
     * @param m  LocoNet message to check for an IPL Identity Report
     * @return true if message is report of IPL Identity
     */
    public static final boolean isIplIdentityReportMessage(LocoNetMessage m) {
        if ((m.getOpCode() == LnConstants.OPC_PEER_XFER)
                && (m.getElement(1) == LnConstants.RE_IPL_OP_LEN)) {
            // Message is a peer-to-peer message of appropriate length for
            // IPL Report message.  Check the individual message type
            if (m.getElement(2) == LnConstants.RE_IPL_IDENTITY_OPERATION) {
                // To be sure the message is a IPL Identity Report operation, check the
                // operation type.
                if (m.getElement(3) == LnConstants.RE_IPL_OP_REPORT) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check message m to determine if it contains an IPL Identity Report
     * message for a specific host manufacturer and specific host device type.
     *
     * @param m message to analyse
     * @param hostMfr    the host manufacturer number
     * @param hostDevice the host device type number
     * @return true if message is report of UR92 IPL Identity
     */
    public static final boolean isIplSpecificIdentityReportMessage(LocoNetMessage m,
            Integer hostMfr, Integer hostDevice) {
        if (!isIplIdentityReportMessage(m)) {
            return false;
        }
        if ((m.getElement(4) == (hostMfr & 0x7F))
                && (m.getElement(5) == (hostDevice & 0x7F))) {
            return true;
        }
        return false;
    }

    /**
     * Check message m to determine if it contains a UR92 IPL Identity Report
     * message.
     *
     * @param m message to analyse
     * @return true if message is report of UR92 IPL Identity
     */
    public static final boolean isIplUr92IdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_UR92);
    }

    /**
     * Check message m to determine if it contains a DT402 IPL Identity Report
     * message.
     *
     * @param m message to analyse
     * @return true if message is report of DT402 IPL Identity
     */
    public static final boolean isIplDt402IdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_DT402);
    }

    /**
     * Check message m to determine if it contains a UT4 IPL Identity Report
     * message.
     *
     * @param m message to analyse
     * @return true if message is report of UT4 IPL Identity
     */
    public static final boolean isIplUt4IdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_UT4);
    }

    /**
     * Check message m to determine if it contains a DSC51 IPL Identity Report
     * message.
     *
     * @param m message to analyse
     * @return true if message is report of DCS51 IPL Identity
     */
    public static final boolean isIplDcs51IdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_DCS51);
    }

    /**
     * Check message m to determine if it contains a DSC52 IPL Identity Report
     * message.
     *
     * @param m message to analyse
     * @return true if message is report of DCS52 IPL Identity
     */
    public static final boolean isIplDcs52IdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_DCS52);
    }

    /**
     * Check message m to determine if it contains a PR3 IPL Identity Report
     * message.
     *
     * @param m message to analyse
     * @return true if message is report of PR3 IPL Identity
     */
    public static final boolean isIplPr3IdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_PR3);
    }

    public static final boolean isIplDt402DIdentityReportMessage(LocoNetMessage m) {
        if (!isIplDt402IdentityReportMessage(m)) {
            return false;
        }
        if (!isIplRf24SlaveIdentityReportMessage(m)) {
            return false;
        } else {
            return true;
        }
    }

    public static final boolean isIplUt4DIdentityReportMessage(LocoNetMessage m) {
        if (!isIplUt4IdentityReportMessage(m)) {
            return false;
        }
        if (!isIplRf24SlaveIdentityReportMessage(m)) {
            return false;
        } else {
            return true;
        }
    }

    public static final boolean isIplPr4IdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_PR4);
    }

    public static final boolean isIplBxp88IdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_BXP88);
    }

    public static final boolean isIplLnwiIdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_LNWI);
    }

    public static final boolean isIplDcs240IdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_DCS240);
    }

    public static final boolean isIplDcs210IdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_DCS210);
    }

    public static final boolean isIplDt500DIdentityReportMessage(LocoNetMessage m) {
        if (!isIplDt500IdentityReportMessage(m)) {
            return false;
        }
        if (!isIplRf24SlaveIdentityReportMessage(m)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check message m to determine if it contains a DT500 IPL Identity Report
     * message.
     *
     * @param m message to analyse
     * @return true if message is report of DT500 IPL Identity
     */
    public static final boolean isIplDt500IdentityReportMessage(LocoNetMessage m) {
        return isIplSpecificIdentityReportMessage(m,
                LnConstants.RE_IPL_MFR_DIGITRAX,
                LnConstants.RE_IPL_DIGITRAX_HOST_DT500);
    }

    /**
     * Determine if message is IPL Identity Report with RF24 as slave device.
     *
     * @param m message to analyse
     * @return true if m contains IPL Identity Report with RF24 as slave, else
     *         false
     */
    private static final boolean isIplRf24SlaveIdentityReportMessage(LocoNetMessage m) {
        if ((extractIplIdentitySlaveManufacturer(m) == LnConstants.RE_IPL_MFR_DIGITRAX)
                && (extractIplIdentitySlaveDevice(m) == LnConstants.RE_IPL_DIGITRAX_SLAVE_RF24)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Extract the IPL Host manufacturer and Device information from m and
     * return the interpreted information as a String.
     *
     * @param m LocoNet Message containg the IPL Identity report
     * @return String containing the interpreted IPL Host Manufacturer and
     *         Device. If m is not a valid IPL Identity report, returns null.
     */
    public static final String extractInterpretedIplHostDevice(LocoNetMessage m) {
        if (!isIplIdentityReportMessage(m)) {
            return null;
        }
        if (isIplDt402DIdentityReportMessage(m)) {
            return interpretHostManufacturerDevice(
                    LnConstants.RE_IPL_MFR_DIGITRAX,
                    LnConstants.RE_IPL_DIGITRAX_HOST_DT402,
                    LnConstants.RE_IPL_MFR_DIGITRAX,
                    LnConstants.RE_IPL_DIGITRAX_SLAVE_RF24);
        }
        if (isIplUt4DIdentityReportMessage(m)) {
            return interpretHostManufacturerDevice(
                    LnConstants.RE_IPL_MFR_DIGITRAX,
                    LnConstants.RE_IPL_DIGITRAX_HOST_UT4,
                    LnConstants.RE_IPL_MFR_DIGITRAX,
                    LnConstants.RE_IPL_DIGITRAX_SLAVE_RF24);
        }
        if (isIplDt500DIdentityReportMessage(m)) {
            return interpretHostManufacturerDevice(
                    LnConstants.RE_IPL_MFR_DIGITRAX,
                    LnConstants.RE_IPL_DIGITRAX_HOST_DT500,
                    LnConstants.RE_IPL_MFR_DIGITRAX,
                    LnConstants.RE_IPL_DIGITRAX_SLAVE_RF24);
        }

        return interpretHostManufacturerDevice(extractIplIdentityHostManufacturer(m), extractIplIdentityHostDevice(m));
    }

    /**
     * Extract the IPL Slave manufacturer and Device information from m.
     *
     * @param m IPL Identity message
     * @return String containing the interpreted IPL Slave Manufacturer and
     *         Device. If m is not a valid IPL Identity report, returns null.
     */
    public static final String extractInterpretedIplSlaveDevice(LocoNetMessage m) {
        if (!isIplIdentityReportMessage(m)) {
            return null;
        }
        return interpretSlaveManufacturerDevice(extractIplIdentitySlaveManufacturer(m), extractIplIdentitySlaveDevice(m));

    }

    /**
     * Get the IPL host manufacturer number from an IPL Identity report
     * message.
     * <p>
     * The invoking method should ensure that message m is an IPL Identity
     * message before invoking this method.
     *
     * @param m IPL Identity message
     * @return Integer containing the IPL host manufacturer number
     */
    public static final Integer extractIplIdentityHostManufacturer(LocoNetMessage m) {
        return m.getElement(4);
    }

    /**
     * Get the host device number from an IPL Identity report message.
     * <p>
     * The invoking method should ensure that message m is is an IPL Identity
     * message before invoking this method.
     *
     * @param m IPL Identity message
     * @return Integer containing the IPL device number
     */
    public static final Integer extractIplIdentityHostDevice(LocoNetMessage m) {
        return m.getElement(5);
    }

    /**
     * Get the slave manufacturer number from an IPL Identity report
     * message.
     * <p>
     * The invoking method should ensure that message m is is an IPL Identity
     * message before invoking this method.
     * <p>
     * NOTE: Not all IPL-capable devices implement a slave manufacturer number.
     *
     * @param m IPL Identity message
     * @return Integer containing the IPL slave manufacturer number
     */
    public static final Integer extractIplIdentitySlaveManufacturer(LocoNetMessage m) {
        return m.getElement(7);
    }

    /**
     * Get the slave device number from an IPL Identity report message.
     * <p>
     * The invoking method should ensure that message m is is an IPL Identity
     * message before invoking this method.
     * <p>
     * NOTE: Not all IPL-capable devices implement a slave device number.
     *
     * @param m IPL Identity message
     * @return Integer containing the IPL slave device number
     */
    public static final Integer extractIplIdentitySlaveDevice(LocoNetMessage m) {
        return m.getElement(6);
    }

    /**
     * Get the host firmware revision number from an IPL Identity report
     * message.
     * <p>
     * The invoking method should ensure that message m is is an IPL Identity
     * message before invoking this method.
     * <p>
     * NOTE: Not all IPL-capable devices implement a host firmware revision
     * number.
     *
     * @param m IPL Identity message
     * @return String containing the IPL host firmware revision in the format
     *         x.y
     */
    public static final String extractIplIdentityHostFrimwareRev(LocoNetMessage m) {
        StringBuilder s = new StringBuilder();
        s.append(Integer.toString((m.getElement(8) & 0x78) >> 3));
        s.append(".");
        s.append(Integer.toString((m.getElement(8) & 0x07)));
        return s.toString();
    }

    /**
     * Get the host firmware revision number from an IPL Identity report
     * message.
     * <p>
     * The invoking method should ensure that message m is is an IPL Identity
     * message before invoking this method..
     * <p>
     * NOTE: Not all IPL-capable devices implement a host firmware revision
     * number.
     *
     * @param m IPL Identity message
     * @return Integer containing the IPL host firmware revision
     */
    public static final Integer extractIplIdentityHostFrimwareRevNum(LocoNetMessage m) {
        return (m.getElement(8));
    }

    /**
     * Get the Slave firmware revision number from an IPL Identity report
     * message.
     * <p>
     * The invoking method should ensure that message m is is an IPL Identity
     * message before invoking this method..
     * <p>
     * NOTE: Not all IPL-capable devices implement a Slave firmware revision
     * number.
     *
     * @param m IPL Identity message
     * @return Integer containing the IPL Slave firmware revision
     */
    public static final Integer extractIplIdentitySlaveFrimwareRevNum(LocoNetMessage m) {
        return ((m.getElement(10) & 0x7F) + ((m.getElement(9) & 0x1) << 7));
    }

    /**
     * Get the slave firmware revision number from an IPL Identity report
     * message.
     * <p>
     * The invoking method should ensure that message m is is an IPL Identity
     * message before invoking this method..
     * <p>
     * NOTE: Not all IPL-capable devices implement a slave firmware revision
     * number.
     *
     * @param m IPL Identity message
     * @return String containing the IPL slave firmware revision in the format
     *         x.y
     */
    public static final String extractIplIdentitySlaveFrimwareRev(LocoNetMessage m) {
        StringBuilder s = new StringBuilder();
        s.append(Integer.toString(((m.getElement(10) & 0x78) >> 3) + ((m.getElement(9) & 0x1) << 4)));
        s.append(".");
        s.append(Integer.toString((m.getElement(10) & 0x07)));
        return s.toString();
    }

    /**
     * Get the host serial number from an IPL Identity report message.
     * <p>
     * The invoking method should ensure that message m is is an IPL Identity
     * message before invoking this method..
     * <p>
     * NOTE: Not all IPL-capable devices implement a host serial number.
     *
     * @param m IPL Identity message
     * @return Long containing the IPL host serial number
     */
    public static final Long extractIplIdentityHostSerialNumber(LocoNetMessage m) {
        Long sn;
        Integer di_f1;
        di_f1 = m.getElement(9);
        sn = (long) (m.getElement(11) + ((di_f1 & 0x2) << 6));
        sn += (((long) m.getElement(12)) << 8) + (((long) di_f1 & 0x4) << 13);
        sn += (((long) m.getElement(13)) << 16) + (((long) di_f1 & 0x8) << 20);
        return sn;
    }

    /**
     * Get the slave serial number from an IPL Identity report message.
     * <p>
     * The invoking method should ensure that message m is is an IPL Identity
     * message before invoking this method.
     * <p>
     * NOTE: Not all IPL-capable devices implement a slave serial number.
     *
     * @param m IPL Identity message
     * @return Long containing the IPL slave serial number
     */
    public static final Long extractIplIdentitySlaveSerialNumber(LocoNetMessage m) {
        Long sn;
        Integer di_f2;
        di_f2 = m.getElement(14);
        sn = (long) (m.getElement(15) + ((di_f2 & 0x1) << 7));
        sn += (((long) m.getElement(16)) << 8) + (((long) di_f2 & 0x2) << 14);
        sn += (((long) m.getElement(17)) << 16) + (((long) di_f2 & 0x4) << 21);
        sn += (((long) m.getElement(18)) << 24) + (((long) di_f2 & 0x8) << 28);
        return sn;
    }

    /**
     * Interpret IPL Identity Host Manufacturer and Host Device number as a
     * string.
     * <p>
     * NOTE: Some IPL-capable devices cannot be completely determined based
     * solely on Host Manufacturer number and Host Device number.
     * <p>
     * NOTE: Some members of a device family do not support IPL. An interpreted
     * IPL Host Manufacturer number and Host Device number might imply that all
     * members do support IPL. As an example, UT4 and UT4R devices do not appear
     * to support IPL, while UT4D appears to support IPL. This method will
     * return "Digitrax UT4(x)" in response to appropriate Host Manufacturer
     * number and appropriate Host Device number.
     *
     * @param hostMfr  host manufacturer number
     * @param hostDevice  host device number
     * @param slaveMfr  slave manufacturer number
     * @param slaveDevice  slave device number
     * @return String containing Manufacturer name and Device model.
     */
    public static final String interpretHostManufacturerDevice(Integer hostMfr, Integer hostDevice,
            Integer slaveMfr, Integer slaveDevice) {
        String s;
        s = "Unknown Host Manufacturer/Device";
        int manuf = hostMfr & 0x7f;
        int device = hostDevice & 0x7f;
        int slave = slaveDevice & 0x7f;
        int smanuf = slaveMfr & 0x7f;
        switch (manuf) {
            case LnConstants.RE_IPL_MFR_DIGITRAX: {
                switch (device) {
                    case LnConstants.RE_IPL_DIGITRAX_HOST_DCS51:
                        s = "Digitrax DCS51"; // NOI18N
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_DCS52:
                        s = "Digitrax DCS52"; // NOI18N
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_DT402:
                        if ((smanuf == LnConstants.RE_IPL_MFR_DIGITRAX)
                                && (slave == LnConstants.RE_IPL_DIGITRAX_SLAVE_RF24)) {
                            s = "Digitrax DT402D"; // NOI18N
                        } else {
                            s = "Digitrax DT402(x)"; // NOI18N
                        }
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_PR3:
                        s = "Digitrax PR3"; // NOI18N
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_UR92:
                        s = "Digitrax UR92"; // NOI18N
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_UT4:
                        if ((smanuf == LnConstants.RE_IPL_MFR_DIGITRAX)
                                && (slave == LnConstants.RE_IPL_DIGITRAX_SLAVE_RF24)) {
                            s = "Digitrax UT4D"; // NOI18N
                        } else {
                            s = "Digitrax UT4(x)"; // NOI18N
                        }
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_DB210OPTO:
                        s = "Digitrax DB210Opto";
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_DB210:
                        s = "Digitrax DB210";
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_DB220:
                        s = "Digitrax DB220";
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_PR4:
                        s = "Digitrax PR4";
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_BXP88:
                        s = "Digitrax BXP88";
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_LNWI:
                        s = "Digitrax LNWI";
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_DCS210:
                        s = "Digitrax DCS210";
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_DCS240:
                        s = "Digitrax DCS240";
                        break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_DT500:
                        if ((smanuf == LnConstants.RE_IPL_MFR_DIGITRAX)
                                && (slave == LnConstants.RE_IPL_DIGITRAX_SLAVE_RF24)) {
                            s = "Digitrax DT500D"; // NOI18N
                        } else {
                            s = "Digitrax DT500(x)"; // NOI18N
                        }
                            break;
                    case LnConstants.RE_IPL_DIGITRAX_HOST_BXPA1:
                        s = "Digitrax BXPA1";
                        break;
                    default:
                        break;
                }
                break;
            }
            case LnConstants.RE_IPL_MFR_RR_CIRKITS:

                switch (device) {
                    case LnConstants.RE_IPL_RRCIRKITS_HOST_TC64:
                        s = "RR-CirKits TC-64"; // NOI18N
                        break;
                    case LnConstants.RE_IPL_RRCIRKITS_HOST_TC64_MKII:
                        s = "RR-CirKits TC-64 Mk-II"; // NOI18N
                        break;
                    case LnConstants.RE_IPL_RRCIRKITS_HOST_LNCP:
                        s = "RR-CirKits LNCP"; // NOI18N
                        break;
                    case LnConstants.RE_IPL_RRCIRKITS_HOST_MOTORMAN:
                        s = "RR-CirKits MotorMan"; // NOI18N
                        break;
                    case LnConstants.RE_IPL_RRCIRKITS_HOST_MOTORMAN_II:
                        s = "RR-CirKits MotorMan-II"; // NOI18N
                        break;
                    case LnConstants.RE_IPL_RRCIRKITS_HOST_SIGNALMAN:
                        s = "RR-CirKits SignalMan"; // NOI18N
                        break;
                    case LnConstants.RE_IPL_RRCIRKITS_HOST_TOWERMAN:
                        s = "RR-CirKits TowerMan"; // NOI18N
                        break;
                    case LnConstants.RE_IPL_RRCIRKITS_HOST_WATCHMAN:
                        s = "RR-CirKits WatchMan"; // NOI18N
                        break;
                    default:
                        s = "RR-CirKits (unknown device)";
                        break;
                }
                break;
            default:
                break;
        }
        return s;
    }
    /**
     * Interpret IPL Identity Host Manufacturer and Host Device number as a
     * string.
     * <p>
     * NOTE: Some IPL-capable devices cannot be completely determined based
     * solely on Host Manufacturer number and Host Device number.
     * <p>
     * NOTE: Some members of a device family do not support IPL. An interpreted
     * IPL Host Manufacturer number and Host Device number might imply that all
     * members do support IPL. As an example, UT4 and UT4R devices do not appear
     * to support IPL, while UT4D appears to support IPL. This method will
     * return "Digitrax UT4(x)" in response to appropriate Host Manufacturer
     * number and appropriate Host Device number.
     *
     * @param hostMfr  host manufacturer number
     * @param hostDevice  host device number
     * @return String containing Manufacturer name and Device model.
     */
    public static final String interpretHostManufacturerDevice(Integer hostMfr, Integer hostDevice) {
        return interpretHostManufacturerDevice(hostMfr, hostDevice, 0, 0);
    }

    /**
     * Interpret IPL Identity Slave Manufacturer and Slave Device number as a
     * string.
     * <p>
     * NOTE: Some IPL-capable devices may not be completely determined based
     * solely on Slave Manufacturer number and Slave Device number.
     *
     * @param slaveMfr  slave manufacturer number
     * @param slaveDevice  slave device number
     * @return String containing Slave Manufacturer name and Device model.
     */
    public static final String interpretSlaveManufacturerDevice(Integer slaveMfr, Integer slaveDevice) {
        String s;
        s = "Unknown Slave Manufacturer/Device";
        int sMfr = slaveMfr & 0x7f;
        int sDevice = slaveDevice & 0x7F;
        switch (sMfr) {
            case LnConstants.RE_IPL_MFR_DIGITRAX: {
                switch (sDevice) {
                    case LnConstants.RE_IPL_DIGITRAX_SLAVE_RF24:
                        s = "Digitrax RF24"; // NOI18N
                        break;
                    default:
                        s = "Digitrax (unknown Slave Device)";
                        break;
                }
                break;
            }
            case LnConstants.RE_IPL_MFR_RR_CIRKITS: {
                s = "RR-CirKits (unknown Slave Device)";
                break;
            }
            default:
                break;
        }
        return s;
    }
    LnIPLImplementation thisone;
    private LocoNetSystemConnectionMemo memo;

    /**
     * Connect this instance's LocoNetListener to the LocoNet Traffic Controller.
     *
     * @param t  a LocoNet Traffic Controller
     */
    public void connect(jmri.jmrix.loconet.LnTrafficController t) {
        if (t != null) {
            // connect to the LnTrafficController
            t.addLocoNetListener(~0, this);
        }
    }
    private javax.swing.Timer swingTmrIplQuery;

    /**
     * Break connection with the LnTrafficController and stop timers.
     */
    public void dispose() {
        if (swingTmrIplQuery != null) {
            swingTmrIplQuery.stop();
        }
        if (memo.getLnTrafficController() != null) {
            memo.getLnTrafficController().removeLocoNetListener(~0, this);
        }
    }

    /**
     * Process all incoming LocoNet messages to look for IPL operations. Ignores
     * all other LocoNet messages.
     *
     * @param m  incoming LocoNet message to be examined
     */
    @Override
    public void message(LocoNetMessage m) {

        if (handleMessageIplDeviceQuery(m)) {
            return;
        } else if (handleMessageIplDeviceReport(m)) {
            return;
        }

        return;
    }

    private boolean handleMessageIplDeviceQuery(LocoNetMessage m) {
        if (isIplIdentityQueryMessage(m)) {
            Integer deviceType = 256 * extractIplIdentityHostManufacturer(m)
                    + extractIplIdentityHostDevice(m);
            int oldvalue = 99999;
            int newvalue = deviceType;
            thisone.firePropertyChange("IplDeviceTypeQuery", oldvalue, newvalue); // NOI18N
            if (waitingForIplReply == true) {
                swingTmrIplQuery.restart();
            }
            return true;
        }
        return false;
    }

    private boolean handleMessageIplDeviceReport(LocoNetMessage m) {
        if (isIplIdentityReportMessage(m)) {
            Integer deviceType = 256 * extractIplIdentityHostManufacturer(m)
                    + extractIplIdentityHostDevice(m);
            int oldvalue = 99999;
            int newvalue = deviceType;
            thisone.firePropertyChange("IplDeviceTypeReport", oldvalue, newvalue); // NOI18N
            if (waitingForIplReply == true) {
                waitingForIplReply = false;
                swingTmrIplQuery.stop();
            }
            return true;
        }
        return false;
    }
    public boolean isIplQueryTimerRunning() {
        return swingTmrIplQuery.isRunning();
    }

    private boolean waitingForIplReply;
 }