package jmri.jmrix.mqtt;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

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
    // The Default consist type is an controller consist
    public MqttConsist(int address, MqttSystemConnectionMemo memo, String sendTopicPrefix) {
        super(address);
        mqttAdapter = memo.getMqttAdapter();
        this.sendTopicPrefix = sendTopicPrefix;
        consistType = Consist.CS_CONSIST;
    }

    // Initialize a consist for the specific address.
    // The Default consist type is controller consist
    public MqttConsist(DccLocoAddress address, MqttSystemConnectionMemo memo, String sendTopicPrefix) {
        super(address);
        mqttAdapter = memo.getMqttAdapter();
        this.sendTopicPrefix = sendTopicPrefix;
        consistType = Consist.CS_CONSIST;
    }

    // Clean Up local storage.
    @Override
    public void dispose() {
        super.dispose();
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
     * On MQTT systems, all addresses but 0 can be used in a consist
     */
    @Override
    public boolean isAddressAllowed(DccLocoAddress address) {
        if (address.getNumber() != 0) {
            return (true);
        } else {
            return (false);
        }
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
     */
    @Override
    public boolean getLocoDirection(DccLocoAddress address) {
        if (consistType == CS_CONSIST) {
            Boolean Direction = consistDir.get(address);
            return (Direction.booleanValue());
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(address, ConsistListener.NotImplemented);
        }
        return false;
    }

    /**
     * Add an Address to the internal consist list object.
     */
    private synchronized void addToConsistList(DccLocoAddress LocoAddress, boolean directionNormal) {

        log.debug("Add to consist list address {} direction{}", LocoAddress, directionNormal);
        Boolean Direction = Boolean.valueOf(directionNormal);
        if (!(consistList.contains(LocoAddress))) {
            consistList.add(LocoAddress);
        }
        consistDir.put(LocoAddress, Direction);
        notifyConsistListeners(LocoAddress, ConsistListener.OPERATION_SUCCESS);
    }

    /**
     * Remove an address from the internal consist list object.
     */
    private synchronized void removeFromConsistList(DccLocoAddress LocoAddress) {
        log.debug("Remove from consist list address {}", LocoAddress);
        consistDir.remove(LocoAddress);
        consistList.remove(LocoAddress);
        notifyConsistListeners(LocoAddress, ConsistListener.OPERATION_SUCCESS);
    }

    /**
     * Add a Locomotive to a Consist.
     *
     * @param LocoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    public synchronized void add(DccLocoAddress LocoAddress, boolean directionNormal) {
        log.debug("Add to consist address {} direction{}", LocoAddress, directionNormal);
        if (consistType == CS_CONSIST) {
            addToConsistList(LocoAddress, directionNormal);
            if (active) publish();
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(LocoAddress, ConsistListener.NotImplemented);
        }
    }

    /**
     * Restore a Locomotive to Consist, but don't write to
     * the command station.  This is used for restoring the consist
     * from a file or adding a consist read from the command station.
     *
     * @param LocoAddress is the Locomotive address to add to the locomotive
     * @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    public synchronized void restore(DccLocoAddress LocoAddress, boolean directionNormal) {
        log.debug("Restore to advanced consist address {} direction {}", LocoAddress, directionNormal);

        if (consistType == CS_CONSIST) {
            addToConsistList(LocoAddress, directionNormal);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(LocoAddress, ConsistListener.NotImplemented);
        }
    }

    /**
     * Remove a Locomotive from this Consist.
     *
     * @param LocoAddress is the Locomotive address to add to the locomotive
     */
    @Override
    public synchronized void remove(DccLocoAddress LocoAddress) {
        log.debug("Remove from advanced consist address {}", LocoAddress);

        if (consistType == CS_CONSIST) {
            removeFromConsistList(LocoAddress);
            if (active) publish();
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(LocoAddress, ConsistListener.NotImplemented);
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
        jmri.util.ThreadingUtil.runOnLayoutEventually(() -> {
            mqttAdapter.publish(this.sendTopicPrefix.replaceFirst("\\{0\\}", String.valueOf(consistAddress.getNumber())), "");
        });

    }

    @SuppressFBWarnings(value = "WMI_WRONG_MAP_ITERATOR", justification = "false positive")
    private String getConsistMakeup() {

        String consistMakeup = "";

        for (DccLocoAddress  address : consistDir.keySet()) {
            consistMakeup = consistMakeup.concat(consistDir.get(address) ? "":"-").concat(String.valueOf(address.getNumber())).concat(" ");
        }

        return consistMakeup.trim();

    }

    /**
     * Publish the consist details to the controller
     */
    private void publish(){
           // Send MQTT message
            jmri.util.ThreadingUtil.runOnLayout(() -> {
                mqttAdapter.publish(this.sendTopicPrefix.replaceFirst("\\{0\\}", String.valueOf(consistAddress.getNumber())), getConsistMakeup());
            });

    }

    private final static Logger log = LoggerFactory.getLogger(MqttConsist.class);

}
