package jmri.jmrit.vsdecoder.listener;

import java.util.List;
import javax.vecmath.Vector3f;
import jmri.AudioException;
import jmri.AudioManager;
import jmri.jmrit.audio.AudioFactory;
import jmri.jmrit.audio.AudioListener;
import jmri.util.PhysicalLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
 * @author Mark Underwood Copyright (C) 2012
 * @author Klaus Killinger Copyright (C) 2018
 */
public class VSDListener {

    // Only one Audio Listener can exist, and it is already present in the Audio Table
    public final static String ListenerSysName = "IAL$";

    private AudioFactory af;
    private AudioListener listener;
    private String sysname;
    private String username;
    private ListeningSpot location;

    public VSDListener() {
        // Initialize the AudioManager (if it isn't already) and get the Listener.
        AudioManager am = jmri.InstanceManager.getDefault(jmri.AudioManager.class);
        am.init();
        af = am.getActiveAudioFactory();
        if (af != null) {
            listener = af.getActiveAudioListener();
            log.debug("Default listener: {}, system name: {}", listener, listener.getSystemName());
            setSystemName(listener.getSystemName());
            setUserName(listener.getUserName());
        } else {
            log.warn("AudioFactory not available");
        }
    }

    public String getSystemName() {
        return sysname;
    }

    public String getUserName() {
        return username;
    }

    public ListeningSpot getLocation() {
        return location;
    }

    void setSystemName(String s) {
        sysname = s;
    }

    void setUserName(String u) {
        username = u;
    }

    public void setLocation(ListeningSpot l) {
        location = l;
        listener.setPosition(new Vector3f(l.getLocation()));
        listener.setOrientation(new Vector3f(l.getLookAtVector()), new Vector3f(l.getUpVector()));
        // Set position here
    }

    private final static Logger log = LoggerFactory.getLogger(VSDListener.class);

}
