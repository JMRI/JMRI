package jmri.jmrit.vsdecoder;

/**
 * class VSDConfig
 *
 * Data capsule ("Model"?) for passing configuration between
 * the GUI and the VSDecoder itself.
 */

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
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision: 21510 $
 */

import jmri.LocoAddress;
import jmri.util.PhysicalLocation;
import jmri.jmrit.roster.RosterEntry;

public class VSDConfig {
    
    private String my_id;
    private String vsd_path;
    private String profile_name;
    private LocoAddress address;
    private float volume;
    private PhysicalLocation location;
    private RosterEntry roster;

    public VSDConfig() {
	// umm... do... nothing? here...
	my_id = "";
	vsd_path = "";
	profile_name = "";
	address = null;
	volume = 0.0f;
	location = null;
	roster = null;
    }

    public String getID() {
	return(my_id);
    }
    
    public String getVSDPath() {
	return(vsd_path);
    }
    
    public String getProfileName() {
	return(profile_name);
    }

    public LocoAddress getLocoAddress() {
	return(address);
    }

    public float getVolume() {
	return(volume);
    }

    public PhysicalLocation getPhysicalLocation() {
	return(location);
    }

    public RosterEntry getRosterEntry() {
	return(roster);
    }

    public void setID(String id) {
	my_id = id;
    }

    public void setVSDPath(String path) {
	vsd_path = path;
    }

    public void setProfileName(String name) {
	profile_name = name;
    }

    public void setLocoAddress(LocoAddress a) {
	address = a;
    }
    
    public void setVolume(float v) {
	volume = v;
    }

    public void setPhysicalLocation(PhysicalLocation p) {
	location = p;
    }

    public void setRosterEntry(RosterEntry r) {
	roster = r;
    }
    
}