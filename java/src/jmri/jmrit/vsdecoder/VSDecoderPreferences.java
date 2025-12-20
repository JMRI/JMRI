package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import jmri.jmrit.XmlFile;
import jmri.jmrit.vsdecoder.listener.ListeningSpot;
import jmri.util.FileUtil;
import jmri.util.PhysicalLocation;
import org.jdom2.*;

/**
 * Manage VSDecoder Preferences.
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
 */
public class VSDecoderPreferences {

    public final static String VSDPreferencesFileName = "VSDecoderPreferences.xml";

    static public final int DefaultMasterVolume = 80;

    // Private variables to hold preference values
    private boolean _autoStartEngine = false; // play engine sound w/o waiting for "Engine Start" button pressed.
    private boolean _autoLoadVSDFile = false; // Automatically load a VSD file.
    private boolean _use_blocks = true;
    private String _defaultVSDFilePath = null;
    private ListeningSpot _listenerPosition;
    private int _masterVolume;

    // Other internal variables
    private String prefFile;
    private ArrayList<PropertyChangeListener> listeners;

    public VSDecoderPreferences(String sfile) {
        prefFile = sfile;
        VSDecoderPrefsXml prefs = new VSDecoderPrefsXml();
        File file = new File(prefFile);
        Element root;

        // Set default values
        _defaultVSDFilePath = FileUtil.getExternalFilename("program:resources/vsdecoder");
        _listenerPosition = new ListeningSpot(); // default to (0, 0, 0) Orientation (0,1,0)
        _masterVolume = DefaultMasterVolume;

        // Try to load preferences from the file
        try {
            root = prefs.rootFromFile(file);
        } catch (IOException e2) {
            log.info("Did not find VSDecoder preferences file.  This is normal if you haven't save the preferences before");
            root = null;
        } catch (JDOMException | RuntimeException e) {
            log.error("Exception while loading VSDecoder preferences", e);
            root = null;
        }
        if (root != null) {
            Element rf = root.getChild("VSDecoderPreferences");
            jmri.util.ThreadingUtil.runOnGUI(() -> {
                load(rf);
            });
        }
    }

    public VSDecoderPreferences() {
    }

    private void load(Element e) {
        if (e == null) {
            return;
        }
        org.jdom2.Attribute a;
        org.jdom2.Element c;

        a = e.getAttribute("isAutoStartingEngine");
        if (a != null) {
            setAutoStartEngine(a.getValue().equals("true"));
        }
        // new attribute name!
        a = e.getAttribute("isAutoLoadingVSDFile");
        if (a != null) {
            setAutoLoadVSDFile(a.getValue().equals("true"));
        } else {
            // try the old name, in case the user has not saved his preferences since the name change;  JMRI 5.5.4
            a = e.getAttribute("isAutoLoadingDefaultVSDFile");
            if (a != null) {
                setAutoLoadVSDFile(a.getValue().equals("true"));
            }
        }
        a = e.getAttribute("useBlocks");
        if (a != null) {
            setUseBlocksSetting(a.getValue().equals("true"));
        }
        c = e.getChild("DefaultVSDFilePath");
        if (c != null) {
            setDefaultVSDFilePath(c.getValue());
        }
        c = e.getChild("ListenerPosition");
        if (c != null) {
            _listenerPosition.parseListeningSpot(c);
        } else {
            _listenerPosition = new ListeningSpot();
        }
        c = e.getChild("MasterVolume");
        if (c != null) {
            setMasterVolume(Integer.parseInt(c.getValue()));
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
        e.setAttribute("isAutoLoadingVSDFile", "" + isAutoLoadingVSDFile());
        e.setAttribute("useBlocks", "" + getUseBlocksSetting());
        ec = new Element("DefaultVSDFilePath");
        ec.setText("" + getDefaultVSDFilePath());
        e.addContent(ec);
        // ListenerPosition generates its own XML
        e.addContent(_listenerPosition.getXml("ListenerPosition"));
        ec = new Element("MasterVolume");
        ec.setText("" + getMasterVolume());
        e.addContent(ec);
        return e;
    }

    public void set(VSDecoderPreferences tp) {
        setAutoStartEngine(tp.isAutoStartingEngine());
        setAutoLoadVSDFile(tp.isAutoLoadingVSDFile());
        setUseBlocksSetting(tp.getUseBlocksSetting());
        setDefaultVSDFilePath(tp.getDefaultVSDFilePath());
        setListenerPosition(tp.getListenerPosition());
        setMasterVolume(tp.getMasterVolume());

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
                || isAutoLoadingVSDFile() != tp.isAutoLoadingVSDFile()
                || getUseBlocksSetting() != tp.getUseBlocksSetting()
                || !(getDefaultVSDFilePath().equals(tp.getDefaultVSDFilePath()))
                || !(getListenerPosition().equals(tp.getListenerPosition()))
                || !(getMasterVolume().equals(tp.getMasterVolume())));
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
                if (!parentDir.mkdir()) { // make directory, check result
                    log.error("failed to make parent directory");
                }
            }
            if (!file.createNewFile()) { // create file, check result
                log.error("createNewFile failed");
            }
        } catch (IOException | RuntimeException exp) {
            log.error("Exception while writing the new VSDecoder preferences file, may not be complete", exp);
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
            log.warn("Exception in storing vsdecoder preferences xml", ex);
        }
    }

    public String getDefaultVSDFilePath() {
        return _defaultVSDFilePath;
    }

    public void setDefaultVSDFilePath(String s) {
        _defaultVSDFilePath = s;
    }

    public boolean isAutoStartingEngine() {
        return _autoStartEngine;
    }

    public void setAutoStartEngine(boolean b) {
        _autoStartEngine = b;
    }

    public boolean isAutoLoadingVSDFile() {
        return _autoLoadVSDFile;
    }

    public void setUseBlocksSetting(boolean b) {
        _use_blocks = b;
    }

    public boolean getUseBlocksSetting() {
        return _use_blocks;
    }

    public void setAutoLoadVSDFile(boolean b) {
        _autoLoadVSDFile = b;
    }

    public ListeningSpot getListenerPosition() {
        log.debug("getListenerPosition(): {}", _listenerPosition);
        return _listenerPosition;
    }

    public void setListenerPosition(ListeningSpot p) {
        VSDecoderManager vm = VSDecoderManager.instance();
        vm.setListenerLocation(vm.getDefaultListenerName(), p);
        _listenerPosition = p;
    }
    // Note:  No setListenerPosition(String) for ListeningSpot implementation

    public PhysicalLocation getListenerPhysicalLocation() {
        return _listenerPosition.getPhysicalLocation();
    }

    public void setListenerPosition(PhysicalLocation p) {
        VSDecoderManager vm = VSDecoderManager.instance();
        vm.setListenerLocation(vm.getDefaultListenerName(), new ListeningSpot(p));
        //_listenerPosition = new ListeningSpot();
        //_listenerPosition.setLocation(p);
    }

    public void setMasterVolume(int v) {
        _masterVolume = v;
    }

    public Integer getMasterVolume() {
        return _masterVolume;
    }

    /**
     * Add an AddressListener.
     * <p>
     * AddressListeners are notified when the user
     * selects a new address and when a Throttle is acquired for that address.
     *
     * @param l listener to add.
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
     * @param l listener to remove.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners == null) {
            return;
        }
        if (listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VSDecoderPreferences.class);

}
