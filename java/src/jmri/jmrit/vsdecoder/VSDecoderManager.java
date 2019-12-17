package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import jmri.Block;
import jmri.IdTag;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.Manager;
import jmri.NamedBean;
import jmri.PhysicalLocationReporter;
import jmri.Reporter;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.vsdecoder.VSDConfig;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.jmrit.vsdecoder.listener.VSDListener;
import jmri.jmrit.vsdecoder.swing.VSDManagerFrame;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.PhysicalLocation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GraphicsEnvironment;
import javax.swing.Timer;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VSDecoderFactory
 *
 * Builds VSDecoders as needed.  Handles loading from XML if needed.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author Mark Underwood Copyright (C) 2011
 * @author Klaus Killinger Copyright (C) 2018
 */
public class VSDecoderManager implements PropertyChangeListener {

    //private static final ResourceBundle rb = VSDecoderBundle.bundle();
    private static final String vsd_property_change_name = "VSDecoder Manager"; //NOI18N

    // Array-pointer for blockParameter
    private static final int radius = 0;
    private static final int slope = 1;
    private static final int rotate_xpos_i = 2;
    private static final int rotate_ypos_i = 3;
    private static final int length = 4;

    // Array-pointer for locoInBlock
    private static final int address = 0;
    private static final int block = 1;
    private static final int distance_to_go = 2;
    private static final int direction = 3;

    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    HashMap<String, VSDListener> listenerTable; // list of listeners
    HashMap<String, VSDecoder> decodertable; // list of active decoders by System ID
    HashMap<String, VSDecoder> decoderAddressMap; // List of active decoders by address
    HashMap<Integer, VSDecoder> decoderInBlock; // list of active decoders by LocoAddress.getNumber()
    HashMap<String, String> profiletable;    // list of loaded profiles key = profile name, value = path
    private HashMap<String, Timer> timertable; // list of active timers by decoder System ID

    private int locoInBlock[][]; // Block status for locos
    private float blockParameter[][][];
    private List<List<PhysicalLocation>> blockPositionlists;
    private List<List<Integer>> reporterlists;
    private List<Boolean> circlelist;
    private PhysicalLocation newPosition;
    private PhysicalLocation posToSet;

    // List of registered event listeners
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    //private static VSDecoderManager instance = null;   // sole instance of this class
    private volatile static VSDecoderManagerThread thread = null; // thread for running the manager

    private VSDecoderPreferences vsdecoderPrefs; // local pointer to the preferences object

    private JmriJFrame managerFrame = null;

    private VSDecoder default_decoder = null;  // shortcut pointer to the default decoder (do we need this?)

    private static int vsdecoderID = 0;
    //private static int listenerID = 0; // for future use
    private int locorow = -1; // Will be increased before first use

    private float speed_ms = 0.0f; // Speed in meters per second
    private int check_time; // Time interval in ms for track following updates
    private float layout_scale;
    private float distance = 0.0f; // Loco running distance in meters
    private float distance_rest = 0.0f; // Block distance to go
    private float distance_rest_old = 0.0f; // Block distance to go, copy
    private float distance_rest_new = 0.0f; // Block distance to go, copy

    private float xPosi;
    public static final int max_decoder = 4; // For now only four locos allowed (arbitrary)
    private int remove_index;
    boolean is_tunnel = false;
    boolean geofile_ok = false;
    int num_setups;

    // Unused?
    //private PhysicalLocation listener_position;
    // constructor - for kicking off by the VSDecoderManagerThread...
    // WARNING: Should only be called from static instance()
    public VSDecoderManager() {
        // Setup the decoder table
        listenerTable = new HashMap<>();
        decodertable = new HashMap<>();
        decoderAddressMap = new HashMap<>();
        timertable = new HashMap<>();
        decoderInBlock = new HashMap<>(); // Key = decoder number
        profiletable = new HashMap<>(); // key = profile name, value = path
        locoInBlock = new int[max_decoder][4]; // Loco address number, current block, distance in cm to go in block, dirfn
        // Setup lists
        reporterlists = new ArrayList<>();
        blockPositionlists = new ArrayList<>();
        circlelist = new ArrayList<>();
        // Get preferences
        String dirname = FileUtil.getUserFilesPath() + "vsdecoder" + File.separator; // NOI18N
        FileUtil.createDirectory(dirname);
        vsdecoderPrefs = new VSDecoderPreferences(dirname + VSDecoderPreferences.VSDPreferencesFileName);
        // Listen to ReporterManager for Report List changes
        setupReporterManagerListener();
        // Get a Listener (the only one for now)
        //VSDListener t = new VSDListener(getNextListenerID());
        VSDListener t = new VSDListener();
        listenerTable.put(t.getSystemName(), t);
        // Update JMRI "Default Audio Listener"
        setListenerLocation(t.getSystemName(), vsdecoderPrefs.getListenerPosition());
        // Look for additional layout geometry data
        VSDGeoFile gf = new VSDGeoFile();
        if (gf.geofile_ok) {
            geofile_ok = true;
            num_setups = gf.getNumberOfSetups();
            reporterlists = gf.getReporterList();
            blockParameter = gf.getBlockParameter();
            blockPositionlists = gf.getBlockPosition();
            circlelist = gf.getCirclingList();
            check_time = gf.check_time;
            layout_scale = gf.layout_scale;
        } else {
            geofile_ok = false;
        }
    }

    public static VSDecoderManager instance() {
        if (thread == null) {
            thread = VSDecoderManagerThread.instance(true);
        }
        return VSDecoderManagerThread.manager();
    }

    public VSDecoderPreferences getVSDecoderPreferences() {
        return vsdecoderPrefs;
    }

    public JmriJFrame provideManagerFrame() {
        if (managerFrame == null) {
            if (GraphicsEnvironment.isHeadless()) {
                String vsdRosterGroup = "VSD";
                if (Roster.getDefault().getRosterGroupList().contains(vsdRosterGroup)) {
                    List<RosterEntry> rosterList;
                    rosterList = Roster.getDefault().getEntriesInGroup(vsdRosterGroup);
                    // Allow <max_decoder> roster entries
                    int entry_counter = 0;
                    for (RosterEntry entry : rosterList) {
                        if (entry_counter < max_decoder) {
                            VSDConfig config = new VSDConfig();
                            config.setLocoAddress(entry.getDccLocoAddress());
                            log.info("Loading Roster \"{}\", VSDecoder {} ...", entry.getId(), config.getLocoAddress());
                            if (entry.getAttribute("VSDecoder_Path") != null && entry.getAttribute("VSDecoder_Profile") != null) {
                                if (LoadVSDFileAction.loadVSDFile(entry.getAttribute("VSDecoder_Path"))) {
                                    // config.xml OK
                                    log.info(" VSD path: {}", entry.getAttribute("VSDecoder_Path"));
                                    config.setProfileName(entry.getAttribute("VSDecoder_Profile"));
                                    log.debug(" entry VSD profile: {}", entry.getAttribute("VSDecoder_Profile"));
                                    VSDecoder newDecoder = VSDecoderManager.instance().getVSDecoder(config);
                                    if (newDecoder != null) {
                                        log.info("VSD {}, profile \"{}\" ready.", config.getLocoAddress(), config.getProfileName());
                                        entry_counter++;
                                    } else {
                                        log.warn("VSD {} failed", config.getProfileName());
                                    }
                                }
                            } else {
                                log.error("Cannot load VSD File - path or profile missing - check your Roster Media");
                            }
                        } else {
                            log.warn("Only {} roster entries allowed. Disgarded {}", max_decoder, rosterList.size() - max_decoder);
                        }
                    }
                    if (entry_counter == 0) {
                        log.warn("No Roster entry found in Roster Group {}", vsdRosterGroup);
                    }
                } else {
                    log.warn("Roster group \"{}\" not found", vsdRosterGroup);
                }
            } else {
                // Run VSDecoder with GUI
                managerFrame = new VSDManagerFrame();
            }
        } else {
            log.warn("Virtual Sound Decoder Manager is already running");
        }
        return managerFrame;
    }

    private String getNextVSDecoderID() {
        // vsdecoderID initialized to zero, pre-incremented before return...
        // first returned ID value is 1.
        return "IAD:VSD:VSDecoderID" + (++vsdecoderID); // NOI18N
    }

    private Integer getNextlocorow() {
        // locorow initialized to -1, pre-incremented before return...
        // first returned value is 0.
        return ++locorow;
    }

    // To be used in the future
    /*
     private String getNextListenerID() {
     // ListenerID initialized to zero, pre-incremented before return...
     // first returned ID value is 1.
     // Prefix is added by the VSDListener constructor
     return "VSDecoderID" + (++listenerID); // NOI18N
     }
     */

    @Deprecated
    public VSDecoder getVSDecoder(String profile_name) {
        VSDecoder vsd;
        String path;
        if (profiletable.containsKey(profile_name)) {
            path = profiletable.get(profile_name);
            log.debug("Profile {} is in table.  Path: {}", profile_name, path);
            vsd = new VSDecoder(getNextVSDecoderID(), profile_name, path);
            decodertable.put(vsd.getId(), vsd);  // poss. broken for duplicate profile names
            decoderAddressMap.put(vsd.getAddress().toString(), vsd);
            return vsd;
        } else {
            // Don't have enough info to try to load from file.
            log.error("Requested profile not loaded: {}", profile_name);
            return null;
        }
    }

    public VSDecoder getVSDecoder(String profile_name, String path) {
        VSDecoder vsd = new VSDecoder(getNextVSDecoderID(), profile_name, path);
        decodertable.put(vsd.getId(), vsd); // poss. broken for duplicate profile names
        if (vsd.getAddress() != null) {
            decoderAddressMap.put(vsd.getAddress().toString(), vsd);
        }
        return vsd;
    }

    /**
     * Provide or build a VSDecoder based on a provided configuration
     */
    public VSDecoder getVSDecoder(VSDConfig config) {
        String path;
        String profile_name = config.getProfileName();
        // First, check to see if we already have a VSDecoder on this Address
        //debugPrintDecoderList();
        if (decoderAddressMap.containsKey(config.getLocoAddress().toString())) {
            return decoderAddressMap.get(config.getLocoAddress().toString());
        }
        if (profiletable.containsKey(profile_name)) {
            path = profiletable.get(profile_name);
            log.debug("Profile {} is in table.  Path: {}", profile_name, path);

            if (!(locorow < max_decoder - 1)) {
                log.warn("VSDecoder not created. Maximal number is {}", max_decoder);
                return null;
            }

            config.setVSDPath(path);
            config.setId(getNextVSDecoderID());
            VSDecoder vsd = new VSDecoder(config);
            decodertable.put(vsd.getId(), vsd);
            decoderAddressMap.put(vsd.getAddress().toString(), vsd);
            decoderInBlock.put(vsd.getAddress().getNumber(), vsd);
            locoInBlock[getNextlocorow()][address] = vsd.getAddress().getNumber();
            //debugPrintDecoderList();
            if (geofile_ok) {
                if (vsd.topspeed == 0) {
                    log.info("Top-speed not defined. No advanced location following possible.");
                } else {
                    initSoundPositionTimer(vsd);
                }
            }
            return vsd;
        } else {
            // Don't have enough info to try to load from file.
            log.error("Requested profile not loaded: {}", profile_name);
            return null;
        }
    }

    /*
     public void debugPrintDecoderList() {
     log.debug("Current Decoder List by System ID:");
     Set<Map.Entry<String, VSDecoder>> ids = decodertable.entrySet();
     Iterator<Map.Entry<String, VSDecoder>> idi = ids.iterator();
     while (idi.hasNext()) {
     Map.Entry<String, VSDecoder> e = idi.next();
     log.debug("    ID: {}, Val: {}", e.getKey(), e.getValue().getAddress().toString());
     }
     log.debug("Current Decoder List by Address:");
     ids = decoderAddressMap.entrySet();
     idi = ids.iterator();
     while (idi.hasNext()) {
     Map.Entry<String, VSDecoder> e = idi.next();
     log.debug("    ID: {}, Val: {}", e.getKey(), e.getValue().getId());
     }
     }
     */
    public VSDecoder getVSDecoderByID(String id) {
        VSDecoder v = decodertable.get(id);
        if (v == null) {
            log.debug("No decoder in table! ID: {}", id);
        }
        return decodertable.get(id);
    }

    public VSDecoder getVSDecoderByAddress(String sa) {
        if (sa == null) {
            log.debug("Decoder Address is Null");
            return null;
        }
        log.debug("Decoder Address: {}", sa);
        VSDecoder rv = decoderAddressMap.get(sa);
        if (rv == null) {
            log.debug("Not found.");
        } else {
            log.debug("Found: {}", rv.getAddress());
        }
        return rv;
    }

    /*
     public VSDecoder getVSDecoderByAddress(String sa) {
     // First, translate the string into a DccLocoAddress
     // no object if no address
     if (sa.equals("")) return null;
        
     DccLocoAddress da = null;
     // ask the Throttle Manager to handle this!
     LocoAddress.Protocol protocol;
     if(InstanceManager.throttleManagerInstance()!=null){
     protocol = InstanceManager.throttleManagerInstance().getProtocolFromString(sa);
     da = (DccLocoAddress)InstanceManager.throttleManagerInstance().getAddress(sa, protocol);
     }

     // now look up the decoder
     if (da != null) {
     return getVSDecoderByAddress(da);
     }
     return(null);
 
     }
     */
    public void setDefaultVSDecoder(VSDecoder d) {
        default_decoder = d;
    }

    public VSDecoder getDefaultVSDecoder() {
        return default_decoder;
    }

    public ArrayList<String> getVSDProfileNames() {
        ArrayList<String> sl = new ArrayList<>();
        for (String p : profiletable.keySet()) {
            sl.add(p);
        }
        return sl;
    }

    public Collection<VSDecoder> getVSDecoderList() {
        return decodertable.values();
    }

    public String getDefaultListenerName() {
        return VSDListener.ListenerSysName;
    }

    public ListeningSpot getDefaultListenerLocation() {
        VSDListener l = listenerTable.get(getDefaultListenerName());
        if (l != null) {
            return l.getLocation();
        } else {
            return null;
        }
    }

    public void setListenerLocation(String id, ListeningSpot sp) {
        VSDListener l = listenerTable.get(id);
        log.debug("Set listener location {} listener: {}", sp, l);
        if (l != null) {
            l.setLocation(sp);
        }
    }

    public void setDecoderPositionByID(String id, PhysicalLocation p) {
        VSDecoder d = decodertable.get(id);
        if (d != null) {
            d.setPosition(p);
        }
    }

    public void setDecoderPositionByAddr(LocoAddress a, PhysicalLocation l) {
        // Find the addressed decoder
        // This is a bit hokey.  Need a better way to index decoder by address
        // OK, this whole LocoAddress vs. DccLocoAddress thing has rendered this SUPER HOKEY.
        if (a == null) {
            log.warn("Decoder Address is Null");
            return;
        }
        if (l == null) {
            log.warn("PhysicalLocation is Null");
            return;
        }
        if (l.equals(PhysicalLocation.Origin)) {
            log.info("Location: {} ... ignoring", l.toString());
            // Physical location at origin means it hasn't been set.
            return;
        }
        log.debug("Decoder Address: {}", a.getNumber());
        for (VSDecoder d : decodertable.values()) {
            // Get the Decoder's address protocol.  If it's a DCC_LONG or DCC_SHORT, convert to DCC
            // since the LnReporter can't tell the difference and will always report "DCC".
            if (d == null) {
                log.debug("VSdecoder null pointer!");
                return;
            }
            LocoAddress pa = d.getAddress();
            if (pa == null) {
                log.info("Vsdecoder {} address null!", d);
                return;
            }
            LocoAddress.Protocol p = d.getAddress().getProtocol();
            if (p == null) {
                log.debug("Vsdecoder {} address = {} protocol null!", d, pa);
                return;
            }
            if ((p == LocoAddress.Protocol.DCC_LONG) || (p == LocoAddress.Protocol.DCC_SHORT)) {
                p = LocoAddress.Protocol.DCC;
            }
            if ((d.getAddress().getNumber() == a.getNumber()) && (p == a.getProtocol())) {
                d.setPosition(l);
                // Loop through all the decoders (assumes N will be "small"), in case
                // there are multiple decoders with the same address.  This will be somewhat broken
                // if there's a DCC_SHORT and a DCC_LONG decoder with the same address number.
                //return;
            }
        }
        // decoder not found.  Do nothing.
        return;
    }

    // VSDecoderManager Events
    public void addEventListener(VSDManagerListener listener) {
        listenerList.add(VSDManagerListener.class, listener);
    }

    public void removeEventListener(VSDManagerListener listener) {
        listenerList.remove(VSDManagerListener.class, listener);
    }

    void fireMyEvent(VSDManagerEvent evt) {
        //Object[] listeners = listenerList.getListenerList();

        for (VSDManagerListener l : listenerList.getListeners(VSDManagerListener.class)) {
            l.eventAction(evt);
        }
    }

    /**
     * Retrieve the Path for a given Profile name.
     * 
     * @param profile the profile to get the path for
     * @return the path for the profile
     */
    public String getProfilePath(String profile) {
        return profiletable.get(profile);
    }

    protected void registerReporterListener(String sysName) {
        Reporter r = jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getReporter(sysName);
        if (r == null) {
            return;
        }
        jmri.NamedBeanHandle<Reporter> h = nbhm.getNamedBeanHandle(sysName, r);

        // Make sure we aren't already registered.
        java.beans.PropertyChangeListener[] ll = r.getPropertyChangeListenersByReference(h.getName());
        if (ll.length == 0) {
            r.addPropertyChangeListener(this, h.getName(), vsd_property_change_name);
        }
    }

    protected void registerBeanListener(Manager beanManager, String sysName) {
        NamedBean b = beanManager.getBeanBySystemName(sysName);
        if (b == null) {
            log.debug("No bean by name {}", sysName);
            return;
        }
        jmri.NamedBeanHandle<NamedBean> h = nbhm.getNamedBeanHandle(sysName, b);

        // Make sure we aren't already registered.
        java.beans.PropertyChangeListener[] ll = b.getPropertyChangeListenersByReference(h.getName());
        if (ll.length == 0) {
            b.addPropertyChangeListener(this, h.getName(), vsd_property_change_name);
            log.debug("Added listener to bean {} type {}", b.getDisplayName(), b.getClass().getName());
        }
    }

    protected void registerReporterListeners() {
        // Walk through the list of reporters
        Set<Reporter> reporterSet = jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getNamedBeanSet();
        for (Reporter r : reporterSet) {
            if (r != null) {
                registerReporterListener(r.getSystemName());
            }
        }

        Set<Block> blockSet = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getNamedBeanSet();
        for (Block b : blockSet) {
            if (b != null) {
                registerBeanListener(jmri.InstanceManager.getDefault(jmri.BlockManager.class), b.getSystemName());
            }
        }
    }

    // This listener listens to the ReporterManager for changes to the list of Reporters.
    // Need to trap list length (name="length") changes and add listeners when new ones are added.
    private void setupReporterManagerListener() {
        // Register ourselves as a listener for changes to the Reporter list.  For now, we won't do this. Just force a
        // save and reboot after reporters are added.  We'll fix this later.
        // jmri.InstanceManager.getDefault(jmri.ReporterManager.class).addPropertyChangeListener(new PropertyChangeListener() {
        // public void propertyChange(PropertyChangeEvent event) {
        //      log.debug("property change name {}, old: {}, new: {}", event.getPropertyName(), event.getOldValue(), event.getNewValue());
        //     reporterManagerPropertyChange(event);
        // }
        //   });
        jmri.InstanceManager.getDefault(jmri.ReporterManager.class).addPropertyChangeListener(this);

        // Now, the Reporter Table might already be loaded and filled out, so we need to get all the Reporters and list them.
        // And add ourselves as a listener to them.
        Set<Reporter> reporterSet = jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getNamedBeanSet();
        for (Reporter r : reporterSet) {
            if (r != null) {
                registerReporterListener(r.getSystemName());
            }
        } 

        Set<Block> blockSet = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getNamedBeanSet();
        for (Block b : blockSet) {
            if (b != null) {
                registerBeanListener(jmri.InstanceManager.getDefault(jmri.BlockManager.class), b.getSystemName());
            }
        }
    }

    protected void shutdownDecoders() {
        // Shut down and destroy all running VSDecoders.
        Set<String> vk = decodertable.keySet();
        Iterator<String> it = vk.iterator();
        while (it.hasNext()) {
            VSDecoder v = decodertable.get(it.next());
            v.shutdown();
            if (timertable.size() > 0) {
                stopSoundPositionTimer(v);
            }
        }
        // Empty the timertable
        timertable.clear();

        // Empty the DecoderTable
        decodertable.clear();
        /*
         vk = decodertable.keySet();
         it = vk.iterator();
         while(it.hasNext()) {
         decodertable.remove(it.next());
         }
         */
        // Empty the AddressMap
        decoderAddressMap.clear();
        /*
         vk = decoderAddressMap.keySet();
         it = vk.iterator();
         while(it.hasNext()) {
         decoderAddressMap.remove(it.next());
         }
         */
        decoderInBlock.clear();
        // Zeros to whole array
        for (int i = 0; i < locoInBlock.length; i++) {
            for (int k = 0; k < locoInBlock[i].length; k++) {
                locoInBlock[i][k] = 0;
            }
        }
        locorow = -1;
        //vsdecoderID = 0;
        log.debug("shutdown decoders done");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        log.debug("property change type {} name {} old {} new {}", 
                evt.getSource().getClass().getName(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        if (evt.getSource() instanceof jmri.ReporterManager) {
            reporterManagerPropertyChange(evt);
        } else if (evt.getSource() instanceof jmri.Reporter) {
            if (geofile_ok) {
                reporterPropertyChangeGeo(evt); // Advanced Location Following
            } else {
                reporterPropertyChange(evt); // Location Following
            }
        } else if (evt.getSource() instanceof jmri.Block) {
            log.debug("Block property change! name: {} old: {} new = {}", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            blockPropertyChange(evt);
        } else if (evt.getSource() instanceof VSDManagerFrame) {
            if (evt.getPropertyName().equals(VSDManagerFrame.PCIDMap.get(VSDManagerFrame.PropertyChangeID.REMOVE_DECODER))) {
                // Shut down the requested decoder and remove it from the manager's hash maps. 
                // Unless there are "illegal" handles, this should put the decoder on the garbage heap.  I think.
                String sa = (String) evt.getNewValue();
                VSDecoder d = this.getVSDecoderByAddress(sa);
                log.debug("Removing Decoder {} ... {}", sa, d.getAddress());
                stopSoundPositionTimer(d);
                d.shutdown();
                decodertable.remove(d.getId());
                decoderAddressMap.remove(sa);
                decoderInBlock.remove(d.getAddress().getNumber());
                locoInBlockRemove(d.getAddress().getNumber());
                timertable.remove(d.getId()); // Remove timer
                locorow--; // prepare array index for eventually adding a new decoder
                //debugPrintDecoderList();
            } else if (evt.getPropertyName().equals(VSDManagerFrame.PCIDMap.get(VSDManagerFrame.PropertyChangeID.CLOSE_WINDOW))) {
                // Note this assumes there is only one VSDManagerFrame open at a time.
                shutdownDecoders();
                if (managerFrame != null) {
                    managerFrame = null;
                }
            }
        } else {
            // Un-Handled source. Does nothing ... yet...
        }
        return;
    }

    public void blockPropertyChange(PropertyChangeEvent event) {
        // Needs to check the ID on the event, look up the appropriate VSDecoder,
        // get the location of the event source, and update the decoder's location.
        @SuppressWarnings("cast") // NOI18N
        String eventName = (String) event.getPropertyName();
        if (event.getSource() instanceof PhysicalLocationReporter) {
            Block blk = (Block) event.getSource();
            String repVal = null;
            // Depending on the type of Block Event, extract the needed report info from
            // the appropriate place...
            // "state" => Get loco address from Block's Reporter if present
            // "value" => Get loco address from event's newValue.
            if (eventName.equals("state")) { // NOI18N
                // Need to decide which reporter it is, so we can use different methods
                // to extract the address and the location.
                if ((Integer) event.getNewValue() == Block.OCCUPIED) {
                    // Get this Block's Reporter's current/last report value.  need to fix this - it could be
                    /// an idtag type reporter.
                    if (blk.getReporter() == null) {
                        log.debug("Block {} has no reporter!  Skipping state-type report", blk.getSystemName());
                        return;
                    }
                    if (blk.isReportingCurrent()) {
                        Object currentReport = blk.getReporter().getCurrentReport();
                        if ( currentReport != null) {
                           if(currentReport instanceof jmri.Reportable) {
                              repVal = ((jmri.Reportable)currentReport).toReportString();
                           } else {
                              repVal = currentReport.toString();
                           }
                        }
                    } else {
                        Object lastReport = blk.getReporter().getLastReport();
                        if ( lastReport != null) {
                           if(lastReport instanceof jmri.Reportable) {
                              repVal = ((jmri.Reportable)lastReport).toReportString();
                           } else {
                              repVal = lastReport.toString();
                           }
                        }
                    }
                } else {
                    log.debug("Ignoring report. not an OCCUPIED event.");
                    return;
                }
            } else if (eventName.equals("value")) {
                if (event.getNewValue() instanceof String) {
                    repVal = event.getNewValue().toString();
                }
                // Else it will still be null from the declaration/assignment above.
            } else {
                log.debug("Not a supported Block event type.  Ignoring.");
                return;
            }  // Type of eventName.
            // Set the decoder's position.
            if (repVal == null) {
                log.warn("Report from Block {} is null!", blk.getUserName());
            }
            if (blk.getDirection(repVal) == PhysicalLocationReporter.Direction.ENTER) {
                setDecoderPositionByAddr(blk.getLocoAddress(repVal), blk.getPhysicalLocation());
            }
            return;
        } else {
            log.debug("Reporter doesn't support physical location reporting.");
        }  // Reporting object implements PhysicalLocationReporter
        return;
    }

    public void reporterPropertyChange(PropertyChangeEvent event) {
        // Needs to check the ID on the event, look up the appropriate VSDecoder,
        // get the location of the event source, and update the decoder's location.
        @SuppressWarnings("cast") // NOI18N
        String eventName = (String) event.getPropertyName();
        if ((event.getSource() instanceof PhysicalLocationReporter) && (eventName.equals("currentReport"))) { // NOI18N
            PhysicalLocationReporter arp = (PhysicalLocationReporter) event.getSource();
            // Need to decide which reporter it is, so we can use different methods
            // to extract the address and the location.
            if (event.getNewValue() instanceof IdTag) {
                // RFID-tag, Digitrax Transponding tags, RailCom tags
                if (event.getNewValue() instanceof jmri.jmrix.loconet.TranspondingTag) {
                    String repVal = ((jmri.Reportable) event.getNewValue()).toReportString();
                    if (arp.getDirection(repVal) == PhysicalLocationReporter.Direction.ENTER) {
                        setDecoderPositionByAddr(arp.getLocoAddress(repVal), arp.getPhysicalLocation(repVal));
                    }
                } else {
                    // newValue is of IdTag type.
                    // Dcc4Pc, Ecos, 
                    // Assume Reporter "arp" is the most recent seen location
                    IdTag newValue = (IdTag) event.getNewValue();
                    setDecoderPositionByAddr(arp.getLocoAddress(newValue.getTagID()), arp.getPhysicalLocation(null));
                }
            } else {
                log.debug("Reporter's return type is not supported.");
                // do nothing
            }

        } else {
            log.debug("Reporter doesn't support physical location reporting or isn't reporting new info.");
        }  // Reporting object implements PhysicalLocationReporter
        return;
    }

    public void reporterPropertyChangeGeo(PropertyChangeEvent event) {
        // Needs to check the ID on the event, look up the appropriate VSDecoder,
        // get the location of the event source, and update the decoder's location.
        @SuppressWarnings("cast") // NOI18N
        String eventName = (String) event.getPropertyName();
        if ((event.getSource() instanceof PhysicalLocationReporter) && (eventName.equals("currentReport"))) { // NOI18N
            PhysicalLocationReporter arp = (PhysicalLocationReporter) event.getSource();
            // Need to decide which reporter it is, so we can use different methods
            // to extract the address and the location.
            if (event.getNewValue() instanceof IdTag) {
                // RFID-tag, Digitrax Transponding tags, RailCom tags
                if (event.getNewValue() instanceof jmri.jmrix.loconet.TranspondingTag) {
                    String repVal = ((jmri.Reportable) event.getNewValue()).toReportString();
                    LocoAddress xa = arp.getLocoAddress(repVal); // e.g. 1709(D)
                    log.debug("repVal: {}, xa: {}, number: {}", repVal, xa, xa.getNumber());
                    // 1) is loco address valid?
                    if (decoderInBlock.containsKey(xa.getNumber())) {
                        VSDecoder d = decoderInBlock.get(xa.getNumber());
                        Reporter rp = (Reporter) event.getSource();
                        int new_rp = Integer.parseInt(rp.getSystemName().substring(2));
                        // 2) Reporter must be valid for GeoData processing
                        //    use the current Reporter list as a filter (changeable by a Train selection)
                        if (reporterlists.get(d.setup_index).contains(new_rp)) {
                            if (arp.getDirection(repVal) == PhysicalLocationReporter.Direction.ENTER) {
                                // currentReport ENTER
                                // -------------------
                                int new_rp_index = reporterlists.get(d.setup_index).indexOf(new_rp);
                                log.debug("new_rp: {} new_rp_index: {}", new_rp, new_rp_index);
                                int old_rp = -1; // set to "undefined"
                                int old_rp_index = -1; // set to "undefined"
                                int ix = getArrayIndex(xa.getNumber()); 
                                if (ix < locoInBlock.length) {
                                    old_rp = locoInBlock[ix][block];
                                    if (old_rp == 0) old_rp = -1; // set to "undefined"
                                    old_rp_index = reporterlists.get(d.setup_index).indexOf(old_rp); // -1 if not found (undefined)
                                } else {
                                    log.warn(" Array locoInBlock INDEX {} IS NOT VALID! Set to 0.", ix);
                                    ix = 0;
                                }
                                log.debug("new_rp: {}, old_rp: {}, new index: {}, old index: {}", new_rp, old_rp, new_rp_index, old_rp_index);
                                // 3) Validation check: don't proceed when it's the same reporter
                                if (new_rp != old_rp) {
                                    // 4) Validation check: reporter must be a new or a neighbour reporter or must rotating in a circle
                                    int lastrepix = reporterlists.get(d.setup_index).size() - 1; // Get the index of the last Reporter
                                    if ((old_rp == -1) // Loco can be in any section, if it's the first reported section; old rp is "undefined"
                                            || (old_rp_index + d.dirfn == new_rp_index) // Loco is running forward or reverse
                                            || (circlelist.get(d.setup_index) && d.dirfn == -1 && old_rp_index == 0 && new_rp_index == lastrepix) // Loco is running reverse and circling
                                            || (circlelist.get(d.setup_index) && d.dirfn ==  1 && old_rp_index == lastrepix && new_rp_index == 0)) { // Loco is running forward and circling
                                        // Validation check: OK
                                        locoInBlock[ix][block] = new_rp; // Set new block number (int)
                                        log.debug(" distance rest (old) to go in block {}: {} cm", old_rp, locoInBlock[ix][distance_to_go]);
                                        locoInBlock[ix][distance_to_go] = Math.round(blockParameter[d.setup_index][new_rp_index][length] * 100.0f); // block distance init: block length in cm
                                        log.debug(" distance rest (new) to go in block {}: {} cm", new_rp, locoInBlock[ix][distance_to_go]);
                                        // get the new sound position point (depends on the loco traveling direction)
                                        if (d.dirfn == 1) {
                                            posToSet = blockPositionlists.get(d.setup_index).get(new_rp_index); // Start position
                                        } else {
                                            posToSet = blockPositionlists.get(d.setup_index).get(new_rp_index + 1); // End position
                                        }
                                        if (old_rp == -1 && d.startPos != null) { // Special case start position: first choice; if found, overwrite it.
                                            posToSet = d.startPos;
                                        }
                                        log.debug("position to set: {}", posToSet);  
                                        setDecoderPositionByAddr(xa, posToSet); // Sound set position
                                        stopSoundPositionTimer(d);
                                        startSoundPositionTimer(d); // timer restart
                                    } else {
                                        log.debug(" Validation failed! Last reporter: {}, new reporter: {}, dirfn: {} for {}", old_rp, new_rp, d.dirfn, xa.getNumber());
                                    }
                                } else {
                                    log.debug(" Same Reporter, position not set!");
                                }
                            }
                        } else {
                            log.debug("Reporter {} not valid for {} setup {}", new_rp, VSDGeoFile.VSDGeoDataFileName, d.setup_index + 1);
                        }
                    } else {
                        log.debug(" decoder address {} is not valid!", xa.getNumber());
                    }
                } else {
                    // newValue is of IdTag type.
                    // Dcc4Pc, Ecos, 
                    // Assume Reporter "arp" is the most recent seen location
                    IdTag tagValue = (IdTag) event.getNewValue();
                    log.debug("new value: {}, id: {}", tagValue, tagValue.getTagID());
                    setDecoderPositionByAddr(arp.getLocoAddress(tagValue.getTagID()), arp.getPhysicalLocation(null));
                } 
            } else {
                log.debug("Reporter's return type is not supported");
                // do nothing
            }
        } else {
            log.debug("Reporter doesn't support physical location reporting or isn't reporting new info");
        } // Reporting object implements PhysicalLocationReporter
        return;
    }

    public void reporterManagerPropertyChange(PropertyChangeEvent event) {
        String eventName = event.getPropertyName();

        log.debug("VSDecoder received Reporter Manager Property Change: {}", eventName);
        if (eventName.equals("length")) { // NOI18N

            // Re-register for all the reporters. The registerReporterListener() will skip
            // any that we're already registered for.
            for (Reporter r : jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getNamedBeanSet()) {
                registerReporterListener(r.getSystemName());
            }

            // It could be that we lost a Reporter.  But since we aren't keeping a list anymore
            // we don't care.
        }
    }

    public int getArrayIndex(int numb) {
        for (int i = 0; i < locoInBlock.length; i++) {
            if (locoInBlock[i][address] == numb) {
                return i;   
            }
        }
        return locoInBlock.length;
    }

    public int getNumberOfDecoders() {
        return locorow + 1;
    }

    public void locoInBlockRemove(int numb) {
        // Works only for <locoInBlock.length> rows
        //  find index first
        remove_index = 0;
        for (int i = 0; i < locoInBlock.length; i++) {
            if (locoInBlock[i][address] == numb) { 
                remove_index = i;
            }
        }
        for (int i = remove_index; i < locoInBlock.length - 1; i++) {   
            for (int k = 0; k < locoInBlock[i].length; k++) {
                locoInBlock[i][k] = locoInBlock[i + 1][k];
            }
        }
        // Delete last row
        int il = locoInBlock.length - 1;
        for (int k = 0; k < locoInBlock[il].length; k++) {
            locoInBlock[il][k] = 0;
        }
    }

    public void loadProfiles(VSDFile vf) {
        Element root;
        String pname;
        root = vf.getRoot();
        if (root == null) {
            return;
        }

        ArrayList<String> new_entries = new ArrayList<>();

        java.util.Iterator<Element> i = root.getChildren("profile").iterator(); // NOI18N
        while (i.hasNext()) {
            Element e = i.next();
            pname = e.getAttributeValue("name");
            log.debug("Profile name: {}", pname);
            if ((pname != null) && !(pname.isEmpty())) { // NOI18N
                profiletable.put(pname, vf.getName());
                new_entries.add(pname);
            } else {
                log.error("Profile name is not valid");
            }
        }

        if (!GraphicsEnvironment.isHeadless()) {
            fireMyEvent(new VSDManagerEvent(this, VSDManagerEvent.EventType.PROFILE_LIST_CHANGE, new_entries));
        }
    }

    void initSoundPositionTimer(VSDecoder d) {
        if (geofile_ok) {
            Timer t = new Timer(check_time, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    calcNewPosition(d);
                }
            });
            t.setRepeats(true);
            timertable.put(d.getId(), t);
            log.debug("timer {} created for decoder {}, id: {}", t, d, d.getId());
        } else {
            log.debug("No timer created, GeoData not available");
        }
    }

    void startSoundPositionTimer(VSDecoder d) {
        Timer t = timertable.get(d.getId());
        if (t != null) {
            t.setInitialDelay(check_time);
            t.start();
            log.debug("timer {} started for decoder id {}, {}, check time: {}", t, d.getId(), d, check_time);
        }
    }

    void stopSoundPositionTimer(VSDecoder d) {
        Timer t = timertable.get(d.getId());
        if (t != null) {
            if (t.isRunning()) {
                t.stop();
                log.debug("timer {} stopped for {}", t, d);
            } else {
                log.debug("timer {} was not running", t);
            }
        }
    }

    // Simple way to calulate train positions within a block
    //  train route is described by a combination of two types of geometric elements: line track or curve track
    //  the train route data is provided by a xml file and gathered by method getBlockValues
    public void calcNewPosition(VSDecoder d) {
        if (d.currentspeed > 0.0f && d.topspeed > 0) { // proceed only, if the loco is running and if a topspeed value is available
            int dadr = d.getAddress().getNumber();
            int dadr_index = getArrayIndex(dadr); // check, if the decoder is in "Block status for locos" - remove this check?
            if (dadr_index < locoInBlock.length) {
                // decoder is valid
                int dadr_block = locoInBlock[dadr_index][block]; // get block number for current decoder/loco
                if (reporterlists.get(d.setup_index).contains(dadr_block)) {
                    int dadr_block_index = reporterlists.get(d.setup_index).indexOf(dadr_block);
                    newPosition = new PhysicalLocation(0.0f, 0.0f, 0.0f, is_tunnel);
                    // calculate current speed in meter/second; support topspeed forward or reverse
                    // JMRI speed is 0-1; currentspeed is speed after speedCurve(); multiply with topspeed (MPH); convert MPH to meter/second; regard layout scale
                    speed_ms = d.currentspeed * (d.dirfn == 1 ? d.topspeed : d.topspeed_rev) * 0.44704f / layout_scale;
                    distance = speed_ms * check_time / 1000; // distance in Meter
                    if (locoInBlock[dadr_index][direction] == 0) { // On start
                        locoInBlock[dadr_index][direction] = d.dirfn;
                    }
                    distance_rest_old = locoInBlock[dadr_index][distance_to_go] / 100.0f; // Distance to go in meter
                    if (locoInBlock[dadr_index][direction] == d.dirfn) { // Last traveling direction
                        distance_rest = distance_rest_old;
                    } else {
                        distance_rest = blockParameter[d.setup_index][dadr_block_index][length] - distance_rest_old;
                        locoInBlock[dadr_index][direction] = d.dirfn;
                    }
                    distance_rest_new = distance_rest - distance; // Distance to go in Meter
                    log.debug(" distance_rest_old: {}, distance_rest: {}, distance_rest_new: {} (all in Meter)", distance_rest_old, distance_rest, distance_rest_new); 
                    // Calculate and set sound position only, if loco would be still inside the block
                    if (distance_rest_new > 0.0f) {
                        // Which geometric element? Radius = 0 means "line"
                        if (blockParameter[d.setup_index][dadr_block_index][radius] == 0.0f) {
                            // Line
                            xPosi = distance * (-d.dirfn) * (float) Math.sqrt(1.0f / (1.0f +
                                blockParameter[d.setup_index][dadr_block_index][slope] * blockParameter[d.setup_index][dadr_block_index][slope]));
                            newPosition.x = d.lastPos.x - xPosi;
                            newPosition.y = d.lastPos.y - xPosi * blockParameter[d.setup_index][dadr_block_index][slope];
                            newPosition.z = 0.0f;
                        } else {
                            // Curve
                            float anglePos = distance / blockParameter[d.setup_index][dadr_block_index][radius] * (-d.dirfn); // distance / radius * (-loco direction)
                            float rotate_xpos = blockParameter[d.setup_index][dadr_block_index][rotate_xpos_i];
                            float rotate_ypos = blockParameter[d.setup_index][dadr_block_index][rotate_ypos_i]; // rotation center point y
                            newPosition.x =  rotate_xpos + (float) Math.cos(anglePos) * (d.lastPos.x - rotate_xpos) - (float) Math.sin(anglePos) * (d.lastPos.y - rotate_ypos);
                            newPosition.y =  rotate_ypos + (float) Math.sin(anglePos) * (d.lastPos.x - rotate_xpos) + (float) Math.cos(anglePos) * (d.lastPos.y - rotate_ypos);
                            newPosition.z = 0.0f;
                        }
                        d.setPosition(newPosition); // Sound set position
                        log.debug(" distance rest to go in block: {} of {} cm", Math.round(distance_rest_new * 100.0f),
                            Math.round(blockParameter[d.setup_index][dadr_block_index][length] * 100.0f));
                        locoInBlock[dadr_index][distance_to_go] = Math.round(distance_rest_new * 100.0f); // Save distance rest in cm
                        log.debug(" saved distance rest: {}", locoInBlock[dadr_index][distance_to_go]);
                    } else {
                        log.debug(" new position not set due to less distance");
                    }
                } else {
                    log.warn(" block for loco address {} not yet identified. May be there is another loco in the same block", dadr);
                }
            } else {
                log.warn(" decoder {} not found", dadr);
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(VSDecoderManager.class);

}
