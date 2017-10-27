package jmri.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import jmri.AddressedProgrammer;
import jmri.Consist;
import jmri.ConsistListener;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.ProgListener;
import jmri.ProgrammerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Default DCC consist. It utilizes the fact that IF a Command
 * Station supports OpsMode Programming, you can write the consist information
 * to CV19, so ANY Command Station that supports Ops Mode Programming can write
 * this address to a Command Station that supports it.
 *
 * @author Paul Bender Copyright (C) 2003-2008
 */
public class DccConsist implements Consist, ProgListener {

    protected ArrayList<DccLocoAddress> consistList = null; // A List of Addresses in the consist
    protected HashMap<DccLocoAddress, Boolean> consistDir = null; // A Hash table
    // containing the directions of
    // each locomotive in the consist,
    // keyed by Loco Address.
    protected HashMap<DccLocoAddress, Integer> consistPosition = null; // A Hash table
    // containing the position of
    // each locomotive in the consist,
    // keyed by Loco Address.
    protected HashMap<DccLocoAddress, String> consistRoster = null; // A Hash table
    // containing the Roster Identifier of
    // each locomotive in the consist,
    // keyed by Loco Address.
    protected int consistType = ADVANCED_CONSIST;
    protected DccLocoAddress consistAddress = null;
    protected String consistID = null;
    // data member to hold the throttle listener objects
    final private ArrayList<ConsistListener> listeners;

    // Initialize a consist for the specific address.
    // In this implementation, we can safely assume the address is a
    // short address, since Advanced Consisting is only possible with
    // a short address.
    // The Default consist type is an advanced consist
    public DccConsist(int address) {
        this(new DccLocoAddress(address, false));
    }

    // Initialize a consist for a specific DccLocoAddress.
    // The Default consist type is an advanced consist
    public DccConsist(DccLocoAddress address) {
        this.listeners = new ArrayList<>();
        consistAddress = address;
        consistDir = new HashMap<>();
        consistList = new ArrayList<>();
        consistPosition = new HashMap<>();
        consistRoster = new HashMap<>();
        consistID = consistAddress.toString();
    }

    // Clean Up local Storage.
    @Override
    public void dispose() {
        if (consistList == null) {
            // already disposed;
            return;
        }
        for (int i = (consistList.size() - 1); i >= 0; i--) {
            DccLocoAddress loco = consistList.get(i);
            if (log.isDebugEnabled()) {
                log.debug("Deleting Locomotive: " + loco.toString());
            }
            try {
                remove(loco);
            } catch (Exception ex) {
                log.error("Error removing loco: {} from consist: {}", loco, consistAddress);
            }
        }
        consistList = null;
        consistDir = null;
        consistPosition = null;
        consistRoster = null;
    }

    // Set the Consist Type
    @Override
    public void setConsistType(int consist_type) {
        if (consist_type == ADVANCED_CONSIST) {
            consistType = consist_type;
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(new DccLocoAddress(0, false), ConsistListener.NotImplemented);
        }
    }

    // get the Consist Type
    @Override
    public int getConsistType() {
        return consistType;
    }

    // get the Consist Address
    @Override
    public DccLocoAddress getConsistAddress() {
        return consistAddress;
    }

    /* is this address allowed?
     * Since address 00 is an analog locomotive, we can't program CV19
     * to include it in a consist, but all other addresses are ok.
     */
    @Override
    public boolean isAddressAllowed(DccLocoAddress address) {
        if (address.getNumber() != 0) {
            return (true);
        } else {
            return (false);
        }
    }

    /* is there a size limit for this consist?
     * For Decoder Assisted Consists, returns -1 (no limit)
     * return 0 for any other consist type.
     */
    @Override
    public int sizeLimit() {
        if (consistType == ADVANCED_CONSIST) {
            return -1;
        } else {
            return 0;
        }
    }

    // get a list of the locomotives in the consist
    @Override
    public ArrayList<DccLocoAddress> getConsistList() {
        return consistList;
    }

    // does the consist contain the specified address?
    @Override
    public boolean contains(DccLocoAddress address) {
        if (consistType == ADVANCED_CONSIST) {
            //String Address = Integer.toString(address);
            return (consistList.contains(address));
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(new DccLocoAddress(0, false), ConsistListener.NotImplemented);
        }
        return false;
    }

    // get the relative direction setting for a specific
    // locomotive in the consist
    @Override
    public boolean getLocoDirection(DccLocoAddress address) {
        if (consistType == ADVANCED_CONSIST) {
            //String Address= Integer.toString(address);
            Boolean Direction = consistDir.get(address);
            return (Direction);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(address, ConsistListener.NotImplemented);
        }
        return false;
    }

    /*
     * Add a Locomotive to an Advanced Consist
     *  @param address is the Locomotive address to add to the locomotive
     *  @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    public void add(DccLocoAddress LocoAddress, boolean directionNormal) {
        if (consistType == ADVANCED_CONSIST) {
            //String Address= Integer.toString(LocoAddress);
            Boolean Direction = directionNormal;
            if (!(consistList.contains(LocoAddress))) {
                consistList.add(LocoAddress);
            }
            consistDir.put(LocoAddress, Direction);
            addToAdvancedConsist(LocoAddress, directionNormal);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(LocoAddress, ConsistListener.NotImplemented);
        }
    }

    /*
     * Restore a Locomotive to an Advanced Consist, but don't write to
     * the command station.  This is used for restoring the consist
     * from a file or adding a consist read from the command station.
     *  @param address is the Locomotive address to add to the locomotive
     *  @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    @Override
    public void restore(DccLocoAddress LocoAddress, boolean directionNormal) {
        if (consistType == ADVANCED_CONSIST) {
            //String Address= Integer.toString(LocoAddress);
            Boolean Direction = directionNormal;
            if (!(consistList.contains(LocoAddress))) {
                consistList.add(LocoAddress);
            }
            consistDir.put(LocoAddress, Direction);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(LocoAddress, ConsistListener.NotImplemented);
        }
    }

    /*
     *  Remove a Locomotive from this Consist
     *  @param address is the Locomotive address to add to the locomotive
     */
    @Override
    public void remove(DccLocoAddress LocoAddress) {
        if (consistType == ADVANCED_CONSIST) {
            //String Address= Integer.toString(LocoAddress);
            consistDir.remove(LocoAddress);
            consistList.remove(LocoAddress);
            consistPosition.remove(LocoAddress);
            consistRoster.remove(LocoAddress);
            removeFromAdvancedConsist(LocoAddress);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(LocoAddress, ConsistListener.NotImplemented);
        }
    }


    /*
     *  Add a Locomotive to an Advanced Consist
     *  @param address is the Locomotive address to add to the locomotive
     *  @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    protected void addToAdvancedConsist(DccLocoAddress LocoAddress, boolean directionNormal) {
        AddressedProgrammer opsProg = InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                .getAddressedProgrammer(LocoAddress.isLongAddress(),
                        LocoAddress.getNumber());
        if (opsProg == null) {
            log.error("Can't make consisting change because no programmer exists; this is probably a configuration error in the preferences");
            return;
        }

        if (directionNormal) {
            try {
                opsProg.writeCV(19, consistAddress.getNumber(), this);
            } catch (ProgrammerException e) {
                // Don't do anything with this yet
                log.warn("Exception writing CV19 while adding from consist", e);
            }
        } else {
            try {
                opsProg.writeCV(19, consistAddress.getNumber() + 128, this);
            } catch (ProgrammerException e) {
                // Don't do anything with this yet
                log.warn("Exception writing CV19 while adding to consist", e);
            }
        }

        InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                .releaseAddressedProgrammer(opsProg);
    }

    /*
     *  Remove a Locomotive from an Advanced Consist
     *  @param address is the Locomotive address to remove from the consist
     */
    protected void removeFromAdvancedConsist(DccLocoAddress LocoAddress) {
        AddressedProgrammer opsProg = InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                .getAddressedProgrammer(LocoAddress.isLongAddress(),
                        LocoAddress.getNumber());
        if (opsProg == null) {
            log.error("Can't make consisting change because no programmer exists; this is probably a configuration error in the preferences");
            return;
        }

        try {
            opsProg.writeCV(19, 0, this);
        } catch (ProgrammerException e) {
            // Don't do anything with this yet
            log.warn("Exception writing CV19 while removing from consist", e);
        }

        InstanceManager.getDefault(jmri.AddressedProgrammerManager.class)
                .releaseAddressedProgrammer(opsProg);
    }

    /*
     *  Set the position of a locomotive within the consist
     *  @param address is the Locomotive address
     *  @param position is a constant representing the position within
     *         the consist.
     */
    @Override
    public void setPosition(DccLocoAddress address, int position) {
        consistPosition.put(address, position);
    }

    /*
     * Get the position of a locomotive within the consist
     * @param address is the Locomotive address of interest
     */
    @Override
    public int getPosition(DccLocoAddress address) {
        if (consistPosition.containsKey(address)) {
            return (consistPosition.get(address));
        }
        // if the consist order hasn't been set, we'll use default
        // positioning based on index in the arraylist.  Lead locomotive
        // is position 0 in the list and the trail is the last locomtoive
        // in the list.
        int index = consistList.indexOf(address);
        if (index == 0) {
            return (Consist.POSITION_LEAD);
        } else if (index == (consistList.size() - 1)) {
            return (Consist.POSITION_TRAIL);
        } else {
            return index;
        }
    }

    /**
     * Set the roster entry of a locomotive within the consist
     *
     * @param address  is the Locomotive address
     * @param rosterId is the roster Identifer of the associated roster entry.
     */
    @Override
    public void setRosterId(DccLocoAddress address, String rosterId) {
        consistRoster.put(address, rosterId);
    }

    /**
     * Get the rosterId of a locomotive within the consist
     *
     * @param address is the Locomotive address of interest
     * @return string roster Identifier associated with the given address in the
     *         consist. Returns null if no roster entry is associated with this
     *         entry.
     */
    @Override
    public String getRosterId(DccLocoAddress address) {
        if (consistRoster.containsKey(address)) {
            return (consistRoster.get(address));
        } else {
            return null;
        }
    }

    /*
     * Add a Listener for consist events
     * @param Listener is a consistListener object
     */
    @Override
    public void addConsistListener(ConsistListener Listener) {
        if (!listeners.contains(Listener)) {
            listeners.add(Listener);
        }
    }

    /*
     * Remove a Listener for consist events
     * @param Listener is a consistListener object
     */
    @Override
    public void removeConsistListener(ConsistListener Listener) {
        if (listeners.contains(Listener)) {
            listeners.remove(Listener);
        }
    }

    // Get and set the
    /*
     * Set the text ID associated with the consist
     * @param String is a string identifier for the consist
     */
    @Override
    public void setConsistID(String ID) {
        consistID = ID;
    }

    /*
     * Get the text ID associated with the consist
     * @return String identifier for the consist
     *         default value is the string Identifier for the
     *         consist address.
     */
    @Override
    public String getConsistID() {
        return consistID;
    }

    /*
     * Reverse the order of locomotives in the consist and flip
     * the direction bits of each locomotive.
     */
    @Override
    public void reverse() {
        // save the old lead locomotive direction.
        Boolean oldDir = consistDir.get(consistList.get(0));
        // reverse the direction of the list
        java.util.Collections.reverse(consistList);
        // and then save the new lead locomotive direction
        Boolean newDir = consistDir.get(consistList.get(0));
        // and itterate through the list to reverse the directions of the
        // individual elements of the list.
        java.util.Iterator<DccLocoAddress> i = consistList.iterator();
        while (i.hasNext()) {
            DccLocoAddress locoaddress = i.next();
            if (oldDir.equals(newDir)) {
                add(locoaddress, getLocoDirection(locoaddress));
            } else {
                add(locoaddress, !getLocoDirection(locoaddress));
            }
            if (consistPosition.containsKey(locoaddress)) {
                switch (getPosition(locoaddress)) {
                    case Consist.POSITION_LEAD:
                        setPosition(locoaddress, Consist.POSITION_TRAIL);
                        break;
                    case Consist.POSITION_TRAIL:
                        setPosition(locoaddress, Consist.POSITION_LEAD);
                        break;
                    default:
                        setPosition(locoaddress, consistList.size() - getPosition(locoaddress));
                        break;
                }
            }
        }
        // notify any listeners that the consist changed
        this.notifyConsistListeners(consistAddress, ConsistListener.OK);
    }

    /*
     * Restore the consist to the command station.
     */
    @Override
    public void restore() {
        // itterate through the list to re-add the addresses to the
        // command station.
        java.util.Iterator<DccLocoAddress> i = consistList.iterator();
        while (i.hasNext()) {
            DccLocoAddress locoaddress = i.next();
            add(locoaddress, getLocoDirection(locoaddress));
        }
        // notify any listeners that the consist changed
        this.notifyConsistListeners(consistAddress, ConsistListener.OK);
    }

    /*
     * Notify all listener objects of a status change.
     * @param LocoAddress is the address of any specific locomotive the
     *       status refers to.
     * @param ErrorCode is the status code to send to the
     *       consistListener objects
     */
    @SuppressWarnings("unchecked")
    protected void notifyConsistListeners(DccLocoAddress LocoAddress, int ErrorCode) {
        // make a copy of the listener vector to notify.
        ArrayList<ConsistListener> v;
        synchronized (this) {
            v = (ArrayList<ConsistListener>) listeners.clone();
        }
        log.debug("Sending Status code: {} to {} listeners for Address {}",
                ErrorCode,
                v.size(), LocoAddress);
        // forward to all listeners
        v.forEach((client) -> {
            client.consistReply(LocoAddress, ErrorCode);
        });
    }

    // This class is to be registered as a programmer listener, so we
    // include the programmingOpReply() function
    @Override
    public void programmingOpReply(int value, int status) {
        log.debug("Programming Operation reply recieved, value is {}, status is {}", value, status);
        notifyConsistListeners(new DccLocoAddress(0, false), ConsistListener.OPERATION_SUCCESS);
    }

    private final static Logger log = LoggerFactory.getLogger(DccConsist.class);
}
