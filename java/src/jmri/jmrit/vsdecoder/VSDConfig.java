package jmri.jmrit.vsdecoder;

/**
 * class VSDConfig
 *
 * Data capsule ("Model"?) for passing configuration between the GUI and the
 * VSDecoder itself.
 */

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
 * 
 */
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.PhysicalLocation;

public class VSDConfig {

    private float DEFAULT_VOLUME = 0.8f;

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
        volume = DEFAULT_VOLUME;
        location = null;
        roster = null;
    }

    public String getId() {
        return (my_id);
    }

    public String getVSDPath() {
        return (vsd_path);
    }

    public String getProfileName() {
        return (profile_name);
    }

    public LocoAddress getLocoAddress() {
        return (address);
    }

    public DccLocoAddress getDccAddress() {
        return (new DccLocoAddress(address.getNumber(), address.getProtocol()));
    }

    public float getVolume() {
        return (volume);
    }

    public PhysicalLocation getPhysicalLocation() {
        return (location);
    }

    public RosterEntry getRosterEntry() {
        return (roster);
    }

    public void setId(String id) {
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

    @Override
    public String toString() {
        return ("Config: ID:" + my_id
                + " Path:" + vsd_path
                + " Profile:" + profile_name
                + " Addr:" + address
                + " Vol:" + volume
                + " Loc:" + location);
    }

}
