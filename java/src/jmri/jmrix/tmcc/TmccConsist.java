package jmri.jmrix.tmcc;

import javax.annotation.Nonnull;

import jmri.Consist;
import jmri.ConsistListener;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

/**
 * This is the Consist definition for a consist on a TMCC system.
 *
 * Based on MqttConsist by
 * @author Dean Cording Copyright (C) 2023
 * with edits/additions by
 * @author Timothy Jump (C) 2025
 */

public class TmccConsist extends jmri.implementation.DccConsist {

    @Nonnull
    public String sendTopicPrefix = "cab/{0}/consist";
    private boolean active = false;

    // Initialize a consist for the specific address.
    // The Default consist type is controller consist
    public TmccConsist(int address, TmccSystemConnectionMemo memo, String sendTopicPrefix) {
        super(address);
          tc = memo.getTrafficController();
        this.sendTopicPrefix = sendTopicPrefix;
        consistType = Consist.CS_CONSIST;
        log.debug("Consist {} created.", this.getConsistAddress());
    }

    // Initialize a consist for the specific address.
    // The Default consist type is controller consist
    public TmccConsist(DccLocoAddress address, TmccSystemConnectionMemo memo, String sendTopicPrefix) {
        super(address);
          tc = memo.getTrafficController();
        this.sendTopicPrefix = sendTopicPrefix;
        consistType = Consist.CS_CONSIST;
        log.debug("Consist {} created.", this.getConsistAddress());
    }

    // Clean Up local storage.
    @Override
    public void dispose() {
        super.dispose();
        log.debug("Consist {} disposed.", this.getConsistAddress());
    }

    // Set the Consist Type.
    @Override
    public void setConsistType(int consist_type) {
        log.debug("Set Consist Type {}", consist_type);
        if (consist_type == Consist.CS_CONSIST) {
            consistType = consist_type;
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(new DccLocoAddress(0, false), ConsistListener.NotImplemented);
        }
    }

    protected SerialTrafficController tc = null;


    /**
     * Is this TMCC ENG (loco ID#) address allowed?
     * TMCC systems only use ENG (loco ID#) addresses 1-98.
     * {@inheritDoc}
     */
    @Override
    public boolean isAddressAllowed(DccLocoAddress address) {
        return address.getNumber() > 0 && address.getNumber() < 99;
    }

    /**
     * Is there a size limit for this consist?
     *
     * @return -1 for Controller Consists (no limit),
     * 0 for any other consist type
     */
    @Override
    public int sizeLimit() {
        if (consistType == CS_CONSIST) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Does the consist contain the specified address?
     * {@inheritDoc}
     */
    @Override
    public boolean contains(DccLocoAddress address) {
        if (consistType == CS_CONSIST) {
            return consistList.contains(address);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(address, ConsistListener.NotImplemented);
        }
        return false;
    }

    /**
     * Get the relative direction setting for a specific
     * locomotive in the consist.
     * {@inheritDoc}
     */
    @Override
    public boolean getLocoDirection(DccLocoAddress address) {
        if (consistType == CS_CONSIST) {
            return consistDir.getOrDefault(address, false);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(address, ConsistListener.NotImplemented);
        }
        return false;
    }

    /**
     * Add an Address to the internal consist list object.
     */
    private synchronized void addToConsistList(DccLocoAddress locoAddress, boolean directionNormal) {

        log.debug("Add to consist list address {} direction {}", locoAddress, directionNormal);
        if (!(consistList.contains(locoAddress))) {
            consistList.add(locoAddress);
        }
        consistDir.put(locoAddress, directionNormal);
        notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);
    }

    /**
     * Remove an address from the internal consist list object.
     */
    private synchronized void removeFromConsistList(DccLocoAddress locoAddress) {
        log.debug("Remove from consist list address {}", locoAddress);
        consistDir.remove(locoAddress);
        consistList.remove(locoAddress);
        notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);
    }

    /**
     * Add a Locomotive to a Consist
     *
     * @param locoAddress - is the Locomotive address to add to the consist.
     * @param directionNormal - is True if the locomotive is traveling the same direction as the consist, or false otherwise.
     */
    @Override
    public synchronized void add(DccLocoAddress locoAddress, boolean directionNormal) {

        // TMCC1 Consist Build
        if (locoAddress.getProtocol() == LocoAddress.Protocol.TMCC1) {
            SerialMessage c = new SerialMessage();
            SerialMessage m = new SerialMessage();
            SerialMessage n = new SerialMessage();
            c.setOpCode(0xFE);
            m.setOpCode(0xFE);
            n.setOpCode(0xFE);

            // TMCC has 6 commands for adding a loco to a consist: head, rear, and mid, plus direction
            if (!contains(locoAddress)) {
                // First loco to consist
                if (consistList.isEmpty()) {
                    // add head loco
                    if (!directionNormal) {
                        // TMCC1 - Assign as Head Unit/Reverse Direction
                        c.putAsWord(0x0030 + (locoAddress.getNumber() * 128)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0030 + (locoAddress.getNumber() * 128) + consistAddress.getNumber());
                        n.putAsWord(0x0025 + locoAddress.getNumber() * 128);
                    } else {
                        // TMCC1 - Assign as Head Unit/Forward Direction
                        c.putAsWord(0x0030 + (locoAddress.getNumber() * 128)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0030 + (locoAddress.getNumber() * 128) + consistAddress.getNumber());
                        n.putAsWord(0x0021 + locoAddress.getNumber() * 128);
                    }
                // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(c, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(n, null);

                // Second loco to consist
                } else if (consistList.size() == 1) {
                    // add rear loco
                    if (!directionNormal) {
                        // TMCC1 - Assign as Rear Unit/Reverse Direction
                        c.putAsWord(0x0030 + (locoAddress.getNumber() * 128)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0030 + (locoAddress.getNumber() * 128) + consistAddress.getNumber());
                        n.putAsWord(0x0027 + locoAddress.getNumber() * 128);
                    } else {
                        // TMCC1 - Assign as Rear Unit/Forward Direction
                        c.putAsWord(0x0030 + (locoAddress.getNumber() * 128)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0030 + (locoAddress.getNumber() * 128) + consistAddress.getNumber());
                        n.putAsWord(0x0023 + locoAddress.getNumber() * 128);
                    }
                // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(c, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(n, null);

                // Additional loco(s) to consist
                } else {
                    // add mid loco
                    if (!directionNormal) {
                        // TMCC1 - Assign as Mid Unit/Reverse Direction
                        c.putAsWord(0x0030 + (locoAddress.getNumber() * 128)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0030 + (locoAddress.getNumber() * 128) + consistAddress.getNumber());
                        n.putAsWord(0x0026 + locoAddress.getNumber() * 128);
                    } else {
                        // TMCC1 - Assign as Mid Unit/Forward Direction
                        c.putAsWord(0x0030 + (locoAddress.getNumber() * 128)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0030 + (locoAddress.getNumber() * 128) + consistAddress.getNumber());
                        n.putAsWord(0x0022 + locoAddress.getNumber() * 128);
                    }
                // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(c, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(n, null);

                }

                // Add Loco to Consist List
                addToConsistList(locoAddress, directionNormal);

            } else {
                log.error("Loco {} is already part of this consist {}", locoAddress, getConsistAddress());
            }
        }

        // TMCC2 Consist Build
        if (locoAddress.getProtocol() == LocoAddress.Protocol.TMCC2) {
            SerialMessage c = new SerialMessage();
            SerialMessage m = new SerialMessage();
            SerialMessage n = new SerialMessage();
            c.setOpCode(0xF8);
            m.setOpCode(0xF8);
            n.setOpCode(0xF8);

            // TMCC has 6 commands for adding a loco to a consist: head, rear, and mid, plus direction
            if (!contains(locoAddress)) {
                // First loco to consist
                if (consistList.isEmpty()) {
                    // add head loco
                    if (!directionNormal) {
                        // TMCC1 - Assign as Head Unit/Reverse Direction
                        c.putAsWord(0x0130 + (locoAddress.getNumber() * 512)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0130 + (locoAddress.getNumber() * 512) + consistAddress.getNumber());
                        n.putAsWord(0x0123 + locoAddress.getNumber() * 512);
                    } else {
                        // TMCC1 - Assign as Head Unit/Forward Direction
                        c.putAsWord(0x0130 + (locoAddress.getNumber() * 512)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0130 + (locoAddress.getNumber() * 512) + consistAddress.getNumber());
                        n.putAsWord(0x0122 + locoAddress.getNumber() * 512);
                    }
                // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(c, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(n, null);

                // Second loco to consist
                } else if (consistList.size() == 1) {
                    // add rear loco
                    if (!directionNormal) {
                        // TMCC1 - Assign as Rear Unit/Reverse Direction
                        c.putAsWord(0x0130 + (locoAddress.getNumber() * 512)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0130 + (locoAddress.getNumber() * 512) + consistAddress.getNumber());
                        n.putAsWord(0x0127 + locoAddress.getNumber() * 512);
                    } else {
                        // TMCC1 - Assign as Rear Unit/Forward Direction
                        c.putAsWord(0x0130 + (locoAddress.getNumber() * 512)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0130 + (locoAddress.getNumber() * 512) + consistAddress.getNumber());
                        n.putAsWord(0x0126 + locoAddress.getNumber() * 512);
                    }
                // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(c, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(n, null);

                // Additional loco(s) to consist
                } else {
                    // add mid loco
                    if (!directionNormal) {
                        // TMCC1 - Assign as Mid Unit/Reverse Direction
                        c.putAsWord(0x0130 + (locoAddress.getNumber() * 512)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0130 + (locoAddress.getNumber() * 512) + consistAddress.getNumber());
                        n.putAsWord(0x0125 + locoAddress.getNumber() * 512);
                    } else {
                        // TMCC1 - Assign as Mid Unit/Forward Direction
                        c.putAsWord(0x0130 + (locoAddress.getNumber() * 512)); // Clear residual consist ID from locomotive
                        m.putAsWord(0x0130 + (locoAddress.getNumber() * 512) + consistAddress.getNumber());
                        n.putAsWord(0x0124 + locoAddress.getNumber() * 512);
                    }
                // send to command station (send twice is set, but number of sends may need to be adjusted depending on efficiency)
                tc.sendSerialMessage(c, null);
                tc.sendSerialMessage(m, null);
                tc.sendSerialMessage(n, null);

                }

                // Add Loco to Consist List
                addToConsistList(locoAddress, directionNormal);

            } else {
                log.error("Loco {} is already part of this consist {}", locoAddress, getConsistAddress());
            }
        }
    }

    /**
     * Restore a Locomotive to Consist, but don't write to
     * the command station.  This is used for restoring the consist
     * from a file or adding a consist read from the command station.
     *
     * @param locoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    public synchronized void restore(DccLocoAddress locoAddress, boolean directionNormal) {
        log.debug("Restore to consist address {} direction {}", locoAddress, directionNormal);

        if (consistType == CS_CONSIST) {
            addToConsistList(locoAddress, directionNormal);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(locoAddress, ConsistListener.NotImplemented);
        }
    }

    /**
     * Remove a Locomotive from this Consist
     * Clear Consist ID from Locomotive
     * @param locoAddress is the Locomotive address to add to the locomotive
     */
    @Override
    public synchronized void remove(DccLocoAddress locoAddress) {
        log.debug("Remove from consist address {}", locoAddress);

        // TMCC1 - Clear Consist ID from Locomotive
        if (locoAddress.getProtocol() == LocoAddress.Protocol.TMCC1) {
            SerialMessage c = new SerialMessage();
            c.setOpCode(0xFE);
            c.putAsWord(0x0030 + (locoAddress.getNumber() * 128));
            tc.sendSerialMessage(c, null);
        }

        // TMCC2 - Clear Consist ID from Locomotive
        if (locoAddress.getProtocol() == LocoAddress.Protocol.TMCC2) {
            SerialMessage c = new SerialMessage();
            c.setOpCode(0xF8);
            c.putAsWord(0x0130 + (locoAddress.getNumber() * 512));
            tc.sendSerialMessage(c, null);
        }

        // Remove Locomotive from this Consist
        if (consistType == CS_CONSIST) {
            removeFromConsistList(locoAddress);
            if (active) {
                publish();
            }
        } else {
                log.error("Consist Type Not Supported");
                notifyConsistListeners(locoAddress, ConsistListener.NotImplemented);
        }
    }

    /**
     * Activates the consist for use with a throttle
     */
    public void activate(){
        log.info("Activating consist {}", consistID);
        active = true;
            publish();
    }

    /**
     * Deactivates and removes the consist from a throttle
     */
     public void deactivate() {

        log.info("Deactivating consist {}", consistID);
        active = false;
    }

    /**
     * Publish the consist details to the controller
     */
    private void publish(){
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TmccConsist.class);

}
