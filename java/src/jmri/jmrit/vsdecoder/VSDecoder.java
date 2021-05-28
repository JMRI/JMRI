package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import jmri.Audio;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.vsdecoder.swing.VSDControl;
import jmri.jmrit.vsdecoder.swing.VSDManagerFrame;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a software "decoder" that responds to throttle inputs and
 * generates sounds in responds to them.
 * <p>
 * Each VSDecoder implements exactly one Sound Profile (describes a particular
 * type of locomotive, say, an EMD GP7).
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
 * @author Mark Underwood Copyright (C) 2011
 * @author Klaus Killinger Copyright (C) 2018-2021
 */
public class VSDecoder implements PropertyChangeListener {

    boolean initialized = false; // This decoder has been initialized
    boolean enabled = false; // This decoder is enabled
    private boolean is_default = false; // This decoder is the default for its file
    private boolean create_xy_series = false; // Create xy coordinates in console

    private VSDConfig config;

    // For use in VSDecoderManager
    int dirfn = 1;
    float currentspeed = 0.0f; // result of speedCurve(T)
    PhysicalLocation lastPos;
    PhysicalLocation startPos;
    int topspeed;
    int topspeed_rev;
    int setup_index; // Can be set by a Route
    boolean is_muted;
    VSDSound savedSound;

    HashMap<String, VSDSound> sound_list; // list of sounds
    HashMap<String, SoundEvent> event_list; // list of events

    /**
     * Construct a VSDecoder with the given system name (id) and configuration
     * (config)
     *
     * @param cfg (VSDConfig) Configuration
     */
    public VSDecoder(VSDConfig cfg) {
        config = cfg;

        sound_list = new HashMap<>();
        event_list = new HashMap<>();

        // Force re-initialization
        initialized = _init();

        try {
            VSDFile vsdfile = new VSDFile(config.getVSDPath());
            if (vsdfile.isInitialized()) {
                log.debug("Constructor: vsdfile init OK, loading XML...");
                this.setXml(vsdfile, config.getProfileName());
            } else {
                log.debug("Constructor: vsdfile init FAILED.");
                initialized = false;
            }
        } catch (java.util.zip.ZipException e) {
            log.error("ZipException loading VSDecoder from {}", config.getVSDPath());
            // would be nice to pop up a dialog here...
        } catch (java.io.IOException ioe) {
            log.error("IOException loading VSDecoder from {}", config.getVSDPath());
            // would be nice to pop up a dialog here...
        }

        // Since the Config already has the address set, we need to call
        // our own setAddress() to register the throttle listener
        this.setAddress(config.getLocoAddress());
        this.enable();

        // Handle Advanced Location Following (if the parameter file is OK)
        if (VSDecoderManager.instance().geofile_ok) {
            this.setup_index = 0;
        }

        if (log.isDebugEnabled()) {
            log.debug("VSDecoder Init Complete.  Audio Objects Created:");
            jmri.InstanceManager.getDefault(jmri.AudioManager.class).getNamedBeanSet(Audio.SOURCE).forEach((s) -> {
                log.debug("\tSource: {}", s);
            });
            jmri.InstanceManager.getDefault(jmri.AudioManager.class).getNamedBeanSet(Audio.BUFFER).forEach((s) -> {
                log.debug("\tBuffer: {}", s);
            });
        }
    }

    /**
     * Construct a VSDecoder with the given system name (id), profile name and
     * VSD file path
     *
     * @param id   (String) System name for this VSDecoder
     * @param name (String) Profile name
     * @param path (String) Path to a VSD file to pull the given Profile from
     */
    public VSDecoder(String id, String name, String path) {

        config = new VSDConfig();
        config.setProfileName(name);
        config.setId(id);

        sound_list = new HashMap<>();
        event_list = new HashMap<>();

        // Force re-initialization
        initialized = _init();

        config.setVSDPath(path);

        try {
            VSDFile vsdfile = new VSDFile(path);
            if (vsdfile.isInitialized()) {
                log.debug("Constructor: vsdfile init OK, loading XML...");
                this.setXml(vsdfile, name);
            } else {
                log.debug("Constructor: vsdfile init FAILED.");
                initialized = false;
            }
        } catch (java.util.zip.ZipException e) {
            log.error("ZipException loading VSDecoder from {}", path);
            // would be nice to pop up a dialog here...
        } catch (java.io.IOException ioe) {
            log.error("IOException loading VSDecoder from {}", path);
            // would be nice to pop up a dialog here...
        }
    }

    private boolean _init() {
        // Do nothing for now
        this.enable();
        return true;
    }

    /**
     * Get the ID (System Name) of this VSDecoder
     *
     * @return (String) system name of this VSDecoder
     */
    public String getId() {
        return config.getId();
    }

    /**
     * Check whether this VSDecoder has completed initialization
     *
     * @return (boolean) true if initialization is complete.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Set the VSD File path for this VSDecoder to use
     *
     * @param p (String) path to VSD File
     */
    public void setVSDFilePath(String p) {
        config.setVSDPath(p);
    }

    /**
     * Get the current VSD File path for this VSDecoder
     *
     * @return (String) path to VSD file
     */
    public String getVSDFilePath() {
        return config.getVSDPath();
    }

    /**
     * Shut down this VSDecoder and all of its associated sounds.
     */
    public void shutdown() {
        log.debug("Shutting down sounds...");
        for (VSDSound vs : sound_list.values()) {
            log.debug("Stopping sound: {}", vs.getName());
            vs.shutdown();
        }
    }

    /**
     * Handle the details of responding to a PropertyChangeEvent from a
     * throttle.
     *
     * @param event (PropertyChangeEvent) Throttle event to respond to
     */
    protected void throttlePropertyChange(PropertyChangeEvent event) {
        // WARNING: FRAGILE CODE
        // This will break if the return type of the event.getOld/NewValue() changes.

        String eventName = event.getPropertyName();

        // Skip this if disabled
        if (!enabled) {
            log.debug("VSDecoder disabled. Take no action.");
            return;
        }

        log.debug("VSDecoderPane throttle property change: {}", eventName);

        if (eventName.equals("throttleAssigned")) {
            Float s = (Float) jmri.InstanceManager.throttleManagerInstance().getThrottleInfo(config.getDccAddress(), Throttle.SPEEDSETTING);
            if (s != null) {
                this.getEngineSound().setFirstSpeed(true); // Auto-start needs this
                // Mimic a throttlePropertyChange to propagate the current (init) speed setting of the throttle.
                log.debug("Existing DCC Throttle found. Speed: {}", s);
                this.throttlePropertyChange(new PropertyChangeEvent(this, Throttle.SPEEDSETTING, null, s));
            }

            // Check for an existing throttle and get loco direction if it exists.
            Boolean b = (Boolean) jmri.InstanceManager.throttleManagerInstance().getThrottleInfo(config.getDccAddress(), Throttle.ISFORWARD);
            if (b != null) {
                dirfn = b ? 1 : -1;
                log.debug("Existing DCC Throttle found. IsForward is {}", b);
                log.debug("Initial dirfn: {} for {}", dirfn, config.getDccAddress());
                this.throttlePropertyChange(new PropertyChangeEvent(this, Throttle.ISFORWARD, null, b));
            } else {
                log.warn("No existing DCC throttle found.");
            }

            // Check for an existing throttle and get ENGINE throttle function key status if it exists.
            // For all function keys used in config.xml (sound-event name="ENGINE") this will send an initial value! This could be ON or OFF.
            if (event_list.get("ENGINE") != null) {
                for (Trigger t : event_list.get("ENGINE").trigger_list.values()) {
                    log.debug("ENGINE trigger  Name: {}, Event: {}, t: {}", t.getName(), t.getEventName(), t);
                    if (t.getEventName().startsWith("F")) {
                        log.debug("F-Key trigger found: {}, name: {}, event: {}", t, t.getName(), t.getEventName());
                        // Don't send an initial value if trigger is ENGINE_STARTSTOP, because that would work against auto-start; BRAKE_KEY would play a sound
                        if (!t.getName().equals("ENGINE_STARTSTOP") && !t.getName().equals("BRAKE_KEY")) {
                            b = (Boolean) jmri.InstanceManager.throttleManagerInstance().getThrottleInfo(config.getDccAddress(), t.getEventName());
                            if (b != null) {
                                this.throttlePropertyChange(new PropertyChangeEvent(this, t.getEventName(), null, b));
                            }
                        }
                    }
                }
            }
        }

        // Iterate through the list of sound events, forwarding the propertyChange event.
        for (SoundEvent t : event_list.values()) {
            t.propertyChange(event);
        }

        if (eventName.equals(Throttle.SPEEDSETTING)) {
            currentspeed = (float) this.getEngineSound().speedCurve((float) event.getNewValue());
        }

        if (eventName.equals(Throttle.ISFORWARD)) {
            dirfn = (Boolean) event.getNewValue() ? 1 : -1;
        }
    }

    /**
     * Set this VSDecoder's LocoAddress, and register to follow events from the
     * throttle with this address.
     *
     * @param l (LocoAddress) LocoAddress to be followed
     */
    public void setAddress(LocoAddress l) {
        // Hack for ThrottleManager Dcc dependency
        config.setLocoAddress(l);
        jmri.InstanceManager.throttleManagerInstance().attachListener(config.getDccAddress(),
                new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                log.debug("property change name: {}, old: {}, new: {}", event.getPropertyName(), event.getOldValue(), event.getNewValue());
                throttlePropertyChange(event);
            }
        });
        log.debug("VSDecoder: Address set to {}", config.getLocoAddress());
    }

    /**
     * Get the currently assigned LocoAddress
     *
     * @return the currently assigned LocoAddress
     */
    public LocoAddress getAddress() {
        return config.getLocoAddress();
    }

    public RosterEntry getRosterEntry() {
        return config.getRosterEntry();
    }

    /**
     * Get the current decoder volume setting for this VSDecoder
     *
     * @return (float) volume level (0.0 - 1.0)
     */
    public float getDecoderVolume() {
        return config.getVolume();
    }

    private void forwardMasterVolume(float volume) {
        log.debug("VSD config id: {}, Master volume: {}, Decoder volume: {}", config.getId(), volume, config.getVolume());
        for (VSDSound vs : sound_list.values()) {
            vs.setVolume(volume * config.getVolume());
        }
    }

    /**
     * Set the decoder volume for this VSDecoder
     *
     * @param decoder_volume (float) volume level (0.0 - 1.0)
     */
    public void setDecoderVolume(float decoder_volume) {
        config.setVolume(decoder_volume);
        float master_vol = 0.01f * VSDecoderManager.instance().getMasterVolume();
        log.debug("config set decoder volume to {}, master volume adjusted: {}", decoder_volume, master_vol);
        for (VSDSound vs : sound_list.values()) {
            vs.setVolume(master_vol * decoder_volume);
        }
    }

    /**
     * Is this VSDecoder muted?
     *
     * @return true if muted
     */
    public boolean isMuted() {
        return getMuteState();
    }

    /**
     * Mute or un-mute this VSDecoder
     *
     * @param m (boolean) true to mute, false to un-mute
     */
    public void mute(boolean m) {
        for (VSDSound vs : sound_list.values()) {
            vs.mute(m);
        }
    }

    private void setMuteState(boolean m) {
        is_muted = m;
    }

    private boolean getMuteState() {
        return is_muted;
    }

    /**
     * set the x/y/z position in the soundspace of this VSDecoder Translates the
     * given position to a position relative to the listener for the component
     * VSDSounds.
     * <p>
     * The idea is that the user-preference Listener Position (relative to the
     * USER's chosen origin) is always the OpenAL Context's origin.
     *
     * @param p (PhysicalLocation) location relative to the user's chosen
     *          Origin.
     */
    public void setPosition(PhysicalLocation p) {
        // Store the actual position relative to the user's Origin locally.
        config.setPhysicalLocation(p);
        if (create_xy_series) {   
            log.info("{}: {}\t{}", this.getAddress(), (float) Math.round(p.x*10000)/10000, p.y);
        }
        log.debug("( {} ). Set Position: {}", this.getAddress(), p);

        this.lastPos = p; // save this position

        // Give all of the VSDSound objects the position translated relative to the listener position.
        // This is a workaround for OpenAL requiring the listener position to always be at (0,0,0).
        /*
         * PhysicalLocation ref = VSDecoderManager.instance().getVSDecoderPreferences().getListenerPhysicalLocation();
         * if (ref == null) ref = PhysicalLocation.Origin;
         */
        for (VSDSound s : sound_list.values()) {
            // s.setPosition(PhysicalLocation.translate(p, ref));
            s.setPosition(p);
        }

        // Set (relative) volume for this location (in case we're in a tunnel)
        float tv = 0.01f * VSDecoderManager.instance().getMasterVolume() * getDecoderVolume();
        log.debug("current master volume: {}, decoder volume: {}", VSDecoderManager.instance().getMasterVolume(), getDecoderVolume());
        if (savedSound.getTunnel()) {
            tv *= VSDSound.tunnel_volume;
            log.debug("VSD: In tunnel, volume: {}", tv);
        } else {
            log.debug("VSD: Not in tunnel, volume: {}", tv);
        }
        if (! getMuteState()) {
            for (VSDSound vs : sound_list.values()) {
                vs.setVolume(tv);
            }
        }
    }

    /**
     * Get the current x/y/z position in the soundspace of this VSDecoder
     *
     * @return PhysicalLocation location of this VSDecoder
     */
    public PhysicalLocation getPosition() {
        return config.getPhysicalLocation();
    }

    /**
     * Respond to property change events from this VSDecoder's GUI
     *
     * @param evt (PropertyChangeEvent) event to respond to
     */
    @SuppressWarnings("cast")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        // Respond to events from the new GUI.
        if (evt.getSource() instanceof VSDControl) {
            if (property.equals(VSDControl.OPTION_CHANGE)) {
                Train selected_train = jmri.InstanceManager.getDefault(TrainManager.class).getTrainByName((String) evt.getNewValue());
                if (selected_train != null) {
                    selected_train.addPropertyChangeListener(this);
                    // Handle Advanced Location Following (if the parameter file is OK)
                    if (VSDecoderManager.instance().geofile_ok) {
                        Route r = selected_train.getRoute();
                        if (r != null) {
                            log.info("Train \"{}\" selected for {} - Route is now \"{}\"", selected_train, this.getAddress(), r.getName());
                            if (r.getName().equals("VSDRoute1")) {
                                this.setup_index = 0;
                            } else if (r.getName().equals("VSDRoute2") && VSDecoderManager.instance().num_setups > 1) {
                                this.setup_index = 1;
                            } else if (r.getName().equals("VSDRoute3") && VSDecoderManager.instance().num_setups > 2) {
                                this.setup_index = 2;
                            } else if (r.getName().equals("VSDRoute4") && VSDecoderManager.instance().num_setups > 3) {
                                this.setup_index = 3;
                            } else {
                                log.warn("\"{}\" is not suitable for VSD Advanced Location Following", r.getName());
                            }
                        } else {
                            log.warn("Train \"{}\" is without Route", selected_train);
                        }
                    }
                }
            }
            return;
        }

        if (property.equals(VSDManagerFrame.MUTE)) {
            // GUI Mute button
            log.debug("VSD: Mute change. value: {}", evt.getNewValue());
            setMuteState((boolean) evt.getNewValue());
            this.mute(getMuteState());
        } else if (property.equals(VSDManagerFrame.VOLUME_CHANGE)) {
            // GUI Volume slider (Master Volume)
            log.debug("VSD: Volume change. value: {}", evt.getOldValue());
            // Slider gives integer 0-100. Need to change that to a float 0.0-1.0
            this.forwardMasterVolume((0.01f * (Integer) evt.getOldValue()));
        } else if (property.equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY)) {
            // Train Location Move
            PhysicalLocation p = getTrainPosition((Train) evt.getSource());
            if (p != null) {
                this.setPosition(getTrainPosition((Train) evt.getSource()));
            } else {
                log.debug("Train has null position");
                this.setPosition(new PhysicalLocation());
            }
        } else if (property.equals(Train.STATUS_CHANGED_PROPERTY)) {
            // Train Status change
            String status = (String) evt.getOldValue();
            log.debug("Train status changed: {}", status);
            log.debug("New Location: {}", getTrainPosition((Train) evt.getSource()));
            if ((status.startsWith(Train.BUILT)) || (status.startsWith(Train.PARTIAL_BUILT))) {
                log.debug("Train built. status: {}", status);
                PhysicalLocation p = getTrainPosition((Train) evt.getSource());
                if (p != null) {
                    this.setPosition(getTrainPosition((Train) evt.getSource()));
                } else {
                    log.debug("Train has null position");
                    this.setPosition(new PhysicalLocation());
                }
            }
        }
    }

    // Methods for handling location tracking based on JMRI Operations
    /**
     * Get the physical location of the given Operations Train
     *
     * @param t (Train) the Train to interrogate
     * @return PhysicalLocation location of the train
     */
    protected PhysicalLocation getTrainPosition(Train t) {
        if (t == null) {
            log.debug("Train is null.");
            return null;
        }
        RouteLocation rloc = t.getCurrentRouteLocation();
        if (rloc == null) {
            log.debug("RouteLocation is null.");
            return null;
        }
        Location loc = rloc.getLocation();
        if (loc == null) {
            log.debug("Location is null.");
            return null;
        }
        return loc.getPhysicalLocation();
    }

    // Methods for handling the underlying sounds
    /**
     * Retrieve the VSDSound with the given system name
     *
     * @param name (String) System name of the requested VSDSound
     * @return VSDSound the requested sound
     */
    public VSDSound getSound(String name) {
        return sound_list.get(name);
    }

    // Java Bean set/get Functions
    /**
     * Set the profile name to the given string
     *
     * @param pn (String) : name of the profile to set
     */
    public void setProfileName(String pn) {
        config.setProfileName(pn);
    }

    /**
     * get the currently selected profile name
     *
     * @return (String) name of the currently selected profile
     */
    public String getProfileName() {
        return config.getProfileName();
    }

    /**
     * Enable this VSDecoder.
     */
    public void enable() {
        enabled = true;
    }

    /**
     * Disable this VSDecoder.
     */
    public void disable() {
        enabled = false;
    }

    /**
     * Get a reference to the EngineSound associated with this VSDecoder
     *
     * @return EngineSound The EngineSound reference for this VSDecoder or null
     */
    public EngineSound getEngineSound() {
        return (EngineSound) sound_list.get("ENGINE");
    }

    /**
     * Get a Collection of SoundEvents associated with this VSDecoder
     *
     * @return {@literal Collection<SoundEvent>} collection of SoundEvents
     */
    public Collection<SoundEvent> getEventList() {
        return event_list.values();
    }

    /**
     * True if this is the default VSDecoder
     *
     * @return boolean true if this is the default VSDecoder
     *
     * @deprecated As of 4.23.3, without a replacement
     */
    @Deprecated // 4.23.3 
    public boolean isDefault() {
        return is_default;
    }

    /**
     * Set whether this is the default VSDecoder or not
     *
     * @param d (boolean) True to set this as the default, False if not.
     *
     * @deprecated As of 4.23.3, without a replacement
     */
    @Deprecated // 4.23.3
    public void setDefault(boolean d) {
        is_default = d;
    }

    /**
     * Get an XML representation of this VSDecoder Includes a subtree of
     * Elements for all of the associated SoundEvents, Triggers, VSDSounds, etc.
     *
     * @return Element XML Element for this VSDecoder
     */
    public Element getXml() {
        Element me = new Element("vsdecoder");
        ArrayList<Element> le = new ArrayList<>();

        me.setAttribute("name", this.config.getProfileName());

        // If this decoder is marked as default, add the default Element.
        if (is_default) {
            me.addContent(new Element("default"));
        }

        for (SoundEvent se : event_list.values()) {
            le.add(se.getXml());
        }

        for (VSDSound vs : sound_list.values()) {
            le.add(vs.getXml());
        }

        me.addContent(le);

        // Need to add whatever else here.
        return me;
    }

    /**
     * Build this VSDecoder from an XML representation
     *
     * @param vf (VSDFile) : VSD File to pull the XML from
     * @param pn (String) : Parameter Name to find within the VSD File.
     */
    @SuppressWarnings("cast")
    public void setXml(VSDFile vf, String pn) {
        Iterator<Element> itr;
        Element e = null;
        Element el = null;
        SoundEvent se;
        String n;

        if (vf == null) {
            log.debug("Null VSD File Name");
            return;
        }

        log.debug("VSD File Name: {}, profile: {}", vf.getName(), pn);
        // need to choose one.
        this.setVSDFilePath(vf.getName());

        // Find the <profile/> element that matches the name pn
        // List<Element> profiles = vf.getRoot().getChildren("profile");
        // java.util.Iterator i = profiles.iterator();
        java.util.Iterator<Element> i = vf.getRoot().getChildren("profile").iterator();
        while (i.hasNext()) {
            e = i.next();
            if (e.getAttributeValue("name").equals(pn)) {
                break;
            }
        }
        // E is now the first <profile/> in vsdfile that matches pn.

        if (e == null) {
            // No matching profile name found.
            return;
        }

        // Set this decoder's name.
        this.setProfileName(e.getAttributeValue("name"));
        log.debug("Decoder Name: {}", e.getAttributeValue("name"));

        // Read and create all of its components.
        // Check for default element.
        if (e.getChild("default") != null) {
            log.debug("{} is default", getProfileName());
            is_default = true;
        } else {
            is_default = false;
        }

        // Check for a flag element to create xy-position-coordinates.
        n = e.getChildText("create-xy-series");
        if ((n != null) && (n.equals("yes"))) {
            create_xy_series = true;
            log.debug("Profile {}: xy-position-coordinates will be created in JMRI System Console", getProfileName());
        } else {
            create_xy_series = false;
            log.debug("Profile {}: xy-position-coordinates will NOT be created in JMRI System Console", getProfileName());
        }

        // Check for an optional sound start-position.
        n = e.getChildText("start-position");
        if (n != null) {
            startPos = PhysicalLocation.parse(n);
        } else {
            startPos = null;
        }
        log.debug("Start position: {}", startPos);

        // +++ DEBUG
        // Log and print all of the child elements.
        itr = (e.getChildren()).iterator();
        while (itr.hasNext()) {
            // Pull each element from the XML file.
            el = itr.next();
            log.debug("Element: {}", el);
            if (el.getAttribute("name") != null) {
                log.debug("  Name: {}", el.getAttributeValue("name"));
                log.debug("   type: {}", el.getAttributeValue("type"));
            }
        }
        // --- DEBUG

        // First, the sounds.
        String prefix = "" + this.getId() + ":";
        log.debug("VSDecoder {}, prefix: {}", this.getId(), prefix);
        itr = (e.getChildren("sound")).iterator();
        while (itr.hasNext()) {
            el = (Element) itr.next();
            if (el.getAttributeValue("type") == null) {
                // Empty sound. Skip.
                log.debug("Skipping empty Sound.");
                continue;
            } else if (el.getAttributeValue("type").equals("configurable")) {
                // Handle configurable sounds.
                ConfigurableSound cs = new ConfigurableSound(prefix + el.getAttributeValue("name"));
                cs.setXml(el, vf);
                sound_list.put(el.getAttributeValue("name"), cs);
            } else if (el.getAttributeValue("type").equals("diesel")) {
                // Handle a diesel Engine sound
                DieselSound es = new DieselSound(prefix + el.getAttributeValue("name"));
                es.setXml(el, vf);
                sound_list.put(el.getAttributeValue("name"), es);
            } else if (el.getAttributeValue("type").equals("diesel3")) {
                // Handle a diesel3 Engine sound
                Diesel3Sound es = new Diesel3Sound(prefix + el.getAttributeValue("name"));
                savedSound = es;
                es.setXml(el, vf);
                sound_list.put(el.getAttributeValue("name"), es);
                topspeed = es.top_speed;
                topspeed_rev = topspeed;
            } else if (el.getAttributeValue("type").equals("steam")) {
                // Handle a steam Engine sound
                SteamSound es = new SteamSound(prefix + el.getAttributeValue("name"));
                savedSound = es;
                es.setXml(el, vf);
                sound_list.put(el.getAttributeValue("name"), es);
                topspeed = es.top_speed;
                topspeed_rev = topspeed;
            } else if (el.getAttributeValue("type").equals("steam1")) {
                // Handle a steam1 Engine sound
                Steam1Sound es = new Steam1Sound(prefix + el.getAttributeValue("name"));
                savedSound = es;
                es.setXml(el, vf);
                sound_list.put(el.getAttributeValue("name"), es);
                topspeed = es.top_speed;
                topspeed_rev = es.top_speed_reverse;
            } else {
                // TODO: Some type other than configurable sound. Handle appropriately
            }
        }

        // Next, grab all of the SoundEvents
        // Have to do the sounds first because the SoundEvent's setXml() will
        // expect to be able to look it up.
        itr = (e.getChildren("sound-event")).iterator();
        while (itr.hasNext()) {
            el = (Element) itr.next();
            switch (SoundEvent.ButtonType.valueOf(el.getAttributeValue("buttontype").toUpperCase())) {
                case MOMENTARY:
                    se = new MomentarySoundEvent(el.getAttributeValue("name"));
                    break;
                case TOGGLE:
                    se = new ToggleSoundEvent(el.getAttributeValue("name"));
                    break;
                case ENGINE:
                    se = new EngineSoundEvent(el.getAttributeValue("name"));
                    break;
                case NONE:
                default:
                    se = new SoundEvent(el.getAttributeValue("name"));
            }
            se.setParent(this);
            se.setXml(el, vf);
            event_list.put(se.getName(), se);
        }
        // Handle other types of children similarly here.
    }

    private static final Logger log = LoggerFactory.getLogger(VSDecoder.class);

}
