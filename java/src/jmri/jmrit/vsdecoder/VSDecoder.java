package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import jmri.Audio;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.vsdecoder.swing.VSDControl;
import jmri.jmrit.vsdecoder.swing.VSDManagerFrame;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual Sound Decoder
 * <p>
 * Implements a software "decoder" that responds to throttle inputs and
 * generates sounds in responds to them.
 * <p>
 * Each VSDecoder implements exactly one Sound Profile (describes a particular
 * type of locomtive, say, an EMD GP7).
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class VSDecoder implements PropertyChangeListener {

    boolean initialized = false; // This decoder has been initialized
    boolean enabled = false; // This decoder is enabled
    private boolean is_default = false; // This decoder is the default for its file

    private VSDConfig config;

    private float tunnelVolume = 0.5f;

    // List of registered event listeners
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    HashMap<String, VSDSound> sound_list; // list of sounds
    HashMap<String, Trigger> trigger_list; // list of triggers
    HashMap<String, SoundEvent> event_list; // list of events

    /**
     * Construct a VSDecoder with a given name and ID (system name)
     *
     * @param id   (String) System Name of this VSDecoder
     * @param name (String) Sound Profile name for this VSDecoder
     */
    @Deprecated
    public VSDecoder(String id, String name) {

        config = new VSDConfig();
        config.setProfileName(name);
        config.setId(id);

        sound_list = new HashMap<String, VSDSound>();
        trigger_list = new HashMap<String, Trigger>();
        event_list = new HashMap<String, SoundEvent>();

        // Force re-initialization
        initialized = _init();
    }

    /**
     * Construct a VSDecoder with the given system name (id) and configuration
     * (config)
     *
     * @param cfg (VSDConfig) Configuration
     */
    public VSDecoder(VSDConfig cfg) {
        config = cfg;

        sound_list = new HashMap<String, VSDSound>();
        trigger_list = new HashMap<String, Trigger>();
        event_list = new HashMap<String, SoundEvent>();

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
            log.error("ZipException loading VSDecoder from " + config.getVSDPath());
            // would be nice to pop up a dialog here...
        } catch (java.io.IOException ioe) {
            log.error("IOException loading VSDecoder from " + config.getVSDPath());
            // would be nice to pop up a dialog here...
        }

        // Since the Config already has the address set, we need to call
        // our own setAddress() to register the throttle listener
        this.setAddress(config.getLocoAddress());
        this.enable();

        if (log.isDebugEnabled()) {
            log.debug("VSDecoder Init Complete.  Audio Objects Created:");
            for (String s : InstanceManager.getDefault(jmri.AudioManager.class).getSystemNameList(Audio.SOURCE)) {
                log.debug("\tSource: " + s);
            }
            for (String s : InstanceManager.getDefault(jmri.AudioManager.class).getSystemNameList(Audio.BUFFER)) {
                log.debug("\tBuffer: " + s);
            }
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

        sound_list = new HashMap<String, VSDSound>();
        trigger_list = new HashMap<String, Trigger>();
        event_list = new HashMap<String, SoundEvent>();

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
            log.error("ZipException loading VSDecoder from " + path);
            // would be nice to pop up a dialog here...
        } catch (java.io.IOException ioe) {
            log.error("IOException loading VSDecoder from " + path);
            // would be nice to pop up a dialog here...
        }
    }

    private boolean _init() {
        // Do nothing for now
        this.enable();
        return (true);
    }

    /**
     * Get the ID (System Name) of this VSDecoder
     *
     * @return (String) system name of this VSDecoder
     */
    public String getId() {
        return (config.getId());
    }

    /**
     * Check whether this VSDecoder has completed initialization
     *
     * @return (boolean) true if initialization is complete.
     */
    public boolean isInitialized() {
        return (initialized);
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
        return (config.getVSDPath());
    }

    // VSDecoder Events
    /**
     * Add a listener for this object's events
     *
     * @param listener handle
     */
    public void addEventListener(VSDecoderListener listener) {
        listenerList.add(VSDecoderListener.class, listener);
    }

    /**
     * Remove a listener for this object's events
     *
     * @param listener handle
     */
    public void removeEventListener(VSDecoderListener listener) {
        listenerList.remove(VSDecoderListener.class, listener);
    }

    /**
     * Fire an event to this object's listeners
     */
    private void fireMyEvent(VSDecoderEvent evt) {
        for (VSDecoderListener l : listenerList.getListeners(VSDecoderListener.class)) {
            l.eventAction(evt);
        }
    }

    /**
     * Handle Window events from this VSDecoder's GUI window.
     *
     * @param e the window event to handle
     */
    public void windowChange(java.awt.event.WindowEvent e) {
        log.debug("decoder.windowChange() - " + e.toString());
        log.debug("param string = " + e.paramString());
        // if (e.paramString().equals("WINDOW_CLOSING")) {
        // Shut down the sounds.
        this.shutdown();

        // }
    }

    /**
     * Shut down this VSDecoder and all of its associated sounds.
     */
    public void shutdown() {
        log.debug("Shutting down sounds...");
        for (VSDSound vs : sound_list.values()) {
            log.debug("Stopping sound: " + vs.getName());
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
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        // Skip this if disabled
        if (!enabled) {
            log.debug("VSDecoder disabled. Take no action.");
            return;
        }

        log.debug("VSDecoderPane throttle property change: " + eventName);

        if (oldValue != null) {
            log.debug("Old: " + oldValue.toString());
        }
        if (newValue != null) {
            log.debug("New: " + newValue.toString());
        }

        // Iterate through the list of sound events, forwarding the propertyChange event.
        for (SoundEvent t : event_list.values()) {
            t.propertyChange(event);
        }

        // Iterate through the list of triggers, forwarding the propertyChange event.
        for (Trigger t : trigger_list.values()) {
            t.propertyChange(event);
        }
    }

    // DCC-specific and unused. Deprecate this.
    @Deprecated
    public void releaseAddress(int number, boolean isLong) {
        // remove the listener, if we can...
    }

    // DCC-specific. Deprecate this.
    @Deprecated
    public void setAddress(int number, boolean isLong) {
        this.setAddress(new DccLocoAddress(number, isLong));
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
        // DccLocoAddress dl = new DccLocoAddress(l.getNumber(), l.getProtocol());
        jmri.InstanceManager.throttleManagerInstance().attachListener(config.getDccAddress(),
                new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                log.debug("property change name " + event.getPropertyName() + " old " + event.getOldValue()
                        + " new " + event.getNewValue());
                throttlePropertyChange(event);
            }
        });
        log.debug("VSDecoder: Address set to " + config.getLocoAddress().toString());
    }

    /**
     * Get the currently assigned LocoAddress
     *
     * @return the currently assigned LocoAddress
     */
    public LocoAddress getAddress() {
        return (config.getLocoAddress());
    }

    /**
     * Get the current master volume setting for this VSDecoder
     *
     * @return (float) volume level (0.0 - 1.0)
     */
    public float getMasterVolume() {
        return (config.getVolume());
    }

    /**
     * Set the current master volume setting for this VSDecoder
     *
     * @param vol (float) volume level (0.0 - 1.0)
     */
    public void setMasterVolume(float vol) {
        log.debug("VSD: float volume = " + vol);
        config.setVolume(vol);
        for (VSDSound vs : sound_list.values()) {
            vs.setVolume(vol);
        }
    }

    /**
     * Is this VSDecoder muted?
     *
     * @return true if muted.
     */
    public boolean isMuted() {
        return (false);
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
        log.debug("( " + this.getAddress() + ") Set Position: " + p.toString());

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
        float tv = config.getVolume();
        if (p.isTunnel()) {
            tv *= tunnelVolume;
            log.debug("VSD: Tunnel volume: " + tv);
        } else {
            log.debug("VSD: Not in tunnel. Volume = " + tv);
        }
        for (VSDSound vs : sound_list.values()) {
            vs.setVolume(tv);
        }
        fireMyEvent(new VSDecoderEvent(this, VSDecoderEvent.EventType.LOCATION_CHANGE, p));
    }

    /**
     * Get the current x/y/z position in the soundspace of this VSDecoder
     *
     * @return PhysicalLocation location of this VSDecoder
     */
    public PhysicalLocation getPosition() {
        return (config.getPhysicalLocation());
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
            if (property.equals(VSDControl.PCIdMap.get(VSDControl.PropertyChangeId.OPTION_CHANGE))) {
                Train selected_train = TrainManager.instance().getTrainByName((String) evt.getNewValue());
                if (selected_train != null) {
                    selected_train.addPropertyChangeListener(this);
                }
            }
            return;
        }

        // Respond to events from the old GUI.
        if ((property.equals(VSDManagerFrame.PCIDMap.get(VSDManagerFrame.PropertyChangeID.MUTE)))
                || (property.equals(VSDecoderPane.PCIDMap.get(VSDecoderPane.PropertyChangeID.MUTE)))) {
            // Either GUI Mute button
            log.debug("VSD: Mute change. value = " + evt.getNewValue());
            Boolean b = (Boolean) evt.getNewValue();
            this.mute(b.booleanValue());

        } else if ((property.equals(VSDManagerFrame.PCIDMap.get(VSDManagerFrame.PropertyChangeID.VOLUME_CHANGE)))
                || (property.equals(VSDecoderPane.PCIDMap.get(VSDecoderPane.PropertyChangeID.VOLUME_CHANGE)))) {
            // Either GUI Volume slider
            log.debug("VSD: Volume change. value = " + evt.getNewValue());
            // Slider gives integer 0-100. Need to change that to a float 0.0-1.0
            this.setMasterVolume((1.0f * (Integer) evt.getNewValue()) / 100.0f);

        } else if (property.equals(VSDecoderPane.PCIDMap.get(VSDecoderPane.PropertyChangeID.ADDRESS_CHANGE))) {
            // OLD GUI Address Change
            log.debug("Decoder set address = " + (LocoAddress) evt.getNewValue());
            this.setAddress((LocoAddress) evt.getNewValue());
            this.enable();

        } else if (property.equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY)) {
            // Train Location Move (either GUI)
            PhysicalLocation p = getTrainPosition((Train) evt.getSource());
            if (p != null) {
                this.setPosition(getTrainPosition((Train) evt.getSource()));
            } else {
                log.debug("Train has null position");
                this.setPosition(new PhysicalLocation());
            }

        } else if (property.equals(Train.STATUS_CHANGED_PROPERTY)) {
            // Train Status change (either GUI)
            String status = (String) evt.getNewValue();
            log.debug("Train status changed: " + status);
            log.debug("New Location: " + getTrainPosition((Train) evt.getSource()));
            if ((status.startsWith(Train.BUILT)) || (status.startsWith(Train.PARTIAL_BUILT))) {
                log.debug("Train built. status = " + status);
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
            return (null);
        }
        RouteLocation rloc = t.getCurrentLocation();
        if (rloc == null) {
            log.debug("RouteLocation is null.");
            return (null);
        }
        Location loc = rloc.getLocation();
        if (loc == null) {
            log.debug("Location is null.");
            return (null);
        }
        return (loc.getPhysicalLocation());
    }

    // Methods for handling the underlying sounds
    /**
     * Retrieve the VSDSound with the given system name
     *
     * @param name (String) System name of the requested VSDSound
     * @return VSDSound the requested sound
     */
    public VSDSound getSound(String name) {
        return (sound_list.get(name));
    }

    /**
     * Turn the bell sound on/off.
     */
    public void toggleBell() {
        VSDSound snd = sound_list.get("BELL");
        if (snd.isPlaying()) {
            snd.stop();
        } else {
            snd.loop();
        }
    }

    /**
     * Turn the horn sound on/off.
     */
    public void toggleHorn() {
        VSDSound snd = sound_list.get("HORN");
        if (snd.isPlaying()) {
            snd.stop();
        } else {
            snd.loop();
        }
    }

    /**
     * Turn the horn sound on.
     */
    public void playHorn() {
        VSDSound snd = sound_list.get("HORN");
        snd.loop();
    }

    /**
     * Turn the horn sound on (Short burst).
     */
    public void shortHorn() {
        VSDSound snd = sound_list.get("HORN");
        snd.play();
    }

    /**
     * Turn the horn sound off.
     */
    public void stopHorn() {
        VSDSound snd = sound_list.get("HORN");
        snd.stop();
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
        return (config.getProfileName());
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
     * Get a Collection of SoundEvents associated with this VSDecoder
     *
     * @return {@literal Collection<SoundEvent>} collection of SoundEvents
     */
    public Collection<SoundEvent> getEventList() {
        return (event_list.values());
    }

    /**
     * True if this is the default VSDecoder
     *
     * @return boolean true if this is the default VSDecoder
     */
    public boolean isDefault() {
        return (is_default);
    }

    /**
     * Set whether this is the default VSDecoder or not
     *
     * @param d (boolean) True to set this as the default, False if not.
     */
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
        ArrayList<Element> le = new ArrayList<Element>();

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

        for (Trigger t : trigger_list.values()) {
            le.add(t.getXml());
        }

        me.addContent(le);

        // Need to add whatever else here.
        return (me);
    }

    /*
     * @Deprecated public void setXml(Element e) { this.setXml(e, null); }
     *
     * @Deprecated public void setXml(Element e, VSDFile vf) { this.setXml(vf); }
     *
     * @Deprecated public void setXml(VSDFile vf) { }
     */
    /**
     * Build this VSDecoder from an XML representation
     *
     * @param vf (VSDFile) : VSD File to pull the XML from
     * @param pn (String) : Parameter Name to find within the VSD File.
     */
    @SuppressWarnings({"cast"})
    public void setXml(VSDFile vf, String pn) {
        Iterator<Element> itr;
        Element e = null;
        Element el = null;
        SoundEvent se;

        if (vf == null) {
            log.debug("Null VSD File Name");
            return;
        }

        log.debug("VSD File Name = " + vf.getName());
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
        log.debug("Decoder Name = " + e.getAttributeValue("name"));

        // Read and create all of its components.
        // Check for default element.
        if (e.getChild("default") != null) {
            log.debug("{} is default", getProfileName());
            is_default = true;
        } else {
            is_default = false;
        }

        // +++ DEBUG
        // Log and print all of the child elements.
        itr = (e.getChildren()).iterator();
        while (itr.hasNext()) {
            // Pull each element from the XML file.
            el = itr.next();
            log.debug("Element: " + el.toString());
            if (el.getAttribute("name") != null) {
                log.debug("  Name: " + el.getAttributeValue("name"));
                log.debug("   type: " + el.getAttributeValue("type"));
            }
        }
        // --- DEBUG

        // First, the sounds.
        String prefix = "" + this.getId() + ":";
        log.debug("VSDecoder " + this.getId() + " prefix = " + prefix);
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
                // Handle a Diesel Engine sound
                DieselSound es = new DieselSound(prefix + el.getAttributeValue("name"));
                es.setXml(el, vf);
                sound_list.put(el.getAttributeValue("name"), es);
            } else if (el.getAttributeValue("type").equals("diesel3")) {
                // Handle a Diesel3 Engine sound
                Diesel3Sound es = new Diesel3Sound(prefix + el.getAttributeValue("name"));
                es.setXml(el, vf);
                sound_list.put(el.getAttributeValue("name"), es);
            } else if (el.getAttributeValue("type").equals("steam")) {
                // Handle a Steam Engine sound
                SteamSound es = new SteamSound(prefix + el.getAttributeValue("name"));
                es.setXml(el, vf);
                sound_list.put(el.getAttributeValue("name"), es);
            } else if (el.getAttributeValue("type").equals("steam1")) {
                // Handle a Steam1 Engine sound
                Steam1Sound es = new Steam1Sound(prefix + el.getAttributeValue("name"));
                es.setXml(el, vf);
                sound_list.put(el.getAttributeValue("name"), es);
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
        // Check for an existing throttle and update speed if it exists.
        Float s = (Float) InstanceManager.throttleManagerInstance().getThrottleInfo(config.getDccAddress(),
                "SpeedSetting");
        if (s != null) {
            // Mimic a throttlePropertyChange to propagate the current (init) speed setting of the throttle.
            log.debug("Existing Throttle found.  Speed = " + s);
            this.throttlePropertyChange(new PropertyChangeEvent(this, "SpeedSetting", null, s));
        } else {
            log.debug("No existing throttle found.");
        }
    }

    private static final Logger log = LoggerFactory.getLogger(VSDecoder.class);

}
