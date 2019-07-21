package jmri;

import java.util.EnumSet;

/**
 * DCC Speed Step Mode.
 *
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
 * @author Austin Hendrix Copyright (C) 2019
 */
@javax.annotation.concurrent.Immutable
public enum SpeedStepMode {
    Unknown("unknown"),
    // NMRA DCC standard speed step modes.
    SpeedStepMode128("128"),
    SpeedStepMode28("28"),
    SpeedStepMode27("27"),
    SpeedStepMode14("14"),
    // Non-DCC speed step modes.
    SpeedStepMode28Mot("motorola_28"), // Motorola 28 speed step mode.
    SpeedStepModeTMCC32("tmcc_32"); // Lionel TMCC 32 speed step mode.

    SpeedStepMode(String name) {
        this.name = name;
    }

    public String name;

    /**
     * Convert a human-readable string to a DCC speed step mode.
     *
     * @param name string version of speed step mode; example "128"
     * @return matching SpeedStepMode
     * @throws IllegalArgumentException if name does not correspond to a valid speed step mode.
     */
    static public SpeedStepMode getByName(String name) {
        for(SpeedStepMode s : SpeedStepMode.values()) {
            if(s.name.equals(name)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid speed step mode: " + name);
    }

    static public EnumSet<SpeedStepMode> getCompatibleModes(
            EnumSet<SpeedStepMode> command_station_modes,
            EnumSet<SpeedStepMode> decoder_modes) {
        EnumSet<SpeedStepMode> result = command_station_modes.clone();
        result.retainAll(decoder_modes);
        return result;
    }

    static public SpeedStepMode bestCompatibleMode(
            EnumSet<SpeedStepMode> command_station_modes,
            EnumSet<SpeedStepMode> decoder_modes) {
        EnumSet<SpeedStepMode> result = getCompatibleModes(command_station_modes, decoder_modes);
        if(result.contains(SpeedStepMode128)) {
            return SpeedStepMode128;
        } else if(result.contains(SpeedStepModeTMCC32)) {
            return SpeedStepModeTMCC32;
        } else if(result.contains(SpeedStepMode28)) {
            return SpeedStepMode28;
        } else if(result.contains(SpeedStepMode28Mot)) {
            return SpeedStepMode28Mot;
        } else if(result.contains(SpeedStepMode27)) {
            return SpeedStepMode27;
        } else if(result.contains(SpeedStepMode14)) {
            return SpeedStepMode14;
        }
        return Unknown;
    }
}
