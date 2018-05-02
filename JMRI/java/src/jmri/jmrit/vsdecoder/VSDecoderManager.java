package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author   Mark Underwood Copyright (C) 2011
 */
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
import jmri.Manager;
import jmri.NamedBean;
import jmri.PhysicalLocationReporter;
import jmri.Reporter;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.jmrit.vsdecoder.listener.VSDListener;
import jmri.jmrit.vsdecoder.swing.VSDManagerFrame;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// VSDecoderFactory
//
// Builds VSDecoders as needed.  Handles loading from XML if needed.
public class VSDecoderManager implements PropertyChangeListener {

    //private static final ResourceBundle rb = VSDecoderBundle.bundle();
    private static final String vsd_property_change_name = "VSDecoder Manager"; //NOI18N
    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    HashMap<String, VSDListener> listenerTable; // list of listeners
    HashMap<String, VSDecoder> decodertable; // list of active decoders by System ID
    HashMap<String, VSDecoder> decoderAddressMap; // List of active decoders by address
    HashMap<String, String> profiletable;    // list of loaded profiles key = profile name, value = path
    List<String> reportertable;        // list of Reporters we are following.

    // List of registered event listeners
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    //private static VSDecoderManager instance = null;   // sole instance of this class
    private static VSDecoderManagerThread thread = null; // thread for running the manager

    private VSDecoderPreferences vsdecoderPrefs; // local pointer to the preferences object

    private JmriJFrame managerFrame = null;

    private VSDecoder default_decoder = null;  // shortcut pointer to the default decoder (do we need this?)

    private static int vsdecoderID = 0;
    //private static int listenerID = 0; // for future use

    // Unused?
    //private PhysicalLocation listener_position;
    // constructor - for kicking off by the VSDecoderManagerThread...
    // WARNING: Should only be called from static instance()
    public VSDecoderManager() {
        // Setup the decoder table
        listenerTable = new HashMap<String, VSDListener>();
        decodertable = new HashMap<String, VSDecoder>();
        decoderAddressMap = new HashMap<String, VSDecoder>();
        profiletable = new HashMap<String, String>();  // key = profile name, value = path
        reportertable = new ArrayList<String>();
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
    }

    public static VSDecoderManager instance() {
        if (thread == null) {
            thread = VSDecoderManagerThread.instance(true);
        }
        return (VSDecoderManagerThread.manager());
    }

    public VSDecoderPreferences getVSDecoderPreferences() {
        return (vsdecoderPrefs);
    }

    public JmriJFrame provideManagerFrame() {
        if (managerFrame == null) {
            managerFrame = new VSDManagerFrame();
        } else {
            log.warn("Virtual Sound Decoder Manager is already running");
        }
        return (managerFrame);
    }

    private String getNextVSDecoderID() {
        // vsdecoderID initialized to zero, pre-incremented before return...
        // first returned ID value is 1.
        return ("IAD:VSD:VSDecoderID" + (++vsdecoderID)); // NOI18N
    }

    // To be used in the future
    /*
     private String getNextListenerID() {
     // ListenerID initialized to zero, pre-incremented before return...
     // first returned ID value is 1.
     // Prefix is added by the VSDListener constructor
     return("VSDecoderID" + (++listenerID)); // NOI18N
     }
     */
    @Deprecated
    public VSDecoder getVSDecoder(String profile_name) {
        VSDecoder vsd;
        String path;
        if (profiletable.containsKey(profile_name)) {
            path = profiletable.get(profile_name);
            log.debug("Profile " + profile_name + " is in table.  Path = " + path);
            vsd = new VSDecoder(getNextVSDecoderID(), profile_name, path);
            decodertable.put(vsd.getId(), vsd);  // poss. broken for duplicate profile names
            decoderAddressMap.put(vsd.getAddress().toString(), vsd);
            return (vsd);
        } else {
            // Don't have enough info to try to load from file.
            log.error("Requested profile not loaded: " + profile_name);
            return (null);
        }
    }

    public VSDecoder getVSDecoder(String profile_name, String path) {
        VSDecoder vsd = new VSDecoder(getNextVSDecoderID(), profile_name, path);
        decodertable.put(vsd.getId(), vsd); // poss. broken for duplicate profile names
        if (vsd.getAddress() != null) {
            decoderAddressMap.put(vsd.getAddress().toString(), vsd);
        }
        return (vsd);
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
            return (decoderAddressMap.get(config.getLocoAddress().toString()));
        }
        if (profiletable.containsKey(profile_name)) {
            path = profiletable.get(profile_name);
            log.debug("Profile " + profile_name + " is in table.  Path = " + path);
            config.setVSDPath(path);
            config.setId(getNextVSDecoderID());
            VSDecoder vsd = new VSDecoder(config);
            decodertable.put(vsd.getId(), vsd);
            decoderAddressMap.put(vsd.getAddress().toString(), vsd);
            //debugPrintDecoderList();
            return (vsd);
        } else {
            // Don't have enough info to try to load from file.
            log.error("Requested profile not loaded: " + profile_name);
            return (null);
        }
    }

    /*
     public void debugPrintDecoderList() {
     log.debug("Current Decoder List by System ID:");
     Set<Map.Entry<String, VSDecoder>> ids = decodertable.entrySet();
     Iterator<Map.Entry<String, VSDecoder>> idi = ids.iterator();
     while (idi.hasNext()) {
     Map.Entry<String, VSDecoder> e = idi.next();
     log.debug("    ID = " +  e.getKey() + " Val = " + e.getValue().getAddress().toString());
     }
     log.debug("Current Decoder List by Address:");
     ids = decoderAddressMap.entrySet();
     idi = ids.iterator();
     while (idi.hasNext()) {
     Map.Entry<String, VSDecoder> e = idi.next();
     log.debug("    ID = " +  e.getKey() + " Val = " + e.getValue().getId());
     }
     }
     */
    public VSDecoder getVSDecoderByID(String id) {
        VSDecoder v = decodertable.get(id);
        if (v == null) {
            log.debug("No decoder in table! ID = " + id);
        }
        return (decodertable.get(id));
    }

    public VSDecoder getVSDecoderByAddress(String sa) {
        if (sa == null) {
            log.debug("Decoder Address is Null");
            return (null);
        }
        log.debug("Decoder Address: " + sa);
        VSDecoder rv = decoderAddressMap.get(sa);
        if (rv == null) {
            log.debug("Not found.");
        } else {
            log.debug("Found: " + rv.getAddress());
        }
        return (rv);
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
        return (default_decoder);
    }

    public ArrayList<String> getVSDProfileNames() {
        ArrayList<String> sl = new ArrayList<String>();
        for (String p : profiletable.keySet()) {
            sl.add(p);
        }
        return (sl);
    }

    public Collection<VSDecoder> getVSDecoderList() {
        return (decodertable.values());
    }

    public String getDefaultListenerName() {
        return (VSDListener.ListenerSysNamePrefix + "ListenerID1"); // NOI18N
    }

    public ListeningSpot getDefaultListenerLocation() {
        VSDListener l = listenerTable.get(getDefaultListenerName());
        if (l != null) {
            return (l.getLocation());
        } else {
            return (null);
        }
    }

    public void setListenerLocation(String id, ListeningSpot sp) {
        VSDListener l = listenerTable.get(id);
        log.debug("Set listener location " + sp + " listener: " + l);
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
            log.debug("Location : " + l + " ... ignoring.");
            // Physical location at origin means it hasn't been set.
            return;
        }
        log.debug("Decoder Address: " + a.getNumber());
        for (VSDecoder d : decodertable.values()) {
            // Get the Decoder's address protocol.  If it's a DCC_LONG or DCC_SHORT, convert to DCC
            // since the LnReporter can't tell the difference and will always report "DCC".
            if (d == null) {
                log.debug("VSdecoder null pointer!");
                return;
            }
            LocoAddress pa = d.getAddress();
            if (pa == null) {
                log.debug("Vsdecoder" + d + " address null!");
                return;
            }
            LocoAddress.Protocol p = d.getAddress().getProtocol();
            if (p == null) {
                log.debug("Vsdecoder" + d + " address = " + pa + " protocol null!");
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
     * getProfilePath()
     *
     * Retrieve the Path for a given Profile name.
     */
    public String getProfilePath(String profile) {
        return (profiletable.get(profile));
    }

    /**
     * Load Profiles from a VSD file Not deprecated anymore. used by the new
     * ConfigDialog.
     */
    public void loadProfiles(String path) {
        try {
            VSDFile vsdfile = new VSDFile(path);
            if (vsdfile.isInitialized()) {
                this.loadProfiles(vsdfile);
            }
        } catch (java.util.zip.ZipException e) {
            log.error("ZipException loading VSDecoder from " + path);
            // would be nice to pop up a dialog here...
        } catch (java.io.IOException ioe) {
            log.error("IOException loading VSDecoder from " + path);
            // would be nice to pop up a dialog here...
        }
    }

    protected void registerReporterListener(String sysName) {
        Reporter r = jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getReporter(sysName);
        if (r == null) {
            return;
        }
        jmri.NamedBeanHandle<Reporter> h = nbhm.getNamedBeanHandle(sysName, r);
        if (h == null) {
            return;
        }
        // Make sure we aren't already registered.
        java.beans.PropertyChangeListener[] ll = r.getPropertyChangeListenersByReference(h.getName());
        if (ll.length == 0) {
            r.addPropertyChangeListener(this, h.getName(), vsd_property_change_name);
        }
    }

    protected void registerBeanListener(Manager beanManager, String sysName) {
        NamedBean b = beanManager.getBeanBySystemName(sysName);
        if (b == null) {
            log.debug("No bean by name " + sysName);
            return;
        }
        jmri.NamedBeanHandle<NamedBean> h = nbhm.getNamedBeanHandle(sysName, b);
        if (h == null) {
            log.debug("no handle for bean " + b.getDisplayName());
            return;
        }
        // Make sure we aren't already registered.
        java.beans.PropertyChangeListener[] ll = b.getPropertyChangeListenersByReference(h.getName());
        if (ll.length == 0) {
            b.addPropertyChangeListener(this, h.getName(), vsd_property_change_name);
            log.debug("Added listener to bean " + b.getDisplayName() + " type " + b.getClass().getName());
        }
    }

    protected void registerReporterListeners() {
        // Walk through the list of reporters
        for (String sysName : jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getSystemNameList()) {
            registerReporterListener(sysName);
        }
        for (String sysname : jmri.InstanceManager.getDefault(jmri.BlockManager.class).getSystemNameList()) {
            registerBeanListener(jmri.InstanceManager.getDefault(jmri.BlockManager.class), sysname);
        }
    }

    // This listener listens to the ReporterManager for changes to the list of Reporters.
    // Need to trap list length (name="length") changes and add listeners when new ones are added.
    private void setupReporterManagerListener() {
        // Register ourselves as a listener for changes to the Reporter list.  For now, we won't do this. Just force a
        // save and reboot after reporters are added.  We'll fix this later.
        // jmri.InstanceManager.getDefault(jmri.ReporterManager.class).addPropertyChangeListener(new PropertyChangeListener() {
        // public void propertyChange(PropertyChangeEvent event) {
        //      log.debug("property change name " + event.getPropertyName() + " old " + event.getOldValue() + " new " + event.getNewValue());
        //     reporterManagerPropertyChange(event);
        // }
        //   });
        jmri.InstanceManager.getDefault(jmri.ReporterManager.class).addPropertyChangeListener(this);

        // Now, the Reporter Table might already be loaded and filled out, so we need to get all the Reporters and list them.
        // And add ourselves as a listener to them.
        for (String sysName : jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getSystemNameList()) {
            registerReporterListener(sysName);
        }
        for (String sysname : jmri.InstanceManager.getDefault(jmri.BlockManager.class).getSystemNameList()) {
            registerBeanListener(jmri.InstanceManager.getDefault(jmri.BlockManager.class), sysname);
        }
    }

    protected void shutdownDecoders() {
        // Shut down and destroy all running VSDecoders.
        Set<String> vk = decodertable.keySet();
        Iterator<String> it = vk.iterator();
        while (it.hasNext()) {
            VSDecoder v = decodertable.get(it.next());
            v.shutdown();
        }
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
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        log.debug("property change type " + evt.getSource().getClass().getName()
                + " name " + evt.getPropertyName() + " old " + evt.getOldValue()
                + " new " + evt.getNewValue());
        if (evt.getSource() instanceof jmri.ReporterManager) {
            reporterManagerPropertyChange(evt);
        } else if (evt.getSource() instanceof jmri.Reporter) {
            reporterPropertyChange(evt);
        } else if (evt.getSource() instanceof jmri.Block) {
            log.debug("Block property change! name = " + evt.getPropertyName() + " old= " + evt.getOldValue() + " new= " + evt.getNewValue());
            blockPropertyChange(evt);
        } else if (evt.getSource() instanceof VSDManagerFrame) {
            if (evt.getPropertyName().equals(VSDManagerFrame.PCIDMap.get(VSDManagerFrame.PropertyChangeID.REMOVE_DECODER))) {
                // Shut down the requested decoder and remove it from the manager's hash maps. 
                // Unless there are "illegal" handles, this should put the decoder on the garbage heap.  I think.
                String sa = (String) evt.getNewValue();
                VSDecoder d = this.getVSDecoderByAddress(sa);
                log.debug("Removing Decoder " + sa + " ... " + d.getAddress());
                d.shutdown();
                decodertable.remove(d.getId());
                decoderAddressMap.remove(sa);
                //debugPrintDecoderList();
            } else if (evt.getPropertyName().equals(VSDManagerFrame.PCIDMap.get(VSDManagerFrame.PropertyChangeID.CLOSE_WINDOW))) {
                // Note this assumes there is only one VSDManagerFrame open at a time.
                shutdownDecoders();
                if (managerFrame != null) {
                    managerFrame.dispose();
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
                        log.debug("Block " + blk.getSystemName() + " has no reporter!  Skipping state-type report.");
                        return;
                    }
                    if (blk.isReportingCurrent()) {
                        repVal = (String) blk.getReporter().getCurrentReport();
                    } else {
                        repVal = (String) blk.getReporter().getLastReport();
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
                log.warn("Report from Block " + blk.getUserName() + " is null!");
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
            if (event.getNewValue() instanceof String) {
                String newValue = (String) event.getNewValue();
                if (arp.getDirection(newValue) == PhysicalLocationReporter.Direction.ENTER) {
                    setDecoderPositionByAddr(arp.getLocoAddress(newValue), arp.getPhysicalLocation(newValue));
                }
            } else if (event.getNewValue() instanceof IdTag) {
                // newValue is of IdTag type.
                // Dcc4Pc, Ecos, 
                // Assume Reporter "arp" is the most recent seen location
                IdTag newValue = (IdTag) event.getNewValue();
                setDecoderPositionByAddr(arp.getLocoAddress(newValue.getTagID()), arp.getPhysicalLocation(null));
            } else {
                log.debug("Reporter's return type is not supported.");
                // do nothing
            }

        } else {
            log.debug("Reporter doesn't support physical location reporting or isn't reporting new info.");
        }  // Reporting object implements PhysicalLocationReporter
        return;
    }

    public void reporterManagerPropertyChange(PropertyChangeEvent event) {
        String eventName = event.getPropertyName();

        log.debug("VSDecoder received Reporter Manager Property Change: " + eventName);
        if (eventName.equals("length")) { // NOI18N

            // Re-register for all the reporters. The registerReporterListener() will skip
            // any that we're already registered for.
            for (String sysName : jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getSystemNameList()) {
                registerReporterListener(sysName);
            }

            // It could be that we lost a Reporter.  But since we aren't keeping a list anymore
            // we don't care.
        }
    }

    public void loadProfiles(VSDFile vf) {
        Element root;
        String pname;
        if ((root = vf.getRoot()) == null) {
            return;
        }

        ArrayList<String> new_entries = new ArrayList<String>();

        java.util.Iterator<Element> i = root.getChildren("profile").iterator(); // NOI18N
        while (i.hasNext()) {
            Element e = i.next();
            log.debug(e.toString());
            if ((pname = e.getAttributeValue("name")) != null) { // NOI18N
                profiletable.put(pname, vf.getName());
                new_entries.add(pname);
            }
        }

 // debug
 /*
         for (String s : new_entries) {
         log.debug("New entry: " + s);
         }
         */
        // /debug
        fireMyEvent(new VSDManagerEvent(this, VSDManagerEvent.EventType.PROFILE_LIST_CHANGE, new_entries));
    }

    private static final Logger log = LoggerFactory.getLogger(VSDecoderManager.class);

}
