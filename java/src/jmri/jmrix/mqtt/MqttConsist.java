package jmri.jmrix.mqtt;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nonnull;

import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;

/**
 * This is the Consist definition for a consist on an MQTT system.
 *
 * @author Dean Cording Copyright (C) 2023
 */
public class MqttConsist extends jmri.implementation.DccConsist {

    private final MqttAdapter mqttAdapter;
    @Nonnull
    public String sendTopicPrefix = "cab/{0}/consist";
    private boolean active = false;

    // Initialize a consist for the specific address.
    // The Default consist type is controller consist
    public MqttConsist(int address, MqttSystemConnectionMemo memo, String sendTopicPrefix) {
        super(address);
        mqttAdapter = memo.getMqttAdapter();
        this.sendTopicPrefix = sendTopicPrefix;
        consistType = Consist.CS_CONSIST;
        log.debug("Consist {} created.", this.getConsistAddress());
    }

    // Initialize a consist for the specific address.
    // The Default consist type is controller consist
    public MqttConsist(DccLocoAddress address, MqttSystemConnectionMemo memo, String sendTopicPrefix) {
        super(address);
        mqttAdapter = memo.getMqttAdapter();
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

    /**
     * Is this address allowed?
     * On MQTT systems, all addresses but 0 can be used in a consist.
     * {@inheritDoc}
     */
    @Override
    public boolean isAddressAllowed(DccLocoAddress address) {
        return address.getNumber() != 0;
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
     * Add a Locomotive to a Consist.
     *
     * @param locoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    public synchronized void add(DccLocoAddress locoAddress, boolean directionNormal) {
        log.debug("Add to consist address {} direction {}", locoAddress, directionNormal);
        if (consistType == CS_CONSIST) {
            addToConsistList(locoAddress, directionNormal);
            if (active) {
                publish();
            }
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(locoAddress, ConsistListener.NotImplemented);
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
     * Remove a Locomotive from this Consist.
     *
     * @param locoAddress is the Locomotive address to add to the locomotive
     */
    @Override
    public synchronized void remove(DccLocoAddress locoAddress) {
        log.debug("Remove from consist address {}", locoAddress);

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
        // Clear MQTT message
        jmri.util.ThreadingUtil.runOnLayoutEventually(() ->
            mqttAdapter.publish(this.sendTopicPrefix.replaceFirst("\\{0\\}", 
                String.valueOf(consistAddress.getNumber())), ""));

    }

    @SuppressFBWarnings(value = "WMI_WRONG_MAP_ITERATOR", justification = "false positive")
    private String getConsistMakeup() {

        String consistMakeup = "";

        for (DccLocoAddress  address : consistDir.keySet()) {
            consistMakeup = consistMakeup.concat(consistDir.get(address) ? "":"-")
                .concat(String.valueOf(address.getNumber())).concat(" ");
        }

        return consistMakeup.trim();

    }

    /**
     * Publish the consist details to the controller
     */
    private void publish(){
        // Send MQTT message
        jmri.util.ThreadingUtil.runOnLayout(() ->
            mqttAdapter.publish(this.sendTopicPrefix.replaceFirst("\\{0\\}", 
                String.valueOf(consistAddress.getNumber())), getConsistMakeup()));

    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MqttConsist.class);

}
