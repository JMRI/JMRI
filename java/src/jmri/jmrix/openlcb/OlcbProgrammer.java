package jmri.jmrix.openlcb;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jmri.ProgListener;
import jmri.ProgrammingMode;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.VerifyNodeIDNumberMessage;
import org.openlcb.implementations.MemoryConfigurationService;

/**
 * Provide access to the hardware DCC decoder programming capability.
 * <p>
 * Programmers come in multiple types:
 * <ul>
 * <li>Global, previously "Service Mode" or on a programming track
 * <li>Addressed, previously "Ops Mode" also known as "programming on the main"
 * </ul>
 * Different equipment may also require different programmers:
 * <ul>
 * <li>DCC CV programming, on service mode track or on the main
 * <li>CBUS Node Variable programmers
 * <li>LocoNet System Variable programmers
 * <li>LocoNet Op Switch programmers
 * <li>etc
 * </ul>
 * Depending on which type you have, only certain modes can be set. Valid modes
 * are specified by the class static constants.
 * <p>
 * You get a Programmer object from a {@link jmri.AddressedProgrammer}, which in turn
 * can be located from the {@link jmri.InstanceManager}.
 * <p>
 * Starting in JMRI 3.5.5, the CV addresses are Strings for generality. The
 * methods that use ints for CV addresses will later be deprecated.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @see jmri.AddressedProgrammer
 * @author Bob Jacobsen Copyright (C) 2015
 * @since 4.1.1
 */
public class OlcbProgrammer extends jmri.jmrix.AbstractProgrammer implements jmri.AddressedProgrammer {

    /// Memory space number used for DCC CVs.
    public static final int SPACE_DCC_CV = 0xF8;


    /// No locomotive is detected on the programming track.
    public static final int ERROR_NO_LOCO = 0x2031;
    /// The verify after a write operation got no ack.
    public static final int ERROR_FAILED_VERIFY = 0x2032;
    /// A POM read never received a reply from the locomotive.
    public static final int ERROR_NO_RAILCOM = 0x2033;
    /// A POM read returned only garbage railcom data (e.g. nacks).
    public static final int ERROR_INVALID_RESPONSE = 0x2034;
    /// Short circuit condition was detected on the programming track.
    public static final int ERROR_PGM_SHORT = 0x2035;

    /// Unimplemented command.
    public static final int ERROR_UNIMPLEMENTED_CMD = 0x1042;

    /// Invalid arguments were given to the command.
    public static final int ERROR_INVALID_ARGUMENTS = 0x1080;

    /// The program track is disabled.
    public static final int ERROR_PGM_DISABLED = 0x1021;

    /// Interface to which this programmer is bound to.
    private final OlcbInterface iface;
    /// Target OpenLCB node to send requests to. This is set to the train node when we are an addressed programmer.
    /// It may be null if we are a global programmer and we have not looked up the programming track node ID yet.
    @Nullable
    private NodeID nid;
    /// Stores the DCC address value (for addressed programmer only).
    private int dccAddress;
    /// Stores the dcc address type (for addressed programmer only).
    private boolean dccIsLong;

    /**
     * Creates a programmer for a given OpenLCB node.
     *
     * @param system system connection memo
     * @param nid    the target node to use for DCC CV programming. This can be a train node or a program track node.
     */
    public OlcbProgrammer(OlcbInterface system, @Nullable NodeID nid) {
        this(system);
        this.nid = nid;
        if (nid != null) {
            // Sends an addressed verify node ID message to ensure that the remote node exists and we have an alias.
            getInterface().getOutputConnection().put(new VerifyNodeIDNumberMessage(nid), null);
        }
    }

    /**
     * Creates an addressed programmer for a train node given by a DCC address.
     *
     * @param system  system connection memo
     * @param isLong  dcc address type
     * @param address dcc address number
     */
    public OlcbProgrammer(OlcbInterface system, boolean isLong, int address) {
        this(system, OlcbThrottle.guessDCCNodeID(isLong, address));
        this.dccIsLong = isLong;
        this.dccAddress = address;
    }

    public OlcbProgrammer(OlcbInterface system) {
        this.iface = system;
        this.nid = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> retval = new ArrayList<>();
        retval.add(ProgrammingMode.DIRECTBYTEMODE);
        retval.add(ProgrammingMode.OPSBYTEMODE);
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void timeout() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeCV(String CV, int val, ProgListener p) {
        getInterface().getMemoryConfigurationService().requestWrite(nid, SPACE_DCC_CV, getCvAddress(CV), new byte[]{(byte) val}, new MemoryConfigurationService.McsWriteHandler() {
            @Override
            public void handleSuccess() {
                notifyProgListenerEnd(p, val, ProgListener.OK);
            }

            @Override
            public void handleFailure(int i) {
                notifyProgListenerEnd(p, 0, olcbErrorToProgStatus(i));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readCV(String CV, ProgListener p) {
        getInterface().getMemoryConfigurationService().requestRead(nid, SPACE_DCC_CV, getCvAddress(CV), 1, new MemoryConfigurationService.McsReadHandler() {
            @Override
            public void handleReadData(NodeID nodeID, int i, long l, byte[] bytes) {
                if (bytes.length < 1) {
                    handleFailure(0x1000);
                    return;
                }
                if (p != null) {
                    notifyProgListenerEnd(p, bytes[0] & 0xff, ProgListener.OK);
                }
            }

            @Override
            public void handleFailure(int i) {
                log.debug("CV {} read - memory config error 0x{}", CV, Integer.toHexString(i));
                notifyProgListenerEnd(p, 0, olcbErrorToProgStatus(i));
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirmCV(String CV, int val, ProgListener p) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLongAddress() {
        return dccIsLong;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAddressNumber() {
        return dccAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAddress() {
        if (dccAddress > 0 || dccIsLong) {
            return Integer.toString(dccAddress) + (dccIsLong ? "L" : "S");
        }
        if (nid != null)
            return nid.toString();
        return "null";
    }

    private OlcbInterface getInterface() {
        return iface;
    }

    /**
     * Translates a (string) CV number into an address to read on the
     *
     * @param cvName CV address as provided to the various interface functions.
     * @return memory space address to perform the read/write to.
     */
    private long getCvAddress(String cvName) {
        int cvNum = Integer.parseInt(cvName);
        return cvNum - 1;
    }

    /**
     * Translates an OpenLCB error code (16-bit integer) to a ProgListener error code.
     *
     * @param olcbError openlcb error code (16-bit usigned integer)
     * @return prog listener error code.
     */
    private static int olcbErrorToProgStatus(int olcbError) {
        switch (olcbError) {
            case ERROR_NO_LOCO:
                return ProgListener.NoLocoDetected;
            case ERROR_FAILED_VERIFY:
                return ProgListener.ConfirmFailed;
            case ERROR_NO_RAILCOM:
                /// @todo how do we represent that the target loco does not support railcom?
                return ProgListener.NoLocoDetected;
            case ERROR_INVALID_RESPONSE:
                return ProgListener.CommError;
            case ERROR_PGM_SHORT:
                return ProgListener.ProgrammingShort;
            case ERROR_UNIMPLEMENTED_CMD:
                return ProgListener.NotImplemented;
            case ERROR_INVALID_ARGUMENTS:
                return ProgListener.SequenceError;
            case ERROR_PGM_DISABLED:
                /// @todo this is not a very accurate representation of a configuration error.
                return ProgListener.ProgrammerBusy;
        }
        if ((olcbError & 0x2000) != 0) {
            // Unknown temporary error
            return ProgListener.SequenceError;
        }
        if ((olcbError & 0x1000) != 0) {
            // Unknown permanent error
            return ProgListener.NotImplemented;
        }
        if (olcbError != 0) {
            return ProgListener.UnknownError;
        } else {
            return ProgListener.OK;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbProgrammer.class);
}
