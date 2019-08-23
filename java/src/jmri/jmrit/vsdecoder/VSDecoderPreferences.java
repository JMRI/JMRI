package jmri.jmrit.vsdecoder;

/*
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
 * @author   Mark Underwood Copyright (C) 2011
 */
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import jmri.jmrit.XmlFile;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.util.FileUtil;
import jmri.util.PhysicalLocation;
import org.jdom2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VSDecoderPreferences {

    public final static String VSDPreferencesFileName = "VSDecoderPreferences.xml";

    static public enum AudioMode {

        ROOM_AMBIENT, HEADPHONES
    }
    static public final Map<AudioMode, String> AudioModeMap;

    static {
        Map<AudioMode, String> aMap = new HashMap<AudioMode, String>();
        aMap.put(AudioMode.ROOM_AMBIENT, "RoomAmbient");
        aMap.put(AudioMode.HEADPHONES, "Headphones");
        AudioModeMap = Collections.unmodifiableMap(aMap);
    }
    static public final AudioMode DefaultAudioMode = AudioMode.ROOM_AMBIENT;

    // Private variables to hold preference values
    private boolean _autoStartEngine = false; // play engine sound w/o waiting for "Engine Start" button pressed.
    private String _defaultVSDFilePath = null;
    private String _defaultVSDFileName = null;
    private boolean _autoLoadDefaultVSDFile = false; // Automatically load a VSD file.
    private ListeningSpot _listenerPosition;
    private AudioMode _audioMode;

    // Other internal variables
    //private Dimension _winDim = new Dimension(800,600);
    private String prefFile;
    private ArrayList<PropertyChangeListener> listeners;

    public VSDecoderPreferences(String sfile) {
        prefFile = sfile;
        VSDecoderPrefsXml prefs = new VSDecoderPrefsXml();
        File file = new File(prefFile);
        Element root;

        // Set default values
        _defaultVSDFilePath = FileUtil.getExternalFilename("program:resources/vsdecoder");
        _defaultVSDFileName = "example.vsd";
        _listenerPosition = new ListeningSpot(); // default to (0, 0, 0) Orientation (0,1,0)
        _audioMode = DefaultAudioMode;

        // Try to load preferences from the file
        try {
            root = prefs.rootFromFile(file);
        } catch (IOException e2) {
            log.info("Did not find VSDecoder preferences file.  This is normal if you haven't save the preferences before");
            root = null;
        } catch (JDOMException | RuntimeException e) {
            log.error("Exception while loading VSDecoder preferences: " + e);
            root = null;
        }
        if (root != null) {
            load(root.getChild("VSDecoderPreferences"));
        }
    }

    public VSDecoderPreferences() {
    }

    public void load(org.jdom2.Element e) {
        if (e == null) {
            return;
        }
        org.jdom2.Attribute a;
        org.jdom2.Element c;
        if ((a = e.getAttribute("isAutoStartingEngine")) != null) {
            setAutoStartEngine(a.getValue().compareTo("true") == 0);
        }
        if ((a = e.getAttribute("isAutoLoadingDefaultVSDFile")) != null) {
            setAutoLoadDefaultVSDFile(a.getValue().compareTo("true") == 0);
        }
        if ((c = e.getChild("DefaultVSDFilePath")) != null) {
            setDefaultVSDFilePath(c.getValue());
        }
        if ((c = e.getChild("DefaultVSDFileName")) != null) {
            setDefaultVSDFileName(c.getValue());
        }
        if ((c = e.getChild("ListenerPosition")) != null) {
            _listenerPosition = new ListeningSpot(c);
        } else {
            _listenerPosition = new ListeningSpot();
        }
        if ((c = e.getChild("AudioMode")) != null) {
            setAudioMode(c.getValue());
        }
    }

    /**
     * An extension of the abstract XmlFile. No changes made to that class.
     *
     */
    static class VSDecoderPrefsXml extends XmlFile {
    }

    private org.jdom2.Element store() {
        org.jdom2.Element ec;
        org.jdom2.Element e = new org.jdom2.Element("VSDecoderPreferences");
        e.setAttribute("isAutoStartingEngine", "" + isAutoStartingEngine());
        e.setAttribute("isAutoLoadingDefaultVSDFile", "" + isAutoLoadingDefaultVSDFile());
        ec = new Element("DefaultVSDFilePath");
        ec.setText("" + getDefaultVSDFilePath());
        e.addContent(ec);
        ec = new Element("DefaultVSDFileName");
        ec.setText("" + getDefaultVSDFileName());
        e.addContent(ec);
        // ListenerPosition generates its own XML
        e.addContent(_listenerPosition.getXml("ListenerPosition"));
        ec = new Element("AudioMode");
        ec.setText("" + AudioModeMap.get(_audioMode));
        e.addContent(ec);
        return e;
    }

    public void set(VSDecoderPreferences tp) {
        setAutoStartEngine(tp.isAutoStartingEngine());
        setAutoLoadDefaultVSDFile(tp.isAutoLoadingDefaultVSDFile());
        setDefaultVSDFilePath(tp.getDefaultVSDFilePath());
        setDefaultVSDFileName(tp.getDefaultVSDFileName());
        setListenerPosition(tp.getListenerPosition());
        setAudioMode(tp.getAudioMode());

        if (listeners != null) {
            for (int i = 0; i < listeners.size(); i++) {
                PropertyChangeListener l = listeners.get(i);
                PropertyChangeEvent e = new PropertyChangeEvent(this, "VSDecoderPreferences", null, this);
                l.propertyChange(e);
            }
        }
    }

    public boolean compareTo(VSDecoderPreferences tp) {
        return (isAutoStartingEngine() != tp.isAutoStartingEngine()
                || isAutoLoadingDefaultVSDFile() != tp.isAutoLoadingDefaultVSDFile()
                || !(getDefaultVSDFilePath().equals(tp.getDefaultVSDFilePath()))
                || !(getDefaultVSDFileName().equals(tp.getDefaultVSDFileName()))
                || !(getListenerPosition().equals(tp.getListenerPosition()))
                || !(getAudioMode().equals(tp.getAudioMode())));
    }

    public void save() {
        if (prefFile == null) {
            return;
        }
        XmlFile xf = new XmlFile() {
        };   // odd syntax is due to XmlFile being abstract
        xf.makeBackupFile(prefFile);
        File file = new File(prefFile);
        try {
            //The file does not exist, create it before writing
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdir()) // make directory, check result
                {
                    log.error("failed to make parent directory");
                }
            }
            if (!file.createNewFile()) // create file, check result
            {
                log.error("createNewFile failed");
            }
        } catch (IOException | RuntimeException exp) {
            log.error("Exception while writing the new VSDecoder preferences file, may not be complete: " + exp);
        }

        try {
            Element root = new Element("vsdecoder-preferences");
            //Document doc = XmlFile.newDocument(root, XmlFile.dtdLocation+"vsdecoder-preferences.dtd");
            Document doc = XmlFile.newDocument(root);
            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/throttle.xsl"?>
/*TODO      java.util.Map<String,String> m = new java.util.HashMap<String,String>();
             m.put("type", "text/xsl");
             m.put("href", jmri.jmrit.XmlFile.xsltLocation+"throttles-preferences.xsl");
             ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
             doc.addContent(0,p);*/
            root.setContent(store());
            xf.writeXML(file, doc);
        } catch (IOException | RuntimeException ex) { // TODO fix null value for Attribute
            log.warn("Exception in storing vsdecoder preferences xml: " + ex);
        }
    }

    public String getDefaultVSDFilePath() {
        return (_defaultVSDFilePath);
    }

    public void setDefaultVSDFilePath(String s) {
        _defaultVSDFilePath = s;
    }

    public String getDefaultVSDFileName() {
        return (_defaultVSDFileName);
    }

    public void setDefaultVSDFileName(String s) {
        _defaultVSDFileName = s;
    }

    public boolean isAutoStartingEngine() {
        return (_autoStartEngine);
    }

    public void setAutoStartEngine(boolean b) {
        _autoStartEngine = b;
    }

    public boolean isAutoLoadingDefaultVSDFile() {
        return (_autoLoadDefaultVSDFile);
    }

    public void setAutoLoadDefaultVSDFile(boolean b) {
        _autoLoadDefaultVSDFile = b;
    }

    public ListeningSpot getListenerPosition() {
        log.debug("getListenerPosition() : " + _listenerPosition.toString());
        return (_listenerPosition);
    }

    public void setListenerPosition(ListeningSpot p) {
        VSDecoderManager vm = VSDecoderManager.instance();
        vm.setListenerLocation(vm.getDefaultListenerName(), p);
        _listenerPosition = p;
    }
    // Note:  No setListenerPosition(String) for ListeningSpot implementation

    public PhysicalLocation getListenerPhysicalLocation() {
        return (_listenerPosition.getPhysicalLocation());
    }

    public void setListenerPosition(PhysicalLocation p) {
        VSDecoderManager vm = VSDecoderManager.instance();
        vm.setListenerLocation(vm.getDefaultListenerName(), new ListeningSpot(p));
        //_listenerPosition = new ListeningSpot();
        //_listenerPosition.setLocation(p);
    }

    public AudioMode getAudioMode() {
        return (_audioMode);
    }

    public void setAudioMode(AudioMode am) {
        _audioMode = am;
    }

    public void setAudioMode(String am) {
        // There's got to be a more efficient way to do this
        Set<Map.Entry<AudioMode, String>> ids = AudioModeMap.entrySet();
        Iterator<Map.Entry<AudioMode, String>> idi = ids.iterator();
        while (idi.hasNext()) {
            Map.Entry<AudioMode, String> e = idi.next();
            log.debug("    ID = " + e.getKey() + " Val = " + e.getValue());
            if (e.getValue().equals(am)) {
                _audioMode = e.getKey();
                return;
            }
        }
        // We fell out of the loop.  Must be an invalid string. Set default
        _audioMode = DefaultAudioMode;
    }

    /**
     * Add an AddressListener. AddressListeners are notified when the user
     * selects a new address and when a Throttle is acquired for that address
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (listeners == null) {
            listeners = new ArrayList<PropertyChangeListener>(2);
        }
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Remove an AddressListener.
     *
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners == null) {
            return;
        }
        if (listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(VSDecoderPreferences.class);
}
