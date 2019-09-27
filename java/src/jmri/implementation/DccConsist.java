package jmri.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jmri.AddressedProgrammer;
import jmri.AddressedProgrammerManager;
import jmri.Consist;
import jmri.ConsistListener;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.VariableTableModel;
import org.jdom2.*;
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
    private final ArrayList<ConsistListener> listeners;


    private AddressedProgrammerManager opsProgManager = null;

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
        this(address,jmri.InstanceManager.getDefault(AddressedProgrammerManager.class));
    }

    // Initialize a consist for a specific DccLocoAddress.
    // The Default consist type is an advanced consist
    public DccConsist(DccLocoAddress address,AddressedProgrammerManager apm) {
        opsProgManager = apm;
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
            return;
        }
        for (int i = (consistList.size() - 1); i >= 0; i--) {
            DccLocoAddress loco = consistList.get(i);
            if (log.isDebugEnabled()) {
                log.debug("Deleting Locomotive: {}",loco);
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
            notifyUnsupportedConsistType();
        }
    }

    private void notifyUnsupportedConsistType(){
        log.error("Consist Type Not Supported");
        notifyConsistListeners(new DccLocoAddress(0, false), ConsistListener.NotImplemented);
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
            return (consistList.contains(address));
        } else {
            notifyUnsupportedConsistType();
        }
        return false;
    }

    // get the relative direction setting for a specific
    // locomotive in the consist
    @Override
    public boolean getLocoDirection(DccLocoAddress address) {
        if (consistType == ADVANCED_CONSIST) {
            Boolean Direction = consistDir.get(address);
            return (Direction);
        } else {
            notifyUnsupportedConsistType();
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
            Boolean Direction = directionNormal;
            if (!(consistList.contains(LocoAddress))) {
                consistList.add(LocoAddress);
            }
            consistDir.put(LocoAddress, Direction);
            addToAdvancedConsist(LocoAddress, directionNormal);
            //set the value in the roster entry for CV19
            setRosterEntryCVValue(LocoAddress);
        } else {
            notifyUnsupportedConsistType();
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
            Boolean Direction = directionNormal;
            if (!(consistList.contains(LocoAddress))) {
                consistList.add(LocoAddress);
            }
            consistDir.put(LocoAddress, Direction);
        } else {
            notifyUnsupportedConsistType();
        }
    }

    /*
     *  Remove a Locomotive from this Consist
     *  @param address is the Locomotive address to add to the locomotive
     */
    @Override
    public void remove(DccLocoAddress LocoAddress) {
        if (consistType == ADVANCED_CONSIST) {
            //reset the value in the roster entry for CV19
            resetRosterEntryCVValue(LocoAddress);
            consistDir.remove(LocoAddress);
            consistList.remove(LocoAddress);
            consistPosition.remove(LocoAddress);
            consistRoster.remove(LocoAddress);
            removeFromAdvancedConsist(LocoAddress);
        } else {
            notifyUnsupportedConsistType();
        }
    }


    /*
     *  Add a Locomotive to an Advanced Consist
     *  @param address is the Locomotive address to add to the locomotive
     *  @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    protected void addToAdvancedConsist(DccLocoAddress LocoAddress, boolean directionNormal) {
        AddressedProgrammer opsProg = opsProgManager 
                .getAddressedProgrammer(LocoAddress.isLongAddress(),
                        LocoAddress.getNumber());
        if (opsProg == null) {
            log.error("Can't make consisting change because no programmer exists; this is probably a configuration error in the preferences");
            return;
        }

        if (directionNormal) {
            try {
                opsProg.writeCV("19", consistAddress.getNumber(), this);
            } catch (ProgrammerException e) {
                // Don't do anything with this yet
                log.warn("Exception writing CV19 while adding from consist", e);
            }
        } else {
            try {
                opsProg.writeCV("19", consistAddress.getNumber() + 128, this);
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
            opsProg.writeCV("19", 0, this);
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
        if (consistType == ADVANCED_CONSIST) {
            //set the value in the roster entry for CV19
            setRosterEntryCVValue(address);
        } 
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
            
   /**
    * Update the value in the roster entry for CV19 for the specified
    * address
    *
    * @param address is the Locomotive address we are updating.
    */
   protected void setRosterEntryCVValue(DccLocoAddress address){
      updateRosterCV(address,getLocoDirection(address),this.consistAddress.getNumber());
   }

   /**
    * Set the value in the roster entry's value for for CV19 to 0
    *
    * @param address is the Locomotive address we are updating.
    */
   protected void resetRosterEntryCVValue(DccLocoAddress address){
      updateRosterCV(address,getLocoDirection(address),0);
   }

   /**
    * If allowed by the preferences, Update the CV19 value in the 
    * specified address's roster entry, if the roster entry is known.
    *
    * @param address is the Locomotive address we are updating.
    * @param direction the direction to set.
    * @param value the numeric value of the consist address. 
    */
   protected void updateRosterCV(DccLocoAddress address,Boolean direction,int value){
        if(!InstanceManager.getDefault(ConsistPreferencesManager.class).isUpdateCV19()){
           log.trace("Consist Manager updates of CV19 are disabled in preferences");
           return;
        }
        if(getRosterId(address)==null){
           // roster entry unknown.
           log.trace("No RosterID for address {} in consist {}.  Skipping CV19 update.",address,consistAddress);
           return;
        }
        RosterEntry entry = Roster.getDefault().getEntryForId(getRosterId(address));

        if(entry==null || entry.getFileName()==null || entry.getFileName().equals("")){
           // roster entry unknown.
           log.trace("No file name available for RosterID {},address {}, in consist {}.  Skipping CV19 update.",getRosterId(address),address,consistAddress);
           return;
        }
        CvTableModel  cvTable = new CvTableModel(null, null);  // will hold CV objects
        VariableTableModel varTable = new VariableTableModel(null,new String[]{"Name","Value"},cvTable);
        entry.readFile();  // read, but don’t yet process

        // load from decoder file
        loadDecoderFromLoco(entry,varTable);

        entry.loadCvModel(varTable, cvTable);
        CvValue cv19Value = cvTable.getCvByNumber("19");
        cv19Value.setValue((value & 0xff) | (direction.booleanValue()?0x00:0x80 ));

        entry.writeFile(cvTable,varTable);
   }

    // copied from PaneProgFrame
    protected void loadDecoderFromLoco(RosterEntry r,VariableTableModel varTable) {
        // get a DecoderFile from the locomotive xml
        String decoderModel = r.getDecoderModel();
        String decoderFamily = r.getDecoderFamily();
        if (log.isDebugEnabled()) {
            log.debug("selected loco uses decoder {} {}",decoderFamily,decoderModel);
        }
        // locate a decoder like that.
        List<DecoderFile> l = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, decoderFamily, null, null, null, decoderModel);
        if (log.isDebugEnabled()) {
            log.debug("found {} matches",l.size());
        }
        if (l.isEmpty()) {
            log.debug("Loco uses {} {} decoder, but no such decoder defined",decoderFamily,decoderModel );
            // fall back to use just the decoder name, not family
            l = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, null, null, null, decoderModel);
            if (log.isDebugEnabled()) {
                log.debug("found {} matches without family key",l.size());
            }
        }
        if (!l.isEmpty()) {
            DecoderFile d = l.get(0);
            loadDecoderFile(d, r, varTable);
        } else {
            if (decoderModel.equals("")) {
                log.debug("blank decoderModel requested, so nothing loaded");
            } else {
                log.warn("no matching \"{}\" decoder found for loco, no decoder info loaded",decoderModel );
            }
        }
    }

    protected void loadDecoderFile(DecoderFile df, RosterEntry re,VariableTableModel variableModel) {
        if (df == null) {
            log.warn("loadDecoder file invoked with null object");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("loadDecoderFile from " + DecoderFile.fileLocation
                    + " " + df.getFileName());
        }

        Element decoderRoot = null;

        try {
            decoderRoot = df.rootFromName(DecoderFile.fileLocation + df.getFileName());
        } catch (JDOMException | IOException e) {
            log.error("Exception while loading decoder XML file: " + df.getFileName(), e);
        }
        // load variables from decoder tree
        df.getProductID();
        if(decoderRoot!=null) {
           df.loadVariableModel(decoderRoot.getChild("decoder"), variableModel);
           // load function names
           re.loadFunctions(decoderRoot.getChild("decoder").getChild("family").getChild("functionlabels"));
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
        v.forEach(client -> {
            client.consistReply(LocoAddress, ErrorCode);
        });
    }

    // This class is to be registered as a programmer listener, so we
    // include the programmingOpReply() function
    @Override
    public void programmingOpReply(int value, int status) {
        log.debug("Programming Operation reply received, value is {}, status is {}", value, status);
        notifyConsistListeners(new DccLocoAddress(0, false), ConsistListener.OPERATION_SUCCESS);
    }

    private static final  Logger log = LoggerFactory.getLogger(DccConsist.class);
}
