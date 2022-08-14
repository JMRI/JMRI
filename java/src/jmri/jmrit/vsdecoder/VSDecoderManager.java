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
import jmri.Audio;
import jmri.Block;
import jmri.IdTag;
import jmri.LocoAddress;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Path;
import jmri.PhysicalLocationReporter;
import jmri.Reporter;
import jmri.implementation.DefaultIdTag;
import jmri.jmrit.display.layoutEditor.*;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.jmrit.vsdecoder.listener.VSDListener;
import jmri.jmrit.vsdecoder.swing.VSDManagerFrame;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import jmri.util.PhysicalLocation;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.GraphicsEnvironment;
import javax.swing.Timer;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VSDecoderFactory, builds VSDecoders as needed, handles loading from XML if needed.
 *
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
 * @author Klaus Killinger Copyright (C) 2018-2022
 */
public class VSDecoderManager implements PropertyChangeListener {

    //private static final ResourceBundle rb = VSDecoderBundle.bundle();
    private static final String vsd_property_change_name = "VSDecoder Manager"; // NOI18N

    // Array-pointer for blockParameter
    private static final int RADIUS = 0;
    private static final int SLOPE = 1;
    private static final int ROTATE_XPOS_I = 2;
    private static final int ROTATE_YPOS_I = 3;
    private static final int LENGTH = 4;

    // Array-pointer for locoInBlock
    private static final int ADDRESS = 0;
    private static final int BLOCK = 1;
    private static final int DISTANCE_TO_GO = 2;
    private static final int DIR_FN = 3;
    private static final int DIRECTION = 4;

    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    private HashMap<String, VSDListener> listenerTable; // list of listeners
    private HashMap<String, VSDecoder> decodertable; // list of active decoders by System ID
    private HashMap<String, VSDecoder> decoderAddressMap; // List of active decoders by address
    private HashMap<Integer, VSDecoder> decoderInBlock; // list of active decoders by LocoAddress.getNumber()
    private HashMap<String, String> profiletable; // list of loaded profiles key = profile name, value = path
    HashMap<VSDecoder, Block> currentBlock; // list of active blocks by decoders
    private HashMap<Block, LayoutEditor> possibleStartBlocks; // list of possible start blocks and their LE panel
    private HashMap<String, Timer> timertable; // list of active timers by decoder System ID

    private int locoInBlock[][]; // Block status for locos
    private float blockParameter[][][];
    private List<List<PhysicalLocation>> blockPositionlists;
    private List<List<Integer>> reporterlists;
    private List<Boolean> circlelist;
    private PhysicalLocation newPosition;
    private PhysicalLocation models_origin;
    private ArrayList<Block> blockList;

    // List of registered event listeners
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    //private static VSDecoderManager instance = null; // sole instance of this class
    private volatile static VSDecoderManagerThread thread = null; // thread for running the manager

    private VSDecoderPreferences vsdecoderPrefs; // local pointer to the preferences object

    private JmriJFrame managerFrame = null;

    private int vsdecoderID = 0;
    private int locorow = -1; // Will be increased before first use

    private int check_time; // Time interval in ms for track following updates
    private float layout_scale;
    private float distance_rest = 0.0f; // Block distance to go
    private float distance_rest_old = 0.0f; // Block distance to go, copy
    private float distance_rest_new = 0.0f; // Block distance to go, copy

    private float xPosi;
    public static final int max_decoder = 4; // For now only four locos allowed (arbitrary)
    boolean geofile_ok = false;
    int num_setups;
    private int lf_version;
    int alf_version;

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
        currentBlock = new HashMap<>(); // key = decoder, value = block
        possibleStartBlocks = new HashMap<>();
        locoInBlock = new int[max_decoder][5]; // Loco address number, current block, distance in cm to go in block, dirfn, direction
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
        // Get a Listener
        VSDListener t = new VSDListener();
        listenerTable.put(t.getSystemName(), t);
        // Update JMRI "Default Audio Listener"
        setListenerLocation(t.getSystemName(), vsdecoderPrefs.getListenerPosition());
        // Look for additional layout geometry data
        VSDGeoFile gf = new VSDGeoFile();
        if (gf.geofile_ok) {
            geofile_ok = true;
            alf_version = gf.alf_version;
            num_setups = gf.getNumberOfSetups();
            reporterlists = gf.getReporterList();
            blockParameter = gf.getBlockParameter();
            blockPositionlists = gf.getBlockPosition();
            circlelist = gf.getCirclingList();
            check_time = gf.check_time;
            layout_scale = gf.layout_scale;
            models_origin = gf.models_origin;
            possibleStartBlocks = gf.possibleStartBlocks;
            blockList = gf.blockList;
        } else {
            geofile_ok = false;
            if (gf.lf_version > 0) {
                lf_version = gf.lf_version;
                log.debug("assume location following");
            }
        }
    }

    /**
     * Provide the VSdecoderManager instance.
     * @return the manager
     */
    public static VSDecoderManager instance() {
        if (thread == null) {
            thread = VSDecoderManagerThread.instance(true);
        }
        return VSDecoderManagerThread.manager();
    }

    /**
     * Get a reference to the VSD Preferences.
     * @return the preferences reference
     */
    public VSDecoderPreferences getVSDecoderPreferences() {
        return vsdecoderPrefs;
    }

    /**
     * Get the master volume of all VSDecoders.
     * @return the master volume
     */
    public int getMasterVolume() {
        return getVSDecoderPreferences().getMasterVolume();
    }

    /**
     * Set the master volume for all VSDecoders.
     * @param mv The new master volume
     */
    public void setMasterVolume(int mv) {
        getVSDecoderPreferences().setMasterVolume(mv);
    }

    /**
     * Get the VSD GUI.
     * @return the VSD frame
     */
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
                            log.info("Loading Roster Entry \"{}\", VSDecoder {} ...", entry.getId(), config.getLocoAddress());
                            if (entry.getAttribute("VSDecoder_Path") != null && entry.getAttribute("VSDecoder_Profile") != null) {
                                if (LoadVSDFileAction.loadVSDFile(entry.getAttribute("VSDecoder_Path"))) {
                                    // config.xml OK
                                    log.info(" VSD path: {}", entry.getAttribute("VSDecoder_Path"));
                                    config.setProfileName(entry.getAttribute("VSDecoder_Profile"));
                                    log.debug(" entry VSD profile: {}", entry.getAttribute("VSDecoder_Profile"));
                                    if (entry.getAttribute("VSDecoder_Volume") != null) {
                                        config.setVolume(Float.parseFloat(entry.getAttribute("VSDecoder_Volume")));
                                    } else {
                                        config.setVolume(0.8f);
                                    }
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

    /**
     * Provide or build a VSDecoder based on a provided configuration.
     *
     * @param config previous configuration, not null.
     * @return vsdecoder, or null on error.
     */
    public VSDecoder getVSDecoder(VSDConfig config) {
        String path;
        String profile_name = config.getProfileName();
        // First, check to see if we already have a VSDecoder on this Address
        if (decoderAddressMap.containsKey(config.getLocoAddress().toString())) {
            return decoderAddressMap.get(config.getLocoAddress().toString());
        }
        if (profiletable.containsKey(profile_name)) {
            path = profiletable.get(profile_name);
            log.debug("Profile {} is in table.  Path: {}", profile_name, path);

            config.setVSDPath(path);
            config.setId(getNextVSDecoderID());
            VSDecoder vsd = new VSDecoder(config);
            decodertable.put(vsd.getId(), vsd);
            decoderAddressMap.put(vsd.getAddress().toString(), vsd);
            decoderInBlock.put(vsd.getAddress().getNumber(), vsd);
            locoInBlock[getNextlocorow()][ADDRESS] = vsd.getAddress().getNumber();

            // set volume for this decoder
            vsd.setDecoderVolume(vsd.getDecoderVolume());

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

    /**
     * Get a VSDecoder by its Id.
     *
     * @param id The Id of the VSDecoder
     * @return vsdecoder, or null on error.
     */
    public VSDecoder getVSDecoderByID(String id) {
        VSDecoder v = decodertable.get(id);
        if (v == null) {
            log.debug("No decoder in table! ID: {}", id);
        }
        return decodertable.get(id);
    }

    /**
     * Get a VSDecoder by its address.
     *
     * @param sa The address of the VSDecoder
     * @return vsdecoder, or null on error.
     */
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

    /**
     * Get a list of all profiles.
     *
     * @return sl The profiles list.
     */
    public ArrayList<String> getVSDProfileNames() {
        ArrayList<String> sl = new ArrayList<>();
        for (String p : profiletable.keySet()) {
            sl.add(p);
        }
        return sl;
    }

    /**
     * Get a list of all VSDecoders.
     *
     * @return the VSDecoder list.
     */
    public Collection<VSDecoder> getVSDecoderList() {
        return decodertable.values();
    }

    /**
     * Get the VSD listener system name.
     *
     * @return the system name.
     */
    public String getDefaultListenerName() {
        return VSDListener.ListenerSysName;
    }

    /**
     * Get the VSD listener location.
     *
     * @return the location or null.
     */
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
            log.info("Location: {} ... ignoring", l);
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

    protected void registerBeanListener(Manager<Block> beanManager, String sysName) {
        NamedBean b = beanManager.getBySystemName(sysName);
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

    /**
     * Delete a VSDecoder
     *
     * @param address The DCC address of the VSDecoder
     */
    public void deleteDecoder(String address) {
        log.debug("delete Decoder called, VSDecoder DCC address: {}", address);
        if (this.getVSDecoderByAddress(address) == null) {
            log.warn("VSDecoder not found");
        } else {
            removeVSDecoder(address);
        }
    }

    private void removeVSDecoder(String sa) {
        VSDecoder d = this.getVSDecoderByAddress(sa);
        stopSoundPositionTimer(d);
        d.shutdown();
        d.disable();

        decodertable.remove(d.getId());
        decoderAddressMap.remove(sa);
        currentBlock.remove(d);
        decoderInBlock.remove(d.getAddress().getNumber());
        locoInBlockRemove(d.getAddress().getNumber());
        timertable.remove(d.getId()); // Remove timer
        locorow--; // prepare array index for eventually adding a new decoder

        d.sound_list.clear();
        d.event_list.clear();

        jmri.AudioManager am = jmri.InstanceManager.getDefault(jmri.AudioManager.class);
        ArrayList<Audio> sources = new ArrayList<>(am.getNamedBeanSet(Audio.SOURCE));
        ArrayList<Audio> buffers = new ArrayList<>(am.getNamedBeanSet(Audio.BUFFER));
        // wait until audio threads are finished and then run audio cleanup via dispose()
        jmri.util.ThreadingUtil.newThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                }
                for (Audio source: sources) {
                    if (source.getSystemName().contains(d.getId())) {
                        source.dispose();
                    }
                }
                for (Audio buffer: buffers) {
                    if (buffer.getSystemName().contains(d.getId())) {
                        buffer.dispose();
                    }
                }
            }
        }).start();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        log.debug("property change type {} name {} old {} new {}",
                evt.getSource().getClass().getName(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        if (evt.getSource() instanceof jmri.ReporterManager) {
            reporterManagerPropertyChange(evt);
        } else if (evt.getSource() instanceof jmri.Reporter) {
            reporterPropertyChange(evt); // Location Following
        } else if (evt.getSource() instanceof jmri.Block) {
            log.debug("Block property change! name: {} old: {} new = {}", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            blockPropertyChange(evt);
        } else if (evt.getSource() instanceof VSDManagerFrame) {
            if (evt.getPropertyName().equals(VSDManagerFrame.REMOVE_DECODER)) {
                // Shut down the requested decoder and remove it from the manager's hash maps.
                // Unless there are "illegal" handles, this should put the decoder on the garbage heap.  I think.
                removeVSDecoder((String) evt.getOldValue());
            } else if (evt.getPropertyName().equals(VSDManagerFrame.CLOSE_WINDOW)) {
                // Note this assumes there is only one VSDManagerFrame open at a time.
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
                    // Is there a Block's Reporter?
                    if (blk.getReporter() == null) {
                        log.debug("Block {} has no reporter!  Skipping state-type report", blk.getSystemName());
                        return;
                    }
                    // Get this Block's Reporter's current/last report value
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
            } else if (eventName.equals("value")) { // NOI18N
                if (event.getNewValue() == null ) {
                    return; // block value was cleared, nothing to do
                }

                int locoAddress = 0;

                if (event.getNewValue() instanceof String) {
                    repVal = event.getNewValue().toString();
                    if (Roster.getDefault().getEntryForId(repVal) != null) {
                        locoAddress = Integer.parseInt(Roster.getDefault().getEntryForId(repVal).getDccAddress()); // numeric RosterEntry Id
                    } else if (org.apache.commons.lang3.StringUtils.isNumeric(repVal)) {
                        locoAddress = Integer.parseInt(repVal);
                    } else if (jmri.InstanceManager.getDefault(TrainManager.class).getTrainByName(repVal) != null) {
                        // Operations Train
                        Train selected_train = jmri.InstanceManager.getDefault(TrainManager.class).getTrainByName(repVal);
                        log.info(" train - name: {}, desc: {}, engine: {}", selected_train.getName(), selected_train.getRawDescription(), selected_train.getLeadEngine());
                        if (selected_train.getLeadEngineDccAddress().isEmpty()) {
                            locoAddress = 0;
                        } else {
                            locoAddress = Integer.parseInt(selected_train.getLeadEngineDccAddress());
                        }
                    }
                    log.debug("loco address: {}", locoAddress);
                } else if (event.getNewValue() instanceof jmri.BasicRosterEntry) {
                    locoAddress = Integer.parseInt(((RosterEntry) event.getNewValue()).getDccAddress());
                } else if (event.getNewValue() instanceof jmri.implementation.DefaultIdTag) {
                    // Covers TranspondingTag also
                    repVal = ((DefaultIdTag) event.getNewValue()).getTagID(); // get the system name without the identifier, e.g. "6"
                    if (org.apache.commons.lang3.StringUtils.isNumeric(repVal)) {
                        locoAddress = Integer.parseInt(repVal);
                    }
                } else {
                    log.warn("Block Value \"{}\" found - unsupported object!", event.getNewValue());
                }

                if (locoAddress != 0) {
                    // look for an existing and configured VSDecoder
                    if (decoderInBlock.containsKey(locoAddress)) {
                        // ready to set the sound position
                        VSDecoder d = decoderInBlock.get(locoAddress);
                        // look for additional geometric layout information
                        if (geofile_ok) {
                            if (alf_version == 2 && blockList.contains(blk)) {
                                handleAlf2(d, locoAddress, blk);
                            } else {
                                log.info("Block {} not valid for panel {}", blk, d.getModels());
                            }
                        } else {
                            d.savedSound.setTunnel(blk.getPhysicalLocation().isTunnel()); // tunnel status
                            d.setPosition(blk.getPhysicalLocation());
                            log.debug("Block value: {}, physical location: {}", event.getNewValue(), blk.getPhysicalLocation());
                        }
                        return;
                    } else {
                        log.warn("Block value \"{}\" is not a valid VSDecoder address", event.getNewValue());
                    }
                }
            } else {
                log.debug("Not a supported Block event type.  Ignoring.");
                return;
            }  // Type of eventName.

            // Set the decoder's position due to the report.
            if (repVal == null) {
                log.warn("Report from Block {} is null!", blk.getSystemName());
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
        if (lf_version == 1 || (geofile_ok && alf_version == 1)) {
            if ((event.getSource() instanceof PhysicalLocationReporter) && (eventName.equals("currentReport"))) { // NOI18N
                PhysicalLocationReporter arp = (PhysicalLocationReporter) event.getSource();
                // Need to decide which reporter it is, so we can use different methods
                // to extract the address and the location.
                if (event.getNewValue() instanceof IdTag) {
                    // RFID-tag, Digitrax Transponding tags, RailCom tags
                    if (event.getNewValue() instanceof jmri.jmrix.loconet.TranspondingTag) {
                        String repVal = ((jmri.Reportable) event.getNewValue()).toReportString();
                        int locoAddress = arp.getLocoAddress(repVal).getNumber();
                        log.debug("Reporter repVal: {}, number: {}", repVal, locoAddress);
                        // Check: is loco address valid?
                        if (decoderInBlock.containsKey(locoAddress)) {
                            VSDecoder d = decoderInBlock.get(locoAddress);
                            // look for additional geometric layout information
                            if (geofile_ok) {
                                Reporter rp = (Reporter) event.getSource();
                                int new_rp = Integer.parseInt(rp.getSystemName().substring(2)); // ??? connection prefix 3 signs? VSDGeoFile checks for non-numeric part, e.g. "IR7a"
                                // Check: Reporter must be valid for GeoData processing
                                //    use the current Reporter list as a filter (changeable by a Train selection)
                                if (reporterlists.get(d.setup_index).contains(new_rp)) {
                                    if (arp.getDirection(repVal) == PhysicalLocationReporter.Direction.ENTER) { 
                                        handleAlf(d, locoAddress, new_rp); // Advanced Location Following version 1
                                    }
                                } else {
                                    log.info("Reporter {} not valid for {} setup {}", new_rp, VSDGeoFile.VSDGeoDataFileName, d.setup_index + 1);
                                }
                            } else {
                                if (arp.getDirection(repVal) == PhysicalLocationReporter.Direction.ENTER) {
                                    d.savedSound.setTunnel(arp.getPhysicalLocation(repVal).isTunnel());
                                    d.setPosition(arp.getPhysicalLocation(repVal));
                                    log.debug("position set to: {}", arp.getPhysicalLocation(repVal));
                                }
                            }
                        } else {
                            log.info(" decoder address {} is not valid!", locoAddress);
                        }
                        return;
                    } else {
                        // newValue is of IdTag type.
                        // Dcc4Pc, Ecos, 
                        // Assume Reporter "arp" is the most recent seen location
                        IdTag newValue = (IdTag) event.getNewValue();
                        decoderInBlock.get(arp.getLocoAddress(newValue.getTagID()).getNumber()).savedSound.setTunnel(arp.getPhysicalLocation(null).isTunnel());
                        setDecoderPositionByAddr(arp.getLocoAddress(newValue.getTagID()), arp.getPhysicalLocation(null));
                    }
                } else {
                    log.info("Reporter's return type is not supported.");
                    // do nothing
                }
            } else {
                log.debug("Reporter doesn't support physical location reporting or isn't reporting new info.");
            }  // Reporting object implements PhysicalLocationReporter
        }
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

    // handle Advanced Location Following version 1
    private void handleAlf(VSDecoder d, int locoAddress, int new_rp) {
        int new_rp_index = reporterlists.get(d.setup_index).indexOf(new_rp);
        int old_rp = -1; // set to "undefined"
        int old_rp_index = -1; // set to "undefined"
        int ix = getArrayIndex(locoAddress); 
        if (ix < locoInBlock.length) {
            old_rp = locoInBlock[ix][BLOCK];
            if (old_rp == 0) old_rp = -1; // set to "undefined"
            old_rp_index = reporterlists.get(d.setup_index).indexOf(old_rp); // -1 if not found (undefined)
        } else {
            log.warn(" Array locoInBlock INDEX {} IS NOT VALID! Set to 0.", ix);
            ix = 0;
        }
        log.debug("new_rp: {}, old_rp: {}, new index: {}, old index: {}", new_rp, old_rp, new_rp_index, old_rp_index);
        // Validation check: don't proceed when it's the same reporter
        if (new_rp != old_rp) {
            // Validation check: reporter must be a new or a neighbour reporter or must rotating in a circle
            int lastrepix = reporterlists.get(d.setup_index).size() - 1; // Get the index of the last Reporter
            if ((old_rp == -1) // Loco can be in any section, if it's the first reported section; old rp is "undefined"
                    || (old_rp_index + d.dirfn == new_rp_index) // Loco is running forward or reverse
                    || (circlelist.get(d.setup_index) && d.dirfn == -1 && old_rp_index == 0 && new_rp_index == lastrepix) // Loco is running reverse and circling
                    || (circlelist.get(d.setup_index) && d.dirfn ==  1 && old_rp_index == lastrepix && new_rp_index == 0)) { // Loco is running forward and circling
                // Validation check: OK
                locoInBlock[ix][BLOCK] = new_rp; // Set new block number (int)
                log.debug(" distance rest (old) to go in block {}: {} cm", old_rp, locoInBlock[ix][DISTANCE_TO_GO]);
                locoInBlock[ix][DISTANCE_TO_GO] = Math.round(blockParameter[d.setup_index][new_rp_index][LENGTH] * 100.0f); // block distance init: block length in cm
                log.debug(" distance rest (new) to go in block {}: {} cm", new_rp, locoInBlock[ix][DISTANCE_TO_GO]);
                // get the new sound position point (depends on the loco traveling direction)
                if (d.dirfn == 1) {
                    d.posToSet = blockPositionlists.get(d.setup_index).get(new_rp_index); // Start position
                } else {
                    d.posToSet = blockPositionlists.get(d.setup_index).get(new_rp_index + 1); // End position
                }
                if (old_rp == -1 && d.startPos != null) { // Special case start position: first choice; if found, overwrite it.
                    d.posToSet = d.startPos;
                }
                d.savedSound.setTunnel(blockPositionlists.get(d.setup_index).get(new_rp_index).isTunnel()); // set the tunnel status
                log.debug("address {}: position to set: {}", d.getAddress(), d.posToSet);
                d.setPosition(d.posToSet); // Sound set position
                stopSoundPositionTimer(d);
                startSoundPositionTimer(d); // timer restart
            } else {
                log.info(" Validation failed! Last reporter: {}, new reporter: {}, dirfn: {} for {}", old_rp, new_rp, d.dirfn, locoAddress);
            }
        } else {
            log.info(" Same PhysicalLocationReporter, position not set!");
        }
    }

    // handle Advanced Location Following version 2
    private void handleAlf2(VSDecoder d, int locoAddress, Block newBlock) {
        if (currentBlock.get(d) != newBlock) {
            int ix = getArrayIndex(locoAddress); // ix = decoder number 0 - max_decoder-1
            if (locoInBlock[ix][DIR_FN] == 0) { // On start
                if (d.getLayoutTrack() == null) {
                    if (possibleStartBlocks.get(newBlock) != null) {
                        d.setModels(possibleStartBlocks.get(newBlock)); // get the models from the HashMap via block
                        log.debug("Block: {}, models: {}", newBlock, d.getModels());
                        TrackSegment ts = null;
                        for (LayoutTrack lt : d.getModels().getLayoutTracks()) {
                            if (lt instanceof TrackSegment) {
                                ts = (TrackSegment) lt;
                                if (ts.getLayoutBlock().getBlock() == newBlock) {
                                    break;
                                }
                            }
                        }
                        log.info("on start - TS: {}, block: {}, panel: {}", ts, newBlock, d.getModels());
                        TrackSegmentView tsv = d.getModels().getTrackSegmentView(ts);
                        d.setLayoutTrack(ts);
                        d.setReturnTrack(d.getLayoutTrack());
                        d.setReturnLastTrack(tsv.getConnect2());
                        d.setLastTrack(tsv.getConnect1());
                        d.setReturnDistance(MathUtil.distance(d.getModels().getCoords(tsv.getConnect1(), tsv.getType1()),
                                d.getModels().getCoords(tsv.getConnect2(), tsv.getType2())));
                        d.setDistance(0);
                        d.distanceOnTrack = 0.5d * d.getReturnDistance(); // halved to get starting position (mid or centre of the track)
                        if (d.dirfn == -1) { // in case the loco is running in reverse direction
                            d.setLayoutTrack(d.getReturnTrack());
                            d.setLastTrack(d.getReturnLastTrack());
                        }
                        locoInBlock[ix][DIR_FN] = d.dirfn;
                        currentBlock.put(d, newBlock);
                        // prepare navigation
                        d.setLocation(new Point2D.Double(0, 0));
                        d.posToSet = new PhysicalLocation(0.0f, 0.0f, 0.0f);
                    } else {
                        log.warn("block {} is not a valid start block; valid start blocks are: {}", newBlock, possibleStartBlocks);
                    }
                }

            } else {

                currentBlock.put(d, newBlock);
                // new block; if end point is already reached, d.distanceOnTrack is zero
                if (d.distanceOnTrack > 0) {
                    // it's still on this track
                    // handle a block change, if the loco reaches the next block before the calculated end
                    boolean result = true; // new block, so go to the next track
                    d.distanceOnTrack = 0;
                    // go to next track
                    LayoutTrack last = d.getLayoutTrack();
                    if (d.getLayoutTrack() instanceof TrackSegment) {
                        TrackSegmentView tsv = d.getModels().getTrackSegmentView((TrackSegment) d.getLayoutTrack());
                        log.debug(" true - layout track: {}, last track: {}, connect1: {}, connect2: {}, last block: {}",
                                d.getLayoutTrack(), d.getLastTrack(), tsv.getConnect1(), tsv.getConnect2(), tsv.getBlockName());
                        if (tsv.getConnect1().equals(d.getLastTrack())) {
                            d.setLayoutTrack(tsv.getConnect2());
                        } else if (tsv.getConnect2().equals(d.getLastTrack())) {
                            d.setLayoutTrack(tsv.getConnect1());
                        } else { // OOPS! we're lost!
                            log.info(" TS lost, c1: {}, c2: {}, last track: {}", tsv.getConnect1(), tsv.getConnect2(), d.getLastTrack());
                            result = false;
                        }
                        if (result) {
                            d.setLastTrack(last);
                            d.setReturnTrack(d.getLayoutTrack());
                            d.setReturnLastTrack(d.getLayoutTrack());
                            log.debug(" next track (layout track): {}, last track: {}", d.getLayoutTrack(), d.getLastTrack());
                        }
                    } else if (d.getLayoutTrack() instanceof LayoutTurnout
                            || d.getLayoutTrack() instanceof LayoutSlip
                            || d.getLayoutTrack() instanceof LevelXing) {
                        // go to next track
                        if (d.nextLayoutTrack != null) {
                            d.setLayoutTrack(d.nextLayoutTrack);
                        } else { // OOPS! we're lost!
                            result = false;
                        }
                        if (result) {
                            d.setLastTrack(last);
                            d.setReturnTrack(d.getLayoutTrack());
                            d.setReturnLastTrack(d.getLayoutTrack());   
                        }
                    }
                }
            }
            startSoundPositionTimer(d);
        } else {
           log.warn(" Same PhysicalLocationReporter, position not set!");
        }
    }

    private void changeDirection(VSDecoder d, int locoAddress, int new_rp_index) {
        PhysicalLocation point1 = blockPositionlists.get(d.setup_index).get(new_rp_index);
        PhysicalLocation point2 = blockPositionlists.get(d.setup_index).get(new_rp_index + 1);
        Point2D coords1 = new Point2D.Double(point1.x, point1.y);
        Point2D coords2 = new Point2D.Double(point2.x, point2.y);
        int direct;
        if (d.dirfn == 1) {
            direct = Path.computeDirection(coords1, coords2);
        } else {
            direct = Path.computeDirection(coords2, coords1);
        }
        locoInBlock[getArrayIndex(locoAddress)][DIRECTION] = direct;
        log.debug(" direction: {} ({})", Path.decodeDirection(direct), direct);
    }

    /**
     * Get index of a decoder.
     * @param number The loco address number.
     * @return the index of a decoder's loco address number
     *         in the array or the length of the array.
     */
    public int getArrayIndex(int number) {
        for (int i = 0; i < locoInBlock.length; i++) {
            if (locoInBlock[i][ADDRESS] == number) {
                return i;
            }
        }
        return locoInBlock.length;
    }

    public void locoInBlockRemove(int numb) {
        // Works only for <locoInBlock.length> rows
        //  find index first
        int remove_index = 0;
        for (int i = 0; i < locoInBlock.length; i++) {
            if (locoInBlock[i][ADDRESS] == numb) {
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
                    float newspeed;
                    if (alf_version == 1) {
                        newspeed = d.currentspeed;
                        d.avgspeed = (newspeed + d.lastspeed) / 2f;
                        calcNewPosition(d);
                        d.lastspeed = newspeed;
                    } else if (alf_version == 2) {
                        if (d.getEngineSound().isEngineStarted() && d.currentspeed > 0.0f) {
                            newspeed = d.currentspeed;
                            d.avgspeed = (newspeed + d.lastspeed) / 2f;
                            d.lastspeed = newspeed;
                            int ix = getArrayIndex(d.getAddress().getNumber()); // ix = decoder number 0-3 (max_decoder)
                            if (locoInBlock[ix][DIR_FN] != d.dirfn) {
                                // traveling direction has changed
                                locoInBlock[ix][DIR_FN] = d.dirfn; // save traveling direction info
                                if (d.distanceOnTrack <= d.getReturnDistance()) {
                                    d.distanceOnTrack = d.getReturnDistance() - d.distanceOnTrack;
                                } else {
                                    d.distanceOnTrack = d.getReturnDistance();
                                }
                                d.setLayoutTrack(d.getReturnTrack());
                                d.setLastTrack(d.getReturnLastTrack());
                                log.debug("direction changed to {}, layout: {}, last: {}, return: {}, d.getReturnDistance: {}, d.distanceOnTrack: {}, d.getDistance: {}",
                                        d.dirfn, d.getLayoutTrack(), d.getLastTrack(), d.getReturnTrack(), d.getReturnDistance(), d.distanceOnTrack, d.getDistance());
                            }
                            float speed_ms = d.avgspeed * (d.dirfn == 1 ? d.topspeed : d.topspeed_rev) * 0.44704f / layout_scale; // calculate the speed
                            d.setDistance(d.getDistance() + speed_ms * check_time / 10.0); // d.getDistance() normally is 0, but can content an overflow
                            d.navigate();
                            Point2D loc = d.getLocation();
                            Point2D loc2 = new Point2D.Double(((float) loc.getX() - models_origin.x) * 0.01f, (models_origin.y - (float) loc.getY()) * 0.01f);
                            d.posToSet.x = (float) loc2.getX();
                            d.posToSet.y = (float) loc2.getY();
                            d.posToSet.z = 0.0f;
                            log.info("address {} position to set: {}", d.getAddress(), d.posToSet);
                            d.setPosition(d.posToSet);
                        }
                    }
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

    // Simple way to calulate loco positions within a block
    //  train route is described by a combination of two types of geometric elements: line track or curve track
    //  the train route data is provided by a xml file and gathered by method getBlockValues
    public void calcNewPosition(VSDecoder d) {
        if (d.currentspeed > 0.0f && d.topspeed > 0) { // proceed only, if the loco is running and if a topspeed value is available
            int dadr = d.getAddress().getNumber();
            int dadr_index = getArrayIndex(dadr); // check, if the decoder is in "Block status for locos" - remove this check?
            if (dadr_index < locoInBlock.length) {
                // decoder is valid
                int dadr_block = locoInBlock[dadr_index][BLOCK]; // get block number for current decoder/loco
                if (reporterlists.get(d.setup_index).contains(dadr_block)) {
                    int dadr_block_index = reporterlists.get(d.setup_index).indexOf(dadr_block);
                    newPosition = new PhysicalLocation(0.0f, 0.0f, 0.0f, d.savedSound.getTunnel());
                    // calculate current speed in meter/second; support topspeed forward or reverse
                    // JMRI speed is 0-1; currentspeed is speed after speedCurve(); multiply with topspeed (MPH); convert MPH to meter/second; regard layout scale
                    float speed_ms = d.avgspeed * (d.dirfn == 1 ? d.topspeed : d.topspeed_rev) * 0.44704f / layout_scale;
                    d.distanceMeter = speed_ms * check_time / 1000; // distance in Meter
                    if (locoInBlock[dadr_index][DIR_FN] == 0) { // On start
                        locoInBlock[dadr_index][DIR_FN] = d.dirfn;
                    }
                    distance_rest_old = locoInBlock[dadr_index][DISTANCE_TO_GO] / 100.0f; // Distance to go in meter
                    if (locoInBlock[dadr_index][DIR_FN] == d.dirfn) { // Last traveling direction
                        distance_rest = distance_rest_old;
                    } else {
                        // traveling direction has changed
                        distance_rest = blockParameter[d.setup_index][dadr_block_index][LENGTH] - distance_rest_old;
                        locoInBlock[dadr_index][DIR_FN] = d.dirfn;
                        changeDirection(d, dadr, dadr_block_index);
                        log.debug("direction changed to {}", locoInBlock[dadr_index][DIRECTION]);
                    }
                    distance_rest_new = distance_rest - d.distanceMeter; // Distance to go in Meter
                    log.debug(" distance_rest_old: {}, distance_rest: {}, distance_rest_new: {} (all in Meter)", distance_rest_old, distance_rest, distance_rest_new);
                    // Calculate and set sound position only, if loco would be still inside the block
                    if (distance_rest_new > 0.0f) {
                        // Which geometric element? RADIUS = 0 means "line"
                        if (blockParameter[d.setup_index][dadr_block_index][RADIUS] == 0.0f) {
                            // Line
                            if (locoInBlock[dadr_index][DIRECTION] == Path.SOUTH) {
                                newPosition.x = d.lastPos.x;
                                newPosition.y = d.lastPos.y - d.distanceMeter;
                            } else if (locoInBlock[dadr_index][DIRECTION] == Path.NORTH) {
                                newPosition.x = d.lastPos.x;
                                newPosition.y = d.lastPos.y + d.distanceMeter;
                            } else {
                                xPosi = d.distanceMeter * (float) Math.sqrt(1.0f / (1.0f +
                                        blockParameter[d.setup_index][dadr_block_index][SLOPE] * blockParameter[d.setup_index][dadr_block_index][SLOPE]));
                                if (locoInBlock[dadr_index][DIRECTION] == Path.SOUTH_WEST || locoInBlock[dadr_index][DIRECTION] == Path.WEST || locoInBlock[dadr_index][DIRECTION] == Path.NORTH_WEST) {
                                    newPosition.x = d.lastPos.x - xPosi;
                                    newPosition.y = d.lastPos.y - xPosi * blockParameter[d.setup_index][dadr_block_index][SLOPE];
                                } else {
                                    newPosition.x = d.lastPos.x + xPosi;
                                    newPosition.y = d.lastPos.y + xPosi * blockParameter[d.setup_index][dadr_block_index][SLOPE];
                                }
                            }
                            newPosition.z = 0.0f;
                        } else {
                            // Curve
                            float anglePos = d.distanceMeter / blockParameter[d.setup_index][dadr_block_index][RADIUS] * (-d.dirfn); // distanceMeter / RADIUS * (-loco direction)
                            float rotate_xpos = blockParameter[d.setup_index][dadr_block_index][ROTATE_XPOS_I];
                            float rotate_ypos = blockParameter[d.setup_index][dadr_block_index][ROTATE_YPOS_I]; // rotation center point y
                            newPosition.x =  rotate_xpos + (float) Math.cos(anglePos) * (d.lastPos.x - rotate_xpos) - (float) Math.sin(anglePos) * (d.lastPos.y - rotate_ypos);
                            newPosition.y =  rotate_ypos + (float) Math.sin(anglePos) * (d.lastPos.x - rotate_xpos) + (float) Math.cos(anglePos) * (d.lastPos.y - rotate_ypos);
                            newPosition.z = 0.0f;
                        }
                        log.debug("position to set: {}", newPosition);
                        d.setPosition(newPosition); // Sound set position
                        log.debug(" distance rest to go in block: {} of {} cm", Math.round(distance_rest_new * 100.0f),
                                Math.round(blockParameter[d.setup_index][dadr_block_index][LENGTH] * 100.0f));
                        locoInBlock[dadr_index][DISTANCE_TO_GO] = Math.round(distance_rest_new * 100.0f); // Save distance rest in cm
                        log.debug(" saved distance rest: {}", locoInBlock[dadr_index][DISTANCE_TO_GO]);
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
